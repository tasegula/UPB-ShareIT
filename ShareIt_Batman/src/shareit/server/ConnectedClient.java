/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 *
 * @author andrei
 */
class ConnectedClient {
    public String username;
    public int listeningPort;
    
    public final Socket socket;
    public final SocketChannel socketChannel;
    public final ObjectInputStream inputStream;
    public final ObjectOutputStream outputStream;

    ConnectedClient(Socket socket) throws IOException {
        this.socket = socket;
        this.socketChannel = socket.getChannel();

        inputStream = new ObjectInputStream(socket.getInputStream());
        outputStream = new ObjectOutputStream(socket.getOutputStream());
    }
}
