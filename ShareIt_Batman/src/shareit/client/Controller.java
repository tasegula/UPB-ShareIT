/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import shareit.ChangePasswordRequest;
import shareit.ChangeUsernameRequest;
import shareit.FileListRequest;
import shareit.FileRequest;
import shareit.SignInRequest;
import shareit.SignUpRequest;
import shareit.User;
import shareit.UserListRequest;
import shareit.Utils;
import shareit.Validations;

/**
 *
 * @author andrei
 */
public class Controller {
    private static Controller instance;
    public int searchCode;
    
    private Controller() {
        searchCode = -1;
    }

    public void shutdown() {
        instance = null;
    }
    
    public synchronized int getCurrentCode() {
        return searchCode;
    }
    
    public synchronized int getNextCode() {
        searchCode++;
        return searchCode;
    }
    
    public synchronized static Controller getInstance() {
        if (instance == null) {
            instance = new Controller();
        }
        return instance;
    }
    
    public void updateHistoryTab(final JList historyResultsList) {
        ArrayList<Object> log = getLogItems();
        Model.getInstance().getMainWindow().updateHistory(log);
    }
    
    public void sendSearchUsersCommand(final String search) {
        new SwingWorker<Boolean, Boolean>() {
            UserListRequest response = new UserListRequest();
            
            @Override
            protected Boolean doInBackground() throws Exception {
                Model m = Model.getInstance();
                Network n = Network.getInstance();
                Controller c = Controller.getInstance();
                                
                ArrayList<User> result = m.getMatchingUsers(search);
                
                if (result.size() == 0) {
                    response.isSuccess = false;
                    response.message = "Users not found";
                } 
                else if(!result.get(0).online) {
                    response.isSuccess = false;
                    response.users = result;
                    response.message = "Offline users found";
                }
                else {
                    response.isSuccess = true;
                    response.users = result;
                }
                
                if (search.isEmpty()) {
                    return true;
                }
                
                for (User u : result) {
                    if (u.username.equals(search)) {
                        response.users = new ArrayList<>();
                        response.users.add(u);
                        response.isSuccess = true;
                        return true;
                    }
                }
                
                response.isSuccess = false;
                response.message = "User not found";
                return true;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Model.getInstance().getMainWindow().clearUsersListResults();
                Model.getInstance().getMainWindow().goToUsersTab();
                Model.getInstance().getMainWindow().updateUserList(response);
            }
        }.execute();
    }
    
    public void sendSearchCommand(final String search, final String wantedUsername) {
        Model.getInstance().getMainWindow().clearFileResults();
        Model.getInstance().getMainWindow().goToSearchTab();
            
        Model m = Model.getInstance();
        Network n = Network.getInstance();
        Controller c = Controller.getInstance();

        final ArrayList<User> users = m.getPeers();
        final int searchCode = c.getNextCode();

        for(User u : users) {
            if (wantedUsername.isEmpty() || wantedUsername.equals(u.username)) {
                n.requestFiles(new FileListRequest(
                        search,
                        Model.getInstance().getUsername(),
                        u.username,
                        searchCode
                ));
            }
        }
    }
    
