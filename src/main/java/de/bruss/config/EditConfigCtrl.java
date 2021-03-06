package de.bruss.config;

import de.bruss.Context;
import de.bruss.deployment.Category;
import de.bruss.deployment.Config;
import de.bruss.deployment.DeploymentUtils;
import de.bruss.filesync.FileSyncContainer;
import de.bruss.logger.LogFileFinder;
import de.bruss.remoteDatabase.RemoteDatabaseUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jdo.annotations.Transactional;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EditConfigCtrl implements Initializable {

    @FXML
    private TextField host;
    @FXML
    private TextField name;
    @FXML
    private ChoiceBox<Category> categorySelector;
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
    private TextField dbUsername;
    @FXML
    private TextField dbPassword;
    @FXML
    private TextField ip;
    @FXML
    private TextField javaPath;
    @FXML
    private TextField jvmOptions;
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
    private CheckBox apacheConfig;
    @FXML
    private CheckBox applicationConfig;
    @FXML
    private CheckBox serviceConfig;
    @FXML
    private CheckBox logFileConfig;

    @FXML
    private VBox springBootConfigGrid;
    @FXML
    private GridPane databaseConfigGrid;
    @FXML
    private VBox fileSyncConfigVBox;
    @FXML
    private GridPane apacheConfigGrid;
    @FXML
    private GridPane applicationConfigGrid;
    @FXML
    private GridPane serviceConfigGrid;
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

    @FXML
    private Button addCategoryBtn;

    private final Logger logger = LoggerFactory.getLogger(EditConfigCtrl.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Context.setEditConfigCtrl(this);
        Context.setProgressBar(progressBar);
        Context.setFileCounterBox(fileCounterBox);
        Context.setFileCounter(fileCounter);

        setCategoriesInChoiceBox();

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

        localPathCol.setCellValueFactory(new PropertyValueFactory<>("localFilePath"));
        localPathCol.setCellFactory(cellFactory);
        localPathCol.setOnEditCommit(new EventHandler<CellEditEvent<FileSyncContainer, String>>() {
            @Override
            public void handle(CellEditEvent<FileSyncContainer, String> t) {
                t.getRowValue().setLocalFilePath(t.getNewValue());
            }
        });

        remotePathCol.setCellValueFactory(new PropertyValueFactory<>("remoteFilePath"));
        remotePathCol.setCellFactory(cellFactory);
        remotePathCol.setOnEditCommit(new EventHandler<CellEditEvent<FileSyncContainer, String>>() {
            @Override
            public void handle(CellEditEvent<FileSyncContainer, String> t) {
                t.getRowValue().setRemoteFilePath(t.getNewValue());
            }
        });
        remotePathCol.setSortType(TableColumn.SortType.ASCENDING);

        actionColumn.setCellFactory(new DeleteFileSyncContainerCellFactory<>());

        this.editConfigVBox.setVisible(false);

        addCategoryBtn.setOnMouseClicked(event -> Context.getConfigTableCtrl().addCategory());
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

    @FXML
    protected void searchLogFile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/LogFileFinder.fxml"));

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Log-Files gefunden auf Server: " + Context.getEditConfigCtrl().getEditConfig().getHost());
        stage.setScene(new Scene((Pane) loader.load()));

        final LogFileFinder controller = (LogFileFinder) loader.getController();
        controller.setLogFilePathField(logFilePath);

        stage.show();
    }

    public void setConfigInView() {
        this.categorySelector.setValue(null);

        this.host.setText(editConfig.getHost());
        this.localPath.setText(editConfig.getLocalPath());
        this.remotePath.setText(editConfig.getRemotePath());
        this.name.setText(editConfig.getName());
        this.serviceName.setText(editConfig.getServiceName());
        this.port.setText(editConfig.getPort());
        this.localDbName.setText(editConfig.getLocalDbName());
        this.remoteDbName.setText(editConfig.getRemoteDbName());
        this.dbUsername.setText(editConfig.getDbUsername());
        this.dbPassword.setText(editConfig.getDbPassword());
        this.springBootConfig.setSelected(editConfig.isSpringBootConfig());
        this.databaseConfig.setSelected(editConfig.isDatabaseConfig());
        this.fileSyncConfig.setSelected(editConfig.isFileSyncConfig());
        this.logFileConfig.setSelected(editConfig.isLogFileConfig());
        this.logFilePath.setText(editConfig.getLogFilePath());
        this.apacheConfig.setSelected(editConfig.isApacheConfig());
        this.applicationConfig.setSelected(editConfig.isApplicationConfig());
        this.serviceConfig.setSelected(editConfig.isServiceConfig());
        this.serverName.setText(editConfig.getServerName());
        this.ip.setText(editConfig.getIP());
        this.javaPath.setText(editConfig.getJavaPath());
        this.jvmOptions.setText(editConfig.getJvmOptions());
        this.categorySelector.getSelectionModel().select(editConfig.getCategory());

        fileSyncTable.getItems().clear();
        fileSyncTable.getItems().addAll(editConfig.getFileSyncList());
        fileSyncTable.refresh();

        setVisibility(VisibilityGroup.SPRING_BOOT, editConfig.isSpringBootConfig());
        setVisibility(VisibilityGroup.DABATASE, editConfig.isDatabaseConfig());
        setVisibility(VisibilityGroup.FILE_SYNC, editConfig.isFileSyncConfig());
        setVisibility(VisibilityGroup.EDIT_ONLY, editConfig.getId() != null);
        setVisibility(VisibilityGroup.APACHE_CONFIG, editConfig.isApacheConfig());
        setVisibility(VisibilityGroup.APPLICATION_CONFIG, editConfig.isApplicationConfig());
        setVisibility(VisibilityGroup.SERVICE_CONFIG, editConfig.isServiceConfig());
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
    protected void toggleApacheConfig(ActionEvent event) {
        editConfig.setApacheConfig(!editConfig.isApacheConfig());
        setVisibility(VisibilityGroup.APACHE_CONFIG, editConfig.isApacheConfig());
    }

    @FXML
    protected void toggleApplicationConfig(ActionEvent event) {
        editConfig.setApplicationConfig(!editConfig.isApplicationConfig());
        setVisibility(VisibilityGroup.APPLICATION_CONFIG, editConfig.isApplicationConfig());
    }

    @FXML
    protected void toggleServiceConfig(ActionEvent event) {
        editConfig.setServiceConfig(!editConfig.isServiceConfig());
        setVisibility(VisibilityGroup.SERVICE_CONFIG, editConfig.isServiceConfig());
    }

    @FXML
    protected void toggleLogFileConfig(ActionEvent event) {
        editConfig.setLogFileConfig(!editConfig.isLogFileConfig());
        setVisibility(VisibilityGroup.LOGFILE, editConfig.isLogFileConfig());
    }

    enum VisibilityGroup {
        SPRING_BOOT, DABATASE, FILE_SYNC, EDIT_ONLY, APACHE_CONFIG, APPLICATION_CONFIG, SERVICE_CONFIG, LOGFILE
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
            case APACHE_CONFIG:
                pane = apacheConfigGrid;
                break;
            case APPLICATION_CONFIG:
                pane = applicationConfigGrid;
                break;
            case SERVICE_CONFIG:
                pane = serviceConfigGrid;
                break;
            case LOGFILE:
                if (Context.getMainSceneCtrl() != null) {
                    Context.getMainSceneCtrl().toggleLogFileButtons(visible && editConfig.getId() != null);
                }
                pane = logFileGrid;
                break;
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

    @Transactional
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
                    dbUsername.getText(),
                    dbPassword.getText(),
                    springBootConfig.isSelected(),
                    databaseConfig.isSelected(),
                    fileSyncConfig.isSelected(),
                    ip.getText(),
                    serverName.getText(),
                    fileSyncTable.getItems(),
                    logFilePath.getText(),
                    javaPath.getText(),
                    jvmOptions.getText(),
                    apacheConfig.isSelected(),
                    applicationConfig.isSelected(),
                    serviceConfig.isSelected(),
                    categorySelector.getSelectionModel().getSelectedItem());
            // @formatter:on
            ConfigService.addConfig(config);
            editConfig = config;

            Category cat = editConfig.getCategory();
            cat.getConfigs().add(editConfig);
            CategoryService.save(cat);
        } else {
            editConfig.setHost(this.host.getText());
            editConfig.setLocalPath(this.localPath.getText());
            editConfig.setRemotePath(this.remotePath.getText());
            editConfig.setName(this.name.getText());
            editConfig.setServiceName(this.serviceName.getText());
            editConfig.setPort(this.port.getText());
            editConfig.setLocalDbName(this.localDbName.getText());
            editConfig.setRemoteDbName(this.remoteDbName.getText());
            editConfig.setDbUsername(this.dbUsername.getText());
            editConfig.setDbPassword(this.dbPassword.getText());
            editConfig.setSpringBootConfig(this.springBootConfig.isSelected());
            editConfig.setDatabaseConfig(this.databaseConfig.isSelected());
            editConfig.setFileSyncConfig(this.fileSyncConfig.isSelected());
            editConfig.setIP(this.ip.getText());
            editConfig.setServerName(this.serverName.getText());
            editConfig.setFileSyncList(this.fileSyncTable.getItems());
            editConfig.setLogFilePath(this.logFilePath.getText());
            editConfig.setJavaPath(this.javaPath.getText());
            editConfig.setJvmOptions(this.jvmOptions.getText());
            editConfig.setApacheConfig(this.apacheConfig.isSelected());
            editConfig.setApplicationConfig(this.applicationConfig.isSelected());
            editConfig.setServiceConfig(this.serviceConfig.isSelected());

            Category oldCat = editConfig.getCategory() != null ? CategoryService.getCategory(editConfig.getCategory().getId()) : null;
            Category newCat = this.categorySelector.getSelectionModel().getSelectedItem() != null ? CategoryService.getCategory(this.categorySelector.getSelectionModel().getSelectedItem().getId()) : null;

            if (oldCat != null && oldCat.getId() != null && oldCat.getConfigs().contains(editConfig)) {
                oldCat.getConfigs().remove(editConfig);
                CategoryService.save(oldCat);
            }

            if (newCat != null && newCat.getId() != null) {
                newCat.getConfigs().add(editConfig);
                CategoryService.save(newCat);
                editConfig.setCategory(newCat);
            } else {
                editConfig.setCategory(null);
            }

            ConfigService.save(editConfig);
        }

        initData(editConfig);
        Context.getConfigTableCtrl().refresh();
    }

    @Transactional
    public void duplicate() throws IllegalAccessException, InvocationTargetException {
        Config newConfig = new Config();
        BeanUtils.copyProperties(newConfig, editConfig);
        newConfig.setId(null);
        newConfig.setName(newConfig.getName() + "_Kopie");
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
            logger.info("Can't deploy without remotePath!");
        }
    }

    @Transactional
    public void delete() {
        ConfigService.remove(editConfig);
        initData(new Config());
        editConfigVBox.setVisible(false);
        Context.getConfigTableCtrl().refresh();
    }

    @Transactional
    public void addConfig() {
        initData(new Config());
    }

    @Transactional
    public Config getEditConfig() {
        return editConfig;
    }

    public void setCategoriesInChoiceBox() {
        this.categorySelector.getItems().clear();
        List<Category> categories = CategoryService.getAll();
        ObservableList<Category> list = FXCollections.observableArrayList(categories);
        this.categorySelector.setItems(list);
    }

}