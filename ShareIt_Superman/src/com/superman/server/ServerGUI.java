package com.superman.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ServerGUI {

	public static void main(String[] args) throws IOException{
		Server server = null;
		String line;
		Scanner sc = new Scanner(System.in);
		int maxClientsNumber = -1;
		int port = -1;
		String IP = "";
		
		if(args.length < 2 || args.length > 3){
			System.out.println("Numar de argumente incorent");
			System.out.println("Lansare Server: java -jar ShareIt_Server.jar adresa_IP numar_port [limita_clienti]");
			
			sc.close();
			return;
		}
		
		IP = args[0];
		port = Integer.parseInt(args[1]);
		if(args.length == 3){
			maxClientsNumber = Integer.parseInt(args[2]);
		}
		
		try {
			server = new Server(IP, port, maxClientsNumber);
		} catch (UnknownHostException e) {
			
			System.out.println("Eroare Startup");
			sc.close();
			return;
		} catch (IOException e) {
			
			System.out.println("Eroare Startup");
			sc.close();
			return;
		}
		server.start();
		
		ServerSocket ssocket = null;
		try {
			ssocket = new ServerSocket(port, 0, InetAddress.getByName(IP));
		} catch (IOException e) {
			System.out.println("Combinatia IP - Port este invalida!");
			sc.close();
			return;
		}
		ssocket.close();
		
		
		while (true) {
			line = sc.nextLine();
			if (line.equals("quit")) {
				System.out.println("Are you sure you want to exit?(y/n)");
				line = sc.nextLine();
				if ("y".equals(line.trim()) || "Y".equals(line.trim())) {
					break;
				}
			}
		}

		Server.setStop(true);
		try {
			server.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		sc.close();
	}

}
