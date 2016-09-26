package de.bruss.ssh;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import de.bruss.settings.Settings;

public class SSHDialogCtrl implements Initializable {

	@FXML
	TextField username;

	@FXML
	PasswordField password;

	@FXML
	TextField filePath;

	@FXML
	public void saveCredentials(ActionEvent event) throws IOException {
		Settings.getInstance().setProperty("username", this.username.getText());
		Settings.getInstance().setProperty("sshPath", this.filePath.getText());
		Settings.getInstance().setPassword(this.password.getText());

		Stage stage = (Stage) username.getScene().getWindow();
		stage.close();
	}

	@FXML
	protected void searchFile(ActionEvent event) {
		final FileChooser fileChooser = new FileChooser();

		File file = fileChooser.showOpenDialog(new Stage());
		filePath.setText(file.getAbsolutePath());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.username.setText(Settings.getInstance().getProperty("username"));
		this.filePath.setText(Settings.getInstance().getProperty("sshPath"));
	}

}
