/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import shareit.ChangePasswordRequest;
import shareit.ChangeUsernameRequest;
import shareit.SignInRequest;
import shareit.SignUpRequest;
import shareit.User;
import shareit.UserListRequest;
import shareit.Utils;

/**
 *
 * @author andrei
 */
public class Server {
    private ServerSocket serverSocket;
    private final Selector selector;
    private boolean doBroadcastSignedInClients;
    
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        if (args.length < 1) {
            System.err.println("No port argument");
            System.exit(-1);
        }

        new Server(Utils.parsePort(args[0])).run();

    }
    
    public Server(int port) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        serverSocket = ssc.socket();
        
        for (; true; ++port) {
            try {
                serverSocket.bind(new InetSocketAddress(port));
                break;
            } catch(IOException ie) {}
        }
        
        selector = Selector.open();
    }

    public void run() throws IOException, ClassNotFoundException {   
        // set it to non-blocking, so we can use select
        serverSocket.getChannel().configureBlocking(false);

        // Register the ServerSocketChannel, so we can listen for
        // incoming connections
        serverSocket.getChannel().register(selector, SelectionKey.OP_ACCEPT);
        
        System.out.println("Listening on " 
                + serverSocket.getLocalSocketAddress());
        
        // main loop for incoming connections
        while(true) {
            if(selector.select() == 0) {
                continue;
            }
            
            // Get the keys of the registered activity
            Set keys = selector.selectedKeys();
            Iterator it = keys.iterator();
            
            while(it.hasNext()) {
                SelectionKey key = (SelectionKey)it.next();
                it.remove();
                
                // incoming connection
                if (key.isAcceptable()) {
                    Socket s = serverSocket.accept();
                    addNewClient(s);
                }
                else if (key.isReadable()) {
                    // process incoming data
                    SocketChannel sc = (SocketChannel)key.channel();
                    processInput(sc, key);
                }
            }

            broadcastSignedInClients();
        }
    }

    void addNewClient(Socket clientSocket) {
        try {
            ConnectedClient client = new ConnectedClient(clientSocket);

            client.socketChannel.configureBlocking(false);
            client.socketChannel.register(selector, SelectionKey.OP_READ);

            Model.getInstance().clients.put(client.socket, client);
        } catch (EOFException ex) {
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void removeClient(ConnectedClient client) {
        Model.getInstance().clients.remove(client.socket);
        if (Model.getInstance().signOutClient(client.username)) {
            scheduleBroadcastSignedInClients();
        }

        try {
            client.inputStream.close();
        } catch (IOException ex) {}
        try {
            client.outputStream.close();
        } catch (IOException ex) {}
        try {
            client.socket.close();
        } catch (IOException ex) {}
    }

    void scheduleBroadcastSignedInClients() {
        doBroadcastSignedInClients = true;
    }

    ArrayList<String> getUsers(Set<String> s) {
        ArrayList<String> result = new ArrayList();
        Iterator it = s.iterator();
        
        while(it.hasNext()) {
            result.add((String)it.next());
        }
        
        return result;
    }
    
    void broadcastSignedInClients() {
        if (!doBroadcastSignedInClients) {
            return;
        }
        doBroadcastSignedInClients = false;
        
        ArrayList<ConnectedClient> connectedClients = Model.getInstance().getSignedInClients();
        HashMap<String, String> allClients = Model.getInstance().getAllUsers();
        ArrayList<User> users = new ArrayList<>();

        for (int i = 0; i < connectedClients.size(); ++i) {
            User u = new User(
                    connectedClients.get(i).username,
                    connectedClients.get(i).socket.getInetAddress(),
                    connectedClients.get(i).listeningPort
            );
            u.online = true;
            users.add(u);
        }

        UserListRequest req = new UserListRequest();
        req.users = users;
        req.allUsers = getUsers(allClients.keySet());
        req.solve(true, null);

        for (int i = 0; i < connectedClients.size(); ++i) {
            try {
                connectedClients.get(i).socketChannel.keyFor(selector).cancel();
                selector.selectNow();
                connectedClients.get(i).socketChannel.configureBlocking(true);

                connectedClients.get(i).outputStream.writeObject(req);
                connectedClients.get(i).socketChannel.configureBlocking(false);
                connectedClients.get(i).socketChannel.register(selector, SelectionKey.OP_READ);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Write userlist req to all users", ex);
            }
        }
    }
    
    void processInput(SocketChannel sc, SelectionKey key) throws ClassNotFoundException {
        ConnectedClient client = null;
        
        try {
            key.cancel();
            selector.selectNow();
            sc.configureBlocking(true);

            client = Model.getInstance().clients.get(sc.socket());

            try {
                Object o = client.inputStream.readObject();
                boolean wasNoError = true;

                if (o instanceof SignUpRequest) {
                    wasNoError = processSignUp(client, (SignUpRequest)o);
                } else if (o instanceof SignInRequest) {
                    wasNoError = processSignIn(client, (SignInRequest)o);
                } else if (o instanceof ChangeUsernameRequest) {
                    wasNoError = processChangeUsername(client, (ChangeUsernameRequest)o);
                } else if (o instanceof ChangePasswordRequest) {
                    wasNoError = processChangePassword(client, (ChangePasswordRequest)o);
                }else {
                    System.err.println("Invalid request");
                    System.exit(-1);
                }
                
                if (wasNoError) {
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                } else {
                    removeClient(client);
                }
            } catch(EOFException ex) {
                // Client has exited
                removeClient(client);
            }
        } catch (IOException ex) {
            if (client == null) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } else {
                removeClient(client);
            }
        }
    }

    boolean processSignUp(ConnectedClient client, SignUpRequest request) {
        String error = Model.getInstance().signUpClient(request.username, request.password);

        if (error == null) {
            request.solve(true, null);
        } else {
            request.solve(false, error);
        }

        try {
            client.outputStream.writeObject(request);
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    boolean processChangePassword(ConnectedClient client, ChangePasswordRequest request) {
        String error = Model.getInstance().updatePassword(client.username, request.newPassword);
        if(error == null) {
            request.solve(true, null);
        }
        else {
            request.solve(false, error);
        }
        
        try {
            client.outputStream.writeObject(request);
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
    
    boolean processChangeUsername(ConnectedClient client, ChangeUsernameRequest request) {
        
        String oldUsername = client.username;
        String newUsername = request.newUsername;
        String error = Model.getInstance().updateUsername(client.socket, oldUsername, newUsername);

        if (error == null) {
            request.solve(true, null);
        } else {
            request.solve(false, error);
        }

        try {
            client.outputStream.writeObject(request);
        } catch (IOException ex) {
            return false;
        }

        if (request.isSuccess) {
            scheduleBroadcastSignedInClients();
        }

        return true;
    }
    
    boolean processSignIn(ConnectedClient client, SignInRequest request) {
        String error = Model.getInstance().signInClient(
                request.username,
                request.password,
                request.listeningPort,
                client
        );

        if (error == null) {
            request.solve(true, null);
        } else {
            request.solve(false, error);
        }

        try {
            client.outputStream.writeObject(request);
        } catch (IOException ex) {
            return false;
        }

        if (request.isSuccess) {
            scheduleBroadcastSignedInClients();
        }

        return true;
    }
}
