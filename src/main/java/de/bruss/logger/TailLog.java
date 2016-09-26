package de.bruss.logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.bruss.deployment.Config;
import de.bruss.ssh.SshUtils;

public class TailLog implements Runnable {

	private Session session;
	private Config config;
	private TailLogCtrl tailLogCtrl;
	private Timer timer;

	public TailLog(Config config, TailLogCtrl tailLogCtrl) throws JSchException {
		super();
		this.session = SshUtils.getSession(config.getHost());
		this.config = config;
		this.session.connect();
		this.tailLogCtrl = tailLogCtrl;
	}

	@Override
	public void run() {
		Channel channel;
		try {
			channel = SshUtils.getShellChannel(session);

			OutputStream ops = channel.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);

			channel.connect();
			InputStream input = channel.getInputStream();

			ps.println("tail -n 100 -f " + config.getLogFilePath());
			ps.close();

			int SIZE = 1024;
			byte[] tmp = new byte[SIZE];

			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						while (input.available() > 0) {
							int i = input.read(tmp, 0, SIZE);
							if (i < 0) {
								break;
							}

							String output = new String(tmp, 0, i);
							if (StringUtils.isNotBlank(output)) {
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										tailLogCtrl.logView.appendText(output);
									}
								});

							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 0, TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS));

		} catch (JSchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		timer.cancel();
	}

}
