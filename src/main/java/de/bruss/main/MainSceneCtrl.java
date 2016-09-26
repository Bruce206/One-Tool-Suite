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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import com.jcraft.jsch.JSchException;

import de.bruss.Context;
import de.bruss.Starter;
import de.bruss.UpdateService;
import de.bruss.deployment.Config;
import de.bruss.logger.DownloadLogService;
import de.bruss.logger.LogFileCtrl;
import de.bruss.settings.Settings;

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
	private Button tailLog_btn;
	@FXML
	private Button downloadLog_btn;

	@FXML
	private Button save_btn;
	@FXML
	private Button duplicate_btn;
	@FXML
	private Button new_btn;
	@FXML
	private Button delete_btn;

	public void appendText(String str) {
		Platform.runLater(() -> consoleout.appendText(str));
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Context.setMainSceneCtrl(this);

		showConsole(Boolean.parseBoolean(Settings.getInstance().getProperty("consoleVisible")));

		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				appendText(String.valueOf((char) b));
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));

		Platform.runLater(() -> {
			save_btn.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), new Runnable() {
				@Override
				public void run() {
					save_btn.fire();
				}
			});
		});
		// PrintStream out;
		// try {
		// out = new PrintStream(new FileOutputStream(Settings.appDataPath + "\\log.txt"));
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
	protected void downloadLog(ActionEvent event) throws IOException, JSchException {
		DownloadLogService downloadLogService = new DownloadLogService(Context.getEditConfigCtrl().getEditConfig(), Context.getProgressBar());

		new Thread(downloadLogService).start();
	}

	@FXML
	protected void dumpAndRestoreDb(ActionEvent event) {
		Context.getEditConfigCtrl().dumpAndRestoreDb();
	}

	@FXML
	protected void syncData(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/FileSyncModal.fxml"));

		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(((Node) event.getSource()).getScene().getWindow());
		stage.setScene(new Scene((Pane) loader.load()));
		stage.show();
	}

	@FXML
	protected void tailLog(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/TailLog.fxml"));

		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Log für " + Context.getEditConfigCtrl().getEditConfig().getName());
		stage.setScene(new Scene((Pane) loader.load()));

		final LogFileCtrl controller = (LogFileCtrl) loader.getController();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				controller.stop();
			}

		});
		stage.show();
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

	@FXML
	protected void reloadCss(ActionEvent event) throws IOException {
		Starter.reloadCss();
	}

	public void toggleConsoleout() throws IOException {
		String consoleVisibleStr = Settings.getInstance().getProperty("consoleVisible");
		boolean newConsoleVisible = !Boolean.parseBoolean(consoleVisibleStr);
		showConsole(newConsoleVisible);
		Settings.getInstance().setProperty("consoleVisible", String.valueOf(newConsoleVisible));
	}

	private void showConsole(boolean visible) {
		if (visible) {
			Context.getPrimaryStage().setHeight(Context.getPrimaryStage().getHeight() + 168);
			consoleout.setPrefHeight(168);
		} else {
			consoleout.setPrefHeight(0);
			consoleout.setMinHeight(0);
			Context.getPrimaryStage().setHeight(Context.getPrimaryStage().getHeight() - 168);
		}
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

	public void toggleLogFileButtons(boolean visible) {
		this.tailLog_btn.setDisable(!visible);
		this.downloadLog_btn.setDisable(!visible);
	}

}