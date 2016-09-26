package de.bruss.filesync;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.IdentityRepositoryFactory;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import de.bruss.Context;
import de.bruss.settings.Settings;
import de.bruss.ssh.SshUtils;

public class SftpService {

	public static DefaultFileSystemManager fsManager = null;
	private static FileSystemOptions fsOptions = null;

	public static void init() throws IOException {
		getFsManager();
	}

	private static DefaultFileSystemManager getFsManager() throws IOException {
		if (fsManager == null) {
			getFileSystemOptions();
			fsManager = (DefaultFileSystemManager) VFS.getManager();
		}

		return fsManager;
	}

	private static FileSystemOptions getFileSystemOptions() throws IOException {
		if (fsOptions == null) {
			fsOptions = new FileSystemOptions();

			SftpFileSystemConfigBuilder.getInstance().setIdentityRepositoryFactory(fsOptions, new IdentityRepositoryFactory() {
				@Override
				public IdentityRepository create(JSch jsch) {
					try {
						return SshUtils.authenticateJschWithPageant(jsch).getIdentityRepository();
					} catch (JSchException | IOException e) {
						return null;
					}
				}
			});

			if (SftpFileSystemConfigBuilder.getInstance().getIdentityInfo(fsOptions) == null && (StringUtils.isBlank(Settings.getInstance().getPassword()) || StringUtils.isBlank(Settings.getInstance().getProperty("username")) || StringUtils.isBlank(Settings.getInstance().getProperty("sshPath")))) { // @formatter:on

				FXMLLoader loader = new FXMLLoader(Context.getMainSceneCtrl().getClass().getResource("/scenes/SSH_Dialog.fxml"));

				Stage stage = new Stage();
				stage.initModality(Modality.WINDOW_MODAL);
				stage.setTitle("SSH Zugangsdaten");
				stage.setScene(new Scene((Pane) loader.load()));

				stage.showAndWait();
			}

			SftpFileSystemConfigBuilder.getInstance().setIdentityInfo(fsOptions, new IdentityInfo(new File(Settings.getInstance().getProperty("sshPath")), Settings.getInstance().getPassword().getBytes()));

			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);

		}

		// File ppk = new File(Settings.getInstance().getProperty("sshPath"));
		// SftpFileSystemConfigBuilder.getInstance().setIdentities(fsOptions, new File[] { ppk });
		// SftpFileSystemConfigBuilder.getInstance().setUserInfo(fsOptions, new MyUserInfo(Settings.getInstance().getProperty("password")));
		return fsOptions;

	}

	public static Object getFsOptions() {
		return fsOptions;
	}

	public static FileObject resolveFile(String uri) throws IOException {
		return getFsManager().resolveFile(uri, fsOptions);
	}

}
