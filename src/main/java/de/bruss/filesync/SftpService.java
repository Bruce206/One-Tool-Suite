package de.bruss.filesync;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import de.bruss.settings.Settings;

public class SftpService {

	public static DefaultFileSystemManager fsManager = null;
	private static FileSystemOptions fsOptions = null;

	private static DefaultFileSystemManager getFsManager() throws FileSystemException {
		if (fsManager == null) {
			getFileSystemOptions();
			fsManager = (DefaultFileSystemManager) VFS.getManager();
		}

		return fsManager;
	}

	private static FileSystemOptions getFileSystemOptions() throws FileSystemException {
		if (fsOptions == null) {
			fsOptions = new FileSystemOptions();
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
			File ppk = new File(Settings.getInstance().getProperty("sshPath"));
			SftpFileSystemConfigBuilder.getInstance().setIdentities(fsOptions, new File[] { ppk });

			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
			SftpFileSystemConfigBuilder.getInstance().setUserInfo(fsOptions, new MyUserInfo(Settings.getInstance().getProperty("password")));
		}

		return fsOptions;

	}

	public static Object getFsOptions() {
		return fsOptions;
	}

	public static FileObject resolveFile(String uri) throws FileSystemException {
		return getFsManager().resolveFile(uri, fsOptions);
	}

}
