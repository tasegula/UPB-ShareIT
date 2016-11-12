package com.superman.gui.java;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class StartController implements Initializable {
	@FXML private Button    startLogin;
	@FXML private TextField startUsername;
	@FXML private TextField startIP;
	@FXML private TextField startPort;

	private final String className = "[StartController] ";
	
	public void buttonHandle(ActionEvent event) {
		loginClient();
	}

	public void textHandle(ActionEvent event) {
		startPort.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode().equals(KeyCode.ENTER)) {
					loginClient();
				}
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE AREA
	// /////////////////////////////////////////////////////////////////////////

	private void loginClient() {
		System.out.println(className + "loginClient(): " 
				+ startUsername.getText() 
				+ "@ " + startIP.getText()
				+ ": " + startPort.getText());
		
		String msg = "Introduceti: \n";
		String transparent = "rgb(206, 206, 206)";
		boolean error = false; 
		boolean logged = false;
		
		if (startUsername.getText() == null) {
			error = true;
			startUsername.setStyle("-fx-background-color: RED");
			msg += "-> Username\n";
		}
		if (startIP.getText() == null) {
			error = true;
			startIP.setStyle("-fx-background-color: RED");
			msg += "-> IP\n";
		}
		if (startPort.getText() == null) {
			error = true;
			startPort.setStyle("-fx-background-color: RED");
			msg += "-> Port\n";			
		}
		if (error) {
			AbstractWindow.promptError(msg);
			return;
		}
		
		try {
			logged = StartWindow.client.logIn(startUsername.getText(),
					startIP.getText(), startPort.getText());
			
			if (logged == false) {
				startUsername.setStyle("-fx-background-color: RED");
				startIP.setStyle("-fx-background-color: " + transparent);
				startPort.setStyle("-fx-background-color: " + transparent);
				
				AbstractWindow.promptError("Username invalid!");
				return;
			}			
			StartWindow.client.start();
			
		} catch (ClassNotFoundException | IOException e) {
			AbstractWindow.promptError("Combinatie IP-Port invalida!");
			
			startUsername.setStyle("-fx-background-color: " + transparent);
			startIP.setStyle("-fx-background-color: RED");
			startPort.setStyle("-fx-background-color: RED");
			return;
		}

		clearTextFields();
		StartWindow.changeStage();
	}

	private void clearTextFields() {
		String transparent = "rgb(206, 206, 206)";
		
		startUsername.clear();
		startIP.clear();
		startPort.clear();
		
		startUsername.setStyle("-fx-background-color: " + transparent);
		startIP.setStyle("-fx-background-color: " + transparent);
		startPort.setStyle("-fx-background-color: " + transparent);
	}
}
