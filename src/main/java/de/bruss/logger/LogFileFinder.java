package de.bruss.logger;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import de.bruss.Context;
import de.bruss.filesync.SftpService;
import de.bruss.settings.Settings;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class LogFileFinder implements Initializable {
	ObservableList<String> items = FXCollections.observableArrayList();

	@FXML
	public ListView<String> logList = new ListView<String>(items);

	@FXML
	private BorderPane borderPane;

	@FXML
	private ProgressIndicator progressIndicator;

	private boolean cancelled = false;

	private String selectedPath;

	private TextField logFilePathField;

	int fileCounter = 0;
	int folderCounter = 0;
	
	private static final int MAX_DEPTH = 2;

	@FXML
	public void logFileSelected(MouseEvent event) {
		cancelled = true;
		logFilePathField.setText(logList.getSelectionModel().getSelectedItem());

		Stage stage = (Stage) borderPane.getScene().getWindow();
		stage.close();
	}

	private static final String[] logFolders = { "/var/log/", "/var/www" };
	private static final List<String> excludeFolders = Arrays.asList("apache2", "dist-upgrade", "ntpstats", "fsck", "exim4", "samba", "installer", "cron-apt", "landscape", "ConsoleKit", "unattended-upgrades");

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			// call before Thread is created
			SftpService.getFsManager();
			logList.setCellFactory(TextFieldListCell.forListView());

			new Thread(new Runnable() {
				@Override
				public void run() {

					for (String path : logFolders) {
						folderCounter++;
						if (cancelled) {
							break;
						}
						String uri = "sftp://" + Settings.getInstance().getProperty("username") + "@" + Context.getEditConfigCtrl().getEditConfig().getHost() + path;
						FileObject fo = null;
						try {
							fo = SftpService.resolveFile(uri);
							findLogFiles(fo, 0);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (fo != null) {
								try {
									fo.close();
								} catch (FileSystemException e) {
									e.printStackTrace();
								}
							}
						}
					}

					System.out.println("Log-Files found: " + logList.getItems().size() + ". Searched " + folderCounter + " folders and " + fileCounter + " files.");
					borderPane.getBottom().setVisible(false);
					progressIndicator.setVisible(false);
				}

				private void findLogFiles(FileObject fo, int depth) throws FileSystemException {
					if (depth++ > MAX_DEPTH || excludeFolders.contains(fo.getName().getBaseName())) {
						return;
					}
					
					for (FileObject file : fo.getChildren()) {
						if (cancelled) {
							return;
						}
						
						if (file.getType().equals(FileType.FOLDER)) {
							folderCounter++;
							findLogFiles(file, depth);
						} else {
							fileCounter++;
							if (file.getName().getExtension().equals("log")) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										String logFilePath = file.getName().getPath();
										logList.getItems().add(logFilePath);
										logList.scrollTo(logFilePath);
									}
								});
							}
						}
					}
				}
			}).start();

		} catch (Exception e) {
			System.err.println("Finding Log-Files failed!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public String getSelectedPath() {
		return selectedPath;
	}

	public void setLogFilePathField(TextField logFilePath) {
		this.logFilePathField = logFilePath;
	}

}
