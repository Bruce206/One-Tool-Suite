package de.bruss.ssh;

import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import de.bruss.Context;
import de.bruss.settings.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class SshUtils {
    private static final Logger logger = LoggerFactory.getLogger(SshUtils.class);

	public static Session getSession(String host) {
		try {
			JSch jsch = new JSch();

			authenticateJschWithPageant(jsch);

			//@formatter:off
			if (jsch.getIdentityNames().size() == 0) {
				if (StringUtils.isBlank(Settings.getInstance().getPassword())
						|| StringUtils.isBlank(Settings.getInstance().getProperty("username")) 
						|| StringUtils.isBlank(Settings.getInstance().getProperty("sshPath"))) { //@formatter:on

					FXMLLoader loader = new FXMLLoader(Context.getMainSceneCtrl().getClass().getResource("/scenes/SSH_Dialog.fxml"));

					Stage stage = new Stage();
					stage.initModality(Modality.WINDOW_MODAL);
					stage.setTitle("SSH Zugangsdaten");
					stage.setScene(new Scene((Pane) loader.load()));

					stage.showAndWait();
				}

				String privateKey = Settings.getInstance().getProperty("sshPath");
				jsch.addIdentity(privateKey, Settings.getInstance().getPassword());
			}

			Session session = jsch.getSession(Settings.getInstance().getProperty("username"), host, 22);

			java.util.Properties props = new java.util.Properties();
			props.put("StrictHostKeyChecking", "no");
			session.setConfig(props);

			return session;

		} catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}

	public static JSch authenticateJschWithPageant(JSch jsch) {
		Connector con = null;

		try {
			ConnectorFactory cf = ConnectorFactory.getDefault();
			con = cf.createConnector();

		} catch (AgentProxyException e) {
			System.err.println("Auth failed :(");
		}

		if (con != null && con.isAvailable()) {
			IdentityRepository irepo = new RemoteIdentityRepository(con);
			if (irepo.getIdentities().size() > 0) {
				jsch.setIdentityRepository(irepo);
				return jsch;
			}
		}

		System.err.println("Identities could not be added");
		return null;
	}
	
	public static boolean isPageantAvailable () throws JSchException {
		JSch jsch = new JSch();
		authenticateJschWithPageant(jsch);
		
		return jsch.getIdentityNames().size() > 0;
		
	}

	public static ChannelSftp getSftpChannel(Session session) throws JSchException {
		Channel channel = session.openChannel("sftp");
		channel.setInputStream(System.in);
		channel.setOutputStream(System.out);
		channel.connect();
		return (ChannelSftp) channel;
	}

	public static ChannelExec getExecChannel(Session session) throws JSchException {
		Channel channel;
		channel = session.openChannel("exec");
		return (ChannelExec) channel;
	}

	public static ChannelShell getShellChannel(Session session) throws JSchException {
		Channel channel;
		channel = session.openChannel("shell");
		return (ChannelShell) channel;
	}

	public static void copyFile(String from, String to, Session session, ProgressBar progressBar) throws JSchException, SftpException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		sftpChannel.put(from, to, new SftpProgressMonitor() {

			private double bytes;
			private double max;

			@Override
			public void init(int op, String src, String dest, long max) {
				this.max = max;
				logger.info("-- Starting upload... FileSize: " + FileUtils.byteCountToDisplaySize(max));
				progressBar.setVisible(true);
			}

			@Override
			public void end() {
				logger.info("-- Finished uploading!");
				progressBar.setVisible(false);
			}

			@Override
			public boolean count(long bytes) {
				this.bytes += bytes;
				progressBar.setProgress(this.bytes / max);
				return true;
			}
		});

		sftpChannel.exit();
		sftpChannel.disconnect();
	}

	public static void downloadFile(String from, String to, Session session, ProgressBar progressBar) throws JSchException, SftpException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		sftpChannel.get(from, to, new SftpProgressMonitor() {

			private double bytes;
			private double max;

			@Override
			public void init(int op, String src, String dest, long max) {
				this.max = max;
				System.out.print("Starting Download (FileSize: " + FileUtils.byteCountToDisplaySize(max) + ") ...");
				progressBar.setVisible(true);
			}

			@Override
			public void end() {
				logger.info(" -> [done]!");
				progressBar.setVisible(false);
			}

			@Override
			public boolean count(long bytes) {
				this.bytes += bytes;
				progressBar.setProgress(this.bytes / max);
				return true;
			}
		});

		sftpChannel.exit();
		sftpChannel.disconnect();
	}

	public static void createRemotePath(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);
		try {
			for (String folder : path.split("/")) {

				if (StringUtils.isNotBlank(folder)) {
					try {
						sftpChannel.mkdir(folder);
						logger.info("Created folder: " + folder);
					} catch (SftpException sftpe) {
						continue;
					} finally {
						sftpChannel.cd(folder);
					}
				} else {
					sftpChannel.cd("/");
				}

			}
		} catch (SftpException e) {
			logger.info("Folder creation failed!");
			e.printStackTrace();
		}

	}

	public static boolean fileExistsOnServer(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);

		try {
			sftpChannel.lstat(path);
		} catch (SftpException se) {
			logger.info("File " + path + " not found on server!");
			return false;
		}

		sftpChannel.exit();
		sftpChannel.disconnect();
		return true;
	}

	public static String sendCommand(Session session, String command) {

		try {
			StringBuilder outputBuffer = new StringBuilder();

			ChannelExec execChannel = SshUtils.getExecChannel(session);
			execChannel.setCommand(command);
			execChannel.connect();

			InputStream commandOutput = execChannel.getInputStream();
			int readByte = commandOutput.read();

			while (readByte != 0xffffffff) {
				outputBuffer.append((char) readByte);
				readByte = commandOutput.read();
			}

			execChannel.disconnect();

			return StringUtils.chomp(outputBuffer.toString());
		} catch (JSchException | IOException e) {
			logger.info("Failed to connect to Server while sending Command: " + command);
			e.printStackTrace();
		}
		return null;
	}

	public static void removeFile(Session session, String path) throws JSchException {
		ChannelSftp sftpChannel = getSftpChannel(session);

		try {
			sftpChannel.rm(path);
		} catch (SftpException se) {
			logger.info("File " + path + " not found on server!");
		}

		sftpChannel.exit();
		sftpChannel.disconnect();
	}
}
