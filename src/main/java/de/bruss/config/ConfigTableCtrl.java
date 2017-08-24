package de.bruss.config;

import de.bruss.Context;
import de.bruss.deployment.Category;
import de.bruss.deployment.Config;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;

import java.net.URL;
import java.util.*;

public class ConfigTableCtrl implements Initializable {

//	@FXML
//	private TableView<Config> configTable;
//	@FXML
//	private TableColumn<Config, String> name;
//	@FXML
//	private TableColumn<Config, String> host;
//	@FXML
//	private TreeTableColumn<Object, String> springBootConfigColumn;
//	@FXML
//	private TreeTableColumn<Object, String> databaseConfigColumn;
//	@FXML
//	private TreeTableColumn<Object, String> fileSyncConfigColumn;
//	@FXML
//	private TreeTableColumn<Object, String> logFileConfigColumn;

    @FXML
    private TreeTableView<Object> treeTableView;

    @FXML
    private TreeTableColumn<Object, String> treetableHostname;
    @FXML
    private TreeTableColumn<Object, String> treetableName;

    @FXML
    private Button addCategoryBtn;

    private static ObservableList<Config> data;

    private Image folderImage = new Image(getClass().getResourceAsStream("/images/folder.png"));
    private Image configImage = new Image(getClass().getResourceAsStream("/images/config.png"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Context.setConfigTableCtrl(this);

//		name.setCellValueFactory(new PropertyValueFactory<>("name"));
//		name.setSortType(TableColumn.SortType.ASCENDING);
//		host.setCellValueFactory(new PropertyValueFactory<>("host"));
//
//		configTable.getSortOrder().add(name);
        refresh();

//		configTable.setRowFactory(config -> {
//			TableRow<Config> row = new TableRow<>();
//
//			row.setOnMouseClicked(event -> {
//				if (event.getButton() == MouseButton.PRIMARY) {
//					if (row.getItem() != null) {
//						Context.getEditConfigCtrl().initData(ConfigService.getConfig(row.getItem().getId()));
//					}
//				}
//			});
//			return row;
//		});

        treeTableView.setEditable(true);
        treetableName.setEditable(true);
        treetableName.setOnEditCommit(event -> {
            if (event.getRowValue().getValue() != null) {
                if (event.getRowValue().getValue() instanceof Category) {
                    final Category category = (Category) event.getRowValue().getValue();
                    category.setName(event.getNewValue());
                    CategoryService.save(category);
                    Context.getEditConfigCtrl().setCategoriesInChoiceBox();
                }

                if (event.getRowValue().getValue() instanceof Config) {
                    final Config config = (Config) event.getRowValue().getValue();
                    config.setName(event.getNewValue());
                    ConfigService.save(config);
                }
            }
        });

        treeTableView.setRowFactory(treeTableView -> {
            final TreeTableRow<Object> row = new TreeTableRow<>();

            final ContextMenu rowMenu = new ContextMenu();
            MenuItem removeItem = new MenuItem("Entfernen");
            removeItem.setOnAction(t -> {
                if (row.getTreeItem().getValue() instanceof Category) {
                    Category cat = (Category) row.getTreeItem().getValue();
                    CategoryService.remove(cat);
                }

                if (row.getTreeItem().getValue() instanceof Config) {
                    Config config = (Config) row.getTreeItem().getValue();
                    ConfigService.remove(config);
                }

                treeTableView.getSelectionModel().clearSelection();
                refresh();

            });
            rowMenu.getItems().add(removeItem);
            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty()))
                    .then(rowMenu)
                    .otherwise((ContextMenu) null));

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (row.getTreeItem() != null && row.getTreeItem().getValue() != null && row.getTreeItem().getValue() instanceof Config) {
                        Context.getEditConfigCtrl().initData(ConfigService.getConfig(((Config) row.getTreeItem().getValue()).getId()));
                    }
                }
            });

            row.setOnDragDetected(event -> {
                // drag was detected, start drag-and-drop gesture
                TreeItem<Object> selected = treeTableView.getSelectionModel().getSelectedItem();
                // to access your Object use 'selected.getValue()'

                if (selected == null || selected.getValue() instanceof Category) {
                    return;
                }

                Dragboard db = treeTableView.startDragAndDrop(TransferMode.ANY);

                // create a miniature of the row you're dragging
                db.setDragView(row.snapshot(null, null));

                // Keep whats being dragged on the clipboard
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(((Config) selected.getValue()).getId()));
                db.setContent(content);

                event.consume();
            });
            row.setOnDragOver(event -> {
                if (row.getTreeItem().getValue() instanceof Category) {
                    row.setStyle("-fx-font-weight: bold;");
                    event.acceptTransferModes(TransferMode.ANY);
                    event.consume();
                }
            });
            row.setOnDragExited(event -> {
                if (row != null && row.getTreeItem() != null && row.getTreeItem().getValue() instanceof Category) {
                    row.setStyle("-fx-font-weight: normal;");
                    event.acceptTransferModes(TransferMode.ANY);
                    event.consume();
                }
            });
            row.setOnDragDropped(event -> {
                boolean success = false;
                if (event.getDragboard().hasString()) {
                    if (!row.isEmpty()) {
                        TreeItem<Object> droppedon = row.getTreeItem();

                        if (droppedon.getValue() instanceof Category) {
                            Category cat = (Category) droppedon.getValue();
                            Config conf = ConfigService.getConfig(Long.parseLong(event.getDragboard().getString()));

                            Category oldCat = conf.getCategory();
                            if (oldCat != null) {
                                oldCat.getConfigs().remove(conf);
                                CategoryService.save(oldCat);
                            }

                            if (cat.getId() != null) {
                                cat.getConfigs().add(conf);
                                CategoryService.save(cat);

                                conf.setCategory(cat);
                                ConfigService.save(conf);
                            } else {
                                conf.setCategory(null);
                                ConfigService.save(conf);
                            }

                            refresh();
                            row.setStyle("-fx-font-weight: normal;");
                            event.setDropCompleted(true);
                            event.consume();
                        }
                    }
                }

            });

            return row;
        });

        addCategoryBtn.setOnMouseClicked(event -> addCategory());

    }

    public void refresh() {
//		int selectedItem = configTable.getSelectionModel().getSelectedIndex();
//		TableColumn<Config, ?> sort = configTable.getSortOrder().get(0);
//
//		data = FXCollections.observableList(ConfigService.getAll());
//
//		configTable.setItems(data);
//		configTable.refresh();
//		configTable.getSortOrder().add(sort);
//		configTable.requestFocus();
//		configTable.getSelectionModel().select(selectedItem);
//		configTable.getFocusModel().focus(selectedItem);

        final Set<String> expandedCategoryNames = new HashSet<>();
        if (treeTableView.getRoot() != null) {
            final TreeItem<Object> oldRoot = treeTableView.getRoot();

            for (TreeItem<Object> item : oldRoot.getChildren()) {
                if (item.isExpanded()) {
                    expandedCategoryNames.add(((Category) item.getValue()).getName());
                }
            }
        }


        final TreeItem<Object> root = new TreeItem<>();
        treeTableView.setRoot(root);
        root.setExpanded(true);
        treeTableView.setShowRoot(false);

        List<Category> categories = CategoryService.getAll();
        List<Config> configsWithoutCategories = ConfigService.getAllWithoutCategory();
        if (configsWithoutCategories.size() > 0) {
            Category noCatCat = new Category("Ohne Kategorie");
            noCatCat.getConfigs().addAll(configsWithoutCategories);
            categories.add(noCatCat);
        }

        List<Config> allConfigs = ConfigService.getAll();
        Category noCatCat = new Category("Fehlerhafte Kategorie");
        for (Config conf : allConfigs) {
            if (conf.getCategory() != null && !conf.getCategory().getConfigs().contains(conf)) {
                noCatCat.getConfigs().add(conf);
            }
        }

        if (noCatCat.getConfigs().size() > 0) {
            categories.add(noCatCat);
        }

        for (Category cat : categories) {
            final TreeItem<Object> catNode = new TreeItem<>(cat);
            catNode.setGraphic(new ImageView(folderImage));
            catNode.setExpanded(expandedCategoryNames.contains(cat.getName()));

            for (Config config : cat.getConfigs()) {
                catNode.getChildren().add(new TreeItem<>(config, new ImageView(configImage)));
            }

            //Adding tree items to the root
            root.getChildren().add(catNode);
        }

        //Creating a column
        TreeTableColumn<String, String> column = new TreeTableColumn<>("Column");
        column.setPrefWidth(150);

        //Defining cell content
        treetableName.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        treetableName.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        treetableHostname.setCellValueFactory(new TreeItemPropertyValueFactory<>("host"));
//        springBootConfigColumn.setCellFactory(new FormattedTreeTableCellFactory("springBootConfig"));
//        databaseConfigColumn.setCellFactory(new FormattedTreeTableCellFactory("databaseConfig"));
//        fileSyncConfigColumn.setCellFactory(new FormattedTreeTableCellFactory("fileSyncConfig"));
//        logFileConfigColumn.setCellFactory(new FormattedTreeTableCellFactory("logFileConfig"));

        treeTableView.getSortOrder().add(treetableName);
        Context.getEditConfigCtrl().setCategoriesInChoiceBox();
    }

    public void addCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Neue Kategorie anlegen");
        dialog.setHeaderText(null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            Category newCat = new Category(name);
            CategoryService.addCategory(newCat);
            Context.getEditConfigCtrl().setCategoriesInChoiceBox();
            refresh();
        });
    }
}