package com.superman.client;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;

import com.superman.common.FileStatus;
import com.superman.common.Packet;
import com.superman.gui.java.DownloadController;

public class DownloadManager extends Thread {

	private HashMap<String, FileStatus> stats        = new HashMap<String, FileStatus>();
	private HashMap<String, String> fileSources = new HashMap<String, String>();
	private LinkedList<String> usersFrom        = new LinkedList<String>();
	private String downloadFolder               = "./downloads/";
	private InetAddress ip;
	private int port;
	
	private boolean stop;
	private final String className              = "[Receiver] ";

	public DownloadManager(InetAddress ip, int port) {
		setIp(ip);
		setPort(port);

		File df = new File(downloadFolder);
		if (df.exists() == false) {
			df.mkdir();
		}

		stop = false;
	}

	public void stopExec() {
		stop = true;
	}

	public HashMap<String, FileStatus> getDownloadStatus() {
		return stats;
	}

	/**
	 * This method will establish a connection with the UploadManager of the
	 * client from whom we want to download the file and will also receive the
	 * data packages
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			if (stop == true) {
				return;
			}

			try {
				ServerSocket sock = new ServerSocket(getPort(), 0, getIp());
				sock.setSoTimeout(1000);
				Socket dld = null;

				try {
					dld = sock.accept();
				} catch (SocketTimeoutException e) {
					try {
						sock.close();
						continue;
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				sock.close();
				ObjectInputStream instream = null;
				try {
					instream = new ObjectInputStream(dld.getInputStream());
				}
				catch (EOFException e){
					e.printStackTrace();
					continue;
				}
				
				Packet p;
				while ((p = (Packet) instream.readObject()) == null) {}

				switch (p.getType()) {
				case 100: {
					if (fileSources.get(p.getPath()) != null && 
						!fileSources.get(p.getPath()).equals(p.getUsername())) {

						instream.close();
						dld.close();
						continue;
					}

					String path = getDownloadFolder() + p.getPath();
					System.out.println(className + "run(): " + path);
					File f = new File(path);

					if (f.exists() == false) {
						f.createNewFile();
					}

					FileOutputStream fileWriter = new FileOutputStream(f, true);
					fileWriter.write(p.getData());
					fileWriter.close();

					stats.put(p.getPath(), 
							new FileStatus(p.getAbsolutePath(),
										   "" + p.getStatus(),
										   "" + p.getFileSize(),
										   p.getUsername(),
										   p.getPath()));
					
					try {
						DownloadController.data.clear();
						DownloadController.data.addAll(stats.values());
						System.out.println(DownloadController.data);
					} catch (Exception e) {}
					
					fileSources.put(p.getPath(), p.getUsername());
					if (usersFrom.contains(p.getUsername()) == false) {
						usersFrom.add(p.getUsername());
					}

					instream.close();
					dld.close();
					
					continue;
				}

				case 200: {
					String path = getDownloadFolder() + p.getPath();
					File f = new File(path);

					if (f.exists() == false) {
						f.mkdir();
					}
					instream.close();
					dld.close();
					continue;
				}

				case 300: {
					stats.remove(p.getPath());
					usersFrom.remove(p.getUsername());
					fileSources.remove(p.getPath());
					
					try {
						DownloadController.data.clear();
					} catch (Exception e) {}
					
					instream.close();
					dld.close();
					continue;
				}
				
				case 330:
					System.out.println("Am primit 330");
					stats.remove(p.getPath());
					usersFrom.remove(p.getUsername());
					fileSources.remove(p.getPath());
					
					try {
						DownloadController.data.clear();
					} catch (Exception e) {}
					
					File f = new File(getDownloadFolder() + p.getPath());
					if(f.exists()){
						f.delete();
					}
					
					instream.close();
					dld.close();
					continue;

				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDownloadFolder() {
		return downloadFolder;
	}

	public void setDownloadFolder(String downloadFolder) {
		this.downloadFolder = downloadFolder + "/";

		File df = new File(downloadFolder);
		if (df.exists() == false) {
			df.mkdir();
		}
	}

	public LinkedList<String> getUsersFrom() {
		return usersFrom;
	}

	public void setUsersFrom(LinkedList<String> usersFrom) {
		this.usersFrom = usersFrom;
	}

	public HashMap<String, String> getFileSources() {
		return fileSources;
	}

	public void setFileSources(HashMap<String, String> fileSources) {
		this.fileSources = fileSources;
	}

}
