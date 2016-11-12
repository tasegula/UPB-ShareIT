package com.superman.common;

import java.io.Serializable;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class FileInfo implements Serializable {
	private boolean isDir;

	private String path;
	private LinkedList<FileInfo> content;
	private String type; // obsolete
	private long size;
	private String username;
	private String filename = "file";

	/**
	 * @return the isDir
	 */
	public boolean isDir() {
		return isDir;
	}

	/**
	 * @param isDir
	 *            the isDir to set
	 */
	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the content
	 */
	public LinkedList<FileInfo> getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(LinkedList<FileInfo> content) {
		this.content = content;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [isDir=" + isDir + ", path=" + path + ", content="
				+ content + ", type=" + type + ", size=" + size + ", username="
				+ getUsername() + "]";
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

}
