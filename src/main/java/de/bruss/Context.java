package de.bruss;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import de.bruss.config.ConfigTableCtrl;
import de.bruss.config.EditConfigCtrl;
import de.bruss.main.MainSceneCtrl;

public class Context {
	private static ProgressBar progressBar;
	private static Stage primaryStage;
	private static MainSceneCtrl mainSceneCtrl;
	private static ConfigTableCtrl configTableCtrl;
	private static EditConfigCtrl editConfigCtrl;
	private static HBox fileCounterBox;
	private static Label fileCounter;

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

	public static ConfigTableCtrl getConfigTableCtrl() {
		return configTableCtrl;
	}

	public static void setConfigTableCtrl(ConfigTableCtrl configTableCtrl) {
		Context.configTableCtrl = configTableCtrl;
	}

	public static EditConfigCtrl getEditConfigCtrl() {
		return editConfigCtrl;
	}

	public static void setEditConfigCtrl(EditConfigCtrl editConfigCtrl) {
		Context.editConfigCtrl = editConfigCtrl;
	}

	public static MainSceneCtrl getMainSceneCtrl() {
		return mainSceneCtrl;
	}

	public static void setMainSceneCtrl(MainSceneCtrl mainSceneCtrl) {
		Context.mainSceneCtrl = mainSceneCtrl;
	}

	public static HBox getFileCounterBox() {
		return fileCounterBox;
	}

	public static void setFileCounterBox(HBox fileCounterBox) {
		Context.fileCounterBox = fileCounterBox;
	}

	public static Label getFileCounter() {
		return fileCounter;
	}

	public static void setFileCounter(Label fileCounter) {
		Context.fileCounter = fileCounter;
	}

}
