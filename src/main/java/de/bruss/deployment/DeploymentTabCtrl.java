package de.bruss.deployment;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import de.bruss.remoteDatabase.RemoteDatabaseUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DeploymentTabCtrl implements Initializable {

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
	private ProgressBar progressBar;

	private static ObservableList<Config> data;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		name.setCellValueFactory(new PropertyValueFactory<Config, String>("name"));
		host.setCellValueFactory(new PropertyValueFactory<Config, String>("host"));
		localDbName.setCellValueFactory(new PropertyValueFactory<Config, String>("localDbName"));
		remoteDbName.setCellValueFactory(new PropertyValueFactory<Config, String>("remoteDbName"));
		serviceName.setCellValueFactory(new PropertyValueFactory<Config, String>("serviceName"));

		refresh();

		ContextMenu cm = new ContextMenu();
		MenuItem cmDeployButton = new MenuItem("Deployen");
		MenuItem cmGetDatabaseButton = new MenuItem("ServerDB -> Lokal-Dump");
		MenuItem cmDumpAndRestoreButton = new MenuItem("ServerDB -> LokalDB");
		cmDeployButton.setStyle("-fx-font-weight: bold;");
		MenuItem cmEditButton = new MenuItem("Editieren");
		MenuItem cmDeleteButton = new MenuItem("Löschen");
		cm.getItems().addAll(cmDeployButton, cmEditButton, cmDeleteButton, cmGetDatabaseButton, cmDumpAndRestoreButton);

		configTable.setRowFactory(config -> {
			TableRow<Config> row = new TableRow<>();

			if (row.getItem() != null && (StringUtils.isBlank(row.getItem().getRemotePath()) || row.getItem().getRemotePath().equals("/"))) {
				System.out.println("disabled");
				cmDeployButton.setDisable(true);
			}

			row.setOnMouseClicked(event -> {
				cmEditButton.setOnAction(cmEvent -> {
					// contextmenu edit clicked
					try {
						FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/EditConfig.fxml"));

						Stage stage = new Stage(StageStyle.DECORATED);
						stage.setScene(new Scene((Pane) loader.load()));

						EditConfigCtrl controller = loader.<EditConfigCtrl> getController();
						controller.initData(this, row.getItem());

						stage.show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				});

				// contextmenu delete clicked
				cmDeleteButton.setOnAction(cmEvent -> {
					ConfigService.remove(row.getItem());
					refresh();
				});

				// contextmenu delete clicked
				cmDeployButton.setOnAction(cmEvent -> {
					if (row.getItem() != null && StringUtils.isNotBlank(row.getItem().getRemotePath()) && !row.getItem().getRemotePath().equals("/")) {

						try {
							DeploymentUtils sshUtils = new DeploymentUtils(row.getItem(), progressBar);
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
						RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(row.getItem(), progressBar, false);
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
						RemoteDatabaseUtils sshUtils = new RemoteDatabaseUtils(row.getItem(), progressBar, true);
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

	@FXML
	protected void addConfig(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/EditConfig.fxml"));

		Stage stage = new Stage(StageStyle.DECORATED);
		stage.setScene(new Scene((Pane) loader.load()));

		EditConfigCtrl controller = loader.<EditConfigCtrl> getController();
		controller.initData(this);

		stage.show();
	}

}