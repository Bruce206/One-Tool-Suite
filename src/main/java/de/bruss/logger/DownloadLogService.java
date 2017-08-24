package de.bruss.logger;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import de.bruss.deployment.Config;
import de.bruss.ssh.SshUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class DownloadLogService implements Runnable {

	private Session session;
	private Config config;
	private ProgressBar progressBar;
	private boolean restoreLocal;

	private final Logger logger = LoggerFactory.getLogger(DownloadLogService.class);

	public DownloadLogService(Config config, ProgressBar progressBar) throws JSchException {
		this.config = config;
		this.progressBar = progressBar;
		this.session = SshUtils.getSession(config.getHost());
		this.session.connect();
	}

	@Override
	public void run() {
		Path tempFile;
		try {
			tempFile = Files.createTempFile("log_" + config.getHost() + "_", ".log");

			SshUtils.downloadFile(config.getLogFilePath(), tempFile.toString(), session, progressBar);

			final FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Logfiles", ".log");
			fileChooser.getExtensionFilters().add(extFilter);
			fileChooser.setTitle("Logdatei speichern...");
			fileChooser.setInitialFileName(StringUtils.defaultString(config.getLogFilePath().substring(config.getLogFilePath().lastIndexOf("/") + 1), config.getHost() + ".log"));
			String downloadsFolder = System.getProperty("user.home") + "\\Downloads";
			fileChooser.setInitialDirectory(Paths.get(downloadsFolder).toFile());

			Platform.runLater(() -> {
				try {
					File file = fileChooser.showSaveDialog(new Stage());
					if (file != null) {
						Files.copy(tempFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
						logger.info("Logfile saved to: " + file.getAbsolutePath());
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								Alert alert = new Alert(AlertType.CONFIRMATION);
								alert.setTitle("Download abgeschlossen");
								alert.setHeaderText("Log-Datei öffnen?");
								ButtonType buttonOk = new ButtonType("Ja", ButtonData.OK_DONE);
								ButtonType buttonCancel = new ButtonType("Nein", ButtonData.CANCEL_CLOSE);
								alert.getButtonTypes().setAll(buttonOk, buttonCancel);

								Optional<ButtonType> result = alert.showAndWait();
								if (result.get() == buttonOk){
								    try {
										Desktop.getDesktop().open(file);
									} catch (IOException e) {
										e.printStackTrace();
									}
								} 
							}
						});
						
					}
				} catch (Exception e) {
					System.err.println("Saving file failed!");
					e.printStackTrace();
				}
			});
		} catch (IOException | JSchException | SftpException e1) {
			e1.printStackTrace();
		}

	}

}
