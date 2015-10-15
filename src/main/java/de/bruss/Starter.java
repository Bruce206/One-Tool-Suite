package de.bruss;

import java.io.IOException;
import java.nio.file.Files;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.bruss.settings.Settings;

public class Starter extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws IOException {

		System.setProperty("objectdb.home", Settings.appDataPath.toString());
		System.setProperty("objectdb.conf", getClass().getResource("/objectdb.conf").getPath());

		if (Files.exists(Settings.appDataPath)) {
			System.out.println("Config-Folder exists");
		} else {
			Files.createDirectory(Settings.appDataPath);
		}

		Context.setPrimaryStage(primaryStage);
		String version = Starter.class.getPackage().getImplementationVersion();
		primaryStage.setTitle("One Tool Suite - Version: " + version);

		primaryStage.getIcons().add(new Image("/images/icon.png"));

		if (Settings.isEmpty()) {
			Parent root = FXMLLoader.load(getClass().getResource("/scenes/SettingsScene.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
		} else {
			Parent root = FXMLLoader.load(getClass().getResource("/scenes/MainScene.fxml"));
			Scene scene = new Scene(root);
			String css = Starter.class.getResource("/style/style.css").toExternalForm();
			scene.getStylesheets().clear();
			scene.getStylesheets().add(css);
			primaryStage.setScene(scene);

		}

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}
		});

		primaryStage.show();
	}

}
