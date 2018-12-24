package coriant.cats.utils;

import java.util.Vector;

public class Dumper {

	public static void main(String[] argv) {
		FileUtil fu = new FileUtilImp();
		Vector<String> v = new Vector<String>();
		String ret = fu.readFile("dumper.txt", v);
		//fu.deleteFile("dumper.txt");
		ret = argv[0];
		for (int i= 1; i<argv.length; i++) {
			ret += " "+argv[i];
		}
		v.add(ret);
		fu.writeFile("dumper.txt", v);
		ThreadUtil.sleep(1);
		if (ret.indexOf("get")>=0 && ret.indexOf("AGT_SERVICE_DISRUPTION_MAXIMUM_DURATION")>=0 && ret.indexOf("AGT_SERVICE_DISRUPTION_COUNT")>=0) {
			System.out.println("SUCCESS: 23 1 234 2343 23423 234234 233234");
		} else if (ret.indexOf("get")>=0 && ret.indexOf("AGT_STREAM_PACKET_LOSS")>=0 ) {
			System.out.println("SUCCESS: 0 2434 234 2343 23423 234234 233234");
		} else if (ret.indexOf("get")>=0) {
			System.out.println("SUCCESS: 0 2434 234 2343 23423 234234 233234");
		} else if (ret.indexOf("make_session")>=0) {
			fu.deleteFile("dumper.txt");
			System.out.println("SUCCESS");			
		} else {
			System.out.println("SUCCESS");
		}
	}
}
