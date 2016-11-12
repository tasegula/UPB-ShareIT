/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client.Tests;

import java.util.ArrayList;
import shareit.User;

/**
 *
 * @author andrei
 */
public class SharedFiles {
    public static void main(String args[]) {
    
        User u = new User();
        u.addSharedFiles("/opt/apps/fiddle");
        ArrayList<String> sf = u.getSharedFiles();
        
        if (sf.size() < 1) {
            throw new Error("Failed");
        } else {
            for (int i = 0; i < sf.size(); i++) {
                System.out.println(sf.get(i));
            }
        }
    
    }
}
