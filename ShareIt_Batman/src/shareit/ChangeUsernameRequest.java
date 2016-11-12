/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

/**
 *
 * @author andrei
 */
public class ChangeUsernameRequest extends Request {
    public String newUsername;
    
    public ChangeUsernameRequest(String username) {
        newUsername = username;
    }
}
