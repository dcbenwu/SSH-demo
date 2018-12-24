package coriant.cats.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.telnet.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Vector;


public class TClient implements Runnable, TelnetNotificationHandler
{
	private final static Log log = LogFactory.getLog(TClient.class);
    protected TelnetClient mTC = null;
    protected TerminalTypeOptionHandler mTtopt = null;
    protected EchoOptionHandler mEchoopt = null;
    protected SuppressGAOptionHandler mGaopt = null;
    protected Vector<String> mRemoteOutput = new Vector<String>();
    protected Boolean mStopped = false;
    protected String mStopReason = null;
    protected OutputStream mOutStream = null; 
    protected Thread mThread = null;
    protected String mIpAddr = null;
    protected String mPort = null;
    int mSize = 0; // in Bytes
    int mBufferSize = 1000000; // MB;
    public static final int mMaxBufferSize = 40; // in blocks of 100KB

    protected String mExpect = null;
    boolean mKeepExpect = false;
    
    protected void clearExpect() {
    	mKeepExpect = true;
    	mExpect = null;
    }
    
    protected String getExpect() {
    	mKeepExpect = false;
    	String str =  mExpect;
    	mExpect = null;
    	return str;
    }
    public void setBufferSize(int size) {
    	if (size <=0)
    		mBufferSize = 1000000;
    	else if (size < mMaxBufferSize) {
    		mBufferSize = size *  100000;
    	} else 
    		mBufferSize = mMaxBufferSize * 100000;
    }
   
    public String login(String ipAddr, String port, String userId, String password, String loginPrompt, String pswordPrompt, String loginErrorPrompt, String loginOkPrompt, String shelf) {
    	log.info("TClient.login >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+", userId= "+userId+", psword= "+password+", Userprompt= "+loginPrompt+", promp= "+pswordPrompt+
    			", errorPrompt= "+loginErrorPrompt+", loginOkPrompt= "+loginOkPrompt+", shelf= "+shelf);
    	
    	boolean print = false;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient:"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(2);
        int num = 0;
        if (shelf != null) {
        	while (true) {
        		if (expect("Destination TID", 30)) {
        			this.sendCommandLine("\r\n");
        			break;
        		} else {
        			num++;
        			if (num >= 5) {
        				return "Did not see 'Destination TID' after 5 tries";
        			}
            		ThreadUtil.sleep_ms(500);
        		}
        	}
        }
        num = 0;
        while (true) {
        	if (this.expect(loginPrompt, 30, print)) {
	            if (print) System.out.println("log in with user id: '"+userId+"'");
	            if (shelf == null)
	            	this.sendCommandLine(userId+"\n");
	            else 
	            	this.sendCommandLine(userId+"\r\n");
	            ThreadUtil.sleep(1);
	            if (this.expect(pswordPrompt, 30, print)) {
		            if (print) System.out.println("sending password: '"+password+"'");
		            if (shelf == null)
		            	this.sendCommandLine(password+"\n");
		            else 
		            	this.sendCommandLine(password+"\r\n");
	            	if (shelf == null) {
		            	ThreadUtil.sleep_ms(500);
		            	this.sendCommandLine("\n");
		            	ThreadUtil.sleep_ms(500);
		            	this.sendCommandLine("\n");
		            	if (this.expect(loginOkPrompt, 30)) {
		            		this.clearOutput();
		            		return "OK";            		
		            	} else if (this.expect(loginErrorPrompt, 30, print)) {
		            		if (print) System.out.println("login incorrect - retry ... ");	
		            	} else {
		            		// retry .... ???
		            	}
	            	} else {
	            		ThreadUtil.sleep_ms(100);
	            		this.sendCommandLine("\r\n");
	            		ThreadUtil.sleep_ms(1000);
	            		if (expect("Packet Shelf Number", 60)) {
	            			this.sendCommandLine(shelf+"\r\n");
	            			ThreadUtil.sleep_ms(500);
	            			this.sendCommandLine("\r\n");
			            	if (this.expect(loginOkPrompt, 60)) {
			            		this.clearOutput();
			            		return "OK";            		
			            	} else if (this.expect(loginErrorPrompt, 30, print)) {
			            		if (print) System.out.println("login incorrect - retry ... ");	
			            	} else {
			            		// retry
			            		int n = 0;
			            		while (n<5) {
				            		this.sendCommandLine("\r\n");
				            		ThreadUtil.sleep_ms(1000);
				            		n++;
			            			if (expect("Packet Shelf Number", 60)) {
				            			this.sendCommandLine(shelf+"\r\n");
				            			ThreadUtil.sleep_ms(500);
				            			this.sendCommandLine("\r\n");
						            	if (this.expect(loginOkPrompt, 60)) {
						            		this.clearOutput();
						            		return "OK";            		
						            	}
			            			} else if (this.expect(loginOkPrompt, 60)) {
						            		this.clearOutput();
						            		return "OK";            		
						            }
			            		}
			            	}
	            			
	            		}  else if (this.expect(loginErrorPrompt, 30, print)) {
		            		if (print) System.out.println("login incorrect - retry ... ");	
	            		} else {
	            			// retry 
	            			
	            		}
	            		
	            	}
	            }
        	}
    		num++;
    		if (num >=5) return "Failed to log on to "+ipAddr;
        }
        
    }
    
