package coriant.cats.sbi.telnet;

import coriant.cats.sbi.TL1TransportInf;
import coriant.cats.utils.TClient;

public class TelnetClient extends TClient implements TL1TransportInf {

	@Override
	public String loginTl1(String ipAddress, String port, String userName, String passwd, String prompt) {
		return super.loginTl1(ipAddress, port);
	}

	@Override
	public void free() {
		// TODO
	}

	@Override
	public String getRemoteAddress() {
		// TODO Auto-generated method stub
		if (this.mTC != null) return mTC.getRemoteAddress().toString();
		return null;
	}

}
