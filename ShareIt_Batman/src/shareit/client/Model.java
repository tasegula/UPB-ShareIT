/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import shareit.FileInfo;
import shareit.User;

/**
 *
 * @author andrei
 */
public class Model implements Serializable {
    
    private final User user = new User();
    private final ArrayList<String> sharedFiles = new ArrayList<>();
    private static Model instance = null;
    private MainWindow mainWindow;
    private ArrayList<User> peers = new ArrayList<>();
    private ArrayList<String> downloadedFilelists = new ArrayList<>();
    private ArrayList<String> allUsers = new ArrayList();
    private Path downloadDirectory;
    private int downloadSlots = 1;
    private int uploadSlots = 1;
    private String logFile = ".actionHistoryLog";
    private int connectionTime = 600;
    private String sharedFilesDiskLocation = ".sharedFilesLog.log";
    private String location = new String("");
    private String description = new String("");
    
    private Model() {
        downloadDirectory = Paths.get("").toAbsolutePath();
    }

    public void shutdown() {
        instance = null;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getLocation() {
        return this.location;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public synchronized void addDownloadedFilelist(String username) {
        for (String currentUsername: downloadedFilelists) {
            if (currentUsername.equals(username)) {
                return;
            }
        }
        downloadedFilelists.add(username);
    }
    
    public synchronized void deleteDownloadedFilelist(String username) {
        downloadedFilelists.remove(username);
    }
    
    public synchronized void clearDownloadedFilelist() {
        downloadedFilelists.clear();
    }
    
    public synchronized ArrayList<String> getDownloadedFilelist() {
        return downloadedFilelists;
    }
    
    public synchronized void setDownloadSlots(int slots) {
        if (slots < 0) {
            slots = 0;
        }
        downloadSlots = slots;
    }

    public synchronized int getDownloadSlots() {
        return downloadSlots;
    }
    
    public synchronized void setUploadSlots(int slots) {
        if (slots < 0) {
            slots = 0;
        }
        uploadSlots = slots;
    }

    public synchronized int getUploadSlots() {
        return uploadSlots;
    }

    public synchronized void setDownloadDirectory(String dl) {
        downloadDirectory = Paths.get(dl);
    }

    public synchronized Path getDownloadDirectory() {
        return downloadDirectory;
    }

    public synchronized String getLogFile() {
        return logFile;
    }
    
    public synchronized void setLogFile(String file) {
        logFile = file;
    }
    
    public synchronized void changeUsername(String newUsername) {
        user.username = newUsername;
    }
    
    public synchronized static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }
    
    public void setConnectionTime(int connectionTime) {
        this.connectionTime = connectionTime;
    }
    
    public int getConnectionTime() {
        return this.connectionTime;
    }

    public synchronized void setPeers(ArrayList<User> peers) {
        this.peers = peers;
    }
    
    public synchronized ArrayList<User> getPeers() {
        return peers;
    }
    
    public synchronized void setPort(int port) {
        user.port = port;
    }
    
    public synchronized void setUsername(String username) {
        user.username = username;
    }
    
    public synchronized void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        if (mainWindow != null) {
            loadSharedFilesFromLog();
            mainWindow.addSharedFiles(sharedFiles);
        }
    }
    
    public synchronized MainWindow getMainWindow() {
        return this.mainWindow;
    }
    
    public synchronized String getUsername() {
        return user.username;
    }

    public synchronized int getPort() {
        return user.port;
    }
            
    public synchronized void addSharedFiles(ArrayList<String> files) {
        int size = files.size();
        for(int i = 0; i < size; i++) {
            if (!sharedFiles.contains(files.get(i))) {
                sharedFiles.add(files.get(i));
            }
        }
        writeSharedFilesLog2Disk();
    }
    
    private void writeSharedFilesLog2Disk() {
        int size = sharedFiles.size();
        File file = new File(sharedFilesDiskLocation);
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            for(int i = 0; i < size; i++) {
                bw.write(sharedFiles.get(i));
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void loadSharedFilesFromLog() {
        
        try {
            File file = new File(sharedFilesDiskLocation);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {}
        
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(sharedFilesDiskLocation));
            String line;
            while ((line = br.readLine()) != null) {
               sharedFiles.add(line);
            }
            br.close();
        } catch (IOException ex) {}
    }
    
    private void deleteSharedFileLog() {
        File file = new File(sharedFilesDiskLocation);
        file.delete();
        
        try {
	    file = new File(sharedFilesDiskLocation);
	    file.createNewFile();
    	} catch (IOException e) {}
    }
    
    void clearSharedFiles() {
        deleteSharedFileLog();
        sharedFiles.clear();
    }
    
    public void removeSharedFile(String path) {
        sharedFiles.remove(path);
    }
    
    public synchronized ArrayList<String> getSharedFiles() {
        return sharedFiles;
    }
    
    public synchronized ArrayList<FileInfo> getMatches(String pattern) {
        int nr_files = sharedFiles.size();
        ArrayList<FileInfo> result = new ArrayList<FileInfo>();
        
        if (pattern == null || pattern.isEmpty()) {
            pattern = "(.*)";
        }
        Pattern p = Pattern.compile(pattern);
        
        for(int i = 0; i < nr_files; i++) {
            Matcher m = p.matcher(sharedFiles.get(i));
            if(m.find()) {
                result.add(new FileInfo(sharedFiles.get(i)));
            }
        }
        
        return result;
    }
    
    public synchronized ArrayList<User> getMatchingUsers(String search) {
        ArrayList<User> result = new ArrayList();
        int online_users = peers.size();
        
        for(int i = 0; i < online_users; i++) {
            if(peers.get(i).username.contains(search)) {
                result.add(peers.get(i));
            }
        }
        
        if(result.size() == 0) {
            int user_nr = allUsers.size();
            for(int i = 0; i < user_nr; i++) {
                if(allUsers.get(i).contains(search)) {
                    User u = new User(allUsers.get(i), null, 0);
                    u.online = false;
                    result.add(u);
                }
            }
        }
        
        return result;
    }

    public synchronized void save() {
        //TODO
    }
    
}
