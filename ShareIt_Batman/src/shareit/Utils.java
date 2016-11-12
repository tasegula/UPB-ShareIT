/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author andrei
 */
public abstract class Utils {
    public static InetAddress parseIp(String ipString) {
        try {
            return InetAddress.getByName(ipString);
        } catch (UnknownHostException ex) {}
        
        return null;
    }
    
    public static Integer parsePort(String portString) {
        try {
            return Integer.parseInt(portString);
        } catch(NumberFormatException e) {}
        
        return null;
    }

    public static String addFilenameToPath(Path path, String filename) {
        Path newPath = Paths.get(path.toString(), Paths.get(filename).getFileName().toString());
        return newPath.toString();
    }
}
