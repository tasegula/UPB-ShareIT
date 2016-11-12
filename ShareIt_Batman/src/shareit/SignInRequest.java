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
public class SignInRequest extends Request {
    public String username;
    public String password;
    public int listeningPort;

    public SignInRequest() {}

    public SignInRequest(String username, String password, int listeningPort) {
        this.username = username;
        this.password = password;
        this.listeningPort = listeningPort;
    }
}
