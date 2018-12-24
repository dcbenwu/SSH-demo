package coriant.cats.sbi;

import coriant.cats.sbi.ssh.SshClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class StreamPair implements IOPair {
    private final static Log log = LogFactory.getLog(SshClient.class);
    Reader is;
    Writer os;
    
    /** Creates a new instance of ReaderConsumer */
    public StreamPair(InputStream is, OutputStream os ) {
        this.is = new InputStreamReader( is );
        this.os = new OutputStreamWriter( os );
    }
    
    public Reader getReader() {
        return is;
    }
    
    public Writer getWriter() {
        return os;
    }
    
    /**
     * TODO evaluate if this is even needed
     */
    public void reset() {
        try {
            is.reset();
        }catch(IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }
    }
    
    public void close() {
        log.debug(" Try to close StreamPair's input and output steam...");
        try {
            if (is != null ) is.close();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (os != null) os.close();
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void free() {
        is = null;
        os = null;
    }

}
