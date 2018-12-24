package coriant.cats.utils;

/*import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;*/

public class SClient
{
	public static void main(String[] args)
	{
		String hostname = "172.21.3.179";
		String username = "root";
		String password = "C0riant@SH";
/*
		try
		{
			// Create a connection instance 

			Connection conn = new Connection(hostname);

			// Now connect 

			conn.connect();

			// Authenticate 

			boolean isAuthenticated = conn.authenticateWithPassword(username, password);

			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			// Create a session 

			Session sess = conn.openSession();
			
			OutputStream out = sess.getStdin();

			sess.execCommand("/usr/cats/driver/T7100/t7100");

			InputStream stdout = new StreamGobbler(sess.getStdout());
			InputStream stderr = new StreamGobbler(sess.getStderr());
	
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
			
			System.out.println("Here is the output from stdout:");
	
				String line = stdoutReader.readLine();
				System.out.println(line);
			
			String cmdline = "this is testing";
			
    		char[] ch = cmdline.toCharArray();
    		byte[] buf = new byte[ch.length];
    		for (int i=0; i<ch.length; i++) buf[i] = (byte)ch[i];
            try
            {
            	out.write(buf, 0 , buf.length);
                    out.flush();                   
            }
            catch (Exception e)
            {
                    System.out.println( "sending command Exception - "+e.toString());
            }
			
            
			System.out.println("Here is the response:");
			
				line = stdoutReader.readLine();
				System.out.println(line);
			
			// Close this session 
			
			sess.close();

			// Close the connection 

			conn.close();

		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
			System.exit(2);
		} */
	}
}

