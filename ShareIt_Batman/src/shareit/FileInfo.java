/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author florin
 */
public class FileInfo implements Serializable {
    public String path;
    public long size;
    public String filetype;
    
    public FileInfo(String path) {
        File f = new File(path);
        this.size = f.length();
        this.path = path;
        
        String[] parts = path.split("\\.");
        if(parts.length == 1) {
            this.filetype = "Unknown";
        }
        else {
            this.filetype = parts[parts.length - 1];
        }
    }
}
