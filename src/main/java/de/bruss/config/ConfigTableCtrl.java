package de.bruss.config;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import de.bruss.deployment.Config;

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

		configTable.setRowFactory(config -> {
			TableRow<Config> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (row.getItem() != null) {
						editConfigTabController.initData(this, row.getItem());
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