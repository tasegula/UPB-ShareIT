package com.superman.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import com.superman.common.FileInfo;
import com.superman.common.Packet;

public class UploadManager extends Thread {

	/**
	 * 
	 * Queue in which we will keep the UploadJob type objects. Each time a slot
	 * will be released , an UploadJob will be added to the queue.
	 */
	private LinkedBlockingQueue<UploadJob>  pendingJobs     = new  LinkedBlockingQueue<UploadJob>();
	LinkedList<UploadJob>           		activejobs      = new LinkedList<UploadJob>();
	private HashMap<String, String> 		canceledjobs    = new HashMap<String, String>();
	private LinkedList<String>      		cancelFromUsers = new LinkedList<String>();
	private Integer uploadSlots;
	private String username;

	private boolean stop;
	private boolean cancelAll;
	
	private final String className = "[Sender] ";
	private final int BUFSIZE = 1000000;// 1MB

	/**
	 * TBD Constructor
	 * 
	 * @throws UnknownHostException
	 */
	public UploadManager(String username) {
		this.uploadSlots = 2;
		stop = false;
		setUsername(username);
		setCancelAll(false);

	}

	public  void stopExec() {
		stop = true;
	}

	public  HashMap<String, Float> getStatus() {

		HashMap<String, Float> status = new HashMap<String, Float>();

		for (int i = 0; i < activejobs.size() && i < uploadSlots; i++) {
			Float percent = ((float) activejobs.get(i).getPos() / (float) activejobs
					.get(i).getFile().getSize()) * 100f;
			String filename = activejobs.get(i).getShortPath();
			status.put(filename, percent);
		}

		return status;
	}

	private void cancelAllJobs() throws IOException {
		pendingJobs.clear();

		for (UploadJob job : activejobs) {
			Socket sockEnd = new Socket(job.getIp(), job.getPort());
			ObjectOutputStream socketOutEnd = new ObjectOutputStream(
					sockEnd.getOutputStream());
			Packet p = new Packet();
			p.setType(330);
			p.setUsername(getUsername());
			p.setPath(job.getShortPath());

			socketOutEnd.writeObject(p);

			socketOutEnd.close();
			sockEnd.close();
		}

		activejobs.clear();
		System.out.println("Canceled all jobs");
	}

	private void cancelAllJobsFromUser(String userTo) {
		for (int i = 0; i < activejobs.size(); i++) {
			if (activejobs.get(i).getUserTo().equals(userTo)) {
				activejobs.remove(i);
				i--;
			}
		}

		UploadJob[] jobsInPending = (UploadJob[]) pendingJobs.toArray();

		for (int i = 0; i < jobsInPending.length; i++) {
			if (jobsInPending[i].getUserTo().equals(userTo)) {
				pendingJobs.remove(jobsInPending[i]);
			}
		}
	}

	private void cancelOneJob(String userTo, String filename)
			throws IOException {
		UploadJob job;
		for (int i = 0; i < activejobs.size(); i++) {
			if (activejobs.get(i).getShortPath().equals(filename)
					&& activejobs.get(i).getUserTo().equals(userTo)) {

				job = activejobs.get(i);
				Socket sockEnd = new Socket(job.getIp(), job.getPort());
				ObjectOutputStream socketOutEnd = new ObjectOutputStream(
						sockEnd.getOutputStream());
				Packet p = new Packet();
				p.setType(330);
				p.setPath(filename);
				p.setUsername(getUsername());

				socketOutEnd.writeObject(p);

				socketOutEnd.close();
				sockEnd.close();

				activejobs.remove(i);
				break;
			}
		}
	}

	public  void cancelAll() {
		setCancelAll(true);
	}

	public  void cancelJob(String userTo, String filename) {
		canceledjobs.put(userTo, filename);
	}

	public  void cancelJobsFromUser(String userTo) {
		cancelFromUsers.add(userTo);
	}

	/**
	 * Creates a new UploadJob object and adds it to the queue.
	 * 
	 * @param filename
	 * @param user
	 */
	public  void setJob(UploadJob job) {
		System.out.println(Thread.currentThread().getName());
		pendingJobs.add(job);
	}

	/**
	 * @return the uploadSlots
	 */
	public  int getUploadSlots() {
		return uploadSlots;
	}

	/**
	 * @param uploadSlots
	 *            Sets the maximum number of uploadSlots
	 */
	public void setUploadSlots(int uploadSlots) {
		synchronized(this.uploadSlots) {
			this.uploadSlots = uploadSlots;
		}
	}

	private void putFileJobs(FileInfo file, InetAddress ip, int port, String parentDir, String userTo) {
		UploadJob job = new UploadJob(file, ip, port, userTo);
		job.setShortPath(parentDir, true);
		job.setType(0);
		pendingJobs.add(job);
	}

