package de.bruss.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import de.bruss.ssh.SshUtils;
import javafx.scene.control.ProgressBar;
import org.apache.commons.lang3.StringUtils;

public class DeploymentUtils implements Runnable {

    private final ServiceType serviceType;
    private Session session;
    private Config config;
    private ProgressBar progressBar;

    public DeploymentUtils(Config config, ProgressBar progressBar) throws JSchException {
        this.config = config;
        this.progressBar = progressBar;
        this.session = SshUtils.getSession(config.getHost());
        this.session.connect();
        this.serviceType = getServiceType();
    }

    @Override
    public void run() {
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(config.getHost());
        Preconditions.checkNotNull(config.getRemotePath());
        Preconditions.checkNotNull(config.getServiceName());
        Preconditions.checkNotNull(config.getLocalPath());

        if (config.getRemotePath().equals("/")) {
            throw new IllegalArgumentException("Remote Path must not be '/'");
        }

        if (config.getLocalPath().equals("\\")) {
            throw new IllegalArgumentException("Local Path must not be '\\'");
        }

        try {
            if (isServiceInstalled()) {
                // send stop for service
                System.out.println("Stopping Service on Remote...    ");
                String response = stopService();
                System.out.println("-- [Done] Response:   " + response.trim());
            } else {
                System.out.println("No Service [" + config.getServiceName() + "] found on Server!");
                if (config.isServiceConfig()) {
                    createConfigFileOnServer();
                }
            }

            // create remote-path if not exists
            if (!SshUtils.fileExistsOnServer(session, config.getRemotePath() + "/")) {
                SshUtils.createRemotePath(session, config.getRemotePath() + "/");
            }

            if (config.isApplicationConfig()) {
                if (!SshUtils.fileExistsOnServer(session, config.getRemotePath() + "/application.properties")) {
                    createApplicationPropertiesOnServer();
                } else {
                    System.out.println("application.properties already found on server. Skipping creation!");
                }
            }

            if (config.isApacheConfig()) {
                if (!SshUtils.fileExistsOnServer(session, getRemoteApacheConfPath())) {
                    createApacheConfigOnServer();
                    reloadApache();
                } else {
                    System.out.println("Apache-Conf already found on server. Skipping creation!");
                }
            }

            // create a database if none is existent
            if (StringUtils.isNotBlank(config.getRemoteDbName()) && StringUtils.isNotBlank(config.getDbUsername())) {
                String response = SshUtils.sendCommand(this.session, "su - postgres -c \"psql -l | grep " + config.getRemoteDbName() + " | wc -l\"");
                if ("0".equals(response)) {
                    System.out.print("No Database " + config.getRemoteDbName() + " exists on server. Creating a new database...");
                    String response2 = SshUtils.sendCommand(this.session, "su - postgres -c \"createdb -O" + config.getDbUsername() + " " + config.getRemoteDbName() + "\"");
                    System.out.println("-- [done] Response: " + response2.trim());
                } else {
                    System.out.println("Database " + config.getRemoteDbName() + " already exists on server. Skipping creation!");
                }

            }

            // archive old .jar-File
            archiveOldJarFileOnServer();

            // rename current jar
            renameLocalJarFile();

            // upload jar
            uploadJar();

            // start service
            System.out.print("Starting Service: " + config.getServiceName() + " ");
            String response2 = startService();
            System.out.println("-- [done] Response: " + response2);
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isServiceInstalled() throws JSchException {
        if (ServiceType.UPSTART.equals(this.serviceType)) {
            return SshUtils.fileExistsOnServer(session, "/etc/init/" + config.getServiceName() + ".conf");
        } else {
            return SshUtils.fileExistsOnServer(session, "/etc/systemd/system/" + config.getServiceName() + ".service");
        }
    }

    private void reloadApache() {
        System.out.print("Restarting Apache");
        String response = SshUtils.sendCommand(session, "service apache2 reload");
        System.out.println("-- [done] " + response);
    }

    private void createApacheConfigOnServer() {
        System.out.print("Creating Apache-Conf...  ");
        try {
            File tempConfFile = File.createTempFile("apache-conf_", "");
            InputStream apacheConfTemplate = getClass().getResourceAsStream("/apache-conf.template");

            StringWriter writer = new StringWriter();
            IOUtils.copy(apacheConfTemplate, writer);
            String apacheConfFile = writer.toString();
            apacheConfFile = apacheConfFile.replaceAll("SERVER_NAME", config.getServerName());
            apacheConfFile = apacheConfFile.replaceAll("PORT", config.getPort());
            apacheConfFile = apacheConfFile.replaceAll("IP", config.getIP());
            Files.write(tempConfFile.toPath(), apacheConfFile.getBytes());

            System.out.print("-- Copying to server... (" + tempConfFile.toPath().toString() + ")");
            SshUtils.copyFile(tempConfFile.toPath().toString(), getRemoteApacheConfPath(), session, progressBar);
            System.out.println("-- [done]");
            System.out.print("Enabling site...");
            String response = SshUtils.sendCommand(this.session, "a2ensite " + "virt-" + config.getServiceName() + ".conf");
            System.out.println("-- [done] Response: " + response);
        } catch (IOException | JSchException | SftpException e) {
            System.out.println("Error occured while creating application.properties file");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void uploadJar() throws JSchException, SftpException {
        SshUtils.copyFile(config.getLocalJarPath(), config.getRemoteJarPath(), session, progressBar);
    }

    private void archiveOldJarFileOnServer() throws JSchException {
        ChannelSftp sftpChannel = SshUtils.getSftpChannel(session);

        try {
            sftpChannel.rename(config.getRemotePath() + "/" + config.getJarName(), config.getRemotePath() + "/" + config.getJarName() + "OLD");
        } catch (SftpException se) {
            System.out.println("No jar file found on server! Skipping archiving of file!");
        }
        sftpChannel.exit();
        sftpChannel.disconnect();
    }

    private void renameLocalJarFile() {

        File dir = new File(config.getLocalPath());
        String[] extensions = new String[]{"jar"};
        List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, false);

        if (files.size() > 1) {
            throw new RuntimeException("More than one Jar File Found in folder!");
        }

        System.out.print("Renaming File on local disk..");

        try {
            FileUtils.moveFile(files.get(0), FileUtils.getFile(config.getLocalJarPath()));
        } catch (IOException ioe) {
            System.out.println("-- Renamed File already exists! Using this file!");
            return;
        }

        System.out.println("-- [done]");
    }

    private void createApplicationPropertiesOnServer() throws IOException {
        System.out.print("Creating application.properties file...  ");
        InputStream applicationPropertiesTemplate = null;
        try {
            File tempConfFile = File.createTempFile("application.properties_", "");
            applicationPropertiesTemplate = getClass().getResourceAsStream("/application.properties.template");

            StringWriter writer = new StringWriter();
            IOUtils.copy(applicationPropertiesTemplate, writer);
            String applicationPropertiesString = writer.toString();
            applicationPropertiesString = applicationPropertiesString.replaceAll("SERVERPORT", config.getPort());
            applicationPropertiesString = applicationPropertiesString.replaceAll("DBNAME", config.getRemoteDbName());
            Files.write(tempConfFile.toPath(), applicationPropertiesString.getBytes());

            System.out.print("-- Copying to server... (" + tempConfFile.toPath().toString() + ")");
            SshUtils.copyFile(tempConfFile.toPath().toString(), config.getRemotePath() + "/application.properties", session, progressBar);
            System.out.println("-- [done]");
        } catch (IOException | JSchException | SftpException e) {
            System.out.println("Error occured while creating application.properties file");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (applicationPropertiesTemplate != null) {
                applicationPropertiesTemplate.close();
            }
        }
    }

    private void createConfigFileOnServer() {
        if (ServiceType.UPSTART.equals(this.serviceType)) {
            System.out.print("Creating config file for service " + config.getServiceName() + " with Service-Type " + this.serviceType.toString());
            try {
                File tempConfFile = File.createTempFile("config", ".conf");
                InputStream confTemplate = getClass().getResourceAsStream("/upstart.conf");

                StringWriter writer = new StringWriter();
                IOUtils.copy(confTemplate, writer);
                String confString = writer.toString();

                confString = confString.replaceAll("JARNAME", config.getServiceName());
                confString = confString.replaceAll("JARPATH", config.getRemotePath());

                if (StringUtils.isBlank(config.getJavaPath())) {
                    config.setJavaPath("java");
                }

                if (StringUtils.isBlank(config.getJvmOptions())) {
                    config.setJvmOptions("");
                }
                confString = confString.replaceAll("JAVAPATH", config.getJavaPath());
                confString = confString.replaceAll("JVMOPTIONS", config.getJvmOptions());

                Files.write(tempConfFile.toPath(), confString.getBytes());

                System.out.print("-- Copying config file to server... (" + tempConfFile.toPath().toString() + ")");
                SshUtils.copyFile(tempConfFile.toPath().toString(), "/etc/init/" + config.getServiceName() + ".conf", session, progressBar);
                System.out.println("-- [done]");
            } catch (IOException | JSchException | SftpException e) {
                System.out.println("Error occured while creating config file");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.print("Creating systemd file...  ");
            try {
                File tempServiceFile = File.createTempFile("systemd", ".service");
                InputStream serviceTemplate = getClass().getResourceAsStream("/systemd.service");

                StringWriter writer = new StringWriter();
                IOUtils.copy(serviceTemplate, writer);
                String serviceString = writer.toString();

                serviceString = serviceString.replaceAll("JARNAME", config.getServiceName());
                serviceString = serviceString.replaceAll("JARPATH", config.getRemotePath());
                serviceString = serviceString.replaceAll("JAVAPATH", StringUtils.defaultIfBlank(config.getJavaPath(), "/usr/lib/jvm/java-8-oracle/bin/java"));
                serviceString = serviceString.replaceAll("JVMOPTIONS", StringUtils.defaultIfBlank(config.getJvmOptions(), ""));

                Files.write(tempServiceFile.toPath(), serviceString.getBytes());

                System.out.print("-- Copying systemd file to server... (" + tempServiceFile.toPath().toString() + ")");
                SshUtils.copyFile(tempServiceFile.toPath().toString(), "/etc/systemd/system/" + config.getServiceName() + ".service", session, progressBar);
                System.out.println("-- [done]");
                System.out.print("Registering Service for start on reboot...");
                String response = SshUtils.sendCommand(this.session, "systemctl enable " + config.getServiceName() + ".service");
                System.out.println("-- [done] Response: " + response);
            } catch (IOException | JSchException | SftpException e) {
                System.out.println("Error occured while creating systemd file");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }

    private String getRemoteApacheConfPath() {
        return "/etc/apache2/sites-available/" + "virt-" + config.getServiceName() + ".conf";
    }

    private String stopService() {
        if (ServiceType.UPSTART.equals(this.serviceType)) {
            return SshUtils.sendCommand(session, "stop " + config.getServiceName());
        } else {
            return SshUtils.sendCommand(session, "service " + config.getServiceName() + " stop");
        }
    }

    private String startService() {
        if (ServiceType.UPSTART.equals(this.serviceType)) {
            return SshUtils.sendCommand(session, "start " + config.getServiceName());
        } else {
            return SshUtils.sendCommand(session, "service " + config.getServiceName() + " start");
        }
    }

    private ServiceType getServiceType() {
        String response = SshUtils.sendCommand(this.session, "lsb_release -r");

        String version = response.substring(response.indexOf(":") + 1, response.length()).trim();

        Double versionDbl = Double.parseDouble(version);

        if (versionDbl < 16) {
            System.out.println("Detected Ubuntu-Version: " + version + ". Using " + ServiceType.UPSTART.toString());
            return ServiceType.UPSTART;
        } else {
            System.out.println("Detected Ubuntu-Version: " + version + ". Using " + ServiceType.SYSTEMD.toString());
            return ServiceType.SYSTEMD;
        }

    }

    public enum ServiceType {
        UPSTART, SYSTEMD
    }
}
