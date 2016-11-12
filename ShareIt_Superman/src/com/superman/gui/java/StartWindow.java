package com.superman.gui.java;

import java.io.IOException;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.superman.client.Client;

public class StartWindow extends Application {
	static final public String title = "Login";
	static final public String fxmlPath = "/com/superman/gui/fxml/Start.fxml";

	protected static DownloadWindow downloadWindow = DownloadWindow.getInstance();
	protected static AccountWindow accountWindow = AccountWindow.getInstance();
	protected static ShareWindow shareWindow = ShareWindow.getInstance();

	protected static Stage applicationStage;
	protected static Scene applicationScene;

	// CLIENT
	public static Client client = null;

	public static void main(String[] args) {
		downloadWindow.initWindows();
		accountWindow.initWindows();
		shareWindow.initWindows();

		getNewClient();
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		applicationStage = primaryStage;
		FXMLLoader myLoader = new FXMLLoader(getClass().getResource(fxmlPath));

		try {
			Pane myPane = (Pane) myLoader.load();
			applicationScene = new Scene(myPane);
		} catch (IOException e) {
			e.printStackTrace();
		}

		applicationStage.setTitle("Start Window");
		applicationStage.setScene(applicationScene);
		applicationStage.show();
		applicationStage.setResizable(false);

		Handler h = new Handler();
		applicationStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,
				h.new ExitHandler());
	}

	///////////////////////////////////////////////////////////////////////////
	//	STATIC AREA
	///////////////////////////////////////////////////////////////////////////
	
	public static void changeStage() {
		applicationStage.hide();
		downloadWindow.show();
	}

	public static void getNewClient() {
			try {
				client = new Client();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
	}

	
	public static void clientLogOut() throws InterruptedException,
			UnknownHostException, ClassNotFoundException, IOException {
		if (client != null) {
			client.logOut();
			client.stopExec();
			client.join();

			client = null;
		}
	}

	public static void exit() {
		downloadWindow.applicationStage.close();
		accountWindow.applicationStage.close();
		shareWindow.applicationStage.close();
		applicationStage.close();

		System.exit(0);
	}
}
