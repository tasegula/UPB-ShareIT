package com.superman.gui.java;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public class Handler {

	public class ExitHandler implements EventHandler<WindowEvent> {

		private final String className = "[ExitHandler] ";
		
		@Override
		public void handle(WindowEvent arg0) {
			System.out.println(className + "exit application; threads will be closed");
			if (StartWindow.client.isAlive()) {
				try {
					StartWindow.clientLogOut();
				} catch (ClassNotFoundException | InterruptedException
						| IOException e) {
					e.printStackTrace();
				} finally {
					StartWindow.exit();
					System.exit(0);
				}
			}
			StartWindow.exit();
			System.exit(0);
		}

	}
}
