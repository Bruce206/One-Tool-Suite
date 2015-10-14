package de.bruss.config;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;

import org.apache.commons.lang3.StringUtils;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.deployment.DeploymentUtils;
import de.bruss.remoteDatabase.RemoteDatabaseUtils;

public class ConfigTableCtrl implements Initializable {

	@FXML
	private TableView<Config> configTable;
	@FXML
	private TableColumn<Config, String> serviceName;
	@FXML
	private TableColumn<Config, String> name;
	@FXML
	private TableColumn<Config, String> host;
	@FXML
	private TableColumn<Config, String> localDbName;
	@FXML
	private TableColumn<Config, String> remoteDbName;

	@FXML
	private EditConfigCtrl editConfigTabController;

	private static ObservableList<Config> data;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		name.setCellValueFactory(new PropertyValueFactory<Config, String>("name"));
		host.setCellValueFactory(new PropertyValueFactory<Config, String>("host"));

		refresh();

		ContextMenu cm = new ContextMenu();
		MenuItem cmDeployButton = new MenuItem("Deployen");
		MenuItem cmGetDatabaseButton = new MenuItem("ServerDB -> Lokal-Dump");
		MenuItem cmDumpAndRestoreButton = new MenuItem("ServerDB -> LokalDB");
		cmDeployButton.setStyle("-fx-font-weight: bold;");
		MenuItem cmDeleteButton = new MenuItem("Löschen");
		cm.getItems().addAll(cmDeployButton, cmDeleteButton, cmGetDatabaseButton, cmDumpAndRestoreButton);

		configTable.setRowFactory(config -> {
			TableRow<Config> row = new TableRow<>();

			if (row.getItem() != null && (StringUtils.isBlank(row.getItem().getRemotePath()) || row.getItem().getRemotePath().equals("/"))) {
				System.out.println("disabled");
				cmDeployButton.setDisable(true);
			}

			row.setOnMouseClicked(event -> {

				// contextmenu delete clicked
				cmDeleteButton.setOnAction(cmEvent -> {
					ConfigService.remove(row.getItem());
					refresh();
				});

				// contextmenu delete clicked
				cmDeployButton.setOnAction(cmEvent -> {
					if (row.getItem() != null && StringUtils.isNotBlank(row.getItem().getRemotePath()) && !row.getItem().getRemotePath().equals("/")) {

						try {
							DeploymentUtils sshUtils = new DeploymentUtils(row.getItem(), Context.getProgressBar());
							Thread t = new Thread(sshUtils);
							t.start();
						} catch (Exception e) {
							System.err.println("Deploying failed!");
							e.printStackTrace();
						}
					} else {
						System.out.println("Can't deploy without remotePath!");
					}

				});

				// contextmenu get Database clicked
				cmGetDatabaseButton.setOnAction(cmEvent -> {

					try {
						RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(row.getItem(), Context.getProgressBar(), false);
						Thread t = new Thread(sshUtils);
						t.start();
					} catch (Exception e) {
						System.err.println("Database Operation failed!");
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				});

				// contextmenu dump and restore clicked
				cmDumpAndRestoreButton.setOnAction(cmEvent -> {

					try {
						RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(row.getItem(), Context.getProgressBar(), true);
						Thread t = new Thread(sshUtils);
						t.start();
					} catch (Exception e) {
						System.err.println("Database Operation failed!");
						e.printStackTrace();
					}
				});

				if (event.getButton() == MouseButton.SECONDARY && (!row.isEmpty())) {
					// right button clicked -> open context menu
					cm.show(configTable, event.getScreenX(), event.getScreenY());
				}

				if (event.getButton() == MouseButton.PRIMARY) {
					if (cm.isShowing()) {
						cm.hide();
					} else {
						if (row.getItem() != null) {
							editConfigTabController.initData(this, row.getItem());
						}
					}
				}
			});
			return row;
		});
	}

	public void refresh() {
		data = FXCollections.observableList(ConfigService.getAll());
		configTable.setItems(data);
	}

}