	private void putMkdirJobs(FileInfo file, InetAddress ip, int port,
			String parentDir, String userTo) {
		
		UploadJob job = new UploadJob(file, ip, port, userTo);
		job.setShortPath(parentDir, true);
		job.setType(1);
		pendingJobs.add(job);

		for (FileInfo f : file.getContent()) {
			System.out.println(className + "putMkdir(): " + f);
			if (f.isDir() == true) {
				putMkdirJobs(f, ip, port, parentDir, userTo);
			} else {
				putFileJobs(f, ip, port, parentDir, userTo);
			}
		}
	}

	private void activateJob(UploadJob job) throws IOException {
		if (job == null) {
			return;
		}
		
		if(job.getType() == 1){
			activejobs.add(job);
			return;
		}

		if (job.getFile().isDir() == false) {
			if(job.getType() != 0){
				job.setShortPath(job.getLocalFile().getName(), false);
			}
			job.setType(0);
			activejobs.add(job);
		} else {
			putMkdirJobs(job.getFile(), job.getIp(), job.getPort(), job.getFile().getPath(), job.getUserTo());
		}

		System.out.println(className + "activateJob(): " + "activated");
	}

	/**
	 * This method will establish the connection with the DownloadManager of the
	 * client to whom we want to send the file and will also send the packages.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public  void run() {
		UploadJob job;
		while (true) {
			
			if (isCancelAll() == true) {
				try {
					cancelAllJobs();
					setCancelAll(false);
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

			for (String userTo : canceledjobs.keySet()) {
				try {
					cancelOneJob(userTo, canceledjobs.get(userTo));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			canceledjobs.clear();

			for (String userTo : cancelFromUsers) {
				cancelAllJobsFromUser(userTo);
			}

			if (stop == true) {
				return;
			}

			

			cancelFromUsers.clear();

			for (int i = 0; i < activejobs.size() && i < getUploadSlots(); i++) {
				job = activejobs.get(i);
				try {
					System.out.println(className + "run(): " + job.getIp() + " / " + job.getPort());
					Socket sock = new Socket(job.getIp(), job.getPort());
					ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());

					Packet p = new Packet();

					if (job.getType() == 0) {
						FileInputStream in = new FileInputStream(job.getLocalFile());
						p.setType(100);

						byte[] buffer = new byte[(
								(job.getFile().getSize() - job.getPos()) > BUFSIZE) ? 
										BUFSIZE : 
										(int) (job.getFile().getSize() - job.getPos())];
						in.skip(job.getPos());
						job.setPos(job.getPos() + in.read(buffer, 0, buffer.length));

						p.setData(buffer);
						p.setStatus(getStatus().get(job.getShortPath()));
						p.setPath(job.getShortPath());
						p.setUsername(getUsername());
						p.setFileSize(job.getFile().getSize());

						outstream.writeObject(p);
						outstream.flush();
						System.out.println(className + "run(): " + "sended " + p.getStatus());

						outstream.close();
						in.close();
						sock.close();
						System.out.println(className + "run(): " + "close sockets and streams");

						if (job.getPos() >= job.getFile().getSize()) {
							Socket sockEnd = new Socket(job.getIp(),
									job.getPort());
							ObjectOutputStream socketOutEnd = new ObjectOutputStream(
									sockEnd.getOutputStream());

							p.setType(300);
							p.setUsername(getUsername());
							p.setData(null);
							p.setPath(job.getShortPath());

							socketOutEnd.writeObject(p);
							socketOutEnd.flush();

							socketOutEnd.close();
							sockEnd.close();
							System.out.println(className + "run(): " + "send end job packet");
							activejobs.remove(i);
							System.out.println(className + "run(): " + "Remove, size = " + activejobs.size());
							i--;
						}

					} else {
						p.setType(200);

						p.setPath(job.getShortPath());
						outstream.writeObject(p);
						outstream.flush();

						outstream.close();
						sock.close();

						activejobs.remove(i);
						System.out.println(className + "run(): " + "Removed directory, size = " + activejobs.size());
						i--;
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
			
			if (activejobs.size() < uploadSlots) {
				for (int i = activejobs.size(); i < uploadSlots; i++) {
					try {
						activateJob(pendingJobs.poll());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	public  String getUsername() {
		return username;
	}

	public  void setUsername(String username) {
		this.username = username;
	}

	public  boolean isCancelAll() {
		return cancelAll;
	}

	public  void setCancelAll(boolean cancelAll) {
		this.cancelAll = cancelAll;
	}

	public  HashMap<String, String> getCanceledjobs() {
		return canceledjobs;
	}

	public  void setCanceledjobs(HashMap<String, String> canceledjobs) {
		this.canceledjobs = canceledjobs;
	}

	public  LinkedList<String> getCancelFromUsers() {
		return cancelFromUsers;
	}

	public  void setCancelFromUsers(LinkedList<String> cancelFromUsers) {
		this.cancelFromUsers = cancelFromUsers;
	}

}
