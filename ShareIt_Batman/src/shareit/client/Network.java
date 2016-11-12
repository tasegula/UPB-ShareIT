/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package shareit.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import shareit.ChangePasswordRequest;
import shareit.ChangeUsernameRequest;
import shareit.FileListRequest;
import shareit.FileRequest;
import shareit.Request;
import shareit.SignInRequest;
import shareit.SignUpRequest;
import shareit.TargetedRequest;
import shareit.User;
import shareit.UserListRequest;
import shareit.Utils;
import shareit.server.Server;

/**
 *
 * @author andrei
 */
public class Network {
    private static final long selectTimeout = 50;

    private static Network instance;

    private ServerSocket listenSocket; // listen for incoming client connections
    private Socket serverConnSocket; // talking with the server
    private CountDownLatch latch = new CountDownLatch(1); // from waiting(1) to ready(0)
    private ObjectOutputStream serverOutput;
    private ObjectInputStream serverInput;
    private Selector selector = null;
    private Selector fileSelector = null;
    private final ConcurrentLinkedQueue<TargetedRequest> requestsToSend = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Request> requestsToServer = new ConcurrentLinkedQueue<>();
    private boolean leaveNetwork = false;

    private Network() {
        try {
            selector = Selector.open();
            fileSelector = Selector.open();
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            listenSocket = ssc.socket();
            listenSocket.bind(new InetSocketAddress(0));
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
            Client.getInstance().exit(false, "Failed to bind Network/ServerSocket");
        }
        Model.getInstance().setPort(listenSocket.getLocalPort());
    }

