package de.bruss.settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

public class Settings {

	private Properties configProp = new Properties();

	// not permanently saved!
	private String password;

	public final static Path appDataPath = Paths.get(System.getenv("APPDATA") + "\\OneToolSuite");
	private final static Path configFilePath = Paths.get(System.getenv("APPDATA") + "\\OneToolSuite\\config.properties");

	private final Logger logger = LoggerFactory.getLogger(Settings.class);

	private Settings() {
		init();
	}

	private void init() {
		InputStream in;
		try {
			in = new FileInputStream(configFilePath.toString());
		} catch (FileNotFoundException e1) {
			return;
		}

		if (in != null) {
			try {
				configProp.load(in);

				// remove if present from previous versions
				configProp.remove("password");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Bill Pugh Solution for singleton pattern
	private static class LazyHolder {
		private static final Settings INSTANCE = new Settings();
	}

	public static Settings getInstance() {
		return LazyHolder.INSTANCE;
	}

	public String getProperty(String key) {
		return configProp.getProperty(key);
	}

	public Set<String> getAllPropertyNames() {
		return configProp.stringPropertyNames();
	}

	public boolean containsKey(String key) {
		return configProp.containsKey(key);
	}

	public void create(String username, String sshPath, String postgresPath) {
		logger.info("Saving Settings...");
		try {
			configProp.setProperty("username", username);
			configProp.setProperty("sshPath", sshPath);
			configProp.setProperty("postgresPath", postgresPath);

			File file = configFilePath.toFile();
			FileOutputStream fileOut = new FileOutputStream(file);
			configProp.store(fileOut, "Configurations - don't share!!");
			fileOut.close();
			init();
			logger.info("-- [done]");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isEmpty() {
		return !Files.exists(configFilePath);
	}

	public void setProperty(String key, String value) throws IOException {
		configProp.setProperty(key, value);
		File file = configFilePath.toFile();
		FileOutputStream fileOut = new FileOutputStream(file);
		configProp.store(fileOut, "Configurations - don't share!!");
		fileOut.close();
		init();
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}