    public String login(String ipAddr, String port) {
    	//log.info("TClient.login >>>>>>>>: ipAddr= "+ipAddr+", port= "+port);
    	
    	boolean print = false;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
        if (!mTC.isConnected()) return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port;
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient:"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
    	//log.info("TClient.login >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+" successful");        
        return "OK";
    }
    
    public String loginTl1_old(String ipAddr, String port, String userId, String password, String loginPrompt, String pswordPrompt, String loginErrorPrompt, String loginOkPrompt, String shelf) {
    	System.out.println("TClient.loginTl1 >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+", userId= "+userId+", psword= "+password+", Userprompt= "+loginPrompt+", promp= "+pswordPrompt+
    			", errorPrompt= "+loginErrorPrompt+", loginOkPrompt= "+loginOkPrompt+", shelf= "+shelf);
    	
    	boolean print = true;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient(TL1):"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(5);
        
        if (this.expect(">", 10, print))
        	return "OK";
        return "Connecting to "+ipAddr+":"+remote_port+" failed: not seeing the '>' prompt";
    }
    
    public String loginTl1(String ipAddr, String port) {
    	//log.info("TClient.loginTl1 >>>>>>>>: ipAddr= "+ipAddr+", port= "+port);
    	
    	boolean print = false;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	log.debug("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
        log.debug("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
        log.debug("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
        log.debug("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient(TL1):"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(5);
        
        if (this.expect(">", 10, print)) {
        	//log.info("TClient.loginTl1 >>>>>>>> done: ipAddr= "+ipAddr+", port= "+port);
        	return "OK";
        }
        return "Connecting to "+ipAddr+":"+remote_port+" failed: not seeing the '>' prompt";
    }
    
    public String loginTS(String ipAddr, String port, String userId, String password, String loginPrompt, String pswordPrompt, String loginErrorPrompt, String loginOkPrompt) {
    	System.out.println("TClient.login >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+", userId= "+userId+", psword= "+password+", Userprompt= "+loginPrompt+", promp= "+pswordPrompt+
    			", errorPrompt= "+loginErrorPrompt+", loginOkPrompt= "+loginOkPrompt);
    	
    	boolean print = true;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient-TS:"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(2);
        int num = 0;
        while (true) {
        	if (this.expect(loginPrompt, 30, print)) {
	            if (print) System.out.println("log in with user id: '"+userId+"'");
	            if (userId != null && !userId.trim().equals("")) 
	            	this.sendCommandLine(userId+"\n");
	            else 
	            	this.sendCommandLine("\n");
	            ThreadUtil.sleep(1);
	            if (this.expect(pswordPrompt, 30, print)) {
		            if (print) System.out.println("sending password: '"+password+"'");
	            	if (password != null && !password.trim().equals("")) 
	            		this.sendCommandLine(password+"\n");
	            	else
	            		this.sendCommandLine("\n");
	            	ThreadUtil.sleep_ms(500);
	            	this.sendCommandLine("\n");
	            	ThreadUtil.sleep_ms(500);
	            	this.sendCommandLine("\n");
	            	if (this.expect(loginOkPrompt, 30)) {
	            		this.clearOutput();
	            		return "OK";            		
	            	} else if (this.expect(loginErrorPrompt, 30, print)) {
	            		if (print) System.out.println("login incorrect - retry ... ");	
	            		return "Failed to log on to "+ipAddr+": "+loginErrorPrompt;
	            	} else {
	            		// retry .... ???
	            	}
	            }
        	}
    		num++;
    		if (num >=5) return "Failed to log on to "+ipAddr;
        }
        
    }
    
    public String loginTSPort(String ipAddr, String port, String prompt) {
    	System.out.println("TClient.loginTS >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+", loginOkPrompt= "+prompt);
    	
    	boolean print = true;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("TClient.loginTSPort: Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("TClient.loginTSPort: After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("TClient.loginTSPort: Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("TClient.loginTSPort: After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        } catch(Exception e) {
        	return "TClient.loginTSPort: TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient(TS):"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(5);
        int num = 0;
        while (!this.isConnected() && num<5) {
        	if (print) System.out.println("TClient.loginTSPort: testing connection: status= "+(this.isConnected()?"connected":"not connected"));
        	ThreadUtil.sleep(5);
        	num++;
        }
    	if (print) System.out.println("TClient.loginTSPort: final status= "+(this.isConnected()?"connected":"not connected"));
        if ((prompt == null || prompt.trim().equals("")) && !this.isConnected()) {
        	return "Connecting to "+ipAddr+":"+remote_port+" failed!";
        }
        
    	if (this.expect(prompt, 10, print))
        	return "OK";
        return "Connecting to "+ipAddr+":"+remote_port+" failed: not seeing the prompt '"+prompt+"'";
    }
    
    public String loginPolatis(String ipAddr, String port, String prompt) {
    	System.out.println("TClient.loginTS >>>>>>>>: ipAddr= "+ipAddr+", port= "+port+", loginOkPrompt= "+prompt);
    	
    	boolean print = true;
    	if (mTC != null) {
    		synchronized(mStopped) {
    			if (!mStopped)
    				return "Session is still on ";
    		}
    	}
    	if (print) System.out.println("Before creating the client");
        mTC = new TelnetClient();
        mTtopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        mEchoopt = new EchoOptionHandler(true, false, true, false);
        mGaopt = new SuppressGAOptionHandler(true, true, true, true);
    	if (print) System.out.println("After creating the client");
        
        try {
        	mTC.addOptionHandler(mTtopt);
        	mTC.addOptionHandler(mEchoopt);
        	mTC.addOptionHandler(mGaopt);
        } catch (Exception e) {
            System.err.println("Error registering option handlers: " + e.getMessage());
            return "Error registering option handlers: " + e.getMessage();
        }
        int remote_port = 23;
        if (port != null) {
        	try {
        		remote_port = Integer.parseInt(port);
        	} catch(Exception e) {
        		remote_port = 23;
        	}
        }
    	if (print) System.out.println("After setting options");
        
        try {
        	mTC.connect(ipAddr, remote_port);
        	mIpAddr = ipAddr;
        	mPort = Integer.toString(remote_port);
        	mTC.setKeepAlive(true); // MR naval00001175
        } catch(Exception e) {
        	return "TClient can't connect to remote IP "+ipAddr+" with port "+remote_port+": "+e.toString();
        }
    	if (print) System.out.println("After connecting");
        mStopped = false;
        mThread = new Thread (this);
        mThread.setName("TClient(Polatis):"+ipAddr+":"+remote_port);
        mThread.start();
        mOutStream = mTC.getOutputStream();
        
        
        
        ThreadUtil.sleep(5);
        this.sendCommandLine("\n\n");
        if (print) System.out.println("TClient.loginPolatis: waited 5 seconds after connecting, now expecting '"+prompt+"'");
        if (this.expect(prompt, 10, print))
        	return "OK";
        return "Connecting to "+ipAddr+":"+remote_port+" failed: not seeing the prompt '"+prompt+"'";
    }
    
    public String su(String password, String prompt, String passwordPrompt) {
    	this.sendCommandLine("\n\n");
    	while (!expect(prompt, 10)) {
    		ThreadUtil.sleep(1);
    		this.sendCommandLine("\n\n");
    	}
    	this.sendCommandLine("su\n");
    	if (!this.expect(passwordPrompt, 10)) {
    		return "su failed";
    	}
    	this.sendCommandLine(password+"\n\n");
    	String str = this.getOutput();
    	//System.out.println("result of su => '"+str+"'");
    	if (str != null && str.indexOf("incorrect")>=0) return "log in as super user failed";
    	this.clearOutput();
    	return "OK";
    }

    /***
     * Callback method called when TelnetClient receives an option
     * negotiation command.
     * <p>
     * @param negotiation_code - type of negotiation command received
     * (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT)
     * <p>
     * @param option_code - code of the option negotiated
     * <p>
     ***/
//    @Override
    public void receivedNegotiation(int negotiation_code, int option_code)
    {
        String command = null;
        if(negotiation_code == TelnetNotificationHandler.RECEIVED_DO)
        {
            command = "DO";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_DONT)
        {
            command = "DONT";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WILL)
        {
            command = "WILL";
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WONT)
        {
            command = "WONT";
        }
        System.err.println("Received " + command + " for option code " + option_code);
   }
    
    public String getOutput() {
    	
    	return this.getOutput(1);
    	/*
    	synchronized(mRemoteOutput) {
    		if (mRemoteOutput.size() == 0) return null;
    		String ret = mRemoteOutput.firstElement();
    		for (int i=1; i<mRemoteOutput.size(); i++) ret += mRemoteOutput.elementAt(i);
    		mRemoteOutput.removeAllElements();
    		return ret;
    	}*/
    }
    
    public String getOutput(int fifty_ms) {
    	int num = 0;
    	while ((fifty_ms == 0) || (fifty_ms > 0 && num<fifty_ms)) {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() > 0) {
		    		String ret = mRemoteOutput.firstElement();
		    		for (int i=1; i<mRemoteOutput.size(); i++) ret += mRemoteOutput.elementAt(i);
		    		mRemoteOutput.removeAllElements();
		    		mSize = 0;
	    		
	    		return ret;
	    		}
	    	}
	    	ThreadUtil.sleep_ms(50);
	    	num++;
    	}
    	return null;
    	
    }    
    
    public boolean expect(String key, int seconds) {
    	return this.expect(key, seconds, false);
    }
    public boolean expect(String key, int seconds, boolean print) {
    	if (key == null || key.trim().equals("")) return true;
    	log.debug("Expecting '"+key+"' ...................................................");
    	int num = 0;
    	int pos = 0;
    	String text = "";
    	while ((seconds == 0) || (seconds > 0 && num<seconds)) {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() > 0) {
		    		for (int i=pos; i<mRemoteOutput.size(); i++) {
		    			String ret = mRemoteOutput.elementAt(i);
		    			text += ret;
		    			if (mKeepExpect) {
		    				if (mExpect == null) mExpect = ret; else mExpect += ret;
		    			}
		    			if (print) System.out.println("TClient.expect: msg["+i+"]= "+ret);
		    			if (text != null && text.indexOf(key)>=0) {
		    				for (int j=0; j<=i; j++) mRemoteOutput.remove(0);
		    				return true;
		    			}
		    		}	
		    		pos = mRemoteOutput.size();
	    		}
	    	}
	    	ThreadUtil.sleep(2);
	    	num += 2;
    	}
    	return false;
    	
    }
    
    public void clearOutput() {
    	synchronized(mRemoteOutput) {
    		mRemoteOutput.removeAllElements();
    		mSize = 0;
    	}    	
    }
    

    
    public String sendCommandLine(String cmdline) {
    	synchronized(mOutStream) {
    		if (cmdline == null) return "OK";
    		if (!mTC.isConnected()) return "TClient.sendCommandLine: connection is broken";
    		char[] ch = cmdline.toCharArray();
    		byte[] buf = new byte[ch.length];
    		for (int i=0; i<ch.length; i++) buf[i] = (byte)ch[i];
            try
            {
            	mOutStream.write(buf, 0 , buf.length);
                    mOutStream.flush();                   
            }
            catch (Exception e)
            {
                    return "sending command Exception - "+e.toString();
            }
    		
    	}
    	return "OK";
    }
    
    public void stop() {
    	if (mThread != null) {
    		try {
    			log.info("TClient.stop: stopping thread "+mThread.getName());
    			synchronized(mStopped) {
    				mStopped = true;
    				mStopReason = "Stopped normally thread "+mThread.getName();
    			}
    			mThread.stop();
    		} catch (Exception e) {
    			log.info("TClient.stop: failed to stop thread "+mThread.getName()+" - "+e.toString());
    		}
    		mThread = null;
    	}
    	ThreadUtil.sleep(5);
    	if (mTC != null) {
    		boolean stopped = false;
    		int num = 0;
    		while (!stopped && num < 5) {
    			num++;
	    		try {
	    			if (mTC.isConnected()) {
	    				log.info("TClient.stop: disconnecting connection "+mIpAddr+":"+mPort);
	    				mTC.disconnect();
	    			} else {
	    				log.info("TClient.stop: connection "+mIpAddr+":"+mPort+" already disconnected");	    				
	    			}
	    		    //mTC.stopSpyStream();
	    			stopped = true;
	    		} catch(Exception e) {
	    			if (num >= 5) {
	    				StackTraceElement[] s = e.getStackTrace();
	    				String trc = e.toString();
	    				for (int i=0; i<s.length; i++) {
	    					trc += "\n"+s[i].toString();
	    				}
	    				log.info("TClient.stop: failed to disconnect connection "+mIpAddr+":"+mPort+" - "+trc);
	    			} else
	    				ThreadUtil.sleep(2);
	    		}
    		}
    	}
    }

    /***
     * Reader thread.
     * Reads lines from the TelnetClient and echoes them
     * on the screen.
     ***/
//    @Override
    public void run()
    {
        BufferedReader instr = null;
        String msg = null;
        try
        {
        	instr = new BufferedReader(new InputStreamReader(mTC.getInputStream()));
            char[] buff = new char[1024];
            int ret_read = 0;

            do
            {
                ret_read = instr.read(buff);
                if(ret_read > 0)
                {
                	synchronized(mRemoteOutput) {
                		mRemoteOutput.add(new String(buff, 0, ret_read));
                		mSize += ret_read;
                		while (mSize > mBufferSize) { 
                			String str = mRemoteOutput.remove(0);
                			mSize -= str.length();
                		}
                	}
                    //System.err.print();
                }
            }
            while (ret_read >= 0);
        }
        catch (IOException e)
        {
        	msg = "Exception while reading socket:" + e.getMessage();
            System.err.println(msg); 
        }

        try
        {
        	mTC.disconnect();
        	synchronized(mStopped) {
        		mStopped = true;
        		mStopReason = msg+", connection disconnected";
        	}
        }
        catch (IOException e)
        {
        	log.info("TClient.run: Exception while closing telnet:" + e.getMessage());
            synchronized (mStopped) {
            	mStopped = true;
            	mStopReason = "Exception while closing telnet:" + e.getMessage();
            }
        }
    }
    
    public boolean isConnected() {
    	if (mTC != null) {
    		return mTC.isConnected();
    	}
    	return false;
    }
    
    public static void main(String[] argv) {
    	TClient tc = new TClient();
    	try {
    	        Vector<String> a = new Vector<String>();
    	        System.err.println("We are in: argv= "+argv.length);
    	        for (int i=0; i<argv.length; i++) {
    	        	if (argv[i] != null && !argv[i].trim().equals(""))
    	        		a.add(argv[i]);
    	        }
    	        String[] args = null;
    	        if (a.size() == 0) {
    	        	args = new String[]{};
    	        } else {
    	        	args = new String[a.size()];
    	        	for (int i=0; i<a.size(); i++) args[i] = a.elementAt(i);
    	        }
    	        if(args.length < 1) {
    	            System.err.println("Usage: tel <remote-ip> [<remote-port>] ");
    	            System.exit(1);
    	        }
    	        String ip = args[0].trim();
    	        String port = null;
    	        if (args.length>1) {
    	        	port = args[1].trim();
    	        }
    	        String userId = ConsoleInput.getInputFromConsole("UserId: ");
    	        String password = ConsoleInput.getInputFromConsole("Password: ");
    	        String ret = tc.login(ip, port, userId, password, "login:", "Password:", "sadf", "sdfsd", null);
    	        if (!ret.equals("OK")) {
    	        	System.out.println(ret);
    	        	System.exit(-1);
    	        }
	        	System.out.println("log in ok");
    	        boolean contd = true;
    	        while (contd) {
    	        	String cmd = ConsoleInput.getInputFromConsole("> ");
    	        	if (cmd.equals("exit")) break;
    	        	ret = tc.sendCommandLine(cmd+"\n");
    	        	if (!ret.equals("OK")) {
    	        		System.out.println("> "+ret);
    	        	} else {
    	        		ThreadUtil.sleep(2);
    	        		String msg = tc.getOutput();
    	        		if (msg != null) {
    	        			System.out.println(msg);
    	        		}
    	        	}
    	        }
    	        tc.stop();
    	        //reader.stop();
	
    	} catch(Exception e) {
    		System.out.println("Run time exception: "+e.toString());
    	}
    }
}
