package de.bruss.config;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import de.bruss.Context;
import de.bruss.deployment.Config;

public class ConfigTableCtrl implements Initializable {

	@FXML
	private TableView<Config> configTable;
	@FXML
	private TableColumn<Config, String> name;
	@FXML
	private TableColumn<Config, String> host;
	@FXML
	private TableColumn<Config, String> springBootConfigColumn;
	@FXML
	private TableColumn<Config, String> remoteDbName;

	private static ObservableList<Config> data;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Context.setConfigTableCtrl(this);

		name.setCellValueFactory(new PropertyValueFactory<Config, String>("name"));
		host.setCellValueFactory(new PropertyValueFactory<Config, String>("host"));

		springBootConfigColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Config, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<Config, String> p) {
				if (p.getValue() != null && p.getValue().isSpringBootConfig()) {
					return new SimpleStringProperty("X");
				} else {
					return new SimpleStringProperty("-");
				}
			}
		});

		refresh();

		configTable.setRowFactory(config -> {
			TableRow<Config> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (row.getItem() != null) {
						Context.getEditConfigCtrl().initData(row.getItem());
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