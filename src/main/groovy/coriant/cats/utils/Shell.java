package coriant.cats.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class Shell {
  /**
   * This class provides APIs for accessing the shell command
   */
	//protected Runtime mRuntime = null;
	Vector<String> mRemoteOutput = null;
	boolean mWindows = false;
	boolean mPrint = false;
	Process mProcess = null;
	
	public Shell() {
	    //mRuntime = Runtime.getRuntime();
	    mRemoteOutput = new Vector<String>();
	    String os = System.getProperty("os.name");
	    boolean mWindows = (os.indexOf("Windows") >= 0);
	}
	
	public void setPrint(boolean print) {
		mPrint = print;
	}

	public String sendCommandLine(String cmds) {
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
		boolean print = true;
	    synchronized(mRemoteOutput) {
		    try {
		      //String cmd1 = "perl /usr/cats/perl/78StartMteraServer.pl 172.21.44.217";
		      /*if (cmd.equals(cmd1)) {
			      if (print) System.out.println("Shell.exec (linux): '"+cmd+"' equal to '"+cmd1+"'");		    	  
		    	  cmd1 = cmd;
		      } else
			      if (print) System.out.println("Shell.exec (linux): '"+cmd+"' not equal to '"+cmd1+"'");	*/	    	  
		    	  
		    	  
		      String [] cmds = {"/bin/sh", "-c", cmd};
		      //String [] cmds = {cmd};
		      Runtime mRuntime = Runtime.getRuntime();
		      if (print) System.out.println("Shell.exec (linux): sending command '"+cmd+"'");
		      Process p = mRuntime.exec(cmds);
		      mRemoteOutput.removeAllElements();
		      InputStream instr = p.getInputStream();
		        try
		        {
		            byte[] buff = new byte[1024];
		            int ret_read = 0;

		            do
		            {
		                ret_read = instr.read(buff);
		                if(ret_read > 0)
		                {
		                		mRemoteOutput.add(new String(buff, 0, ret_read));
		                    //System.err.print();
		                }
		            }
		            while (ret_read >= 0);
		        }
		        catch (IOException e)
		        {
		            System.err.println("Exception while reading socket:" + e.getMessage());
		            System.out.println("Exception while reading socket:" + e.getMessage());
		            return "Exception while reading socket:" + e.getMessage();
		        }
		      if (print) System.out.println("Shell.exec (linux): after buf= "+mRemoteOutput.size());
		      //p.waitFor();
		    } catch (Exception e) {
		      return "exec exception: " + e.toString();
		    }
	    }
	    return "OK";
	}

	public String exec_saved(String cmd) {
	    synchronized(mRemoteOutput) {
		    try {
		      String [] cmds = {"/bin/sh", "-c", cmd};
		      Runtime mRuntime = Runtime.getRuntime();
		      Process p = mRuntime.exec(cmds);
		      mRemoteOutput.removeAllElements();
		      InputStreamReader i = new InputStreamReader(p.getInputStream());
		      BufferedReader in = new BufferedReader(i);
		      String line = in.readLine();
		      //System.out.println("before buf= "+mRemoteOutput.size());
		      while (line != null) {
		    	  mRemoteOutput.add(line);//System.out.println(line);
		        line = in.readLine();
		      }
		      //System.out.println("after buf= "+mRemoteOutput.size());
		      //p.waitFor();
		    } catch (Exception e) {
		      return "exec exception: " + e.toString();
		    }
	    }
	    return "OK";
	}
	  public String execWin(String cmd) {
		  synchronized (mRemoteOutput) {
		    try {
		      String [] cmds = cmd.split(" ");
		      Runtime mRuntime = Runtime.getRuntime();
		      Process p = mRuntime.exec("cmd "+cmd);
		      
		      InputStream instr = p.getInputStream();
		        try
		        {
		            byte[] buff = new byte[1024];
		            int ret_read = 0;

		            do
		            {
		                ret_read = instr.read(buff);
		                if(ret_read > 0)
		                {
		                		mRemoteOutput.add(new String(buff, 0, ret_read));
		                    //System.err.print();
		                }
		            }
		            while (ret_read >= 0);
		        }
		        catch (IOException e)
		        {
		            System.err.println("Exception while reading socket:" + e.getMessage());
		        }
		    } catch (Exception e) {
		      return "exec exception: " + e.toString();
		    }
		  }
		    return "OK";
	}

	    public String getOutput() {
	    	synchronized(mRemoteOutput) {
	    		if (mRemoteOutput.size() == 0) return null;
	    		String ret = mRemoteOutput.firstElement();
	    		for (int i=1; i<mRemoteOutput.size(); i++) ret += "\n"+mRemoteOutput.elementAt(i);
	    		mRemoteOutput.removeAllElements();
	    		return ret;
	    	}
	    }
	    
	    public void clearOutput() {
	    	synchronized(mRemoteOutput) {
	    		mRemoteOutput.removeAllElements();
	    	}    	
	    }	    
	
	  
	  public static void main(String [] argv) {
		    if (argv == null || argv.length == 0) {
		      System.out.println("Missing command");
		      return;
		    }
		    Shell sh = new Shell();
		    System.out.println("we are in "+(sh.mWindows?"Windows":"Linux"));
		    while (true) {
		    	//System.out.print();
		    	String cmd = ConsoleInput.getInputFromConsole("Tao's Shell > ");
		    	if (cmd.startsWith("exit")) return;
			    String ret = sh.sendCommandLine(cmd);
			    if (!ret.equals("OK")) {
			      System.out.println("Tao's Shell > "+ret);
			    } 
			    ret = sh.getOutput();
				System.out.println(ret);
			    
		    }
	  }
	  
}