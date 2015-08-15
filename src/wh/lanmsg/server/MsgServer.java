package wh.lanmsg.server;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import wh.lanmsg.Debug;

/**
 * 
 * Created on Aug 14, 2015.
 * 
 * @author  Wes Hampson
 */
public class MsgServer
{
    private final ServerSocket serverSocket;
    private final List<Socket> clientSockets;
    private final Debug dbg;
    
    private volatile boolean isRunning;
    
    public MsgServer() throws IOException
    {
        serverSocket = new ServerSocket();
        clientSockets = new ArrayList<>();
        dbg = Debug.getInstance();
        isRunning = false;
    }
    
    public InetAddress getInetAddress()
    {
        return serverSocket.getInetAddress();
    }
    
    public int getLocalPort()
    {
        return serverSocket.getLocalPort();
    }
    
    public void start(String host, int port) throws IOException
    {
        start(InetAddress.getByName(host), port);
    }
    
    public void start(InetAddress bindAddr, int port) throws IOException
    {
        System.out.printf("Binding address...\n");
        serverSocket.bind(new InetSocketAddress(bindAddr, port));
        System.out.printf("Listening on port %d...\n", port);
        
        
        isRunning = true;
        System.out.printf("Server started.\n");
        
        Runnable cmdInputThread = new Runnable()
        {
            @Override
            public void run()
            {
                commandInput();
            }
        };
        new Thread(cmdInputThread).start();
        
        listen();
    }
    
    public void stop() throws IOException
    {
        isRunning = false;
        
        System.out.printf("Terminating client connections...\n");
        for (Socket s : clientSockets) {
            s.close();
        }
        serverSocket.close();
    }
    
    private void commandInput()
    {
        BufferedReader in;
        String cmd;
        
        in = new BufferedReader(new FileReader(FileDescriptor.in));
        try {
            while (isRunning) {
                cmd = in.readLine();
                switch (cmd) {
                    case "/quit":
                    case "/stop":
                        System.out.printf("Stopping server...\n");
                        stop();
                        break;
                    default:
                        System.out.printf("Error: unrecognized command\n");
                }
            }
        } catch (IOException ex) {
            System.out.printf("An error has occured [%s]",
                    ex.getClass().getName());
        }
    }
    
    private void listen() throws IOException
    {
        System.out.printf("Now accepting new connections.\n");
        try {
            while (isRunning) {
                final Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            receive(clientSocket);
                        } catch (IOException ex) {
                            System.err.printf(
                                    "Error: client failed to connect [%s]\n",
                                    ex.getClass().getName());
                        }
                    }
                };
                new Thread(r).start();
            }
        } catch (SocketException ex) {
            if (!isRunning) {
                // to be expected when server has been stopped
                System.out.printf("Server stopped.\n");
            } else {
                throw ex;
            }
        }
    }
    
    private void receive(Socket s) throws IOException
    {
        System.out.printf("New connection established. [%s:%d]\n",
                s.getInetAddress().getHostAddress(), s.getPort());
        
        InputStream in = s.getInputStream();
        
        int protocolVer, sizeOfData;
        byte[] data;
        try {
            while (isRunning) {
                protocolVer = in.read();
                if (protocolVer == -1) {
                    System.out.println(
                            "Connection terminated by foreign host.\n");
                    break;
                }
                sizeOfData = in.read();
                data = new byte[sizeOfData];
                in.read(data);
                System.out.printf("Message received.\n");
                System.out.printf("    protocol ver: %d\n", protocolVer);
                System.out.printf("    size of data: %d\n", sizeOfData);
                System.out.printf("    data dump:\n");
                System.out.printf("    %s\n",
                        dbg.hexDump(data).replaceAll("\n", "\n    "));
            }
        } catch (SocketException ex) {
            if (!isRunning) {
                // to be expected when server has been stopped
                System.out.printf("Connection terminated.\n");
            } else {
                throw ex;
            }
        }
    }
}