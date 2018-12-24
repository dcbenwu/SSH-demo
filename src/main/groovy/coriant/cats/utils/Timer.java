package coriant.cats.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timer {
	  /**
	   *
	   * @return current date in long
	   */
	static long mPrev = 0;
	
	public static void start() {
		mPrev = getTime();
	}
	  public static long getTime() {
	    Date now = new Date();
	    return now.getTime();
	  }
	  
	  public static String stamp(String msg) {
		  long time = getTime();
		  String str = "Time elapsed: "+(time-mPrev)+" - "+msg;
		  mPrev = time;
		  return str;
	  }
	  
	  public static String getTimeString(Date now, String pattern) {
			if (pattern == null || pattern.trim().equals(""))
				return now.toLocaleString();
			else {
				SimpleDateFormat df = new SimpleDateFormat(pattern);
				return df.format(now);
			}

	  }


}
