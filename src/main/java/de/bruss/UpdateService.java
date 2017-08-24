package de.bruss;

import de.bruss.settings.Settings;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

public class UpdateService implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(UpdateService.class);

	@Override
	public void run() {
		logger.info("Suche nach neuer Version...");
		String version = UpdateService.class.getPackage().getImplementationVersion();

		String urlString = "http://apps.bruce-io.de/check/OneToolSuite/" + version;
		URL url;
		try {
			url = new URL(urlString);
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();

			Scanner scanner = new Scanner(is);
			scanner.useDelimiter("\\A");
			String responseString = scanner.hasNext() ? scanner.next() : "";
			scanner.close();

			Boolean updateRequired = Boolean.parseBoolean(responseString);
			// Boolean updateRequired = true;

			if (updateRequired) {
				logger.info("Neue Version gefunden! Starte Download...");
				Settings.getInstance().setProperty("showChangelog", "true");
				File tempDir = File.createTempFile("update_OneToolSuite", Long.toString(System.nanoTime()));
				// file only needed to create path
				tempDir.delete();
				// create die now
				tempDir.mkdir();

				// get new jar
				URL newVersionUrl = new URL("http://apps.bruce-io.de/download/OneToolSuite");
				ReadableByteChannel rbc = Channels.newChannel(newVersionUrl.openStream());
				FileOutputStream fos = new FileOutputStream(tempDir.getAbsolutePath().concat("/newversion.jar"));
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();

				// get updater
				URL updaterUrl = new URL("http://apps.bruce-io.de/updater/OneToolSuite");
				File updater = new File(tempDir.getAbsolutePath().concat("/updater.jar"));
				FileOutputStream fos2 = new FileOutputStream(updater);
				fos2.getChannel().transferFrom(Channels.newChannel(updaterUrl.openStream()), 0, Long.MAX_VALUE);
				fos2.close();

				File thisJar = new File(System.getProperty("java.class.path"));
				String path = thisJar.getAbsolutePath();
				logger.info(path);

				ProcessBuilder pb = new ProcessBuilder("java", "-jar", updater.getAbsolutePath(), path);

				logger.info("Update heruntergeladen! Starte Neu!");

				pb.start();
				Platform.exit();
			} else {
				logger.info("Keine neuere Version verfügbar!");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
