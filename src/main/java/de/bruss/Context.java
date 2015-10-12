package de.bruss;

import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class Context {
	private static ProgressBar progressBar;
	private static Stage primaryStage;

	public static ProgressBar getProgressBar() {
		return progressBar;
	}

	public static void setProgressBar(ProgressBar progressBar) {
		Context.progressBar = progressBar;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Context.primaryStage = primaryStage;
	}

}
