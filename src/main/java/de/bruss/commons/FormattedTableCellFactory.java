package de.bruss.commons;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import com.cathive.fonts.fontawesome.FontAwesomeIcon;
import com.cathive.fonts.fontawesome.FontAwesomeIconView;

public class FormattedTableCellFactory<Config, String> implements Callback<TableColumn<Config, String>, TableCell<Config, String>> {

	private java.lang.String property;

	public FormattedTableCellFactory(java.lang.String property) {
		this.property = property;
	}

	@Override
	public TableCell<Config, String> call(TableColumn<Config, String> p) {
		TableCell<Config, String> cell = new TableCell<Config, String>() {
			@Override
			protected void updateItem(Object item, boolean empty) {
				de.bruss.deployment.Config config = null;
				if (getTableRow() != null) {
					config = (de.bruss.deployment.Config) getTableRow().getItem();
				}

				super.updateItem((String) item, empty);

				boolean checked = false;

				if (config != null) {
					switch (property) {
					case "springBootConfig": {
						if (config.isSpringBootConfig()) {
							checked = true;
						}
						break;
					}
					case "databaseConfig": {
						if (config.isDatabaseConfig()) {
							checked = true;
						}
						break;
					}
					case "fileSyncConfig": {
						if (config.isFileSyncConfig()) {
							checked = true;
						}
						break;
					}
					}
				}

				if (checked) {
					FontAwesomeIconView icon = new FontAwesomeIconView();
					icon.setIcon(FontAwesomeIcon.ICON_OK);
					setGraphic(icon);
				}
			}
		};
		return cell;
	}
}