/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit;

import java.net.InetAddress;

/**
 *
 * @author andrei
 * will be used to get file list from user
 * and for quick search which makes the request to all online users
 */
public class SharedFilesRequest extends Request {
    public InetAddress ip;
    public int port;
}
