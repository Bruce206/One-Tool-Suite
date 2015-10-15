package de.bruss;

import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import de.bruss.config.ConfigTableCtrl;
import de.bruss.config.EditConfigCtrl;

public class Context {
	private static ProgressBar progressBar;
	private static Stage primaryStage;
	private static ConfigTableCtrl configTableCtrl;
	private static EditConfigCtrl editConfigCtrl;

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

}
