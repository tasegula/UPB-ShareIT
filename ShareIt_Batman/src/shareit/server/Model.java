/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import shareit.Validations;

/**
 *
 * @author andrei
 */
public class Model {
    private static Model instance;

    public final HashMap<Socket, ConnectedClient> clients = new HashMap<>();

    private final ArrayList<ConnectedClient> signedInClients = new ArrayList<>();
    private final HashMap<String, String> registeredClients = new HashMap<>();

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }
    
    private Model() {}
    
    public String updatePassword(String username, String password) {
        String error = Validations.validatePassword(password);
        if(error != null) return error;
        
        registeredClients.put(username, password);
        
        return null;
    }
    
    public String updateUsername(Socket socket, String oldUsername, String newUsername) {
        
        String error = Validations.validateUsername(newUsername);
        if (error != null) return error;
        
        error = registeredClients.get(newUsername);
        if (error != null) {
            System.err.println("Username already taken");
            return "Username already taken";
        }
        
        String password = registeredClients.get(oldUsername);
        if (password == null) {
            System.err.println("user not found for that username");
            return "User not found for that username";
        }
        
        registeredClients.remove(oldUsername);
        registeredClients.put(newUsername, password);
        
        ConnectedClient client = clients.get(socket);
        if (client == null) {
            System.err.println("user not found for that socket");
            return "User not found for that socket";
        }
        client.username = newUsername;
        
        return null;
    }

    public String signUpClient(String username, String password) {
        String error;

        error = Validations.validateUsername(username);
        if (error != null) {
            return error;
        }

        error = Validations.validatePassword(password);
        if (error != null) {
            return error;
        }

        if (registeredClients.get(username) != null) {
            return "Username taken";
        }

        registeredClients.put(username, password);

        return null;
    }

    public String signInClient(String username, String password, int listeningPort, ConnectedClient client) {
        String error;

        error = Validations.validateUsername(username);
        if (error != null) {
            return error;
        }

        error = Validations.validatePassword(password);
        if (error != null) {
            return error;
        }

        if (isSignedIn(username)) {
            return "Already signed in";
        }
        if (registeredClients.get(username) == null) {
            return "Unregistered user";
        }
        if (!registeredClients.get(username).equals(password)) {
            return "Wrong password";
        }

        client.username = username;
        client.listeningPort = listeningPort;
        signedInClients.add(client);

        return null;
    }

    // Returns true if it actually signed out someone
    public boolean signOutClient(String username) {
        for (int i = 0; i < signedInClients.size(); ++i) {
            if (signedInClients.get(i).username.equals(username)) {
                signedInClients.remove(i);
                return true;
            }
        }

        return false;
    }

    public boolean isSignedIn(String username) {
        for (ConnectedClient client : signedInClients) {
            if (client.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<ConnectedClient> getSignedInClients() {
        return signedInClients;
    }
    
    public HashMap<String, String> getAllUsers() {
        return registeredClients;
    }
}
