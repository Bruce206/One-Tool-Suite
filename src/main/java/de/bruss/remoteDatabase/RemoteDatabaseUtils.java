package de.bruss.remoteDatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.bruss.deployment.Config;
import de.bruss.settings.Settings;
import de.bruss.ssh.SshUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RemoteDatabaseUtils implements Runnable {

	private Session session;
	private Config config;
	private ProgressBar progressBar;
	private boolean restoreLocal;

	public RemoteDatabaseUtils(Config config, ProgressBar progressBar, boolean restoreLocal) throws JSchException {

		this.config = config;
		this.progressBar = progressBar;
		this.session = SshUtils.getSession(config.getHost());
		this.restoreLocal = restoreLocal;
		this.session.connect();

	}

	private void restoreLocalDb(Path path) throws IOException {
		Process p;
		ProcessBuilder pb;
		pb = new ProcessBuilder(Settings.getInstance().getProperty("postgresPath") + "\\bin\\pg_restore.exe", "--username", "cms_admin", "--dbname", config.getLocalDbName(), path.toString());

		Map<String, String> env = pb.environment();
		env.put("PGPASSWORD", "cms");
		pb.redirectErrorStream(true);
		p = pb.start();
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String ll;
		while ((ll = br.readLine()) != null) {
			System.out.println(ll);
		}

	}
	
	private void dropLocalDb() throws IOException, InterruptedException, ExecutionException, LocalDatabaseInUseException {
		System.out.println("Dropping local database: " + config.getLocalDbName());
		
		Process p;
		ProcessBuilder pb;
		pb = new ProcessBuilder(Settings.getInstance().getProperty("postgresPath") + "\\bin\\dropdb.exe", "--username", "cms_admin", config.getLocalDbName());

		Map<String, String> env = pb.environment();
		env.put("PGPASSWORD", "cms");
		pb.redirectErrorStream(true);
		p = pb.start();

		int exitValue = p.waitFor();
		
		String output = "";
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String ll;
		while ((ll = br.readLine()) != null) {
			output += ll;
		}
		
		// continue if db doesn't exist because it will be created later
		if (exitValue > 0 && !output.contains("existiert nicht")) {

			final FutureTask<Boolean> alert = new FutureTask<Boolean>(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Datenbank konnte nicht gelöscht werden");
					alert.setHeaderText("Die Datenbank konnte nicht gelöscht werden!\n Falls noch Verbindungen zu dieser Datenbank offen sind (pgAdmin, Programm läuft o.Ä.) bitte schließen und OK klicken!");

					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == ButtonType.OK) {
						return true;
					} else {
						return false;
					}
				}
			});
			
			Platform.runLater(alert);
			
			if (alert.get()) {
				dropLocalDb();
				return;
			} else {
				throw new LocalDatabaseInUseException();
			}
		}



		System.out.println("-- [done]");
	}

	private void createLocalDb() throws IOException {
		Process p;
		ProcessBuilder pb;
		pb = new ProcessBuilder(Settings.getInstance().getProperty("postgresPath") + "\\bin\\createdb.exe", "--username", "postgres", "--owner", "cms_admin", config.getLocalDbName());

		Map<String, String> env = pb.environment();
		env.put("PGPASSWORD", "postgres");
		pb.redirectErrorStream(true);
		p = pb.start();
		InputStream is = p.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String ll;
		while ((ll = br.readLine()) != null) {
			System.out.println(ll);
		}
	}

	@Override
	public void run() {
		try {
			
			Preconditions.checkNotNull(config);
			Preconditions.checkNotNull(config.getRemoteDbName());
			
			if (StringUtils.isBlank(config.getRemoteDbName())) {
				throw new IllegalArgumentException("Remote Db Name should not be empty!");
			}

			// Create Dump
			System.out.println("Removing old dump from /tmp/" + config.getRemoteDbName() + ".dump");
			if (SshUtils.fileExistsOnServer(session, "/tmp/" + config.getRemoteDbName() + ".dump")) {
				SshUtils.removeFile(session, "/tmp/" + config.getRemoteDbName() + ".dump");
			}
			
			
			System.out.println("Sending pgdump for database: " + config.getRemoteDbName());
			String response2 = SshUtils.sendCommand(this.session,
					"su - postgres -c \"pg_dump -Ucms_admin -f/tmp/" + config.getRemoteDbName() + ".dump -Fc " + config.getRemoteDbName() + "\"");
			System.out.println("Database dump complete: " + response2);

			// Download file
			Path dbFile = Files.createTempFile("db_", ".dump");
			SshUtils.downloadFile("/tmp/" + config.getRemoteDbName() + ".dump", dbFile.toString(), session, progressBar);

			if (this.restoreLocal) {
				Preconditions.checkNotNull(config.getLocalDbName());
				dropLocalDb();

				System.out.println("Creating local database: " + config.getLocalDbName());
				createLocalDb();
				System.out.println("-- [done]");

				// restore db
				System.out.println("Restoring local database: " + config.getLocalDbName() + " from dump: " + dbFile.toString());
				restoreLocalDb(dbFile);
				System.out.println("-- [done]");
			} else {
				final FileChooser fileChooser = new FileChooser();

				FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Dumps", "*.dump", "*.backup");
				fileChooser.getExtensionFilters().add(extFilter);
				fileChooser.setTitle("Datenbankbackup speichern...");
				fileChooser.setInitialFileName(StringUtils.defaultString(config.getLocalDbName(), config.getRemoteDbName()) + ".dump");
				String downloadsFolder = System.getProperty("user.home") + "\\Downloads";
				System.out.println(downloadsFolder);
				fileChooser.setInitialDirectory(Paths.get(downloadsFolder).toFile());

				Platform.runLater(() -> {
					try {
						File file = fileChooser.showSaveDialog(new Stage());
						if (file != null && file.isFile()) {
							Files.copy(dbFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
							System.out.println("Dump saved to: " + file.getAbsolutePath());
						}
					} catch (Exception e) {
						System.out.println("Saving file failed!");
						e.printStackTrace();
					}
				});
			}

			System.out.println("Process finished! :)");

		} catch (IOException | JSchException | SftpException | RuntimeException | InterruptedException | ExecutionException | LocalDatabaseInUseException e) {
			System.out.println("Database backup / restore failed!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	public class LocalDatabaseInUseException extends Exception {
	}

}
