package de.bruss.deployment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.commons.lang3.StringUtils;

@Entity
public class Config {

	@Id
	@GeneratedValue
	private Long id;

	// general
	private String host;
	private String name;

	// spring boot
	private String serviceName;
	private String port;
	private String localPath;
	private String remotePath;

	// database
	private String remoteDbName;
	private String localDbName;

	// filetransfer
	private String remoteFilePath;
	private String localFilePath;

	public Config(String localPath, String remotePath, String host, String name, String serviceName, String port, String localDbName, String remoteDbName, String remoteFilePath, String localFilePath) {
		super();
		setLocalPath(localPath);
		setRemotePath(remotePath);
		this.host = host;
		this.name = name;
		this.serviceName = serviceName;
		this.port = port;
		this.localDbName = localDbName;
		this.remoteDbName = remoteDbName;
		this.remoteFilePath = remoteFilePath;
		this.localFilePath = localFilePath;
	}

	public Config() {
		// TODO Auto-generated constructor stub
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		if (localPath != null) {
			localPath = localPath.replace('\\', '/');

			if (!localPath.endsWith("/")) {
				localPath += "/";
			}
			this.localPath = localPath;
		}
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		if (!remotePath.endsWith("/")) {
			remotePath += "/";
		}
		this.remotePath = remotePath;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getJarName() {
		return serviceName + ".jar";
	}

	public String getLocalJarPath() {
		return localPath + getJarName();
	}

	public String getRemoteJarPath() {
		return remotePath + getJarName();
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRemoteDbName() {
		return remoteDbName;
	}

	public void setRemoteDbName(String remoteDbName) {
		this.remoteDbName = remoteDbName;
	}

	public String getLocalDbName() {
		return localDbName;
	}

	public void setLocalDbName(String localDbName) {
		this.localDbName = localDbName;
	}

	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	public void setRemoteFilePath(String remoteFilePath) {
		if (StringUtils.isNotBlank(remoteFilePath)) {
			if (!remoteFilePath.startsWith("/")) {
				remoteFilePath = "/" + remoteFilePath;
			}

			if (remoteFilePath.endsWith("/")) {
				remoteFilePath = remoteFilePath.substring(0, remoteFilePath.length() - 2);
			}

			this.remoteFilePath = remoteFilePath;
		}

	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		if (StringUtils.isNotBlank(localFilePath)) {
			localFilePath = localFilePath.replace('\\', '/');

			if (!localFilePath.endsWith("/")) {
				localFilePath = localFilePath.concat("/");
			}

			this.localFilePath = localFilePath;
		}
	}

	@Override
	public String toString() {
		return "Config [id=" + id + ", host=" + host + ", name=" + name + ", serviceName=" + serviceName + ", port=" + port + ", localPath=" + localPath + ", remotePath=" + remotePath + ", remoteDbName=" + remoteDbName + ", localDbName=" + localDbName + ", remoteFilePath=" + remoteFilePath
				+ ", localFilePath=" + localFilePath + "]";
	}

}
