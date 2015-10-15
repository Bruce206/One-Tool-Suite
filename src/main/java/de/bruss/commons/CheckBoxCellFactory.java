package de.bruss.commons;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

public class CheckBoxCellFactory<Config, Boolean> implements Callback<TableColumn<Config, Boolean>, TableCell<Config, Boolean>> {
	@Override
	public TableCell<Config, Boolean> call(TableColumn<Config, Boolean> p) {
		return new CheckBoxTableCell<>();
	}
}
