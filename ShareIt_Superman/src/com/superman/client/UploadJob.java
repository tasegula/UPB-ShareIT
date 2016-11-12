package com.superman.client;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;

import com.superman.common.FileInfo;

public class UploadJob {

	/**
	 * Name of the file which will be uploaded
	 */
	private FileInfo    file;
	private String      shortPath;
	private int         port;
	private InetAddress ip;
	private float       uploadStatus;
	private int         pos;
	private File        localFile;
	private Socket      uploadSocket;
	private int         type;
	private String      userTo;
	
	private final String className = "[UploadJob] ";

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            Name of the uploaded file
	 * @param useFor
	 *            Destination user
	 */
	public UploadJob(FileInfo file, InetAddress ip, int port, String userTo) {
		setFile(file);
		setIp(ip);
		setPort(port);
		setUploadStatus(0);
		setPos(0);
		System.out.println(className + "UploadJob(): " + file);
		setLocalFile(openFile(file));
		setUserTo(userTo);
	}

	public File openFile(FileInfo f) {
		return new File(f.getPath());
	}

	/**
	 * @return the filename
	 */
	public FileInfo getFile() {
		return file;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFile(FileInfo file) {
		this.file = file;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public float getUploadStatus() {
		return uploadStatus;
	}

	public void setUploadStatus(float uploadStatus) {
		this.uploadStatus = uploadStatus;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public File getLocalFile() {
		return localFile;
	}

	public void setLocalFile(File localFile) {
		this.localFile = localFile;
	}

	public Socket getUploadSocket() {
		return uploadSocket;
	}

	public void setUploadSocket(Socket uploadSocket) {
		this.uploadSocket = uploadSocket;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getShortPath() {
		return shortPath;
	}

	public void setShortPath(String parent, boolean modify) {
		if(modify == false){
			this.shortPath = parent;
			return;
		}
		
		String s = "";
		s = file.getPath().substring(parent.length(), file.getPath().length());
		this.shortPath = s;
	}

	public String getUserTo() {
		return userTo;
	}

	public void setUserTo(String userTo) {
		this.userTo = userTo;
	}

}
