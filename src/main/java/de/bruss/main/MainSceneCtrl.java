package de.bruss.main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.bruss.UpdateService;

public class MainSceneCtrl implements Initializable {

	@FXML
	private TextArea consoleout;

	@FXML
	private AnchorPane consolePanel;

	public void appendText(String str) {
		Platform.runLater(() -> consoleout.appendText(str));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				appendText(String.valueOf((char) b));
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));

		// PrintStream out;
		// try {
		// out = new PrintStream(new FileOutputStream(Settings.appDataPath + "\\output.txt"));
		// System.setOut(out);
		// } catch (FileNotFoundException e1) {
		// e1.printStackTrace();
		// }

	}

	@FXML
	protected void changeSettings(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/SettingsScene.fxml"));

		Stage stage = new Stage(StageStyle.DECORATED);
		stage.setScene(new Scene((Pane) loader.load()));
		stage.show();
	}

	@FXML
	protected void toggleConsoleout(ActionEvent event) {
		if (consolePanel.getHeight() > 75) {
			consolePanel.setPrefHeight(75);
		} else {
			consolePanel.setPrefHeight(200);
		}
	}

	@FXML
	protected void checkForUpdates(ActionEvent event) throws IOException {
		UpdateService updateService = new UpdateService();
		Thread t = new Thread(updateService);
		t.setDaemon(true);
		t.start();

	}
}