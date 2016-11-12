package com.superman.common;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class Packet implements Serializable {
	private Integer type;
	private String username;
	private LinkedList<UserInfo> userlist;
	private LinkedList<FileInfo> filelist;
	private String query;
	private String path;
	private float status;
	private byte[] data;
	private InetAddress dmanagerIP;
	private int dmanagerPort;
	private String absolutePath;
	private long fileSize;

	public Packet() {

	}

	public Packet(Integer type, String username, LinkedList<UserInfo> userlist,
			LinkedList<FileInfo> filelist, String query) {
		setType(type);
		setUsername(username);
		setFilelist(filelist);
		setUserlist(userlist);
		setQuery(query);
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LinkedList<UserInfo> getUserlist() {
		return userlist;
	}

	public void setUserlist(LinkedList<UserInfo> userlist) {
		this.userlist = userlist;
	}

	public LinkedList<FileInfo> getFilelist() {
		return filelist;
	}

	public void setFilelist(LinkedList<FileInfo> filelist) {
		this.filelist = filelist;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public float getStatus() {
		return status;
	}

	public void setStatus(float status) {
		this.status = status;
	}

	public InetAddress getDmanagerIP() {
		return dmanagerIP;
	}

	public void setDmanagerIP(InetAddress dmanagerIP) {
		this.dmanagerIP = dmanagerIP;
	}

	public int getDmanagerPort() {
		return dmanagerPort;
	}

	public void setDmanagerPort(int dmanagerPort) {
		this.dmanagerPort = dmanagerPort;
	}

	/**
	 * @return the absolutePath
	 */
	public String getAbsolutePath() {
		return absolutePath;
	}

	/**
	 * @param absolutePath the absolutePath to set
	 */
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
