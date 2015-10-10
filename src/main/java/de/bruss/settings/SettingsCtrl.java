package de.bruss.settings;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsCtrl implements Initializable {

	@FXML
	private TextField username;

	@FXML
	private PasswordField password;

	@FXML
	private TextField filePath;

	@FXML
	private TextField postgresPath;

	private boolean isInitialEdit;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (!Settings.isEmpty()) {
			isInitialEdit = false;
			username.setText(Settings.getInstance().getProperty("username"));
			password.setText(Settings.getInstance().getProperty("password"));
			filePath.setText(Settings.getInstance().getProperty("sshPath"));
			postgresPath.setText(Settings.getInstance().getProperty("postgresPath"));
		} else {
			isInitialEdit = true;
		}
	}

	@FXML
	protected void searchFile(ActionEvent event) {
		final FileChooser fileChooser = new FileChooser();

		File file = fileChooser.showOpenDialog(new Stage());
		filePath.setText(file.getAbsolutePath());
	}

	@FXML
	protected void searchPostgresPath(ActionEvent event) {
		final DirectoryChooser fileChooser = new DirectoryChooser();

		File file = fileChooser.showDialog(new Stage());
		postgresPath.setText(file.getAbsolutePath());
	}

	@FXML
	protected void save(ActionEvent event) throws IOException {
		Settings.getInstance().create(username.getText(), password.getText(), filePath.getText(), postgresPath.getText());

		if (isInitialEdit) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/MainScene.fxml"));

			Stage stage = new Stage(StageStyle.DECORATED);
			stage.setScene(new Scene((Pane) loader.load()));

			stage.show();
		}

		((Stage) username.getScene().getWindow()).close();
	}

}
