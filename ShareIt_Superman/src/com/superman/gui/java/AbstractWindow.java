package com.superman.gui.java;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("deprecation")
public abstract class AbstractWindow {
	protected Stage applicationStage;
	protected Scene applicationScene;

	protected boolean isActive = false;

	public void show() {
		applicationStage.show();
	}

	public void hide() {
		applicationStage.hide();
	}

	public void init(String title, String fxml) {
		if (isActive == false) {
			isActive = true;
			applicationStage = new Stage();
			applicationStage.setTitle(title);
			applicationStage.setResizable(false);

			FXMLLoader myLoader = new FXMLLoader(getClass().getResource(fxml));

			try {
				Pane myPane = (Pane) myLoader.load();
				applicationScene = new Scene(myPane);
			} catch (IOException e) {
				e.printStackTrace();
			}

			applicationStage.setScene(applicationScene);

			Handler h = new Handler();
			applicationStage.addEventHandler(
					WindowEvent.WINDOW_CLOSE_REQUEST,
					h.new ExitHandler());
		}
	}
	
	
	public static void promptExit(String msg) {
		Stage dialogStage = new Stage();
		
		Text message = new Text(msg);
		message.setFont(new Font("Calibri", 18));
		
		Button okButton = new Button("OK");
		okButton.prefWidth(100);
		okButton.prefHeight(10);
		okButton.addEventHandler(MouseEvent.MOUSE_CLICKED, 
			new EventHandler<MouseEvent>() {
				
				@Override
				public void handle(MouseEvent event) {
					try {
						StartWindow.clientLogOut();
					} catch (Exception e) {
						e.printStackTrace();
					}
					StartWindow.accountWindow.applicationStage.hide();
					StartWindow.applicationStage.show();

					StartWindow.getNewClient();
					dialogStage.close();
				}
		});
		
		Button cancelButton = new Button("Cancel");
		cancelButton.prefWidth(100);
		cancelButton.prefHeight(10);
		cancelButton.addEventHandler(
				MouseEvent.MOUSE_CLICKED, 
				new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						dialogStage.close();
						return;
					}
				}
		);
		
		Label spaceLabel1 = new Label();
		Label spaceLabel2 = new Label();
		Label spaceLabel3 = new Label();
		
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setScene(new Scene(
				VBoxBuilder.create().children(
						spaceLabel3,
						message,
						okButton,
						spaceLabel1,
						cancelButton,
						spaceLabel2
				).alignment(Pos.CENTER).minHeight(100).minWidth(300).build()));
		dialogStage.show();
	}
	
	public static void promptError(String msg) {
		Stage dialogStage = new Stage();
		
		Text message = new Text(msg);
		message.setFont(new Font("Calibri", 18));
		
		dialogStage.initModality(Modality.WINDOW_MODAL);
		dialogStage.setScene(new Scene(
				VBoxBuilder.create().children(message).
				alignment(Pos.CENTER).minHeight(100).minWidth(300).build()));
		dialogStage.show();
	}

}
