package de.bruss.filesync;

import java.io.File;
import java.io.IOException;

import javafx.application.Platform;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.settings.Settings;

public class FileSyncService implements Runnable {

	public int checkFileCount = 0;
	public int checkFolderCount = 0;
	public int updateFileCount = 0;
	public int createdFileCount = 0;
	public int createdFolderCount = 0;
	public long downloadSizeCount = 0;

	private String host;
	private String remoteFilePath;
	private String localFilePath;

	public static DefaultFileSystemManager fsManager = null;

	public FileSyncService(final Config config) throws IOException {
		this.host = config.getHost();
		this.localFilePath = config.getLocalFilePath().substring(0, config.getLocalFilePath().length() - 1);
		this.remoteFilePath = config.getRemoteFilePath();
	}

	@Override
	public void run() {
		Context.getFileCounterBox().setVisible(true);

		checkFileCount = 0;
		checkFolderCount = 0;
		updateFileCount = 0;
		createdFileCount = 0;
		createdFolderCount = 0;
		downloadSizeCount = 0;
		FileSystemOptions fsOptions = new FileSystemOptions();

		try {
			SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
			File ppk = new File(Settings.getInstance().getProperty("sshPath"));
			SftpFileSystemConfigBuilder.getInstance().setIdentities(fsOptions, new File[] { ppk });

			SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
			SftpFileSystemConfigBuilder.getInstance().setUserInfo(fsOptions, new MyUserInfo(Settings.getInstance().getProperty("password")));

			if (fsManager == null) {
				fsManager = (DefaultFileSystemManager) VFS.getManager();
			}

			String uri = "sftp://" + Settings.getInstance().getProperty("username") + "@" + host + remoteFilePath;

			FileObject fo = fsManager.resolveFile(uri, fsOptions);

			File localFolder = new File(localFilePath);
			if (!localFolder.exists()) {
				localFolder.mkdir();
			}

			syncFiles(fo, localFilePath);

			fo.close();
		} catch (FileSystemException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Folders checked: " + checkFolderCount);
		System.out.println("Folders created: " + createdFolderCount);
		System.out.println("Files checked: " + checkFileCount);
		System.out.println("Files updated: " + updateFileCount);
		System.out.println("Files created: " + createdFileCount);
		System.out.println("Downloaded total: " + FileUtils.byteCountToDisplaySize(downloadSizeCount));
		Context.getFileCounterBox().setVisible(false);
	}

	private void syncFiles(FileObject file, String localPath) throws FileSystemException, IOException {
		final int count = checkFileCount + checkFolderCount;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				Context.getFileCounter().setText("" + count);
			}

		});

		File newFile = new File(localPath + "/" + file.getName().getBaseName());

		if (file.getType() == FileType.FOLDER) {
			checkFolderCount++;
			if (!newFile.exists()) {
				createdFolderCount++;
				newFile.mkdir();
			}
			for (FileObject child : file.getChildren()) {
				syncFiles(child, localPath + "/" + file.getName().getBaseName());
			}
		}

		if (file.getType() == FileType.FILE) {
			checkFileCount++;
			if (newFile.lastModified() != file.getContent().getLastModifiedTime()) {
				if (!newFile.exists()) {
					createdFileCount++;
				} else {
					updateFileCount++;
				}

				downloadSizeCount += file.getContent().getSize();
				FileUtils.copyInputStreamToFile(file.getContent().getInputStream(), newFile);
				newFile.setLastModified(file.getContent().getLastModifiedTime());
			}

		}
		file.close();
	}
}
