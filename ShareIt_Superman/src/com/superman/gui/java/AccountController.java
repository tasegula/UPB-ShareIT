package com.superman.gui.java;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

public class AccountController {
	@FXML private ToggleButton aToggle_download;
	@FXML private ToggleButton aToggle_account;
	@FXML private ToggleButton aToggle_share;

	@FXML private ChoiceBox<String> aChoose;
	@FXML private Button aButton;
	@FXML private Label aFolder;
	
	private final String className = "[AccountController] ";

	public void toggleButton(ActionEvent event) {
		ToggleButton tButton = (ToggleButton) event.getTarget();
		tButton.setSelected(false);

		if (tButton == aToggle_share) {
			StartWindow.accountWindow.applicationStage.hide();
			StartWindow.shareWindow.show();
		} else if (tButton == aToggle_download) {
			StartWindow.accountWindow.applicationStage.hide();
			StartWindow.downloadWindow.show();
		} else if (tButton == aToggle_account) {
			tButton.setSelected(true);
		}
	}

	public void buttonHandle(ActionEvent event) {
//		try {
//			AbstractWindow.promptExit("Aplicatia se va inchide!\n" 
//					+ "Descarile curente se vor inchide!\n"
//					+ "Incarcarile curente se vor inchide!\n");
//			
//			StartWindow.clientLogOut();
//		} catch (ClassNotFoundException | InterruptedException | IOException e) {
//			e.printStackTrace();
//		}
//
//		StartWindow.accountWindow.applicationStage.hide();
//		StartWindow.applicationStage.show();
//
//		StartWindow.getNewClient();
		
		AbstractWindow.promptExit("Aplicatia se va inchide!\n" 
					+ "Descarile curente se vor inchide!\n"
					+ "Incarcarile curente se vor inchide!\n");
	}
	
	public void aChoiceHandle(MouseEvent event) {
		String choice = aChoose.getValue().toString();
		StartWindow.client.setUploadSlots(Integer.parseInt(choice));
		System.out.println(className + "aChoiceHandle(): " + Integer.parseInt(choice));
	}
	
	public void chooseFolderHandle(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Select directory to add");

		File file = directoryChooser.showDialog(StartWindow.shareWindow.applicationStage);
		if (file != null) {
			StartWindow.client.setDownloadFolder(file.getAbsolutePath());
			aFolder.setText(file.getAbsolutePath());
			aFolder.setVisible(true);
		}
	}
}
