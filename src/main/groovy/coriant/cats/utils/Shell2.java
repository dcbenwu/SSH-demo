package coriant.cats.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class Shell2 implements Runnable {
  /**
   * This class provides APIs for accessing the shell command
   */
	protected Runtime mRuntime = null;
	Vector<String> mRemoteOutput = null;
	boolean mWindows = false;
	boolean mPrint = false;
	Process mProcess = null;
    Boolean mStopped = false;
    OutputStream mOutStream = null; 
    Thread mThread = null;
	
	public Shell2() {
	    mRuntime = Runtime.getRuntime();
	    mRemoteOutput = new Vector<String>();
	    String os = System.getProperty("os.name");
	    boolean mWindows = (os.indexOf("Windows") >= 0);
	}
	
	public void setPrint(boolean print) {
		mPrint = print;
	}

	public String start(String cmds) {
		if (mWindows)
			return this.execWin(cmds);
		else
			return this.exec(cmds);
	}
	

    public String loginSession(String ipAddr, String port, String userId, String password, String loginPrompt, String pswordPrompt) {
    	
    	return "OK";
    }
    
    public void stop() {
    	
    }
	
	
	public String exec(String cmd) {
	    //synchronized(mRemoteOutput) {
		    try {
		      String [] cmds = {"/bin/sh", "-c", cmd};
		      mRuntime = Runtime.getRuntime();
		      mProcess = mRuntime.exec(cmds);
		      mOutStream = mProcess.getOutputStream();
		      mThread = new Thread (this);
		      mThread.start();
		    } catch (Exception e) {
		      return "exec exception: " + e.toString();
		    }
	   // }
	    return "OK";
	}

	  public String execWin(String cmd) {
		  synchronized (mRemoteOutput) {
		    try {
		      String [] cmds = cmd.split(" ");
		      mRuntime = Runtime.getRuntime();
		      mProcess = mRuntime.exec("cmd "+cmd);
		        mThread = new Thread (this);
		        mThread.start();
		        mOutStream = mProcess.getOutputStream();
		      
		    } catch (Exception e) {
		      return "exec exception: " + e.toString();
		    }
		  }
		    return "OK";
	}

	    public String getOutput() {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() > 0)  {
		    		String ret = mRemoteOutput.firstElement();
		    		for (int i=1; i<mRemoteOutput.size(); i++) ret += "\n"+mRemoteOutput.elementAt(i);
		    		mRemoteOutput.removeAllElements();
		    		return ret;
	    		}
	    	}
	    	System.out.println("No output in the vector");
	    	return null;
	    }
	    
	    public void clearOutput() {
	    	synchronized(mRemoteOutput) {
	    		mRemoteOutput.removeAllElements();
	    	}    	
	    }	    
	
	    public void run()
	    {
	    	System.out.println("reading thread started running");
	        InputStream instr =  mProcess.getInputStream();

	        try
	        {
	            byte[] buff = new byte[1024];
	            int ret_read = 0;

	            do
	            {
	                ret_read = instr.read(buff);
	                if(ret_read > 0)
	                {
	                	//System.out.println(new String(buff, 0, ret_read));
	                	synchronized(mRemoteOutput) {
	                		mRemoteOutput.add(new String(buff, 0, ret_read));
	                	}
	                    //System.out.println("received from process: "+mRemoteOutput.lastElement());
	                } else {
	                    //System.out.println("nothing received from process: "+ret_read);	  
	                }
                    ThreadUtil.sleep_ms(500);
	            }
	            while (ret_read >= 0);
                System.out.println("done reading: "+ret_read);	  
	            
	            
	        }
	        catch (Exception e)
	        {
	            System.err.println("Exception while reading socket:" + e.getMessage());
	        }

	        try
	        {
	        	mProcess.destroy();
	        }
	        catch (Exception e)
	        {
	            System.err.println("Exception while closing telnet:" + e.getMessage());
	        }
	    }
	  
	    public String sendCommandLine(String cmdline) {
	    	synchronized(mOutStream) {
	    		if (cmdline == null || cmdline.trim().equals("")) return "OK";
	    		char[] ch = cmdline.toCharArray();
	    		byte[] buf = new byte[ch.length];
	    		for (int i=0; i<ch.length; i++) buf[i] = (byte)ch[i];
	            try
	            {
	            	mOutStream.write(buf, 0 , buf.length);
	                    mOutStream.flush();
	            }
	            catch (IOException e)
	            {
	                    return "sending command Exception - "+e.toString();
	            }
	    		
	    	}
	    	return "OK";
	    }
	  public static void main(String [] argv) {
		    /*if (argv == null || argv.length == 0) {
		      System.out.println("Missing command");
		      return;
		    }*/
		  FileUtil fu = new FileUtilImp();
		  OutputFile file = fu.getOutputFile("shell2.txt");
		    Shell2 sh = new Shell2();
		    System.out.println("we are in "+(sh.mWindows?"Windows":"Linux"));
		    String ret = sh.start("/users/taoyang/stl/bin/dump2");
	    	System.out.println("connecting to dumper2 "+ret);
		    if (!ret.equals("OK")) {
		    	return;
		    }
		    ThreadUtil.sleep(1);
		    int i = 0;
		    while (true) {
		    	//String msg = ConsoleInput.getInputFromConsole("enter stuff to dumper ");
		    	String msg = "message to dumper "+i++;
		    	sh.sendCommandLine(msg);
		    	ThreadUtil.sleep(1);
		    	ret = sh.getOutput();
		    	//System.out.println("stuff from dumper-> "+ret);
		    	file.writelf(ret);
		    }
	  }
	  
}