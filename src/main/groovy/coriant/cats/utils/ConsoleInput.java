package coriant.cats.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ConsoleInput {

	
	public static String getInputFromConsole(String msg) {
		System.out.print(msg);
		 
		try{
		    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		    String s = bufferRead.readLine();
	 
		    return s;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	 		
	}
	
	BufferedReader mReader = null;
	public String getLine(PrintStream out, String msg) {
		out.print(msg);
		 
		try{
			if (mReader == null)
				mReader = new BufferedReader(new InputStreamReader(System.in));
		    String s = mReader.readLine();
	 
		    return s;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	 		
	}
	
	
	
}
