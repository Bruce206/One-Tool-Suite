package de.bruss.deployment;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import de.bruss.commons.BrussUtils;
import de.bruss.filesync.FileSyncContainer;

@Entity
public class Config implements Comparable<Config> {

	@Id
	@GeneratedValue
	private Long id;

	// general
	private String host;
	private String name;

	// spring boot
	private String serviceName;
	private String localPath;
	private String remotePath;

	// database
	private String remoteDbName;
	private String localDbName;

	// logfile
	private boolean logFileConfig = false;
	private String logFilePath;

	// autoconfig
	private boolean autoconfig = false;
	private String port;
	private String serverName;
	private String ip;

	private boolean springBootConfig = false;
	private boolean databaseConfig = false;
	private boolean fileSyncConfig = false;

	@Embedded
	List<FileSyncContainer> fileSyncList = new ArrayList<FileSyncContainer>();

	// @formatter:off
	public Config(String localPath, 
					String remotePath, 
					String host,
					String name, 
					String serviceName, 
					String port, 
					String localDbName, 
					String remoteDbName, 
					boolean springBootConfig, 
					boolean databaseConfig, 
					boolean fileSyncConfig,
					boolean autoconfig,
					String ip,
					String serverName,
					List<FileSyncContainer> fileSyncList,
					String logFilePath) {
		super();
		setLocalPath(localPath);
		setRemotePath(remotePath);
		this.host = host;
		this.name = name;
		this.serviceName = serviceName;
		this.port = port;
		this.localDbName = localDbName;
		this.remoteDbName = remoteDbName;
		this.springBootConfig = springBootConfig;
		this.databaseConfig = databaseConfig;
		this.fileSyncConfig = fileSyncConfig;
		this.autoconfig = autoconfig;
		this.ip = ip;
		this.serverName = serverName;
		this.fileSyncList = fileSyncList;
		this.logFilePath = logFilePath;
	}
	
	// @formatter:on

	public Config() {
		// default constructor
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
		this.localPath = BrussUtils.formatPath(localPath, false, true);
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = BrussUtils.formatPath(remotePath, true, true);
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

	public boolean isSpringBootConfig() {
		return springBootConfig;
	}

	public void setSpringBootConfig(boolean springBootConfig) {
		this.springBootConfig = springBootConfig;
	}

	public boolean isDatabaseConfig() {
		return databaseConfig;
	}

	public void setDatabaseConfig(boolean databaseConfig) {
		this.databaseConfig = databaseConfig;
	}

	public boolean isFileSyncConfig() {
		return fileSyncConfig;
	}

	public void setFileSyncConfig(boolean fileSyncConfig) {
		this.fileSyncConfig = fileSyncConfig;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public boolean isAutoconfig() {
		return autoconfig;
	}

	public void setAutoconfig(boolean autoconfig) {
		this.autoconfig = autoconfig;
	}

	@Override
	public int compareTo(Config config) {
		return this.id.compareTo(config.id);
	}

	public List<FileSyncContainer> getFileSyncList() {
		return fileSyncList;
	}

	public void setFileSyncList(List<FileSyncContainer> fileSyncList) {
		this.fileSyncList = fileSyncList;
	}

	public String getLogFilePath() {
		return logFilePath;
	}

	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	public boolean isLogFileConfig() {
		return logFileConfig;
	}

	public void setLogFileConfig(boolean logFileConfig) {
		this.logFileConfig = logFileConfig;
	}

	@Override
	public String toString() {
		return "Config [id=" + id + ", host=" + host + ", name=" + name + ", serviceName=" + serviceName + ", localPath=" + localPath + ", remotePath=" + remotePath + ", remoteDbName=" + remoteDbName + ", localDbName=" + localDbName + ", autoconfig=" + autoconfig + ", port=" + port
				+ ", serverName=" + serverName + ", ip=" + ip + ", springBootConfig=" + springBootConfig + ", databaseConfig=" + databaseConfig + ", fileSyncConfig=" + fileSyncConfig + ", fileSyncList=" + fileSyncList + "]";
	}

}
