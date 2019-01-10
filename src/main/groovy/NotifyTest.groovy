import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.Semaphore

class NotifyTest {

    static final Log log = LogFactory.getLog(NotifyTest.class)

    def go() {
        ExecutorService executor = Executors.newCachedThreadPool()
        final int permits = 2
        Semaphore sshLimits = new Semaphore(permits)

        def joeFutureMap = [:]
        def danFutureMap = [:]
        def searchDate = new Date()
        def count = 0
        (1..20).each {
            log.info("Summit JoeDo task " + (++count) + " for ip " + it)
            JoeDo joeDo = new JoeDo(it)
            Future future = executor.submit(joeDo)
            joeFutureMap[it] = future
        }
        count = 0
        (1..20).each {
            log.info("Summit DanDo task " + (++count) + " for ip " + it)
            DanDo danDo = new DanDo(it)
            Future future = executor.submit(danDo)
            danFutureMap[it] = future
        }

        // try to test Semaphore is released entilly
        log.info("acquireUninterruptibly Semaphore")
        sshLimits.acquireUninterruptibly(permits)
        log.info("Finally release all semaphores")
        sshLimits.release(permits)

        executor.shutdown()
    }
    static void main(args) {

        NotifyTest notifyTest = new NotifyTest()
        notifyTest.go()

    }
    private class JoeDo implements Runnable {
        def job

        JoeDo(job) {
            this.job = job
        }
        void run() {
            Thread.sleep(1000)
            if (job == 1) {
                Thread.sleep(10000)
            }
            log.info("JoeDo the job " + job)
        }
    }

    private class DanDo implements Runnable {
        def job
        DanDo(job) {
            this.job = job
        }
        void run() {
            Thread.sleep(1000)
            log.info("DanDo the job " + job)
        }
    }
}
