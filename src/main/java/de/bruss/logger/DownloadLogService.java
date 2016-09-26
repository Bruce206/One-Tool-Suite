package de.bruss.logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.bruss.deployment.Config;
import de.bruss.ssh.SshUtils;

public class DownloadLogService implements Runnable {

	private Session session;
	private Config config;
	private ProgressBar progressBar;
	private boolean restoreLocal;

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
						System.out.println("Logfile saved to: " + file.getAbsolutePath());
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
