/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.util.ArrayList;

/**
 *
 * @author florin
 */
public class FileListRequest extends PeerRequest {
    public ArrayList<FileInfo> fileList;
    public String search;
    public int code;
    
    public FileListRequest(String search, String requesterUsername, String responderUsername, int code) {
        super(requesterUsername, responderUsername);
        
        this.search = search;
        this.code = code;
    }
    
    public void setFiles(ArrayList<FileInfo> files) {
        fileList = new ArrayList();
        int nr = files.size();
        
        for(int i = 0; i < nr; i++) {
            fileList.add(files.get(i));
        }
    }
}
