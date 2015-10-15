package de.bruss.config;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.deployment.DeploymentUtils;
import de.bruss.filesync.FileSyncService;
import de.bruss.remoteDatabase.RemoteDatabaseUtils;

public class EditConfigCtrl {

	@FXML
	private TextField host;
	@FXML
	private TextField name;
	@FXML
	private TextField localPath;
	@FXML
	private TextField remotePath;
	@FXML
	private TextField serviceName;
	@FXML
	private TextField port;
	@FXML
	private TextField localDbName;
	@FXML
	private TextField remoteDbName;
	@FXML
	private TextField remoteFilePath;
	@FXML
	private TextField localFilePath;

	private ConfigTableCtrl configTableCtrl;
	private Config editConfig;

	@FXML
	protected void save(ActionEvent event) {
		if (editConfig.getId() == null) {
			Config config = new Config(localPath.getText(), remotePath.getText(), host.getText(), name.getText(), serviceName.getText(), port.getText(), localDbName.getText(), remoteDbName.getText(), remoteFilePath.getText(), localFilePath.getText());
			ConfigService.addConfig(config);
		} else {
			editConfig.setHost(this.host.getText());
			editConfig.setLocalPath(this.localPath.getText());
			editConfig.setRemotePath(this.remotePath.getText());
			editConfig.setName(this.name.getText());
			editConfig.setServiceName(this.serviceName.getText());
			editConfig.setPort(this.port.getText());
			editConfig.setLocalDbName(this.localDbName.getText());
			editConfig.setRemoteDbName(this.remoteDbName.getText());
			editConfig.setRemoteFilePath(this.remoteFilePath.getText());
			editConfig.setLocalFilePath(this.localFilePath.getText());
			ConfigService.save(editConfig);
		}

		configTableCtrl.refresh();
	}

	public void initData(ConfigTableCtrl configTableCtrl) {
		this.configTableCtrl = configTableCtrl;
	}

	public void initData(ConfigTableCtrl configTableCtrl, Config editConfig) {
		this.configTableCtrl = configTableCtrl;
		this.editConfig = editConfig;
		setConfigInView();
	}

	@FXML
	protected void searchJarPath(ActionEvent event) {
		final DirectoryChooser fileChooser = new DirectoryChooser();

		File file = fileChooser.showDialog(new Stage());
		if (file != null) {
			localPath.setText(file.getAbsolutePath());
		}

	}

	@FXML
	protected void addConfig(ActionEvent event) {
		editConfig = new Config();
		setConfigInView();
	}

	private void setConfigInView() {
		this.host.setText(editConfig.getHost());
		this.localPath.setText(editConfig.getLocalPath());
		this.remotePath.setText(editConfig.getRemotePath());
		this.name.setText(editConfig.getName());
		this.serviceName.setText(editConfig.getServiceName());
		this.port.setText(editConfig.getPort());
		this.localDbName.setText(editConfig.getLocalDbName());
		this.remoteDbName.setText(editConfig.getRemoteDbName());
		this.remoteFilePath.setText(editConfig.getRemoteFilePath());
		this.localFilePath.setText(editConfig.getLocalFilePath());
	}

	@FXML
	protected void delete(ActionEvent event) {
		ConfigService.remove(editConfig);
		configTableCtrl.refresh();
	}

	@FXML
	protected void deploy(ActionEvent action) {
		if (editConfig != null && StringUtils.isNotBlank(editConfig.getRemotePath()) && !editConfig.getRemotePath().equals("/")) {

			try {
				DeploymentUtils sshUtils = new DeploymentUtils(editConfig, Context.getProgressBar());
				Thread t = new Thread(sshUtils);
				t.start();
			} catch (Exception e) {
				System.err.println("Deploying failed!");
				e.printStackTrace();
			}
		} else {
			System.out.println("Can't deploy without remotePath!");
		}
	}

	@FXML
	protected void getDbDump(ActionEvent event) {
		try {
			RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(editConfig, Context.getProgressBar(), false);
			Thread t = new Thread(sshUtils);
			t.start();
		} catch (Exception e) {
			System.err.println("Database Operation failed!");
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	protected void dumpAndRestoreDb(ActionEvent event) {
		try {
			RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(editConfig, Context.getProgressBar(), true);
			Thread t = new Thread(sshUtils);
			t.start();
		} catch (Exception e) {
			System.err.println("Database Operation failed!");
			e.printStackTrace();
		}
	}

	@FXML
	protected void syncData(ActionEvent event) {
		try {
			FileSyncService fileSyncService = new FileSyncService(editConfig);
			Thread t = new Thread(fileSyncService);
			t.start();
		} catch (Exception e) {
			System.err.println("FyleSync Operation failed!");
			e.printStackTrace();
		}
	}
}