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
 */
public class TargetedRequest {
    public final InetAddress ip;
    public final int port;
    public final Request request;

    public TargetedRequest(InetAddress ip, int port, Request request) {
        this.ip = ip;
        this.port = port;
        this.request = request;
    }
}
