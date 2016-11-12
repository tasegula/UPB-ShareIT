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
public abstract class Validations {
    public static String validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return "Empty username";
        }
        for (int i = 0; i < username.length(); ++i) {
            if (!Character.isLetterOrDigit(username.charAt(i))) {
                return "Username can only have letters and digits";
            }
        }
        return null;
    }
    
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Empty password";
        }
        return null;
    }
}
