package wh.lanmsg;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import wh.lanmsg.server.MsgServer;

/**
 *
 * Created on Aug 14, 2015.
 * 
 * @author  Wes Hampson
 */
public class Main
{
    private static final String PROGRAM_TITLE   = "LANMsg";
    private static final String PROGRAM_VERSION = "0.1";
    private static final String PROGRAM_AUTHOR  = "Wes Hampson";
    
    public static void main(String[] args)
    {
        System.out.printf("%s %s\n", PROGRAM_TITLE, PROGRAM_VERSION);
        System.out.printf("Created by %s\n", PROGRAM_AUTHOR);
        
        if (args.length < 3) {
            showCommandLineUsage();
            return;
        }
        try {
            switch (args[0]) {
                case "--server":
                    MsgServer server = new MsgServer();
                    server.start(args[1], Integer.parseInt(args[2]));
                    break;
                case "--client":
                    client(args[1], Integer.parseInt(args[2]));
                    break;
                default:
                    showCommandLineUsage();
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void showCommandLineUsage()
    {
        System.out.printf("Usage: %s --server ip port\n", PROGRAM_TITLE);
        System.out.printf("       %s --client ip port\n", PROGRAM_TITLE);
    }
    
    private static void client(String host, int port)
            throws UnknownHostException, IOException
    {
        Debug dbg = Debug.getInstance();
        InetAddress bindAddr = InetAddress.getByName(host);
        Socket serverSocket = new Socket(bindAddr, port);
        OutputStream out = serverSocket.getOutputStream();
        
        BufferedReader in;
        in = new BufferedReader(new FileReader(FileDescriptor.in));
        
        String msg;
        byte[] data;
        while (true) {
            System.out.print("> ");
            msg = in.readLine();
            if (msg.equals("/quit")) {
                break;
            }
            data = msg.getBytes();
            
            out.write(1);       // (placeholder) protocol ver
            out.write(data.length);
            out.write(data);
        }
        serverSocket.close();
    }
}