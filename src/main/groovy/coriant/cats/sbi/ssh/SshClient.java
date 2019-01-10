package coriant.cats.sbi.ssh;

import coriant.cats.sbi.BlockingConsumer;
import coriant.cats.sbi.StreamPair;
import coriant.cats.sbi.TL1TransportInf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SshClient implements TL1TransportInf {
	private final static Log log = LogFactory.getLog(SshClient.class);
	protected SSHConnectInf connection;
	protected BlockingConsumer consumer;
	protected Thread consumerThread;
	
	//int timeout = 120*1000;
    int timeout = 30*1000;
	String ipAddress = null;
	String port = null;
	
	@Override
	public String sendCommandLine(String cmd) {
	    log.debug("Going to Send command: " + cmd);
		synchronized (consumer) {
			if (!isConnected()) return "connection is broken";
			try {
			    log.info("send command: " + cmd);
				consumer.send(cmd);
			} catch (Exception e) {
				log.error(e);
				return "Exception: " + e.getMessage();
			}
		}
		return "OK";
	}

	@Override
	public String getOutput() {
		String msg = null;
		synchronized (consumer) {
			try {
				consumer.wait(50);
				msg = consumer.getAndClear();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		if (msg == null || msg.trim().isEmpty())
			return null;
		return msg;
	}

	@Override
	public boolean isConnected() {
		if (connection != null)
			return connection.isConnect();
		return false;
	}

	@Override
	public String loginTl1(String ipAddress, String port, String userName, String passwd, String prompt) {
		try {
			log.debug("Try to sign in " + userName + "@" + ipAddress + ":" + port + " with " + passwd);
			this.ipAddress = ipAddress;
			this.port = port;
			connection = new SSHConnectJsch();
			connection.setConfig(ipAddress,Integer.parseInt(port),userName,passwd, "shell", 30000);
			connection.connect();
			consumer = new BlockingConsumer(new StreamPair(connection.getInputStream(),
														   connection.getOutputStream()));
	        consumerThread = new Thread(consumer);
	        consumerThread.setDaemon(true);
	        consumerThread.start();	        
			expect(Pattern.compile(prompt), this.timeout);
			log.info(String.format("Logging to %s@%s:%s with password:%s successfully!",userName,ipAddress,port,passwd));
		} catch (Exception ex) {
			log.info(String.format("Failed: Logging to %s@%s:%s with password:%s!",userName,ipAddress,port,passwd));
			log.error(ex);
			if (connection != null) stop();
			return ex.getMessage();
		}
		return "OK";
	}
	

	@Override
	public void stop() {
		if (consumer != null) {
			synchronized (consumer) {
				consumer.stop();		
				consumer=null;
			}
		}
		if (connection != null) {
		    connection.stop();
        }
		try {
			if (consumerThread == null) return;
			consumerThread.join();
			consumerThread = null;
		} catch (InterruptedException e) {
			log.error(e);
		}
	}

	public void free() {
	    if (consumer != null) consumer.free();
	    if (consumerThread != null) consumerThread.interrupt();
    }

	public String expect(Pattern pattern, int timeout) throws Exception {
		try {
			String str = "";
			String originalStr = "";

			boolean end = false;

			long startTime = System.currentTimeMillis();
			long endTime = startTime + timeout;
			boolean foundTimeout = false;
			boolean foundEof = false;

			while (true) {
				if (System.currentTimeMillis() >= endTime) {
					log.error("Timeout "+ endTime + " when it is " + System.currentTimeMillis());
					foundTimeout = true;
					break;
				}
				synchronized (consumer) {
					originalStr = str;
					str = consumer.pause();
					log.debug("get output: " + str);
					foundEof = consumer.foundEOF();
					log.debug("pattern is " + pattern);
					end = matchLine(str, pattern);
					log.debug("match result is " + end);
					if (!end) {
						consumer.resume();
					} else {
						log.debug("found pattern " + pattern.pattern() + "in string " + str);
						consumer.resume(str.length());
						break;
					}
					if (foundEof) {
						log.error("Found EOF");
						break;
					}
					if (str.length() > originalStr.length()) {//if there are new output, we reset the timeout;
						endTime = System.currentTimeMillis() + timeout;
					}
					long singleTimeout = endTime - System.currentTimeMillis();
					if (singleTimeout > 0) {
						log.debug("Waiting for more input for " + singleTimeout + "ms");
						consumer.waitForBuffer(singleTimeout);
					}
				}
			}
			String errMsg = null;
			if (!end) {
				if (!this.isConnected()) {
					errMsg = "connection to host "+ipAddress+": "+port+" has been reset by remote host in expect method";
				} else if (foundEof) {
					errMsg = "connection to host "+ipAddress+": "+port+" is corrupted in expect method";
				} else {
					errMsg = "The host "+ipAddress+" failed to respond within timeout limit of " + (System.currentTimeMillis()-startTime)/1000 + " seconds in expect method";
				}				
				log.error(errMsg);
			}
			String response = null;
			synchronized (consumer) {
				response = consumer.getAndClear();
			}
			log.info(response);
			return "OK";
		} catch(Exception e) {
			log.error(e);
			throw new Exception("exception - "+e.getMessage());
		}		
	}

	public boolean matchLine (String str, Pattern p) {
		if (str.trim().isEmpty()) {
			return false;
		}
		String lines [] = str.split("\n");
		for (String line:lines) {
			Matcher m = p.matcher(line.trim());
			if (m.matches()) return true;
		}
		return false;
	}

	@Override
	public String getRemoteAddress() {
		return ipAddress;
	}

}
