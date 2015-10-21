package de.bruss.filesync;

import javax.persistence.Embeddable;

import de.bruss.commons.BrussUtils;

@Embeddable
public class FileSyncContainer {
	private String remoteFilePath;
	private String localFilePath;

	public FileSyncContainer(String remoteFilePath, String localFilePath) {
		super();
		this.remoteFilePath = remoteFilePath;
		this.localFilePath = localFilePath;
	}

	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	public void setRemoteFilePath(String remoteFilePath) {
		this.remoteFilePath = BrussUtils.formatPath(remoteFilePath, true, true);
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = BrussUtils.formatPath(localFilePath, false, true);
	}
}