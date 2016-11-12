package com.superman.gui.java;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import com.superman.common.FileInfo;
import com.superman.common.FileStatus;
import com.superman.common.UserInfo;

public class DownloadController {
	@FXML private ToggleButton dToggle_download;
	@FXML private ToggleButton dToggle_account;
	@FXML private ToggleButton dToggle_share;

	@FXML private TextField dSearch_file;
	@FXML private TextField dSearch_user;

	@FXML private ListView<String>        dList_user;
	@FXML private TreeTableView<FileInfo> dTree;
	@FXML private TableView<FileStatus>   dTable;

	private String selectedUser = null;
	private boolean initTable = false;

	public static ObservableList<FileStatus> data;
	private FileStatus cancelFile = null;
	
	TableColumn<FileStatus, String> fileCol;
	TableColumn<FileStatus, Double> progCol;
	TableColumn<FileStatus, String> sizeCol;
	TableColumn<FileStatus, String> userCol;
	
	private final String className = "[DownloadController] ";

	public void toggleButton(ActionEvent event) {
		ToggleButton tButton = (ToggleButton) event.getTarget();
		tButton.setSelected(false);

		if (tButton == dToggle_account) {
			StartWindow.downloadWindow.hide();
			StartWindow.accountWindow.show();
		} else if (tButton == dToggle_share) {
			StartWindow.downloadWindow.hide();
			StartWindow.shareWindow.show();
		} else if (tButton == dToggle_download) {
			getUserList();
			tButton.setSelected(true);
		}
	}

