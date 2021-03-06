package de.bruss.filesync;

import de.bruss.Context;
import de.bruss.deployment.Config;
import de.bruss.settings.Settings;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.Util;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FileSyncService implements Runnable {

	public int checkFileCount = 0;
	public int checkFolderCount = 0;
	public int updateFileCount = 0;
	public int createdFileCount = 0;
	public int createdFolderCount = 0;
	public long downloadSizeCount = 0;
	public int localFilesDeleted = 0;
	public int localFoldersDeleted = 0;
	public FileObject currentfile;

	private String host;
	private List<FileSyncContainer> fileSyncList;
	private FileSyncController fileSyncController;

	Thread t;

	private final Logger logger = LoggerFactory.getLogger(FileSyncService.class);

	public FileSyncService(final Config config, final FileSyncController fileSyncController) {
		this.host = config.getHost();
		this.fileSyncList = config.getFileSyncList();
		this.fileSyncController = fileSyncController;
	}

	@Override
	public void run() {
		checkFileCount = 0;
		checkFolderCount = 0;
		updateFileCount = 0;
		createdFileCount = 0;
		createdFolderCount = 0;
		downloadSizeCount = 0;
		localFilesDeleted = 0;
		localFoldersDeleted = 0;

		try {
			for (FileSyncContainer container : fileSyncList) {
				String uri = "sftp://" + Settings.getInstance().getProperty("username") + "@" + host + container.getRemoteFilePath();

				FileObject fo = SftpService.resolveFile(uri);
				String localFilePath = container.getLocalFilePath();
				File localFolder = new File(localFilePath.substring(0, localFilePath.length() - 1));
				if (!localFolder.exists()) {
					Files.createDirectories(localFolder.toPath());
				}

				try {
					syncFiles(fo, localFilePath, true);
				} catch (InterruptedException e) {
					logger.info("interrupted");
					fo.close();
					break;
				}
				fo.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info("Folders checked: " + checkFolderCount);
		logger.info("Folders created: " + createdFolderCount);
		logger.info("Files checked: " + checkFileCount);
		logger.info("Files updated: " + updateFileCount);
		logger.info("Files created: " + createdFileCount);
		logger.info("Files deleted locally: " + localFilesDeleted);
		logger.info("Folders deleted locally: " + localFoldersDeleted);
		logger.info("Downloaded total: " + FileUtils.byteCountToDisplaySize(downloadSizeCount));
		Platform.runLater(() -> {
			fileSyncController.setFinished();
		});

	}

	private void syncFiles(FileObject file, String localPath, boolean initial) throws FileSystemException, IOException, InterruptedException {

		String fileSize;
		try {
			fileSize = FileUtils.byteCountToDisplaySize(file.getContent().getSize());
		} catch (FileSystemException e) {
			fileSize = "?";
		}

		String finalFileSize = fileSize;

		Platform.runLater(() -> {
			fileSyncController.setFoldersChecked(String.valueOf(checkFolderCount));
			fileSyncController.setFoldersCreated(String.valueOf(createdFolderCount));
			fileSyncController.setFilesChecked(String.valueOf(checkFileCount));
			fileSyncController.setFilesUpdated(String.valueOf(updateFileCount));
			fileSyncController.setFilesCreated(String.valueOf(createdFileCount));
			fileSyncController.setFilesDeleted(String.valueOf(localFilesDeleted));
			fileSyncController.setFoldersDeleted(String.valueOf(localFoldersDeleted));
			fileSyncController.setTotalDowloadSize(FileUtils.byteCountToDisplaySize(downloadSizeCount));
			fileSyncController.setCurrentFile(file.getName().getPath());
			fileSyncController.setCurrentSize(finalFileSize);
		});

		currentfile = file;

		String foldername = "/" + file.getName().getBaseName();
		File newFile = new File(localPath + foldername);

		if (file.getType() == FileType.FOLDER) {
			checkFolderCount++;

			if (initial) {
				foldername = "";
			}

			if (!initial && !newFile.exists()) {
				createdFolderCount++;
				newFile.mkdir();
			}

			List<String> remoteFiles = new ArrayList<String>();
			for (FileObject child : file.getChildren()) {
				syncFiles(child, localPath + foldername, false);
				remoteFiles.add(child.getName().getBaseName());
			}

			for (File localFile : new File(localPath + foldername).listFiles()) {
				if (!remoteFiles.contains(localFile.getName())) {
					if (localFile.isDirectory()) {
						FileUtils.deleteDirectory(new File("directory"));
						localFoldersDeleted++;
					} else {
						Files.delete(localFile.toPath());
						localFilesDeleted++;
					}

				}
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

				Context.getProgressBar().setProgress(0);

				copy(file, newFile, new CopyStreamListener() {
					double percentage = 0;

					@Override
					public void bytesTransferred(CopyStreamEvent event) {
						bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
					}

					@Override
					public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
						percentage = Double.valueOf(totalBytesTransferred) / Double.valueOf(streamSize);
						fileSyncController.setSyncProgress(percentage);
					}
				});

				// FileUtils.copyInputStreamToFile(file.getContent().getInputStream(), newFile);
				newFile.setLastModified(file.getContent().getLastModifiedTime());
			}

		}
		file.close();

	}

	private void copy(FileObject sourceFile, File newFile, CopyStreamListener progressMonitor) throws IOException {
		InputStream sourceFileIn = sourceFile.getContent().getInputStream();
		try {
			OutputStream destinationFileOut = new FileOutputStream(newFile);
			try {
				Util.copyStream(sourceFileIn, destinationFileOut, Util.DEFAULT_COPY_BUFFER_SIZE, sourceFile.getContent().getSize(), progressMonitor);
			} finally {
				destinationFileOut.close();
			}
		} finally {
			sourceFileIn.close();
		}
	}

	class UpdateGuiThread implements Runnable {

		@Override
		public void run() {
			Timeline timeline = new Timeline(new KeyFrame(Duration.millis(1000), ae -> {
				logger.info("update " + LocalDateTime.now() + " " + Thread.currentThread().isInterrupted() + " files: " + checkFileCount);
				Platform.runLater(() -> {

					fileSyncController.setFoldersChecked(String.valueOf(checkFolderCount));
					fileSyncController.setFoldersCreated(String.valueOf(createdFolderCount));
					fileSyncController.setFilesChecked(String.valueOf(checkFileCount));
					fileSyncController.setFilesUpdated(String.valueOf(updateFileCount));
					fileSyncController.setFilesCreated(String.valueOf(createdFileCount));
					fileSyncController.setFilesDeleted(String.valueOf(localFilesDeleted));
					fileSyncController.setFoldersDeleted(String.valueOf(localFoldersDeleted));
					fileSyncController.setTotalDowloadSize(FileUtils.byteCountToDisplaySize(downloadSizeCount));
					fileSyncController.setCurrentFile(currentfile.getName().getPath());
					// try {
					// fileSyncController.setCurrentSize(FileUtils.byteCountToDisplaySize(currentfile.getContent().getSize()));
					// } catch (FileSystemException e) {
					// fileSyncController.setCurrentSize("?");
					// }
				});
			}));
			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();
		}

	}
}
