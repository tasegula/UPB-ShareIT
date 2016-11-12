/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 *
 * @author andrei
 */
public class User implements Serializable {
    public String username;
    public InetAddress ip;
    public int port;
    public ArrayList<String> files;
    public boolean online;
    
    public User() {
        files = new ArrayList();
    }
    
    public User(String name, InetAddress ip, int port) {
        this.username = name;
        this.ip = ip;
        this.port = port;
        this.files = new ArrayList();
    }
    
    public String getName() {
        return username;
    }
    
    public void addSharedFiles(String path) {
        if (new File(path).exists()) {
            if (new File(path).isFile()) {
                if (!files.contains(path)) {
                    files.add(path);
                }
            } else {
                File dir = new File(path);
                for (final File fileEntry : dir.listFiles()) {
                    addSharedFiles(fileEntry.toString());
                }
            }
        } else {
            throw new Error("No such path");
        }
    }
    
    public ArrayList<String> getSharedFiles() {
        return files;
    }

    public String toString() {
        return username;
    }
}
