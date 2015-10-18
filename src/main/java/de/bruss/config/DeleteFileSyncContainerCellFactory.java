package de.bruss.config;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DeleteFileSyncContainerCellFactory<FileSyncContainer, String> implements Callback<TableColumn<FileSyncContainer, String>, TableCell<FileSyncContainer, String>> {

	@Override
	public TableCell<FileSyncContainer, String> call(TableColumn<FileSyncContainer, String> p) {
		TableCell<FileSyncContainer, String> cell = new TableCell<FileSyncContainer, String>() {
			@Override
			protected void updateItem(Object item, boolean empty) {
				if (getTableRow() != null && getTableRow().getItem() != null) {

					final de.bruss.filesync.FileSyncContainer container = (de.bruss.filesync.FileSyncContainer) getTableRow().getItem();

					if (container != null) {
						super.updateItem((String) item, empty);

						Button button = new Button("Löschen");
						button.setOnAction(e -> {
							getTableView().getItems().remove(container);
						});
						setGraphic(button);
					}

				}
			}
		};
		return cell;
	}
}
