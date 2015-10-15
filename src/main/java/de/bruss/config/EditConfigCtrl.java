package de.bruss.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.deployment.DeploymentUtils;
import de.bruss.filesync.FileSyncService;
import de.bruss.remoteDatabase.RemoteDatabaseUtils;

public class EditConfigCtrl implements Initializable {

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
	@FXML
	private CheckBox springBootConfig;
	@FXML
	private CheckBox databaseConfig;
	@FXML
	private CheckBox fileSyncConfig;

	@FXML
	private GridPane springBootConfigGrid;
	@FXML
	private GridPane databaseConfigGrid;
	@FXML
	private GridPane fileSyncConfigGrid;

	@FXML
	private Button spring_btn_1;
	@FXML
	private Button db_btn_1;
	@FXML
	private Button db_btn_2;
	@FXML
	private Button file_btn_1;

	@FXML
	private ProgressBar progressBar;

	private Config editConfig = new Config();

	@FXML
	private HBox fileCounterBox;

	@FXML
	private Label fileCounter;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Context.setEditConfigCtrl(this);
		Context.setProgressBar(progressBar);
		Context.setFileCounterBox(fileCounterBox);
		Context.setFileCounter(fileCounter);

		editConfig = new Config();
		setConfigInView();
	}

	@FXML
	protected void save(ActionEvent event) {
		if (editConfig == null || editConfig.getId() == null) {
			// @formatter:off
			Config config = new Config(
					localPath.getText(), 
					remotePath.getText(), 
					host.getText(), 
					name.getText(), 
					serviceName.getText(), 
					port.getText(), 
					localDbName.getText(), 
					remoteDbName.getText(), 
					remoteFilePath.getText(), 
					localFilePath.getText(),
					springBootConfig.isSelected(),
					databaseConfig.isSelected(),
					fileSyncConfig.isSelected());
			// @formatter:on
			ConfigService.addConfig(config);
			editConfig = config;
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
			editConfig.setSpringBootConfig(this.springBootConfig.isSelected());
			editConfig.setDatabaseConfig(this.databaseConfig.isSelected());
			editConfig.setFileSyncConfig(this.fileSyncConfig.isSelected());
			ConfigService.save(editConfig);
		}

		setConfigInView();
		Context.getConfigTableCtrl().refresh();
	}

	public void initData(Config editConfig) {
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
		this.springBootConfig.setSelected(editConfig.isSpringBootConfig());
		this.databaseConfig.setSelected(editConfig.isDatabaseConfig());
		this.fileSyncConfig.setSelected(editConfig.isFileSyncConfig());

		// reversed visible-flags because we don't actually want to toggle it
		toggleGrid(springBootConfigGrid, Lists.newArrayList(spring_btn_1), !editConfig.isSpringBootConfig());
		toggleGrid(databaseConfigGrid, Lists.newArrayList(db_btn_1, db_btn_2), !editConfig.isDatabaseConfig());
		toggleGrid(fileSyncConfigGrid, Lists.newArrayList(file_btn_1), !editConfig.isFileSyncConfig());
	}

	@FXML
	protected void delete(ActionEvent event) {
		ConfigService.remove(editConfig);
		editConfig = new Config();
		setConfigInView();
		Context.getConfigTableCtrl().refresh();
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

	@FXML
	protected void toggleSpringBootConfig(ActionEvent event) {
		toggleGrid(springBootConfigGrid, Lists.newArrayList(spring_btn_1), editConfig.isSpringBootConfig());
		editConfig.setSpringBootConfig(!editConfig.isSpringBootConfig());
	}

	@FXML
	protected void toggleDatabaseConfig(ActionEvent event) {
		toggleGrid(databaseConfigGrid, Lists.newArrayList(db_btn_1, db_btn_2), editConfig.isDatabaseConfig());
		editConfig.setDatabaseConfig(!editConfig.isDatabaseConfig());
	}

	@FXML
	protected void toggleFileSyncConfig(ActionEvent event) {
		toggleGrid(fileSyncConfigGrid, Lists.newArrayList(file_btn_1), editConfig.isFileSyncConfig());
		editConfig.setFileSyncConfig(!editConfig.isFileSyncConfig());
	}

	private void toggleGrid(GridPane grid, List<Node> nodesToToggle, boolean visible) {
		if (visible) {
			grid.setPrefHeight(10);
			grid.setMinHeight(10);
			grid.setVisible(false);
			for (Node node : nodesToToggle) {
				node.setVisible(false);
			}
		} else {
			grid.setVisible(true);
			grid.setMinHeight(GridPane.USE_COMPUTED_SIZE);
			grid.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
			for (Node node : nodesToToggle) {
				node.setVisible(true);
			}
		}
	}

	@FXML
	protected void toggleConsoleout(ActionEvent event) {
		Context.getMainSceneCtrl().toggleConsoleout();
	}

	@FXML
	protected void clearLog(ActionEvent event) throws IOException {
		Context.getMainSceneCtrl().clearLog();
	}

}