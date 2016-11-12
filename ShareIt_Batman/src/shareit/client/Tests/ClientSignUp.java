/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client.Tests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import shareit.SignUpRequest;
import shareit.client.Model;
import shareit.client.Network;

/**
 *
 * @author andrei
 */
public class ClientSignUp {

    public static void main(String[] args) throws ClassNotFoundException, UnknownHostException {
        
        Model client = Model.getInstance();
        String username = "florin";
        String password = "papa";
        InetAddress ip = InetAddress.getByName("127.0.0.1");
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientSignUp.class.getName()).log(Level.SEVERE, null, ex);
        }
        int port = 8080;
        
        client.setPort(port);
        client.setUsername(username);

        SignUpRequest signup = new SignUpRequest();
        signup.username = username;
        signup.password = password;
        
        Network.signUp(ip, port, signup);

    }
}
