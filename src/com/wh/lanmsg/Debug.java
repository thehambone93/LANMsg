package com.wh.lanmsg;

/**
 * Functions useful for debugging and analysis purposes.
 * 
 * Created on Aug 14, 2015.
 * 
 * @author  Wes Hampson
 */
public class Debug
{
    private static final char UNPRINTABLE_CHAR_PLACEHOLDER = '.';
    
    private static Debug instance = null;
    
    public static Debug getInstance()
    {
        if (instance == null) {
            instance = new Debug();
        }
        return instance;
    }
    
    protected Debug()
    {
        // singleton class; hide default constructor from external classes
    }
    
    /**
     * Generates a formatted hex dump of the specified byte array, then prints
     * the hex dump to {@code stdout}. This is a convenience method for
     * {@link #hexDump(byte[])}.
     * 
     * @param buf the byte buffer containing the data to be outputted
     */
    public void printHexDump(byte[] buf)
    {
        System.out.printf("%s\n", hexDump(buf));
    }
    
    /**
     * Generates a formatted hex dump of the specified byte array.
     * 
     * @param buf the byte buffer containing the data to be outputted
     * @return the hex dump as a String
     */
    public String hexDump(byte[] buf)
    {
        int off, len, padLen;
        char c;
        StringBuilder outputBuf;
        
        outputBuf = new StringBuilder();
        
        for (off = 0; off < buf.length; off += len) {
            outputBuf.append(String.format("%08x  ", off));
            
            len = Math.min(buf.length - off, 16);
            for (int i = 0; i < len; i++) {
                outputBuf.append(String.format("%02x ", buf[off + i]));
            }
            
            padLen = (16 - len) * 3;
            for (int i = 0; i < padLen; i++) {
                outputBuf.append(' ');
            }
            
            outputBuf.append(' ');
            for (int i = 0; i < len; i++) {
                c = (char) buf[off + i];
                if (c > 0x1F && c < 0x7F) {
                   outputBuf.append(c);
                } else {
                    outputBuf.append(UNPRINTABLE_CHAR_PLACEHOLDER);
                }
            }
            
            if (buf.length - off > 16) {
                outputBuf.append('\n');
            }
        }
        
        return (outputBuf.toString());
    }
}