	public void searchUserHandle(MouseEvent event) {
		System.out.println(className + "search(): " + event);
		
		dSearch_user.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				getUserList();
			}
			
		});
		
		dSearch_user.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				getUserList(dSearch_user.getText());
				
				if (event.getCode().equals(KeyCode.ENTER)) {
					getUserList(dSearch_user.getText());
				}
			}
		});
	}

	public void searchFileHandle(MouseEvent event) {		
		dSearch_file.setOnKeyReleased(new EventHandler<KeyEvent>() {
			LinkedList<FileInfo> results = null;

			@Override
			public void handle(KeyEvent event) {
				System.out.print(className + "searchFileHandle(): " 
						+ selectedUser + " / " + dSearch_file.getText());
				
				if (event.getCode().equals(KeyCode.ENTER)) {
					if (selectedUser == null) {
						System.out.println(" -> @server");
						try {
							results = StartWindow.client.search(dSearch_file.getText());
						} catch (ClassNotFoundException | IOException e) {
							e.printStackTrace();
							return;
						}
	
						addFileInTree(results);
					}
					else {
						System.out.println(" -> @user");
						results = StartWindow.client.filterUserFilelist(dSearch_file.getText());
						addFileInTree(results);
					}
				}
				else {
					System.out.println();
				}
			}
		});
	}

	public void userButtonHandle(ActionEvent event) {
		getUserList();
	}

	public void listAddFilelist(MouseEvent event) {
		dList_user.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {				
				if (event.getButton().equals(MouseButton.SECONDARY)) {
					addFileInTree(new LinkedList<FileInfo>());
					dList_user.getSelectionModel().clearSelection();
					selectedUser = null;
					return;
				}
				else {
					String selectedItem = dList_user.getSelectionModel().selectedItemProperty().getValue();
					List<FileInfo> filelist = null;

					try {
						filelist = StartWindow.client.getUserFileList(selectedItem);
						selectedUser = selectedItem;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return;
					} catch (IOException e) {
						e.printStackTrace();
						return;
					}

					addFileInTree(filelist);
				}
			}
		});
		
		String selectedItem = dList_user.getSelectionModel().selectedItemProperty().getValue();
		List<FileInfo> filelist = null;

		try {
			filelist = StartWindow.client.getUserFileList(selectedItem);
			selectedUser = selectedItem;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		addFileInTree(filelist);
		
	}

	public void listDelFilelist(MouseEvent event) {
		
	}

	public void dTree_download(MouseEvent event) {
		if (initTable == false) {			
			initTable = true;
			initTableView();
		}

		dTree.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					if (event.getClickCount() == 2) {
						TreeItem<FileInfo> selectedItem = dTree
								.getSelectionModel().selectedItemProperty()
								.getValue();
						try {
							StartWindow.client.startDownload(selectedItem.getValue().getUsername(), 
															 selectedItem.getValue().getPath());
						} catch (IOException e) {
							e.printStackTrace();
						}

						System.out.println(className + "dTreeDownload(): " + selectedItem);
					}
				}
			}
		});
	}

	public void dTable_clicked(MouseEvent event) {
		dTable.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				cancelFile = dTable.getSelectionModel().selectedItemProperty().getValue();
				System.out.println(className + "dtable_click(): " + cancelFile);
			}
		});
	}
	
	public void dTable_context(MouseEvent event) {
		
	}
	
	public void cancelHandle(ActionEvent event) {
		if (cancelFile != null) {
			try {
				StartWindow.client.cancelDownloadJob(cancelFile.getShortPath());
				data.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
			cancelFile = null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	//	PRIVATE AREA
	///////////////////////////////////////////////////////////////////////////
	
	private void getUserList() {
		try {
			StartWindow.client.updateUserlist();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		ObservableList<String> userlist = FXCollections.observableArrayList();

		for (UserInfo user : StartWindow.client.getUserlist()) {
			userlist.add(user.getUsername());
		}

		dList_user.setItems(userlist);
	}
	
	private void getUserList(String query) {
		ObservableList<String> userlist = FXCollections.observableArrayList();

		try {
			StartWindow.client.updateUserlist();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		try {
			for (String user : StartWindow.client.getFilteredUsersList(query)) {
				userlist.add(user);
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		dList_user.setItems(userlist);
	}

	private void expandTree(FileInfo dir, TreeItem<FileInfo> tree) {
		TreeItem<FileInfo> child = new TreeItem<>(dir);

		for (FileInfo file : dir.getContent()) {
			if (file.isDir()) {
				expandTree(file, child);
			} else {
				child.getChildren().add(new TreeItem<FileInfo>(file));
			}
		}
		tree.getChildren().add(child);
	}

	@SuppressWarnings("unchecked")
	private void initTableView() {
		System.out.println(className + "initTableView(): " + "just one time");
		ObservableList<TableColumn<FileStatus, ?>> colList = dTable.getColumns();

		data = FXCollections.observableArrayList();

		dTable.setEditable(true);

		fileCol = (TableColumn<FileStatus, String>) colList.get(0);
		progCol = (TableColumn<FileStatus, Double>) colList.get(1);
		sizeCol = (TableColumn<FileStatus, String>) colList.get(2);
		userCol = (TableColumn<FileStatus, String>) colList.get(3);

		fileCol.setCellValueFactory(new PropertyValueFactory<FileStatus, String>(
				"shortPath"));
		
		progCol.setCellValueFactory(new PropertyValueFactory<FileStatus, Double>(
				"progress"));
		progCol.setCellFactory(ProgressBarTableCell.forTableColumn());
		
		sizeCol.setCellValueFactory(new PropertyValueFactory<FileStatus, String>(
				"size"));
		userCol.setCellValueFactory(new PropertyValueFactory<FileStatus, String>(
				"username"));

		dTable.setItems(data);
	}

	@SuppressWarnings("unchecked")
	private void addFileInTree(List<FileInfo> filelist) {
		FileInfo rootFile = new FileInfo();
		rootFile.setPath(selectedUser + "'s Filelist");
		TreeItem<FileInfo> froot = new TreeItem<FileInfo>(rootFile);

		for (FileInfo file : filelist) {
			if (file.isDir()) {
				expandTree(file, froot);
			} else {
				froot.getChildren().add(new TreeItem<FileInfo>(file));
			}
		}

		ObservableList<TreeTableColumn<FileInfo, ?>> colList = dTree
				.getColumns();

		TreeTableColumn<FileInfo, String> fileColumn = (TreeTableColumn<FileInfo, String>) colList.get(0);
		TreeTableColumn<FileInfo, String> sizeColumn = (TreeTableColumn<FileInfo, String>) colList.get(1);
		TreeTableColumn<FileInfo, String> userColumn = (TreeTableColumn<FileInfo, String>) colList.get(2);

		fileColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileInfo, String> param) -> 
						new ReadOnlyStringWrapper(param.getValue().getValue().getFilename())
		);
		sizeColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileInfo, String> param) -> 
						new ReadOnlyStringWrapper("" + 
								(!param.getValue().getValue().isDir() ? 
										param.getValue().getValue().getSize() : 
										""))
		);
		userColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<FileInfo, String> param) -> 
						new ReadOnlyStringWrapper(param.getValue().getValue().getUsername()));

		froot.setExpanded(true);
		dTree.setRoot(froot);
		dTree.setShowRoot(false);
	}
	
}
