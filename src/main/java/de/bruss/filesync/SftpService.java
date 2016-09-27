package de.bruss.filesync;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.jcraft.jsch.JSchException;

import de.bruss.Context;
import de.bruss.settings.Settings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SftpService {

	public static DefaultFileSystemManager fsManager = null;
	private static FileSystemOptions fsOptions = null;

	public static void init() throws IOException, JSchException {

	}

	public static DefaultFileSystemManager getFsManager() throws IOException {
		if (fsManager == null) {
			try {
				getFileSystemOptions();
			} catch (JSchException e) {
				e.printStackTrace();
			}
			fsManager = (DefaultFileSystemManager) VFS.getManager();
		}

		return fsManager;
	}

	private static FileSystemOptions getFileSystemOptions() throws IOException, JSchException {
		fsOptions = new FileSystemOptions();

		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);

		// not working :( wait for update maybe?
		// if (SshUtils.isPageantAvailable()) {
		// SftpFileSystemConfigBuilder.getInstance().setIdentityRepositoryFactory(fsOptions, new IdentityRepositoryFactory() {
		// @Override
		// public IdentityRepository create(JSch jsch) {
		// Connector con = null;
		//
		// try {
		// ConnectorFactory cf = ConnectorFactory.getDefault();
		// con = cf.createConnector();
		//
		// } catch (AgentProxyException e) {
		// System.err.println("Auth failed :(");
		// }
		//
		// if (con != null && con.isAvailable()) {
		// IdentityRepository irepo = new RemoteIdentityRepository(con);
		// if (irepo.getIdentities().size() > 0) {
		// return irepo;
		// }
		// }
		//
		// System.err.println("Identities could not be added");
		// return null;
		// }
		// });
		// }

		if (StringUtils.isBlank(Settings.getInstance().getPassword()) || StringUtils.isBlank(Settings.getInstance().getProperty("username"))
				|| StringUtils.isBlank(Settings.getInstance().getProperty("sshPath"))) {

			FXMLLoader loader = new FXMLLoader(Context.getMainSceneCtrl().getClass().getResource("/scenes/SSH_Dialog.fxml"));

			Stage stage = new Stage();
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setTitle("SSH Zugangsdaten");
			stage.setScene(new Scene((Pane) loader.load()));

			stage.showAndWait();
		}

		SftpFileSystemConfigBuilder.getInstance().setIdentityInfo(fsOptions,
				new IdentityInfo(new File(Settings.getInstance().getProperty("sshPath")), Settings.getInstance().getPassword().getBytes()));

		return fsOptions;
	}

	public static Object getFsOptions() {
		return fsOptions;
	}

	public static FileObject resolveFile(String uri) throws IOException {
		return getFsManager().resolveFile(uri, fsOptions);
	}

}
