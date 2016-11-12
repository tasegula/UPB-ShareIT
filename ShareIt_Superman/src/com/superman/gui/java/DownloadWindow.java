package com.superman.gui.java;

public class DownloadWindow extends AbstractWindow {
	static final public String title = "Transfer";
	static final public String fxmlPath = "/com/superman/gui/fxml/Transfer.fxml";

	ShareWindow shareWindow = null;
	AccountWindow accountWindow = null;

	// Singleton
	public static DownloadWindow instance;
	private static boolean isInit = false;

	private DownloadWindow() {
		init(title, fxmlPath);
	}

	public static DownloadWindow getInstance() {
		if (isInit == false) {
			isInit = true;
			instance = new DownloadWindow();
		}
		return instance;
	}

	public void initWindows() {
		accountWindow = StartWindow.accountWindow;
		shareWindow = StartWindow.shareWindow;
	}

}
