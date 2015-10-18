package de.bruss.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.List;

import javafx.scene.control.ProgressBar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.bruss.ssh.SshUtils;

public class DeploymentUtils implements Runnable {

	private Session session;
	private Config config;
	private ProgressBar progressBar;

	public DeploymentUtils(Config config, ProgressBar progressBar) throws JSchException {
		this.config = config;
		this.progressBar = progressBar;
		this.session = SshUtils.getSession(config.getHost());
		this.session.connect();
	}

	@Override
	public void run() {

		Preconditions.checkNotNull(config);
		Preconditions.checkNotNull(config.getHost());
		Preconditions.checkNotNull(config.getRemotePath());
		Preconditions.checkNotNull(config.getServiceName());
		Preconditions.checkNotNull(config.getLocalPath());

		if (config.getRemotePath().equals("/")) {
			throw new IllegalArgumentException("Remote Path must not be '/'");
		}

		if (config.getLocalPath().equals("\\")) {
			throw new IllegalArgumentException("Local Path must not be '\\'");
		}

		try {
			if (SshUtils.fileExistsOnServer(session, "/etc/init/" + config.getServiceName() + ".conf")) {
				// send stop for service
				System.out.println("Stopping Service on Remote...    ");
				String response = SshUtils.sendCommand(session, "stop " + config.getServiceName());
				System.out.println("-- [Done] Response:   " + response);
			} else {
				createConfigFileOnServer();
			}

			if (!SshUtils.fileExistsOnServer(session, config.getRemotePath())) {
				SshUtils.createRemotePath(session, config.getRemotePath());
			}

			if (config.isAutoconfig()) {
				if (!SshUtils.fileExistsOnServer(session, config.getRemotePath() + "/application.properties")) {
					createApplicationPropertiesOnServer();
				}

				if (!SshUtils.fileExistsOnServer(session, getRemoteApacheConfPath())) {
					createApacheConfigOnServer();
					restartApache();
				}
			} else {
				System.out.println("Überspringe Autoconfig");
			}

			// archive old .jar-File
			archiveOldJarFileOnServer();

			// rename current jar
			renameLocalJarFile();

			// upload jar
			uploadJar();

			// start service
			System.out.print("Starting Service: " + config.getServiceName() + " ");
			String response2 = SshUtils.sendCommand(session, "start " + config.getServiceName());
			System.out.println("-- [done] Response: " + response2);
		} catch (JSchException | SftpException e) {
			e.printStackTrace();
		}

	}

	private void restartApache() {
		SshUtils.sendCommand(session, "service apache2 reload");
	}

	private void createApacheConfigOnServer() {
		System.out.print("Creating Apache-Conf...  ");
		try {
			File tempConfFile = File.createTempFile("apache-conf_", "");
			InputStream apacheConfTemplate = getClass().getResourceAsStream("/apache-conf.template");

			StringWriter writer = new StringWriter();
			IOUtils.copy(apacheConfTemplate, writer);
			String apacheConfFile = writer.toString();
			apacheConfFile = apacheConfFile.replaceAll("SERVER_NAME", config.getServerName());
			apacheConfFile = apacheConfFile.replaceAll("PORT", config.getPort());
			apacheConfFile = apacheConfFile.replaceAll("IP", config.getIP());
			Files.write(tempConfFile.toPath(), apacheConfFile.getBytes());

			System.out.print("-- Copying to server... (" + tempConfFile.toPath().toString() + ")");
			SshUtils.copyFile(tempConfFile.toPath().toString(), getRemoteApacheConfPath(), session, progressBar);
			System.out.println("-- [done]");
		} catch (IOException | JSchException | SftpException e) {
			System.out.println("Error occured while creating application.properties file");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void uploadJar() throws JSchException, SftpException {
		SshUtils.copyFile(config.getLocalJarPath(), config.getRemoteJarPath(), session, progressBar);
	}

	private void archiveOldJarFileOnServer() throws JSchException {
		ChannelSftp sftpChannel = SshUtils.getSftpChannel(session);

		try {
			sftpChannel.rename(config.getRemotePath() + config.getJarName(), config.getRemotePath() + config.getJarName() + "OLD");
		} catch (SftpException se) {
			System.out.println("No jar file found on server! Skipping archiving of file!");
		}
		sftpChannel.exit();
		sftpChannel.disconnect();
	}

	private void renameLocalJarFile() {

		File dir = new File(config.getLocalPath());
		String[] extensions = new String[] { "jar" };
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);

		if (files.size() > 1) {
			throw new RuntimeException("More than one Jar File Found in folder!");
		}

		System.out.print("Renaming File on local disk..");

		try {
			FileUtils.moveFile(files.get(0), FileUtils.getFile(config.getLocalJarPath()));
		} catch (IOException ioe) {
			System.out.println("-- Renamed File already exists! Using this file!");
			return;
		}

		System.out.println("-- [done]");
	}

	private void createApplicationPropertiesOnServer() {
		System.out.print("Creating application.properties file...  ");
		try {
			File tempConfFile = File.createTempFile("application.properties_", "");
			InputStream applicationPropertiesTemplate = getClass().getResourceAsStream("/application.properties.template");

			StringWriter writer = new StringWriter();
			IOUtils.copy(applicationPropertiesTemplate, writer);
			String applicationPropertiesString = writer.toString();
			applicationPropertiesString = applicationPropertiesString.replaceAll("SERVERPORT", config.getPort());
			Files.write(tempConfFile.toPath(), applicationPropertiesString.getBytes());

			System.out.print("-- Copying to server... (" + tempConfFile.toPath().toString() + ")");
			SshUtils.copyFile(tempConfFile.toPath().toString(), config.getRemotePath() + "/application.properties", session, progressBar);
			System.out.println("-- [done]");
		} catch (IOException | JSchException | SftpException e) {
			System.out.println("Error occured while creating application.properties file");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void createConfigFileOnServer() {
		System.out.print("Creating config file...  ");
		try {
			File tempConfFile = File.createTempFile("config", ".conf");
			InputStream confTemplate = getClass().getResourceAsStream("/upstart.conf");

			StringWriter writer = new StringWriter();
			IOUtils.copy(confTemplate, writer);
			String confString = writer.toString();

			confString = confString.replaceAll("JARNAME", config.getServiceName());
			confString = confString.replaceAll("JARPATH", config.getRemotePath());

			Files.write(tempConfFile.toPath(), confString.getBytes());

			System.out.print("-- Copying config file to server... (" + tempConfFile.toPath().toString() + ")");
			SshUtils.copyFile(tempConfFile.toPath().toString(), "/etc/init/" + config.getServiceName() + ".conf", session, progressBar);
			System.out.println("-- [done]");
		} catch (IOException | JSchException | SftpException e) {
			System.out.println("Error occured while creating config file");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private String getRemoteApacheConfPath() {
		return "/etc/apache2/sites-enabled/" + "virt-" + config.getServiceName() + ".conf";
	}
}
