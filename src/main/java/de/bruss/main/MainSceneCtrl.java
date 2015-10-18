package de.bruss.main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.bruss.Context;
import de.bruss.UpdateService;
import de.bruss.deployment.Config;

public class MainSceneCtrl implements Initializable {

	@FXML
	private TextArea consoleout;

	@FXML
	private Button spring_btn_1;
	@FXML
	private Button db_btn_1;
	@FXML
	private Button db_btn_2;
	@FXML
	private Button file_btn_1;

	@FXML
	private Button save_btn;
	@FXML
	private Button duplicate_btn;
	@FXML
	private Button new_btn;
	@FXML
	private Button delete_btn;

	private boolean consoleVisible = true;

	public void appendText(String str) {
		Platform.runLater(() -> consoleout.appendText(str));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Context.setMainSceneCtrl(this);

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
	protected void delete(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Image img = new Image("/images/user-trash-2.png");
		ImageView imgView = new ImageView();
		imgView.setImage(img);
		alert.setGraphic(imgView);
		alert.setHeaderText("Soll die Konfiguration wirklich gelöscht werden?");

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			Context.getEditConfigCtrl().delete();
		}
	}

	@FXML
	protected void deploy(ActionEvent action) {
		Config currentConf = Context.getEditConfigCtrl().getEditConfig();

		Alert alert = new Alert(AlertType.CONFIRMATION);
		Image img = new Image("/images/deploy-2.png");
		ImageView imgView = new ImageView();
		imgView.setImage(img);
		alert.setGraphic(imgView);
		alert.setHeaderText("Soll folgende App installiert / aktualisiert werden?");

		String message = "App:\t\t\t" + currentConf.getServiceName() + "\nServer:\t\t" + currentConf.getHost() + "\nAutoconfig:\t" + (currentConf.isAutoconfig() ? "Ja" : "Nein");

		alert.setContentText(message);

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			Context.getEditConfigCtrl().deploy();
		}

	}

	@FXML
	protected void getDbDump(ActionEvent event) {
		Context.getEditConfigCtrl().getDbDump();
	}

	@FXML
	protected void dumpAndRestoreDb(ActionEvent event) {
		Context.getEditConfigCtrl().dumpAndRestoreDb();
	}

	@FXML
	protected void syncData(ActionEvent event) {
		Context.getEditConfigCtrl().syncData();
	}

	@FXML
	protected void addConfig(ActionEvent event) {
		Context.getEditConfigCtrl().addConfig();
	}

	@FXML
	protected void addCopyConfig(ActionEvent event) throws IllegalAccessException, InvocationTargetException {
		Context.getEditConfigCtrl().duplicate();
	}

	@FXML
	protected void save(ActionEvent event) {
		Context.getEditConfigCtrl().save();
	}

	@FXML
	protected void changeSettings(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/SettingsScene.fxml"));

		Stage stage = new Stage(StageStyle.DECORATED);
		stage.setScene(new Scene((Pane) loader.load()));
		stage.show();
	}

	public void toggleConsoleout() {
		if (consoleVisible) {
			consoleout.setPrefHeight(0);
			consoleout.setMinHeight(0);
			Context.getPrimaryStage().setHeight(Context.getPrimaryStage().getHeight() - 168);
		} else {
			Context.getPrimaryStage().setHeight(Context.getPrimaryStage().getHeight() + 168);
			consoleout.setPrefHeight(168);
		}
		consoleVisible = !consoleVisible;
	}

	@FXML
	protected void checkForUpdates(ActionEvent event) throws IOException {
		UpdateService updateService = new UpdateService();
		Thread t = new Thread(updateService);
		t.setDaemon(true);
		t.start();
	}

	public void clearLog() throws IOException {
		this.consoleout.clear();
	}

	public void toggleSpringBootButtons(boolean visible) {
		this.spring_btn_1.setDisable(!visible);
	}

	public void toggleDatabaseButtons(boolean visible) {
		this.db_btn_1.setDisable(!visible);
		this.db_btn_2.setDisable(!visible);
	}

	public void toggleFileSyncBootButtons(boolean visible) {
		this.file_btn_1.setDisable(!visible);
	}

	public void toggleEditOnlyVisibility(boolean visible) {
		this.duplicate_btn.setDisable(!visible);
		this.delete_btn.setDisable(!visible);
		this.save_btn.setDisable(false);
	}

}