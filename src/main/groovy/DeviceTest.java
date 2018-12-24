import coriant.cats.sbi.ConsumerImpl;
import coriant.cats.sbi.TL1TransportInf;
import coriant.cats.sbi.ssh.SshClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DeviceTest {
    private final static Log log = LogFactory.getLog(DeviceTest.class);
    public static void main(String[] args) {
        TL1TransportInf sshClient = new SshClient();

        String ret = sshClient.loginTl1("172.29.202.60", "3183", "admin1", "1Transport!", ">");

        log.info("Login result: " + ret);

        if (sshClient.isConnected()) {
            try {
                String cmd = "act-user::Admin1:123::1Transport!;";
                log.info("Sender started.................");
                Thread.sleep(1000);
                sshClient.sendCommandLine(cmd);
                while (true) {
                    String outman = sshClient.getOutput();
                    if(outman == null) continue;
                    if(outman.contains("act-user::Admin1:123")) continue;
                    if (outman.contains(";")) {
                        log.info(outman);
                        break;
                    }
                    log.info(outman);
                }
                Thread.sleep(3000);
                cmd = "INH-MSG-ALL:::234;";
                sshClient.sendCommandLine(cmd);
                while (true) {
                    String outman = sshClient.getOutput();
                    if(outman == null) continue;
                    if(outman.contains(cmd)) continue;
                    if (outman.contains(";")) {
                        log.info(outman);
                        break;
                    }
                    log.info(outman);
                }
                Thread.sleep(3000);
                cmd = "rtrv-inv::all:tag456:::;";
                sshClient.sendCommandLine(cmd);

                while (true) {
                    String outman = sshClient.getOutput();
                    if(outman == null) continue;
                    if(outman.contains(cmd)) continue;
                    if (outman.contains(";")) {
                        log.info(outman);
                        break;
                    }
                    log.info(outman);
                }

                cmd = "canc-user::Admin1:q54321::;";
                // act-user::Admin1:123::1Transport!;
                // canc-user::Admin1:tag::;
                sshClient.sendCommandLine(cmd);
                log.info(sshClient.getOutput());
                log.info(sshClient.getOutput());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sshClient.stop();


        System.out.println("End of program.");
    }

}
