/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrei
 */
public class Client {
    private static Client instance;
    
    private Client() {}
    
    public synchronized static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void exit(final boolean isSuccess, final String errorMessage) {
        //TODO: the only way the client wil exit
    }
    
    private void run() {
        while (true) {
            Model.getInstance().setMainWindow(new MainWindow());
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new SignInWindow().setVisible(true);
                }
            });
            try {
                Network.getInstance().run();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
            Model.getInstance().shutdown();
            Controller.getInstance().shutdown();
        }
    }
    
    public static void main(String[] args) {
        Client.getInstance().run();
    }
}
