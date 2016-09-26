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
import de.bruss.Context;
import de.bruss.commons.FormattedTableCellFactory;
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
	private TableColumn<Config, String> databaseConfigColumn;
	@FXML
	private TableColumn<Config, String> fileSyncConfigColumn;
	@FXML
	private TableColumn<Config, String> logFileConfigColumn;

	private static ObservableList<Config> data;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Context.setConfigTableCtrl(this);

		name.setCellValueFactory(new PropertyValueFactory<Config, String>("name"));
		name.setSortType(TableColumn.SortType.ASCENDING);
		host.setCellValueFactory(new PropertyValueFactory<Config, String>("host"));

		springBootConfigColumn.setCellFactory(new FormattedTableCellFactory<Config, String>("springBootConfig"));
		databaseConfigColumn.setCellFactory(new FormattedTableCellFactory<Config, String>("databaseConfig"));
		fileSyncConfigColumn.setCellFactory(new FormattedTableCellFactory<Config, String>("fileSyncConfig"));
		logFileConfigColumn.setCellFactory(new FormattedTableCellFactory<Config, String>("logFileConfig"));

		configTable.getSortOrder().add(name);
		refresh();

		configTable.setRowFactory(config -> {
			TableRow<Config> row = new TableRow<>();

			row.setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					if (row.getItem() != null) {
						Context.getEditConfigCtrl().initData(ConfigService.getConfig(row.getItem().getId()));
					}
				}
			});
			return row;
		});
	}

	public void refresh() {
		int selectedItem = configTable.getSelectionModel().getSelectedIndex();
		TableColumn<Config, ?> sort = configTable.getSortOrder().get(0);

		data = FXCollections.observableList(ConfigService.getAll());

		configTable.setItems(data);
		configTable.refresh();
		configTable.getSortOrder().add(sort);
		configTable.requestFocus();
		configTable.getSelectionModel().select(selectedItem);
		configTable.getFocusModel().focus(selectedItem);

	}
}