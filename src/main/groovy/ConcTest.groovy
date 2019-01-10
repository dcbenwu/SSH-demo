import coriant.cats.sbi.TL1TransportInf
import coriant.cats.sbi.ssh.SshClient
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Semaphore

class ConcTest {
    private final static Log log = LogFactory.getLog(ConcTest.class)
    private final String port = "3183";
    private final String user = "admin1";
    private final String passwd = "1Transport!"

    def sshConcurGo(List<String> ipArr) {
        ExecutorService executor = Executors.newCachedThreadPool()
        final int permits = 20
        Semaphore sshLimits = new Semaphore(permits)

        def futureMap = [:]
        def searchDate = new Date()
        def count = 0
        ipArr.each {
            log.info("Summit ssh task " + (++count) + " for ip " + it)
            SSHTask sshTask = new SSHTask(it, port, user, passwd, sshLimits, searchDate)
            Future future = executor.submit(sshTask)
            futureMap[it] = future
        }

        // try to test Semaphore is released entilly
        log.info("acquireUninterruptibly Semaphore")
        sshLimits.acquireUninterruptibly(permits)
        log.info("Finally release all semaphores")
        sshLimits.release(permits)

        executor.shutdown()
    }

    static void main(args) {
        def ipArr = ["172.29.202.60", "172.29.157.18"]
        def longIpArr =[]
        (1..255).each {
            longIpArr.add("172.29.202." + it)
        }
        def concTest = new ConcTest()
        concTest.sshConcurGo(longIpArr)
    }

    private class SSHTask implements Runnable {
        private String ip;
        private String port;
        private String user;
        private String passwd;
        private Semaphore sshLimits;
        private Date searchDate;

        private String logTag;

        SSHTask(ip, port, user, passwd, sshLimits, searchDate) {
            this.ip = ip;
            this.port = port;
            this.user = user;
            this.passwd = passwd;
            this.sshLimits = sshLimits;
            this.searchDate = searchDate;

            this.logTag = "Tag ip " + ip + ": "
            log.info("SSHTask initial on " + ip);
        }

        @Override
        void run() {
            try {
                log.info(logTag + "Try to start SSHTask")
                def semaphoreBegin = System.currentTimeMillis()
                sshLimits.acquire()
                def semaphoreEnd = System.currentTimeMillis()
                log.info(logTag + "Acquire semaphore takes " + (semaphoreEnd - semaphoreBegin) + " us")
                TL1TransportInf sshClient = new SshClient();
                String ret = sshClient.loginTl1(ip, port, user, passwd, "^.*>\$");
                log.info(logTag + "Login result: " + ret);
                if (sshClient.isConnected()) {
                    try {
                        String cmd = "act-user::Admin1:123::1Transport!;";
                        log.info(logTag + "Sender started.................");
                        Thread.sleep(1000);
                        sshClient.sendCommandLine(cmd);
                        while (true) {
                            String outman = sshClient.getOutput();
                            if (outman == null) continue;
                            //if (outman.contains("act-user::Admin1:123")) continue;
                            if (outman.contains(";")) {
                                log.info(logTag + outman);
                                break;
                            }
                            log.info(logTag + "\n" + outman);
                        }
                        Thread.sleep(3000);
                        cmd = "INH-MSG-ALL:::234;";
                        sshClient.sendCommandLine(cmd);
                        while (true) {
                            String outman = sshClient.getOutput();
                            if (outman == null) continue;
                            //if (outman.contains(cmd)) continue;
                            if (outman.contains(";")) {
                                log.info(logTag + outman);
                                break;
                            }
                            log.info(logTag + "\n" + outman);
                        }
                        Thread.sleep(3000);
                        cmd = "rtrv-inv::all:tag456:::;";
                        sshClient.sendCommandLine(cmd);

                        while (true) {
                            String outman = sshClient.getOutput();
                            if (outman == null) continue;
                            //if (outman.contains(cmd)) continue;
                            if (outman.contains(";")) {
                                log.info(logTag + outman);
                                break;
                            }
                            log.info(logTag + "\n" + outman);
                        }

                        cmd = "canc-user::Admin1:q54321::;";
                        // act-user::Admin1:123::1Transport!;
                        // canc-user::Admin1:tag::;
                        sshClient.sendCommandLine(cmd);
                        log.info(logTag + sshClient.getOutput());
                        log.info(logTag + sshClient.getOutput());

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        log.error(logTag+e.getMessage(), e)
                    }
                }

                sshClient.stop();

            }catch (Exception ex) {
                ex.printStackTrace();
                log.error(logTag+ex.getMessage(), ex)
            } finally {
                log.info(logTag + "Release semaphore")
                sshLimits.release()
            }
        }
    }
}
