package de.bruss.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.deployment.DeploymentUtils;
import de.bruss.filesync.FileSyncContainer;
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
	private TextField ip;
	@FXML
	private TextField serverName;
	@FXML
	private TextField logFilePath;
	@FXML
	private CheckBox springBootConfig;
	@FXML
	private CheckBox databaseConfig;
	@FXML
	private CheckBox fileSyncConfig;
	@FXML
	private CheckBox autoconfig;
	@FXML
	private CheckBox logFileConfig;

	@FXML
	private GridPane springBootConfigGrid;
	@FXML
	private GridPane databaseConfigGrid;
	@FXML
	private VBox fileSyncConfigVBox;
	@FXML
	private GridPane autoconfigGrid;
	@FXML
	private GridPane logFileGrid;

	@FXML
	private ProgressBar progressBar;

	private Config editConfig = new Config();

	@FXML
	private ScrollPane scrollPane;

	@FXML
	private VBox editConfigVBox;

	@FXML
	private HBox fileCounterBox;

	@FXML
	private Label fileCounter;

	@FXML
	private TableView<FileSyncContainer> fileSyncTable;
	@FXML
	private TableColumn<FileSyncContainer, String> localPathCol;
	@FXML
	private TableColumn<FileSyncContainer, String> remotePathCol;
	@FXML
	private TableColumn<FileSyncContainer, String> actionColumn;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Context.setEditConfigCtrl(this);
		Context.setProgressBar(progressBar);
		Context.setFileCounterBox(fileCounterBox);
		Context.setFileCounter(fileCounter);

		Callback<TableColumn<FileSyncContainer, String>, TableCell<FileSyncContainer, String>> cellFactory = new Callback<TableColumn<FileSyncContainer, String>, TableCell<FileSyncContainer, String>>() {
			@Override
			public TableCell<FileSyncContainer, String> call(TableColumn<FileSyncContainer, String> param) {
				return new EditingCell();
			}
		};

		fileSyncTable.setFixedCellSize(25);
		fileSyncTable.prefHeightProperty().bind(Bindings.size(fileSyncTable.getItems()).multiply(25).add(fileSyncTable.getItems().size() > 0 ? 25.1 : 50.1));
		fileSyncTable.minHeightProperty().bind(fileSyncTable.prefHeightProperty());
		fileSyncTable.maxHeightProperty().bind(fileSyncTable.prefHeightProperty());

		localPathCol.setCellValueFactory(new PropertyValueFactory<FileSyncContainer, String>("localFilePath"));
		localPathCol.setCellFactory(cellFactory);
		localPathCol.setOnEditCommit(new EventHandler<CellEditEvent<FileSyncContainer, String>>() {
			@Override
			public void handle(CellEditEvent<FileSyncContainer, String> t) {
				t.getRowValue().setLocalFilePath(t.getNewValue());
			}
		});

		remotePathCol.setCellValueFactory(new PropertyValueFactory<FileSyncContainer, String>("remoteFilePath"));
		remotePathCol.setCellFactory(cellFactory);
		remotePathCol.setOnEditCommit(new EventHandler<CellEditEvent<FileSyncContainer, String>>() {
			@Override
			public void handle(CellEditEvent<FileSyncContainer, String> t) {
				t.getRowValue().setRemoteFilePath(t.getNewValue());
			}
		});
		remotePathCol.setSortType(TableColumn.SortType.ASCENDING);

		actionColumn.setCellFactory(new DeleteFileSyncContainerCellFactory<FileSyncContainer, String>());

		this.editConfigVBox.setVisible(false);
	}

	public void initData(Config editConfig) {
		this.editConfigVBox.setVisible(true);
		if (editConfig.getId() != null) {
			this.editConfig = ConfigService.getConfig(editConfig.getId());
		} else {
			this.editConfig = editConfig;
		}

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

	public void setConfigInView() {
		this.host.setText(editConfig.getHost());
		this.localPath.setText(editConfig.getLocalPath());
		this.remotePath.setText(editConfig.getRemotePath());
		this.name.setText(editConfig.getName());
		this.serviceName.setText(editConfig.getServiceName());
		this.port.setText(editConfig.getPort());
		this.localDbName.setText(editConfig.getLocalDbName());
		this.remoteDbName.setText(editConfig.getRemoteDbName());
		this.springBootConfig.setSelected(editConfig.isSpringBootConfig());
		this.databaseConfig.setSelected(editConfig.isDatabaseConfig());
		this.fileSyncConfig.setSelected(editConfig.isFileSyncConfig());
		this.logFileConfig.setSelected(editConfig.isLogFileConfig());
		this.logFilePath.setText(editConfig.getLogFilePath());
		this.autoconfig.setSelected(editConfig.isAutoconfig());
		this.serverName.setText(editConfig.getServerName());
		this.ip.setText(editConfig.getIP());

		fileSyncTable.getItems().clear();
		fileSyncTable.getItems().addAll(editConfig.getFileSyncList());
		fileSyncTable.refresh();

		setVisibility(VisibilityGroup.SPRING_BOOT, editConfig.isSpringBootConfig());
		setVisibility(VisibilityGroup.DABATASE, editConfig.isDatabaseConfig());
		setVisibility(VisibilityGroup.FILE_SYNC, editConfig.isFileSyncConfig());
		setVisibility(VisibilityGroup.EDIT_ONLY, editConfig.getId() != null);
		setVisibility(VisibilityGroup.AUTOCONFIG, editConfig.isAutoconfig());
		setVisibility(VisibilityGroup.LOGFILE, editConfig.isLogFileConfig());

	}

	@FXML
	protected void addFileSyncContainer(ActionEvent event) {
		fileSyncTable.getItems().add(new FileSyncContainer("", ""));
		fileSyncTable.refresh();
	}

	@FXML
	protected void toggleSpringBootConfig(ActionEvent event) {
		editConfig.setSpringBootConfig(!editConfig.isSpringBootConfig());
		setVisibility(VisibilityGroup.SPRING_BOOT, editConfig.isSpringBootConfig());
	}

	@FXML
	protected void toggleDatabaseConfig(ActionEvent event) {
		editConfig.setDatabaseConfig(!editConfig.isDatabaseConfig());
		setVisibility(VisibilityGroup.DABATASE, editConfig.isDatabaseConfig());
	}

	@FXML
	protected void toggleFileSyncConfig(ActionEvent event) {
		editConfig.setFileSyncConfig(!editConfig.isFileSyncConfig());
		setVisibility(VisibilityGroup.FILE_SYNC, editConfig.isFileSyncConfig());
	}

	@FXML
	protected void toggleAutoconfig(ActionEvent event) {
		editConfig.setAutoconfig(!editConfig.isAutoconfig());
		setVisibility(VisibilityGroup.AUTOCONFIG, editConfig.isAutoconfig());
	}

	@FXML
	protected void toggleLogFileConfig(ActionEvent event) {
		editConfig.setLogFileConfig(!editConfig.isLogFileConfig());
		setVisibility(VisibilityGroup.LOGFILE, editConfig.isLogFileConfig());
	}

	enum VisibilityGroup {
		SPRING_BOOT, DABATASE, FILE_SYNC, EDIT_ONLY, AUTOCONFIG, LOGFILE
	}

	private void setVisibility(VisibilityGroup visibilityGroup, boolean visible) {
		Pane pane = null;

		switch (visibilityGroup) {
		case SPRING_BOOT:
			if (Context.getMainSceneCtrl() != null) {
				Context.getMainSceneCtrl().toggleSpringBootButtons(visible && editConfig.getId() != null);
			}
			pane = springBootConfigGrid;
			break;
		case DABATASE:
			if (Context.getMainSceneCtrl() != null) {
				Context.getMainSceneCtrl().toggleDatabaseButtons(visible && editConfig.getId() != null);
			}
			pane = databaseConfigGrid;
			break;
		case FILE_SYNC:
			if (Context.getMainSceneCtrl() != null) {
				Context.getMainSceneCtrl().toggleFileSyncBootButtons(visible && editConfig.getId() != null);
			}
			pane = fileSyncConfigVBox;
			break;
		case EDIT_ONLY:
			if (Context.getMainSceneCtrl() != null) {
				Context.getMainSceneCtrl().toggleEditOnlyVisibility(visible);
			}
			break;
		case AUTOCONFIG:
			pane = autoconfigGrid;
		case LOGFILE:
			if (Context.getMainSceneCtrl() != null) {
				Context.getMainSceneCtrl().toggleLogFileButtons(visible && editConfig.getId() != null);
			}
			pane = logFileGrid;
		default:
			break;
		}

		if (pane != null) {
			pane.setVisible(visible);

			if (!visible) {
				pane.setPrefHeight(10);
				pane.setMinHeight(10);
			} else {
				pane.setMinHeight(GridPane.USE_COMPUTED_SIZE);
				pane.setPrefHeight(GridPane.USE_COMPUTED_SIZE);
			}
		}

	}

	@FXML
	protected void toggleConsoleout(ActionEvent event) throws IOException {
		Context.getMainSceneCtrl().toggleConsoleout();
	}

	@FXML
	protected void clearLog(ActionEvent event) throws IOException {
		Context.getMainSceneCtrl().clearLog();
	}

	public void save() {
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
					springBootConfig.isSelected(),
					databaseConfig.isSelected(),
					fileSyncConfig.isSelected(),
					autoconfig.isSelected(),
					ip.getText(),
					serverName.getText(),
					fileSyncTable.getItems(),
					logFilePath.getText());
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
			editConfig.setSpringBootConfig(this.springBootConfig.isSelected());
			editConfig.setDatabaseConfig(this.databaseConfig.isSelected());
			editConfig.setFileSyncConfig(this.fileSyncConfig.isSelected());
			editConfig.setAutoconfig(this.autoconfig.isSelected());
			editConfig.setIP(this.ip.getText());
			editConfig.setServerName(this.serverName.getText());
			editConfig.setFileSyncList(fileSyncTable.getItems());
			ConfigService.save(editConfig);
		}

		initData(editConfig);
		Context.getConfigTableCtrl().refresh();
	}

	public void duplicate() throws IllegalAccessException, InvocationTargetException {
		Config newConfig = new Config();
		BeanUtils.copyProperties(newConfig, editConfig);
		newConfig.setId(null);
		newConfig.setName("Kopie von " + newConfig.getName());
		initData(newConfig);
		save();
	}

	public void dumpAndRestoreDb() {
		try {
			RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(editConfig, Context.getProgressBar(), true);
			Thread t = new Thread(sshUtils);
			t.start();
		} catch (Exception e) {
			System.err.println("Database Operation failed!");
			e.printStackTrace();
		}
	}

	public void getDbDump() {
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

	public void deploy() {
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

	public void delete() {
		ConfigService.remove(editConfig);
		initData(new Config());
		editConfigVBox.setVisible(false);
		Context.getConfigTableCtrl().refresh();
	}

	public void addConfig() {
		initData(new Config());
	}

	public Config getEditConfig() {
		return editConfig;
	}

}