package coriant.cats.sbi;

import coriant.cats.sbi.ssh.SshClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

public abstract class ConsumerImpl implements Consumer {

    private final static Log log = LogFactory.getLog(ConsumerImpl.class);
    
    public static final int BUFFERMAX = 16 * 1024;
    
    StringBuffer buffer;
    
    IOPair pair;
    boolean stopRequested = false;
    boolean foundEOF = false;
    
    /** Creates a new instance of ConsumerImpl */
    public ConsumerImpl(IOPair pair) {
        this.pair = pair;
        buffer = new StringBuffer(1024);
    }
    
    /**
     * A few easy functions that a generic Consumer can do for its children
     */
    // TODO add some synch
    public void send(String str) throws IOException {
        String printStr = str;
        printStr = printStr.replaceAll("\\r", "\\\\r");
        printStr = printStr.replaceAll("\\n", "\\\\n");
        
        log.trace("Sending: >>>" + printStr + "<<<");
        Writer writer = pair.getWriter();
        writer.write( str );
        writer.flush();
    }
 
    public void send(int value) throws IOException {        
        log.trace("Sending: >>>" + value + "<<<");
        Writer writer = pair.getWriter();
        writer.write(value);
        writer.flush();
    }
    
    public void resume() {
        resume(-1);
    }
    
    public void clear() {
    	resume(buffer.length());
    }
    
    public void stop() {
        log.trace("Requesting stop");
        pair.close();
        stopRequested = true;
    }

    public void free() {
        pair.free();
        stopRequested = true;
    }

    public boolean foundEOF() {
        return foundEOF;
    }
}
