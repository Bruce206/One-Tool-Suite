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
    private String dbUsername;
    private String dbPassword;

    // logfile
    private boolean logFileConfig = false;
    private String logFilePath;

    // autoconfig
    private boolean apacheConfig = false;
    private boolean applicationConfig = false;
    private boolean serviceConfig = false;
    private String port;
    private String serverName;
    private String ip;
    private String javaPath;
    private String jvmOptions;

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
                  String dbUsername,
                  String dbPassword,
                  boolean springBootConfig,
                  boolean databaseConfig,
                  boolean fileSyncConfig,
                  String ip,
                  String serverName,
                  List<FileSyncContainer> fileSyncList,
                  String logFilePath,
                  String javaPath,
                  String jvmOptions,
                  boolean apacheConfig,
                  boolean applicationConfig,
                  boolean serviceConfig) {
        super();
        setLocalPath(localPath);
        setRemotePath(remotePath);
        this.host = host;
        this.name = name;
        this.serviceName = serviceName;
        this.port = port;
        this.localDbName = localDbName;
        this.remoteDbName = remoteDbName;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.springBootConfig = springBootConfig;
        this.databaseConfig = databaseConfig;
        this.fileSyncConfig = fileSyncConfig;
        this.ip = ip;
        this.serverName = serverName;
        this.fileSyncList = fileSyncList;
        this.logFilePath = logFilePath;
        this.javaPath = javaPath;
        this.jvmOptions = jvmOptions;
        this.apacheConfig = apacheConfig;
        this.applicationConfig = applicationConfig;
        this.serviceConfig = serviceConfig;
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
        this.remotePath = BrussUtils.formatPath(remotePath, true, false);
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
        return remotePath + "/" + getJarName();
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

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public String getJvmOptions() {
        return jvmOptions;
    }

    public void setJvmOptions(String jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public boolean isApacheConfig() {
        return apacheConfig;
    }

    public void setApacheConfig(boolean apacheConfig) {
        this.apacheConfig = apacheConfig;
    }

    public boolean isApplicationConfig() {
        return applicationConfig;
    }

    public void setApplicationConfig(boolean applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    public boolean isServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(boolean serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    @Override
    public String toString() {
        return "Config{" +
                "id=" + id +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", localPath='" + localPath + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", remoteDbName='" + remoteDbName + '\'' +
                ", localDbName='" + localDbName + '\'' +
                ", dbUsername='" + dbUsername + '\'' +
                ", dbPassword='" + dbPassword + '\'' +
                ", logFileConfig=" + logFileConfig +
                ", logFilePath='" + logFilePath + '\'' +
                ", apacheConfig=" + apacheConfig +
                ", applicationConfig=" + applicationConfig +
                ", serviceConfig=" + serviceConfig +
                ", port='" + port + '\'' +
                ", serverName='" + serverName + '\'' +
                ", ip='" + ip + '\'' +
                ", javaPath='" + javaPath + '\'' +
                ", jvmOptions='" + jvmOptions + '\'' +
                ", springBootConfig=" + springBootConfig +
                ", databaseConfig=" + databaseConfig +
                ", fileSyncConfig=" + fileSyncConfig +
                ", fileSyncList=" + fileSyncList +
                '}';
    }
}
