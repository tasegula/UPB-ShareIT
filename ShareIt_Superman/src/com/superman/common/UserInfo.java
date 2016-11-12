package com.superman.common;

import java.io.Serializable;
import java.net.InetAddress;

@SuppressWarnings("serial")
public class UserInfo implements Serializable {
	private String username;
	private InetAddress IP;
	private int port;

	public UserInfo() {

	}

	public UserInfo(String username, InetAddress IP, int port) {
		setUsername(username);
		setIP(IP);
		setPort(port);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the iP
	 * 
	 * @see InetAddress
	 */
	public InetAddress getIP() {
		return IP;
	}

	/**
	 * @param iP
	 *            the iP to set
	 * 
	 * @see InetAddress
	 */
	public void setIP(InetAddress iP) {
		IP = iP;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserInfo [username=" + username + ", IP=" + IP + ", port="
				+ port + "]";
	}

}
