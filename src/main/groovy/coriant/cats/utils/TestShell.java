package coriant.cats.utils;

import java.io.IOException;
import java.io.InputStream;

public class TestShell {

	public static void main(String[] argc) {
		try {
	      Runtime mRuntime = Runtime.getRuntime();
	      String[] cmds = {"/bin/sh", "-c", "/users/taoyang/stl/bin/dump2"};
	      Process p = mRuntime.exec(cmds);
	      System.out.println("Called ...");
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
	                	System.out.println(new String(buff, 0, ret_read));
	                }
	            }
	            while (ret_read >= 0);
	        }
	        catch (IOException e)
	        {
	            System.err.println("Exception while reading socket:" + e.getMessage());
	        }
		} catch(Exception e) {
            System.err.println("Exception while doing runtime:" + e.getMessage());			
		}
		
	}
}
