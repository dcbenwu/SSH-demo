package coriant.cats.sbi;

public interface TL1TransportInf {
	public String sendCommandLine(String cmd);
	public String getOutput();
	public boolean isConnected();
	public String loginTl1(String ipAddress, String port, String userName, String passwd, String prompt);
	public void stop();
	public void free();
	public String getRemoteAddress();
}
