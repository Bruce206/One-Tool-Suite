package de.bruss.deployment;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Config {

	@Id
	@GeneratedValue
	private long id;

	private String localPath;
	private String remotePath;
	private String host;
	private String name;
	private String serviceName;
	private String port;
	private String remoteDbName;
	private String localDbName;

	public Config(String localPath, String remotePath, String host, String name, String serviceName, String port, String localDbName, String remoteDbName) {
		super();
		setLocalPath(localPath);
		setRemotePath(remotePath);
		this.host = host;
		this.name = name;
		this.serviceName = serviceName;
		this.port = port;
		this.localDbName = localDbName;
		this.remoteDbName = remoteDbName;
	}

	public Config() {
		// TODO Auto-generated constructor stub
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		if (!localPath.endsWith("\\")) {
			localPath += "\\";
		}
		this.localPath = localPath;
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

	@Override
	public String toString() {
		return "Config [id=" + id + ", localPath=" + localPath + ", remotePath=" + remotePath + ", host=" + host + ", name=" + name + ", serviceName=" + serviceName + ", port=" + port
				+ ", remoteDbName=" + remoteDbName + ", localDbName=" + localDbName + "]";
	}
	
}
