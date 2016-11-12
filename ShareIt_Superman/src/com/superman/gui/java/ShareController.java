package com.superman.gui.java;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import com.superman.common.FileInfo;

public class ShareController implements Initializable {
	@FXML private TreeView<String> sTree;
	@FXML private Label sLabel;

	@FXML private ToggleButton sToggle_download;
	@FXML private ToggleButton sToggle_account;
	@FXML private ToggleButton sToggle_share;

	@FXML private Button sButton_add;
	@FXML private Button sButton_addDir;
	@FXML private Button sButton_del;

	private boolean isEmpty = true;
	TreeItem<String> root = null;
	
	private final String className = "[ShareController] ";

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
	}

	public void toggleButton(ActionEvent event) {
		ToggleButton tButton = (ToggleButton) event.getTarget();
		tButton.setSelected(false);

		if (tButton == sToggle_account) {
			StartWindow.shareWindow.applicationStage.hide();
			StartWindow.accountWindow.show();
		} else if (tButton == sToggle_download) {
			StartWindow.shareWindow.applicationStage.hide();
			StartWindow.downloadWindow.show();
		} else if (tButton == sToggle_share) {
			tButton.setSelected(true);
		}

	}

	public void addFiles(ActionEvent event) {
		initTreeFilelist();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select files to add");

		List<File> list = fileChooser
				.showOpenMultipleDialog(StartWindow.shareWindow.applicationStage);

		if (list != null) {
			System.out.println(list.get(0).getParent());
			TreeItem<String> tree = new TreeItem<String>(list.get(0)
					.getParent());

			for (File file : list) {
				TreeItem<String> item = new TreeItem<String>(file.toString());
				tree.getChildren().add(item);

				try {
					StartWindow.client.addFile(file.getAbsolutePath());
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			}
			root.getChildren().add(tree);
			sTree.setRoot(root);
		}
		root.getChildren().clear();
		addFilelistInTree(StartWindow.client.getFilelist());
	}

	public void addDirectory(ActionEvent event) {
		initTreeFilelist();
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select directory to add");

		File file = directoryChooser
				.showDialog(StartWindow.shareWindow.applicationStage);
		if (file != null) {
			expandTree(file, root);
			sTree.setRoot(root);

			try {
				StartWindow.client.addFile(file.getAbsolutePath());
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
		root.getChildren().clear();
		addFilelistInTree(StartWindow.client.getFilelist());
	}

	public void delFiles(ActionEvent event) {
		ObservableList<TreeItem<String>> selected = sTree.getSelectionModel().getSelectedItems();
		System.out.println(className + "delFiles(): " + selected);
		
		for (TreeItem<String> tis : selected) {
			StartWindow.client.removeFile(tis.getValue());
		}
		
		root.getChildren().clear();
		addFilelistInTree(StartWindow.client.getFilelist());
	}

	public void sFilelist_clicked(MouseEvent event) {
		
	}

	public void sFilelist_context(MouseEvent event) {
		
	}

	public void labelHandle(MouseEvent event) {
		sLabel.setVisible(false);
		sLabel.toBack();
		
		initTreeFilelist();
	}

	///////////////////////////////////////////////////////////////////////////
	//	PRIVATE AREA
	///////////////////////////////////////////////////////////////////////////
	
	private void initTreeFilelist() {
		if (isEmpty) {
			isEmpty = false;
			root = new TreeItem<String>(StartWindow.client.getUsername()
					+ "'s Filelist");
			
			sTree.setRoot(root);
			sTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			
			List<FileInfo> filelist = StartWindow.client.getFilelist();
			addFilelistInTree(filelist);
		}
	}
	
	private void addFilelistInTree(List<FileInfo> filelist) {
		for (FileInfo file : filelist) {
			if (file.isDir()) {
				if (file.getContent().size() != 0) {
					expandTreeFileInfo(file, root);
				}
			} else {
				root.getChildren().add(new TreeItem<String>(file.getPath()));
			}
		}
	}

	private void expandTree(File dir, TreeItem<String> tree) {
		TreeItem<String> child = new TreeItem<>(dir.getPath());

		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				expandTree(file, child);
			} else {
				child.getChildren().add(new TreeItem<String>(file.getPath()));
			}
		}
		tree.getChildren().add(child);
	}

	private void expandTreeFileInfo(FileInfo dir, TreeItem<String> tree) {
		TreeItem<String> child = new TreeItem<>(dir.getPath());

		for (FileInfo file : dir.getContent()) {
			if (file.isDir()) {
				expandTreeFileInfo(file, child);
			} else {
				child.getChildren().add(new TreeItem<String>(file.getPath()));
			}
		}
		tree.getChildren().add(child);
	}
}
