package de.bruss.filesync;

import com.jcraft.jsch.UserInfo;

public class MyUserInfo implements UserInfo {

	String password;

	public MyUserInfo(String password) {
		this.password = password;
	}

	@Override
	public String getPassphrase() {
		return password;
	}

	@Override
	public String getPassword() {
		return null;
	}

	public void setPassword(String passwd) {
		password = passwd;
	}

	@Override
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	public boolean promptPassphrase(String message) {
		return true;
	}

	@Override
	public boolean promptYesNo(String message) {
		return false;
	}

	@Override
	public void showMessage(String message) {
	}

}
