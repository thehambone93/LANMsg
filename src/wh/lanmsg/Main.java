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
import wh.lanmsg.io.MessageDataOutputStream;
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
    private static final Debug DBG = Debug.getInstance();
    
    public static void main(String[] args)
    {
        System.out.printf("%s %s\n", PROGRAM_TITLE, PROGRAM_VERSION);
        System.out.printf("Created by %s\n", PROGRAM_AUTHOR);
        
        MessageDataOutputStream out = new MessageDataOutputStream(4);
        out.seek(5);
        out.write((byte) 'D');
        out.skip(1);
        out.write((byte) 'E');
        out.skip(-5);
        out.write((byte) 'C');
        DBG.printHexDump(out.toArray());
        
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
        final int VER_MAJ     = 0;
        final int VER_MIN     = 1;
        final int VER_SUBMIN  = 0;
        final int VER_BUILD   = 0;
        final int PROTO_VER   = (VER_MAJ & 0xFF) << 0
                | (VER_MIN & 0xFF) << 8
                | (VER_SUBMIN & 0xFF) << 16
                | (VER_SUBMIN & 0xFF) << 24;
        final int HEADER_SIZE = 0x30;
        final short TYPE_TEXT   = 1;
        final short G_BCAST     = 1 << 2;
        final byte  ENC_UTF8    = 1;
        
        InetAddress bindAddr;
        Socket serverSocket;
        BufferedReader in;
        OutputStream out;
        MessageDataOutputStream headerBuf;
        MessageDataOutputStream dataBuf;
        
        bindAddr = InetAddress.getByName(host);
        serverSocket = new Socket(bindAddr, port);
        in = new BufferedReader(new FileReader(FileDescriptor.in));
        out = serverSocket.getOutputStream();
        headerBuf = new MessageDataOutputStream(HEADER_SIZE);
        dataBuf = new MessageDataOutputStream();
        
        String msg;
        byte[] header;
        byte[] data;
        
        while (true) {
            System.out.print("> ");
            msg = in.readLine();
            if (msg.equals("/quit")) {
                break;
            }
            // create data packet
            dataBuf.write("DATA".getBytes());
            dataBuf.writeInt(msg.length() + 1);
            dataBuf.write(ENC_UTF8);
            dataBuf.write(msg.getBytes("UTF-8"));
            data = dataBuf.toArray();
            
            // create header packet
            headerBuf.write("HEAD".getBytes());
            headerBuf.writeInt(PROTO_VER);
            headerBuf.writeShort(TYPE_TEXT);
            headerBuf.writeShort(G_BCAST);
            headerBuf.writeShort((short) 0);
            headerBuf.skip(2);
            headerBuf.writeLong(System.currentTimeMillis());
            headerBuf.writeInt(0x0C4A);                       // sender id
            headerBuf.writeInt(0x2F6D);                       // recipient id
            headerBuf.writeInt(data.length);
            header = headerBuf.toArray();
            
            System.out.printf("Transmitting...\n");
            DBG.printHexDump(header);
            out.write(header);
            
            System.out.printf("\nTransmitting...\n");
            DBG.printHexDump(data);
            out.write(data);
        }
        serverSocket.close();
    }
}