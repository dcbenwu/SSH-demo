package coriant.cats.sbi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Reader;

public class BlockingConsumer extends ConsumerImpl {
	private final static Log log = LogFactory.getLog(BlockingConsumer.class);
	Boolean callerProcessing = Boolean.FALSE;

	boolean foundMore = false;

	public BlockingConsumer(IOPair pair) {
		super(pair);
	}

	/**
	 * TODO Handle timeout of zero to expect, that shouldn't wait
	 */
	public void run() {
		log.debug(BlockingConsumer.class.getName() + " get to run ...");
		int length;
		char cs[] = new char[256];
		Reader reader = pair.getReader();

		log.debug("Starting primary loop");
		while ( !stopRequested && !foundEOF ) {
			try {
				log.debug("Reading from reader......");
				length = reader.read(cs); // blocking
			} catch(IOException ioe) {
				// The Pipe most likely closed on us.
				log.debug( "While checking ready: " + ioe.getMessage());
				foundEOF = true;
				break;
			}

			if( length == -1 ) { //EOF
				log.debug("Found the EOF");
				log.debug("Current buffer: " + buffer.toString() );
				foundEOF = true;
				break;
			}

			// don't modify the buffer while processing is happening
			// written as while loop to prevent spurious interrupts
			log.debug("Waiting for synch before appending");
			synchronized(this) { // this could be just before notify, I think
				if( log.isTraceEnabled() ) {
					String print = new String(cs, 0, length);
					print = print.replaceAll("\n", "\\\\n");
					print = print.replaceAll("\r", "\\\\r");
					log.trace("Appending >>>" + print + "<<<");
					StringBuffer sb = new StringBuffer();
					for( int i=0; i < length; i++) {
						sb.append("," + ((int) cs[i]) );
					}
					log.trace("Codes: " + sb.toString() );
				}

				buffer.append( cs, 0, length ); // thread safe

                if (buffer.toString().contains("M  q54321 COMPLD")) {
                    log.info("found user logout, stop read stream to avoid blocking");
                    stopRequested = true;
                    foundEOF = true;
                }
				/**
				 * TODO Trim down buffer.  This current method won't work if a resume comes in, since it's offset
				 * will be invalid once this delete method runs
				 *
				 * // since we only added one char, we should only have to remove one
				 * if( buffer.length() > BUFFERMAX )
				 * buffer.delete(0, BUFFERMAX - buffer.length() );
				 */

				log.debug("Waking up who ever if listening");
				notify(); // seeing that we read something, wake people up
			}

		} // end while loop


		synchronized(this) {
			notify();
		}

		if( stopRequested ) {
			log.info("Stop Requested");
			pair.close();
		}
		if ( foundEOF )
			log.info("Found EOF to stop while loop");

		log.debug("Leaving primary loop");
	}

	/**
	 * What is something came in between when we last checked and when this method is called
	 */
	public void waitForBuffer(long timeoutMilli) {
		if( foundEOF ) {
			log.debug("Wanted to wait for buffer but foundEOF");
			return;
		} 

		log.debug("Synching on this to wait");
		synchronized(this) {
			try {
				log.debug("Waiting for some additional event");
				if( timeoutMilli > 0 )
					wait(timeoutMilli);
				else
					wait();
			} catch(InterruptedException ie) {
				log.info("Woken up, while waiting for buffer");
			}
		}
	}

	public String pause() {
		// TODO mark offset, so that it can be trimmed by resume coming in later
		String currentBuffer;
		currentBuffer = buffer.toString();
		return currentBuffer;
	}
	
	public String getAndClear() { //this method should be synchronized with this consumer
		String currentBuffer;
		currentBuffer = buffer.toString();
		buffer.delete(0, currentBuffer.length());
		return currentBuffer;
	}

	/**
	 * @arg offset Offset is end - 1, so offset is used as a 1 indexed value.
	 */
	public void resume(int offset) {
		if ( offset < 0 )
			return;
		synchronized(this) {
			log.debug("Moving buffer up by " + offset);
			StringBuffer smaller = buffer.delete(0, offset); // + 1
		}
	}

	/**
	 * We have more input since wait started
	 */

	public static void main(String args[]) throws Exception {
		final StringBuffer buffer = new StringBuffer("The lazy fox");
		Thread t1 = new Thread() {
			public void run() {
				synchronized(buffer) {
					buffer.delete(0,4);
					buffer.append(" in the middle");
					System.err.println("Middle");
					try { Thread.sleep(4000); } catch(Exception e) {}
					buffer.append(" of fall");
					System.err.println("Fall");
				}
			}
		};
		Thread t2 = new Thread() {
			public void run() {
				try { Thread.sleep(1000); } catch(Exception e) {}
				buffer.append(" jump over the fence");
				System.err.println("Fence");
			}
		};
		t1.start();
		t2.start();

		t1.join();
		t2.join();
		System.err.println(buffer);
	}

}