    public void addFileSearchResults(final FileListRequest fl) {
        saveLogItem(fl);
        if(fl.code != getCurrentCode()) return;
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Model.getInstance().getMainWindow().updateFileResults(fl);
            }
        });
    }
    
    public void writeLogs(ArrayList<Object> log) {
        try {
            FileOutputStream out = new FileOutputStream(Model.getInstance().getLogFile());
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(log);
            oout.flush();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeLogItems(int[] idxs) {
        ArrayList<Object> log = getLogItems();
        int size = idxs.length;
        for(int i = 0; i < size; i++) {
            log.remove(idxs[i]-i);
        }
        
        writeLogs(log);
    }
    
    public void saveLogItem(Object item) {
        ArrayList<Object> log = getLogItems();
        log.add(item);
        writeLogs(log);
    }
    
    
    public ArrayList<Object> getLogItems() {
        
        ObjectInputStream ois;
        ArrayList<Object> log;
        try {
            ois = new ObjectInputStream(new FileInputStream(Model.getInstance().getLogFile()));
        } catch (IOException ex) {
            log = new ArrayList<>();
            return log;
        }
        
        try {
            log = (ArrayList<Object>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            log = new ArrayList<>();
        }
        
        return log;
    }
    
    public void clearLog() {
        PrintWriter pw;
        try {
            pw = new PrintWriter(Model.getInstance().getLogFile());
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addRecursively(String path, ArrayList<String> rez) {
        File current = new File(path);
        
        if (current.isDirectory()) {
            ArrayList<String> names = new ArrayList<String>(Arrays.asList(current.list()));
            for (int i = 0; i < names.size(); ++i)
                addRecursively(path + "/" + names.get(i), rez);
        } else {
            String fullpath = current.getAbsolutePath();
            rez.add(fullpath);
        }
    }
    
    public void addSharedResultsList(final String path) {
        new SwingWorker<Boolean, Boolean>() {
            ArrayList<String> sharedFiles = new ArrayList();
            
            @Override
            protected Boolean doInBackground() throws Exception {
                addRecursively(path, sharedFiles);
                return true;
            }
            
            @Override
            protected void done() {
                Model.getInstance().getMainWindow().addSharedFiles(sharedFiles);
                Model.getInstance().addSharedFiles(sharedFiles);
            }
        }.execute();
    }
    
    public void signIn(
            final SignInWindow signInWindow,
            final String username,
            final String password,
            final String ipString,
            final String portString
    ) {
        new SwingWorker<Boolean, Boolean>() {
            SignInRequest response;

            @Override
            protected Boolean doInBackground() throws Exception {
                response = new SignInRequest();

                String error = Validations.validateUsername(username);
                if (error != null) {
                    response.solve(false, error);
                    return false;
                }

                error = Validations.validatePassword(password);
                if (error != null) {
                    response.solve(false, error);
                    return false;
                }

                InetAddress ip = Utils.parseIp(ipString);
                Integer port = Utils.parsePort(portString);

                if (ip == null || port == null) {
                    response.solve(false, "Invalid ip/port");
                    return false;
                }

                response = Network.getInstance().signIn(
                        ip,
                        port,
                        new SignInRequest(username, password, Model.getInstance().getPort())
                );

                return true;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (response.isSuccess) {
                    signInWindow.dispose();
                    Model.getInstance().getMainWindow().setVisible(true);
                    return;
                }

                JOptionPane.showMessageDialog(
                          signInWindow,
                        response.message,
                        "Sign In Error",
                        JOptionPane.ERROR_MESSAGE
                );
                signInWindow.setEnabled(true);
            }

        }.execute();
    }
    
    public void signOut() {
        Model.getInstance().getMainWindow().dispose();
        Model.getInstance().setMainWindow(null);

        Network.getInstance().signOut();
    }
    
    public static void signUp(
            final SignUpWindow signUpWindow,
            final String username,
            final String password,
            final String ipString,
            final String portString
    ) {
        new SwingWorker<Boolean, Boolean>() {
            SignUpRequest response;

            @Override
            protected Boolean doInBackground() throws Exception {
                response = new SignUpRequest();
                
                String error = Validations.validateUsername(username);
                if (error != null) {
                    response.solve(false, error);
                    return false;
                }

                error = Validations.validatePassword(password);
                if (error != null) {
                    response.solve(false, error);
                    return false;
                }

                InetAddress ip = Utils.parseIp(ipString);
                Integer port = Utils.parsePort(portString);

                if (ip == null || port == null) {
                    response.solve(false, "Invalid ip/port");
                    return false;
                }

                response = Network.signUp(
                        ip,
                        port,
                        new SignUpRequest(username, password)
                );

                return true;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (response.isSuccess) {
                    signUpWindow.dispose();
                    new SignInWindow().setVisible(true);
                    return;
                }
                
                JOptionPane.showMessageDialog(
                        signUpWindow,
                        response.message,
                        "Sign Up Error",
                        JOptionPane.ERROR_MESSAGE
                );
                signUpWindow.setEnabled(true);
            }
            
        }.execute();
    }
    
    public void downloadFile(String path, String username) {
        FileRequest req = new FileRequest(path, Model.getInstance().getUsername(), username);
        Network.getInstance().requestFile(req);
        saveLogItem(req);
    }
    
    public void changeUsername(String newUsername) {
        String oldUsername = Model.getInstance().getUsername();
        ChangeUsernameRequest request = new ChangeUsernameRequest(newUsername);
        
        if (!oldUsername.equals(newUsername)) {
            Network.getInstance().changeUsername(request);
        } else {
            request.solve(false, "You already have that username!");
            Model.getInstance().getMainWindow().updateUsername(request);
        }
    }
    
    public void changeUsernameHandler(ChangeUsernameRequest req) {
        if (req.isResolved && req.isSuccess) {
            Model.getInstance().changeUsername(req.newUsername);
            saveLogItem(req);
        }
        Model.getInstance().getMainWindow().updateUsername(req);
    }
    
    public void changePassword(String newPassword) {
        ChangePasswordRequest req = new ChangePasswordRequest(newPassword);
        Network.getInstance().changePassword(req);
    }
    
    public void changePasswordHandler(ChangePasswordRequest req) {
        Model.getInstance().getMainWindow().updatePassword(req);
        saveLogItem(req);
    }
     
    public void changeLocation(String newLocation) {
        
    }
    public void changeDescription(String newDescription) {
        
    }
    
    public void saveInfo(String username,
                        String password,
                        String location,
                        String description) {
        
        if(!username.isEmpty()) {
            changeUsername(username);
        }
        if(!password.isEmpty()) {
            changePassword(password);
        }
        if(!location.isEmpty()) {
            changeLocation(location);
        }
        if(!description.isEmpty()) {
            changeDescription(description);
        }     
    }
    
    public void updateUserList(UserListRequest response) {        
        if (response.isResolved) {
            if (response.isSuccess) {
                final ArrayList<User> peers = response.users;
                final String myUsername = Model.getInstance().getUsername();

                for (int i = 0; i < peers.size(); ++i) {
                    if (peers.get(i).username.equals(myUsername)) {
                        peers.remove(i);
                        break;
                    }
                }

                Collections.sort(peers, new Comparator<User>() {
                    @Override
                    public int compare(User u1, User u2) {
                        return u1.username.compareTo(u2.username);
                    }
                });

                Model.getInstance().setPeers(peers);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        UserListRequest userList = new UserListRequest();
                        userList.users = peers;
                        userList.solve(true, null);
                        Model.getInstance().getMainWindow().updateUserList(userList);
                        saveLogItem(userList);
                    }
                });
            } else {
                Client.getInstance().exit(false, "Got unsuccesful userlist request");
            }
        } else {
            Client.getInstance().exit(false, "Got unresolved userlist request");
        }
    }
}
