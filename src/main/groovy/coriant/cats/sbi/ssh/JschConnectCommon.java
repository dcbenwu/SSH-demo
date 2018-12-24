package coriant.cats.sbi.ssh;

import com.jcraft.jsch.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JschConnectCommon extends SSHConnect {
	protected Session		session;
	protected Channel 	channel;

	private final static Log log = LogFactory.getLog(JschConnectCommon.class);

	@Override
	public boolean isConnect() {
		if (session == null || channel == null) return false;
		if (!session.isConnected()) {
			return false;
		}
		if (channel.isClosed() || channel.isEOF() || !channel.isConnected())
			return false;
		return true;
	}

	@Override
	public void stop() {
		if (channel !=null && channel.isConnected()) {
			log.info("channel is connected, stop it...");
			channel.disconnect();
			channel = null;
		}
		if (session !=null && session.isConnected()) {
			log.info("session is connected, stop it...");
			session.disconnect();
			session = null;
		}
	}

	public static class LocalUserInfo	implements UserInfo, UIKeyboardInteractive {
		public String getPassword(){ 
			log.info("getPassword is needed");
			return null; 
		}
		public boolean promptYesNo(String str){ 
			log.info("promptYesNo is called");
			return true; 
		}
		public String getPassphrase(){ 
			log.info("getPassphrase is called");
			return null; 
		}
		public boolean promptPassphrase(String message){ 
			log.info("promptPassphrase is called");
			return false; 
		}
		public boolean promptPassword(String message){ 
			log.info("promptPassword is called");
			return false; 
		}
		public void showMessage(String message){ 
			log.info("showMessage is called");
		}
		public String[] promptKeyboardInteractive(String destination,
				String name,
				String instruction,
				String[] prompt,
				boolean[] echo){
			log.info("promptKeyboardInteractive is called");
			return null;
		}
	}
	
	public static class JSchLogger implements  Logger {
		Log log;
		
		public JSchLogger(Log log) {
			this.log = log;
		}
		@Override
		public boolean isEnabled(int level) {
			switch (level) {
			case Logger.FATAL:
				return log.isFatalEnabled();
			case Logger.ERROR:
				return log.isErrorEnabled();
			case Logger.WARN:
				return log.isWarnEnabled();
			case Logger.INFO:
				return log.isWarnEnabled(); //prompt its level to suppress log information
			case Logger.DEBUG:
				return log.isDebugEnabled();
			}
			return false;
		}

		@Override
		public void log(int level, String message) {
			// TODO Auto-generated method stub
			switch (level) {
			case Logger.FATAL:
				log.fatal(message);
				break;
			case Logger.ERROR:
				log.error(message);
				break;
			case Logger.WARN:
				log.warn(message);
				break;
			case Logger.INFO:
				log.info(message);
				break;
			case Logger.DEBUG:
				log.debug(message);
				break;
			}
		}
	}

}
