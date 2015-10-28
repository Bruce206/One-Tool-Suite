package de.bruss.filesync;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import de.bruss.Context;

public class FileSyncController implements Initializable {

	@FXML
	Label foldersChecked;
	@FXML
	Label foldersCreated;
	@FXML
	Label foldersDeleted;

	@FXML
	Label filesChecked;
	@FXML
	Label filesCreated;
	@FXML
	Label filesUpdated;
	@FXML
	Label filesDeleted;

	@FXML
	Label totalDownloadSize;

	@FXML
	Label currentFile;
	@FXML
	Label currentSize;
	@FXML
	ProgressBar syncProgressBar;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			FileSyncService fileSyncService = new FileSyncService(Context.getEditConfigCtrl().getEditConfig(), this);
			Thread t = new Thread(fileSyncService);
			t.start();
		} catch (Exception e) {
			System.err.println("FyleSync Operation failed!");
			e.printStackTrace();
		}
	}

	@FXML
	public void close(ActionEvent event) {
		((Stage) foldersChecked.getScene().getWindow()).close();
	}

	public void setFoldersChecked(String foldersChecked) {
		this.foldersChecked.setText(foldersChecked);
	}

	public void setFoldersCreated(String foldersCreated) {
		this.foldersCreated.setText(foldersCreated);
	}

	public void setFoldersDeleted(String foldersDeleted) {
		this.foldersDeleted.setText(foldersDeleted);
	}

	public void setFilesChecked(String filesChecked) {
		this.filesChecked.setText(filesChecked);
	}

	public void setFilesUpdated(String filesUpdated) {
		this.filesUpdated.setText(filesUpdated);
	}

	public void setFilesCreated(String filesCreated) {
		this.filesCreated.setText(filesCreated);
	}

	public void setFilesDeleted(String filesDeleted) {
		this.filesDeleted.setText(filesDeleted);
	}

	public void setCurrentFile(String currentFile) {
		this.currentFile.setText(currentFile);
	}

	public void setCurrentSize(String currentSize) {
		this.currentSize.setText(currentSize);
	}

	public void setSyncProgress(double progress) {
		this.syncProgressBar.setProgress(progress);
	}

	public void setTotalDowloadSize(String totalDownloadSize) {
		this.totalDownloadSize.setText(totalDownloadSize);
	}

}
