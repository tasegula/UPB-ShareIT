package com.superman.gui.java;

public class AccountWindow extends AbstractWindow {
	static final public String title = "Cont";
	static final public String fxmlPath = "/com/superman/gui/fxml/Account.fxml";

	DownloadWindow downloadWindow = null;
	ShareWindow shareWindow = null;

	// Singleton
	public static AccountWindow instance = null;
	private static boolean isInit = false;

	private AccountWindow() {
		init(title, fxmlPath);
	}

	public static AccountWindow getInstance() {
		if (isInit == false) {
			isInit = true;
			instance = new AccountWindow();
		}
		return instance;
	}

	public void initWindows() {
		downloadWindow = StartWindow.downloadWindow;
		shareWindow = StartWindow.shareWindow;
	}

}