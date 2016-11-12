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
public class SignUpRequest extends Request {
    public String username;
    public String password;

    public SignUpRequest() {}

    public SignUpRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
