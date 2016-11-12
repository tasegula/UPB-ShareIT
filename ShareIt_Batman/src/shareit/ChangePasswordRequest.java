/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

/**
 *
 * @author florin
 */
public class ChangePasswordRequest extends Request {
    public String newPassword;
    
    public ChangePasswordRequest(String passwd) {
        this.newPassword = passwd;
    }
}
