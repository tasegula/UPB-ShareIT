package com.superman.common;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileStatus {
	private SimpleStringProperty username;
	private SimpleStringProperty size;
	private SimpleStringProperty filename;
	private SimpleStringProperty shortPath;

	private SimpleDoubleProperty progress;
	private SimpleStringProperty prog;

	public FileStatus() {
		filename  = new SimpleStringProperty();
		size      = new SimpleStringProperty();
		username  = new SimpleStringProperty();
		shortPath = new SimpleStringProperty();

		progress  = new SimpleDoubleProperty();
		prog      = new SimpleStringProperty(); 
	}
	
	public FileStatus(String f, String p, String s, String u, String sp) {
		filename  = new SimpleStringProperty(f);
		size      = new SimpleStringProperty(s);
		username  = new SimpleStringProperty(u);
		shortPath = new SimpleStringProperty(sp);

		progress  = new SimpleDoubleProperty(Double.valueOf(p) / 100.0);
		prog      = new SimpleStringProperty(p);
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username.get();
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username.set(username);
	}

	/**
	 * @return the progress
	 */
//	public Double getProgress() {
//		return Double.valueOf(this.progress.get());
//	}

	/**
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(String progress) {
		double p = Double.valueOf(progress) / 100.0;
		System.out.println(p + " = " + Double.valueOf(progress));
		
		this.progress.set(p);
		this.prog.set(progress);
	}

	public SimpleDoubleProperty progressProperty() {
		return this.progress;
	}
	
	/**
	 * @return the size
	 */
	public String getSize() {
		return size.get();
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(String size) {
		this.size.set(size + "B");
	}
	
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename.get();
	}

	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename.set(filename);
	}

	/**
	 * @return the shortPath
	 */
	public String getShortPath() {
		return shortPath.get();
	}

	/**
	 * @param shortPath the shortPath to set
	 */
	public void setShortPath(String shortPath) {
		this.shortPath.set(shortPath);
	}


	/**
	 * @return the prog
	 */
	public String getProg() {
		//this.prog.set(Double.valueOf(this.progress.get()));
		return this.prog.get();
	}

	/**
	 * @param prog the prog to set
	 */
	public void setProg(String progress) {
		this.prog.set(progress);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileStatus [username=" + username 
				+ ", progress=" + progress 
				+ ", progressBar=" + prog
				+ ", size=" + size 
				+ ", filename=" + filename 
				+ ", shortPath=" + shortPath + "]";
	}
}
