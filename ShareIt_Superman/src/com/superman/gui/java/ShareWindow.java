package com.superman.gui.java;

public class ShareWindow extends AbstractWindow {
	static final public String title = "Partajare";
	static final public String fxmlPath = "/com/superman/gui/fxml/Share.fxml";

	DownloadWindow downloadWindow = null;
	AccountWindow accountWindow = null;

	// Singleton
	public static ShareWindow instance;
	private static boolean isInit = false;

	private ShareWindow() {
		init(title, fxmlPath);
	}

	public static ShareWindow getInstance() {
		if (isInit == false) {
			isInit = true;
			instance = new ShareWindow();
		}
		return instance;
	}

	public void initWindows() {
		downloadWindow = StartWindow.downloadWindow;
		accountWindow = StartWindow.accountWindow;
	}

}