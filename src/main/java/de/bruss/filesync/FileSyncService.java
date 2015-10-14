package de.bruss.filesync;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class FileSyncService {

	public static int checkFileCount = 0;
	public static int checkFolderCount = 0;
	public static int updateFileCount = 0;
	public static int createdFileCount = 0;
	public static int createdFolderCount = 0;
	public static long downloadSizeCount = 0;

	public static void main(String[] args) throws IOException {
		copyRemoteFiles("apps.bruce-io.de", "root", "var/www/xibisone.xibisone.de/cms", "C:/temp/test");
	}

	public static void copyRemoteFiles(final String host, final String user, final String remotePath, final String localPath) throws IOException {
		FileSystemOptions fsOptions = new FileSystemOptions();

		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOptions, "no");
		File ppk = new File("D:/Users/Bruce/Documents/Bruss.openssh");
		System.out.println(ppk.canRead());
		SftpFileSystemConfigBuilder.getInstance().setIdentities(fsOptions, new File[] { ppk });
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(fsOptions, false);
		SftpFileSystemConfigBuilder.getInstance().setUserInfo(fsOptions, new MyUserInfo("A8mz5XB!"));

		DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();

		String uri = "sftp://" + user + "@" + host + "/" + remotePath;

		FileObject fo = fsManager.resolveFile(uri, fsOptions);

		File localFolder = new File(localPath);
		if (!localFolder.exists()) {
			localFolder.mkdir();
		}

		syncFiles(fo, localPath);

		fo.close();

		fsManager.close();

		System.out.println("Folders checked: " + checkFolderCount);
		System.out.println("Folders created: " + createdFolderCount);
		System.out.println("Files checked: " + checkFileCount);
		System.out.println("Files updated: " + updateFileCount);
		System.out.println("Files created: " + createdFileCount);
		System.out.println("Downloaded total: " + FileUtils.byteCountToDisplaySize(downloadSizeCount));

		checkFileCount = 0;
		checkFolderCount = 0;
		updateFileCount = 0;
		createdFileCount = 0;
		createdFolderCount = 0;
		downloadSizeCount = 0;
	}

	private static void syncFiles(FileObject file, String localPath) throws FileSystemException, IOException {
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
