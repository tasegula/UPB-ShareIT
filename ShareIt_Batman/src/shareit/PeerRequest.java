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
public abstract class PeerRequest extends Request {
    public String requesterUsername;
    public String responderUsername;

    public PeerRequest(String requesterUsername, String responderUsername) {
        this.requesterUsername = requesterUsername;
        this.responderUsername = responderUsername;
    }
}