    public synchronized static Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }

        return instance;
    }
    
    public SignInRequest signIn(InetAddress serverIp, int port, SignInRequest req) throws ClassNotFoundException {
        
        try {
            SocketChannel sc = SocketChannel.open();
            serverConnSocket = sc.socket();
            serverConnSocket.connect(new InetSocketAddress(serverIp, port));

            serverOutput = new ObjectOutputStream(serverConnSocket.getOutputStream());
            serverInput = new ObjectInputStream(serverConnSocket.getInputStream());
        } catch (IOException ex) {
            req.solve(false, "Could not connecto to server");
            return req;
        }
        
        SignInRequest response = req;
        try {
            serverOutput.writeObject(req);
            response = (SignInRequest) serverInput.readObject();
        } catch (IOException ex) {
            response.solve(false, "Could not talk to server");
        }

        if (response.isSuccess) {
            Model.getInstance().setUsername(req.username);
            latch.countDown();
        }
        
        return response;
    }

    public static SignUpRequest signUp(InetAddress serverIp, int port, SignUpRequest request) throws ClassNotFoundException {
        Socket server;

        try {
            server = new Socket(serverIp, port);
        } catch (IOException ex) {
            request.solve(false, "Could not connect to server");
            return request;
        }

        SignUpRequest response = request;
        try {
            ObjectOutputStream networkOutput = new ObjectOutputStream(server.getOutputStream());
            ObjectInputStream networkInput = new ObjectInputStream(server.getInputStream());
            networkOutput.writeObject(request);
            response = (SignUpRequest)networkInput.readObject();
        } catch (IOException ex) {
            response.solve(false, "Could not talk to server");
        } finally {
            try {
                server.close();
            } catch (IOException ex) {}
        }

        return response;
    }

    private TargetedRequest createRequestForUsername(String username, Request request) {
        for (User peer : Model.getInstance().getPeers()) {
            if (peer.username.equals(username)) {
                return new TargetedRequest(peer.ip, peer.port, request);
            }
        }
        return null;
    }

    public void requestFile(FileRequest request) {
        TargetedRequest tr = createRequestForUsername(request.responderUsername, request);
        if (tr == null) {
            return;
        }

        requestsToSend.add(tr);
    }
    
    public void requestFiles(FileListRequest request) {
        TargetedRequest tr = createRequestForUsername(request.responderUsername, request);
        if (tr == null) {
            return;
        }

        requestsToSend.add(tr);
    }
    
    public void changeUsername(ChangeUsernameRequest req) {
        requestsToServer.add(req);
    }
    
    public void changePassword(ChangePasswordRequest req) {
        requestsToServer.add(req);
    }
    
    public void signOut() {
        leaveNetwork = true;
    }

    private void shutdown() {
        instance = null;
        try {
            listenSocket.close();
        } catch (IOException ex) {}
        try {
            serverConnSocket.close();
        } catch (IOException ex) {}
        try {
            serverOutput.close();
        } catch (IOException ex) {}
        try {
            serverInput.close();
        } catch (IOException ex) {}
    }
    
    private void sendServerRequests() {
        Request tr;

        while ((tr = requestsToServer.poll()) != null) {
            sendServerRequest(tr);
        }
    }
    
    private void sendServerRequest(Request request) {
        try {
            serverConnSocket.getChannel().keyFor(selector).cancel();
            selector.selectNow();
            serverConnSocket.getChannel().configureBlocking(true);
            
            serverOutput.writeObject(request);
            
            serverConnSocket.getChannel().configureBlocking(false);
            serverConnSocket.getChannel().register(selector, SelectionKey.OP_READ);
            
        } catch (IOException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendRequest(InetAddress ip, int port, Request request) {
        try {
            SocketChannel sc = SocketChannel.open(new InetSocketAddress(ip, port));
            Socket peerSocket = sc.socket();
            ObjectOutputStream outputStream = new ObjectOutputStream(peerSocket.getOutputStream());

            outputStream.writeObject(request);
            
            outputStream.flush();
            outputStream.close();
            peerSocket.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void sendResponse(String username, Request request) {
        for (User peer : Model.getInstance().getPeers()) {
            if (peer.username.equals(username)) {
                sendRequest(peer.ip, peer.port, request);
                return;
            }
        }
    }

    private void sendRequests() {
        TargetedRequest tr;

        while ((tr = requestsToSend.poll()) != null) {
            sendRequest(tr.ip, tr.port, tr.request);
        }
    }

    private void sendFileChunk(SelectionKey key) {
        SocketChannel sc = (SocketChannel)key.channel();
        FileRequest fr = (FileRequest)key.attachment();

        if (fr.inputFile == null) {
            if (!fr.openInputFile(fr.filename)) {
                removeFileRequest(key, fr);
                return;
            }
            fr.buffer = new byte[FileRequest.CHUNK_SIZE];
            fr.sizeInBuffer = 0;
        }

        int readBytes;
        try {
            readBytes = fr.inputFile.read(fr.buffer, fr.sizeInBuffer, FileRequest.CHUNK_SIZE - fr.sizeInBuffer);
        } catch (IOException ex) {
            removeFileRequest(key, fr);
            return;
        }

        if (readBytes >= 0) {
            fr.sizeInBuffer += readBytes;
        } else {
            if (fr.sizeInBuffer == 0) {
                //Terminat de trimis fisier
                removeFileRequest(key, fr);
                return;
            } else {}
        }

        ByteBuffer bb = ByteBuffer.allocate(fr.sizeInBuffer);
        bb.put(fr.buffer, 0, fr.sizeInBuffer);
        bb.rewind();

        int written = 0;
        try {
            key.cancel();
            fileSelector.selectNow();

            sc.configureBlocking(true);
            written = sc.write(bb);

            sc.configureBlocking(false);
           sc.register(fileSelector, SelectionKey.OP_WRITE, fr);
        } catch (IOException ex) {}

        if (written != 0) {
            for (int i = written; i < fr.sizeInBuffer; ++i) {
                fr.buffer[i - written] = fr.buffer[i];
            }
        }
        fr.sizeInBuffer -= written;
    }

    private void receiveFileChunk(SelectionKey key) {
        SocketChannel sc = (SocketChannel)key.channel();
        FileRequest fr = (FileRequest)key.attachment();

        if (fr.outputFile == null) {
            if (!fr.openOutputFile(Utils.addFilenameToPath(Model.getInstance().getDownloadDirectory(), fr.filename))) {
                removeFileRequest(key, fr);
                return;
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(FileRequest.CHUNK_SIZE);
        int readBytes;
        
        try {
            readBytes = sc.read(bb);
        } catch (IOException ex) {
            removeFileRequest(key, fr);
            return;
        }

        if (readBytes == -1) {
            // Transfer is done
            removeFileRequest(key, fr);
            return;
        }

        if (readBytes > 0) {
            try {
                fr.outputFile.write(bb.array(), 0, readBytes);
            } catch (IOException ex) {}
        }
    }

    private void removeFileRequest(SelectionKey key, FileRequest fr) {
        Socket sock = ((SocketChannel)key.channel()).socket();
        key.cancel();

        try {
            if (fr.inputFile != null) {
                fr.inputFile.close();
            }
            if (fr.outputFile != null) {
                fr.outputFile.close();
            }
        } catch (IOException ie) {}
        
        try {
            fileSelector.selectNow();
        } catch (IOException ex) {}
        try {
            sock.close();
        } catch (IOException ex) {}
    }
    
    public void run() throws ClassNotFoundException {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            listenSocket.getChannel().configureBlocking(false);
            serverConnSocket.getChannel().configureBlocking(false);

            listenSocket.getChannel().register(selector, SelectionKey.OP_ACCEPT);
            serverConnSocket.getChannel().register(selector, SelectionKey.OP_READ);
        } catch (IOException ie) {
            System.exit(-1);
        }
        
        System.out.println("Listening on " 
                + listenSocket.getLocalSocketAddress());
        
        // main loop for incoming connections
        while(true) {
            try {
                if (leaveNetwork) {
                    shutdown();
                    return;
                }
                selector.select(Network.selectTimeout);
                
                // Get the keys of the registered activity
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();

                while(it.hasNext()) {
                    SelectionKey key = (SelectionKey)it.next();
                    it.remove();

                    // incoming connection
                    if (key.isAcceptable()) {
                        processRequestFromPeer(listenSocket.accept());
                    }
                    else if (key.isReadable()) {
                        // process incoming data
                        SocketChannel sc = (SocketChannel)key.channel();

                        processInputFromServer(sc, key);
                    }
                }

                fileSelector.select(Network.selectTimeout);
                keys = fileSelector.selectedKeys();
                it = keys.iterator();

                int remainingUploadSlots = Model.getInstance().getUploadSlots();
                int remainingDownloadSlots = Model.getInstance().getDownloadSlots();

                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey)it.next();
                    it.remove();

                    if (key.isReadable()) {
                        if (remainingDownloadSlots > 0) {
                            --remainingDownloadSlots;
                            receiveFileChunk(key);
                        }
                    } else
                    if (key.isWritable()) {
                        if (remainingUploadSlots > 0) {
                            --remainingUploadSlots;
                            sendFileChunk(key);
                        }
                    }
                }

                sendRequests();
                sendServerRequests();
            } catch (IOException ex) {
                Logger.getLogger(Network.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    void processInputFromServer(SocketChannel sc, SelectionKey key) throws ClassNotFoundException {
        try {
            key.cancel();
            selector.selectNow();
            sc.configureBlocking(true);

            Object o = serverInput.readObject();

            if (o instanceof UserListRequest) {
                Controller.getInstance().updateUserList((UserListRequest)o);
            }
            if (o instanceof ChangeUsernameRequest) {
                Controller.getInstance().changeUsernameHandler((ChangeUsernameRequest)o);
            }
            if(o instanceof ChangePasswordRequest) {
                Controller.getInstance().changePasswordHandler((ChangePasswordRequest)o);
            }

            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, "Lost connection with server", ex);
            System.exit(-1);
        }
    }

    private void processRequestFromPeer(Socket peerSocket) throws ClassNotFoundException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(peerSocket.getInputStream());
            Object o = inputStream.readObject();

            if (o instanceof FileRequest) {
                FileRequest fr = (FileRequest)o;
                
                if (fr.isResolved) {
                    peerSocket.getChannel().configureBlocking(false);
                    peerSocket.getChannel().register(fileSelector, SelectionKey.OP_READ, fr);
                } else {
                    fr.solve(true, null);

                    User user = null;
                    ArrayList<User> users = Model.getInstance().getPeers();
                    for (int i = 0; i < users.size(); ++i) {
                        if (users.get(i).username.equals(fr.requesterUsername)) {
                            user = users.get(i);
                        }
                    }

                    SocketChannel newSc = SocketChannel.open(new InetSocketAddress(user.ip, user.port));
                    Socket newPeerSocket = newSc.socket();

                    ObjectOutputStream outputStream = new ObjectOutputStream(newPeerSocket.getOutputStream());
                    outputStream.writeObject(fr);
                    outputStream.flush();

                    newSc.configureBlocking(false);
                    newSc.register(fileSelector, SelectionKey.OP_WRITE, fr);

                    try {
                        inputStream.close();
                    } catch (IOException ex) {}
                    try {
                        peerSocket.close();
                    } catch (IOException ex) {}
                }
                return;
            }

            try {
                inputStream.close();
            } catch (IOException ex) {}
            try {
                peerSocket.close();
            } catch (IOException ex) {}
            inputStream = null;
            peerSocket = null;

            if (o instanceof FileListRequest) {
                FileListRequest flr = (FileListRequest)o;

                if (flr.isResolved) {
                    Controller c = Controller.getInstance();
                    c.addFileSearchResults(flr);
                } else {
                    Model m = Model.getInstance();
                    flr.fileList = m.getMatches(flr.search);
                    flr.solve(true, "");
                    sendResponse(flr.requesterUsername, flr);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
