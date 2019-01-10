package coriant.cats.sbi.ssh;

import com.jcraft.jsch.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;

public class SSHConnectJsch extends JschConnectCommon implements SSHConnectInf {
	private final static Log log = LogFactory.getLog(SSHConnectJsch.class);
		
	@Override
	public void connect() throws Exception {
		try {
			log.info(String.format("Start logging to %s@%s:%d",userName, ipAddress, port));
			JSch jsch = new JSch();
			JSchLogger logger = new JSchLogger(log);
			JSch.setLogger(logger);
			session = jsch.getSession(userName, ipAddress, port);
			session.setPassword(password);
			Hashtable<String, String> config = new Hashtable<String, String>();
			config.put("StrictHostKeyChecking", "no");
			//config.put("kex", "diffie-hellman-group1-sha1");
			session.setConfig(config);
			UserInfo ui = new LocalUserInfo();
			session.setUserInfo(ui);
			session.connect(timeout);
			if (subSystem.equals(CONNECTION_TYPE_SHELL)) {
				channel = session.openChannel("shell");
				((ChannelShell)channel).setPtySize(400, 24, 640, 480);
			}
			else {
				channel = (ChannelSubsystem) session.openChannel("subsystem");
				((ChannelSubsystem)channel).setSubsystem(subSystem);
				((ChannelSubsystem)channel).setPty(true);
				((ChannelSubsystem)channel).setPtySize(400, 24, 640, 480);
			}
			in =  channel.getInputStream();
			out = channel.getOutputStream();
			channel.connect();
		} catch (JSchException je) {
			String errorInfo = "LogTag on " + ipAddress + ": " + je.getMessage();
			if (errorInfo.indexOf("Auth ")>=0) errorInfo = "LogTag on " + ipAddress + ": " + SSHConnectInf.AUTH_ERROR;
			log.fatal(errorInfo, je);
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			throw new Exception(errorInfo);
		} catch (Exception ex) {
			String errorInfo = "LogTag on " + ipAddress + ": " + ex.getMessage();
			log.fatal(errorInfo, ex);
			if (session != null && session.isConnected()) {
				session.disconnect();
			}
			if (channel != null && channel.isConnected()) {
				channel.disconnect();
			}
			throw new Exception(errorInfo);
		}
	}


}
