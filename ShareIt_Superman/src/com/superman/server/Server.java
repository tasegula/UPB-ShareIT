package com.superman.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import com.superman.common.FileInfo;
import com.superman.common.Packet;
import com.superman.common.UserInfo;

public class Server extends Thread {
	private static LinkedList<UserInfo> userlist;
	private static HashMap<String, LinkedList<FileInfo>> filelists;
	private static int port;
	private static InetAddress IP;
	private static boolean stop;
	private static int maxClientsNumber;
	private static int loggedClients;
	
	private final String className = "[Server] ";

	public Server() throws UnknownHostException, IOException {
		userlist = new LinkedList<UserInfo>();
		filelists = new HashMap<String, LinkedList<FileInfo>>();
		ServerSocket ssocket = new ServerSocket(0, 0,
				InetAddress.getLocalHost());
		setPort(ssocket.getLocalPort());
		ssocket.close();

		setIP(InetAddress.getLocalHost());
		setMaxClientNumber(1000);
		setLoggedClients(0);

		setStop(false);
	}
	
	public Server(String IP, int port, int clientNumber) throws IOException{
		userlist = new LinkedList<UserInfo>();
		filelists = new HashMap<String, LinkedList<FileInfo>>();
//		ServerSocket ssocket = new ServerSocket(0, 0,
//				InetAddress.getLocalHost());
//		setPort(ssocket.getLocalPort());
//		ssocket.close();
//
//		setIP(InetAddress.getLocalHost());
		
		if("0.0.0.0".equals(IP)){
			setIP(InetAddress.getLocalHost());
		}else{
			setIP(InetAddress.getByName(IP));
		}
		
		if(port == 0){
			ServerSocket ssocket = new ServerSocket(0, 0,
					InetAddress.getLocalHost());
			setPort(ssocket.getLocalPort());
			ssocket.close();
		}else{
			setPort(port);
		}
		
		if(clientNumber == -1){
			setMaxClientNumber(1000);
		}else{
			setMaxClientNumber(clientNumber);
		}
		
		setLoggedClients(0);

		setStop(false);
	}

	/**
	 * Register the given user.
	 * 
	 * @param username
	 *            the user to be registered
	 * @param IP
	 *            the IP
	 * @param port
	 *            the port
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @see InetAddress
	 */
	public boolean registerClient(String username, Socket client,
			int port) throws IOException, ClassNotFoundException {

		boolean userExists = false;
		ObjectOutputStream out = new ObjectOutputStream(
				client.getOutputStream());

		for (UserInfo user : userlist) {
			if (user.getUsername().equals(username)) {
				userExists = true;
				break;
			}
		}

		/* If username exists: send error code 400; else: send succes code 200 */
		if (userExists == true || getLoggedClients() >= getMaxClientsNumber()) {

			Packet p = new Packet(400, null, null, null, null);
			out.writeObject(p);

			out.close();
			client.close();

			return false;
		} else {
			UserInfo user = new UserInfo(username, client.getInetAddress(), port);
			userlist.add(user);
			loggedClients++;

			Packet p = new Packet(200, null, null, null, null);
			out.writeObject(p);

			out.close();
			client.close();

			return true;
		}
	}

	public void recSearchItem(FileInfo file,
			LinkedList<FileInfo> results, String query) {
		if (file.isDir() == true) {
			recSearchList(file.getContent(), results, query);
		} else {
			String[] tokens = file.getPath().split("\\\\");
			
			if ((tokens[tokens.length - 1].length() - 
				StringUtils.getLevenshteinDistance(tokens[tokens.length - 1], query)) <= 5) {
				results.add(file);
			}
		}
	}

	public void recSearchList(LinkedList<FileInfo> flist,
			LinkedList<FileInfo> results, String query) {
		for (FileInfo file : flist) {
			recSearchItem(file, results, query);
		}
	}

	/**
	 * Search for the given query in every filelist.
	 * 
	 * @param filter
	 *            the search filter; a query
	 * @return the result of the given query
	 * @throws IOException
	 * 
	 * @see FileInfo
	 */
	public void search(String query, Socket client) throws IOException {

		LinkedList<FileInfo> results = new LinkedList<FileInfo>();

		for (String username : filelists.keySet()) {
			recSearchList(filelists.get(username), results, query);
		}

		Packet res = new Packet(300, null, null, null, null);
		res.setFilelist(results);

		ObjectOutputStream out = new ObjectOutputStream(
				client.getOutputStream());
		out.writeObject(res);

		out.close();
		client.close();
	}

	/**
	 * Update the filelist of the given user.
	 * 
	 * @param username
	 *            a String representing the user of whom the filelist is updated
	 * @param filelist
	 *            a LinkedList of FileInfo Object to update the filelist
	 * @throws IOException
	 * 
	 * @see FileInfo
	 */
	static void updateFilelist(String username, LinkedList<FileInfo> filelist,
			Socket client) throws IOException {

		filelists.put(username, filelist);

		Packet p = new Packet(200, null, null, null, null);

		ObjectOutputStream out = new ObjectOutputStream(
				client.getOutputStream());
		out.writeObject(p);

		out.close();
		client.close();
	}

	public static void sendUserlist(Socket client) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(
				client.getOutputStream());

		Packet p = new Packet(220, null, userlist, null, null);
		out.writeObject(p);

		out.close();
		client.close();
	}

	/**
	 * Removes an user from the system.
	 * 
	 * @param username
	 * @throws IOException
	 */
	public static void removeUser(String username, Socket client)
			throws IOException {

		for (UserInfo user : userlist) {
			if (user.getUsername().equals(username)) {
				userlist.remove(user);
				break;
			}
		}
		loggedClients--;

		Packet p = new Packet(200, null, null, null, null);

		ObjectOutputStream out = new ObjectOutputStream(
				client.getOutputStream());
		out.writeObject(p);

		out.close();
		client.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Server [userlist=" + userlist + ", filelist=" + filelists + "]";
	}

	@Override
	public void run() {
		System.out.println(className + "run(): " + "IP=" + getIP() + ": " + getPort());

		while (true) {
			if (isStop()) {
				return;
			}

			ServerSocket ssocket = null;

			try {
				ssocket = new ServerSocket(getPort(), 0, InetAddress.getLocalHost());
				ssocket.setSoTimeout(1000);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			Socket client = null;
			try {
				client = ssocket.accept();
			} catch (SocketTimeoutException e) {
				try {
					ssocket.close();
					continue;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				ssocket.close();
				ObjectInputStream in = new ObjectInputStream(
						client.getInputStream());

				Packet p = null;
				while ((p = (Packet) in.readObject()) == null) {
				}

				/*
				 * 100 - login; 300 - search; 400 - send userlist; 500 - update
				 * filelist; 600 - logout;
				 */
				switch (p.getType()) {
				case 100:
					registerClient(p.getUsername(), client, p.getDmanagerPort());
					break;

				case 300:
					search(p.getQuery(), client);
					break;

				case 400:
					sendUserlist(client);
					break;

				case 500:
					updateFilelist(p.getUsername(), p.getFilelist(), client);
					break;

				case 600:
					removeUser(p.getUsername(), client);
					break;

				}

				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static LinkedList<UserInfo> getUserlist() {
		return userlist;
	}

	public static void setUserlist(LinkedList<UserInfo> userlist) {
		Server.userlist = userlist;
	}

	public static HashMap<String, LinkedList<FileInfo>> getFilelists() {
		return filelists;
	}

	public static void setFilelists(
			HashMap<String, LinkedList<FileInfo>> filelists) {
		Server.filelists = filelists;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Server.port = port;
	}

	public static InetAddress getIP() {
		return IP;
	}

	public static void setIP(InetAddress iP) {
		IP = iP;
	}

	public static boolean isStop() {
		return stop;
	}

	public static void setStop(boolean stop) {
		Server.stop = stop;
	}

	/**
	 * @return the clientNumber
	 */
	public static int getMaxClientsNumber() {
		return maxClientsNumber;
	}

	/**
	 * @param clientNumber the clientNumber to set
	 */
	public static void setMaxClientNumber(int clientNumber) {
		Server.maxClientsNumber = clientNumber;
	}

	/**
	 * @return the loggedClients
	 */
	public static int getLoggedClients() {
		return loggedClients;
	}

	/**
	 * @param loggedClients the loggedClients to set
	 */
	public static void setLoggedClients(int loggedClients) {
		Server.loggedClients = loggedClients;
	}

}
