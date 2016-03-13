package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.Monitoring;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static co.paralleluniverse.comsat.bench.Utils.fmt;

public class MonitoringServer extends Application<Configuration> {
    private static Timer ts;
    private static Timer tp;

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {}

    @Override
    public void run(Configuration cfg, Environment env) throws ClassNotFoundException {
        env.jersey().register(new MonitorResource());
    }

    @Path("/monitor")
    @Produces(MediaType.TEXT_PLAIN)
    public static final class MonitorResource {
        private static AtomicReference<Monitoring.SysStats> sysStats = new AtomicReference<>();
        private static AtomicReference<HandlerUtils.HandlerStats> handlerStats = new AtomicReference<>();
        private static AtomicLong samples = new AtomicLong(0L);
        private static boolean doSysMon = true;

        @Path("start") @GET public boolean start(@QueryParam("sampleIntervalMS") Long sampleIntervalMS, @QueryParam("printIntervalMS") Long printIntervalMS, @QueryParam("sysMon") Boolean sysMon) {
            startMonitoring(sampleIntervalMS, printIntervalMS, sysMon);
            return true;
        }

        public static void startMonitoring(Long sampleIntervalMS, Long printIntervalMS, Boolean sysMon) {
            tp = new Timer(true);
            ts = new Timer(true);
            final long actualSampleIntervalMS = sampleIntervalMS != null ? sampleIntervalMS : 100L;
            doSysMon = sysMon == null || sysMon;
            if (actualSampleIntervalMS > 0 && doSysMon)
                ts.schedule (
                    new TimerTask() {
                        @Override
                        public synchronized void run() {
                            sysStats.set(Monitoring.sampleSys());
                            handlerStats.set(HandlerUtils.getStats());
                            samples.incrementAndGet();
                        }
                    },
                    0,
                    actualSampleIntervalMS
                );

            final long actualPrintIntervalMS = printIntervalMS != null ? printIntervalMS : 1000L;
            if (actualPrintIntervalMS > 0)
                tp.schedule (
                    new TimerTask() {
                        @Override
                        public synchronized void run() {
                            getAndPrintStats();
                        }
                    },
                    actualPrintIntervalMS,
                    actualPrintIntervalMS
                );
            System.err.println (
                "Monitoring start request: sample interval = " + (actualSampleIntervalMS > 0 ? actualSampleIntervalMS + " ms" : "N/A") +
                    ", print interval = " + (actualPrintIntervalMS > 0 ? actualPrintIntervalMS + " ms" : "N/A") +
                    ", monitor system = " + doSysMon);
        }

        @Path("stop") @GET public boolean stop() {
            stopMonitoring();
            return true;
        }

        @Path("exit") @GET public boolean exit() {
            stopMonitoring();
            new Thread(() -> {
                System.err.println("Shutdown requested, terminating");
                System.exit(0);
            }).start();
            return true;
        }

        public static void stopMonitoring() {
            if (tp != null) tp.cancel();
            if (ts != null) ts.cancel();
            if (tp != null || ts != null) {
                tp = null;
                ts = null;
                System.err.println("Monitoring stopped, printing last sample");
                getAndPrintStats();
            } else {
                System.err.println("Monitoring has already been stopped");
            }
        }

        @Path("reset") @GET public boolean reset() {
            resetMonitoring();
            return true;
        }

        public static void resetMonitoring() {
            if (tp != null) tp.cancel();
            if (tp != null) ts.cancel();
            sysStats.set(null);
            Monitoring.resetSampleSys();
            HandlerUtils.resetStats();
            System.err.println("Monitoring reset");
        }

        public static void getAndPrintStats() {
            final HandlerUtils.HandlerStats hs = handlerStats.get();
            final Monitoring.SysStats ss = sysStats.get();
            printStats(hs, ss, doSysMon);
        }

        static void printStats(HandlerUtils.HandlerStats hs, Monitoring.SysStats ss, boolean doSysMon) {
            if (hs != null && (ss != null || !doSysMon)) {
                final Date now = new Date();
                final String nowS = fmt(now);
                System.err.println (
                    "[" + nowS + "] Monitoring sample: " + samples.get() + "\n" +
                        "* Handler stats:\n" +
                        "\t- # Reqs started: " + hs.startedRequests + "\n" +
                        "\t- # Reqs completed: " + hs.completedRequests + "\n" +
                        "\t- First request:\n" +
                        "\t\t- Start: " + fmt(hs.firstRequestStart) + "\n" +
                        "\t\t- End: " + fmt(hs.firstRequestEnd) + "\n" +
                        "\t- Last request:\n" +
                        "\t\t- Start: " + fmt(hs.lastRequestStart) + "\n" +
                        "\t\t- End: " + fmt(hs.lastRequestEnd) + "\n" +
                        "\t- Concurrency:" + "\n" +
                        "\t\t- Now: " + + hs.concurrency + "\n" +
                        "\t\t- Avg: " + hs.avgConcurrency + "\n" +
                        "\t\t- Max: " + hs.maxConcurrency + "\n" +
                        (doSysMon ?
                            "* System stats:\n" +
                                "\t- Avg OS load: " + ss.avgOSLoad + "\n" +
                                "\t- Total compilation time (ms): " + ss.totalCompilationTime + "\n" +
                                "\t- CPU:" + "\n" +
                                "\t\t- Now: " + ss.cpu + "\n" +
                                "\t\t- Avg: " + ss.avgCpu + "\n" +
                                "\t\t- Max: " + ss.maxCpu + "\n" +
                                "\t- Threads:" + "\n" +
                                "\t\t- Now: " + ss.threads + "\n" +
                                "\t\t- Avg: " + ss.avgThreads + "\n" +
                                "\t\t- Max: " + ss.maxThreads + "\n" +
                                "\t- Daemon threads:" + "\n" +
                                "\t\t- Now: " + ss.daemonThreads + "\n" +
                                "\t\t- Avg: " + ss.avgDaemonThreads + "\n" +
                                "\t\t- Max: " + ss.maxDaemonThreads + "\n" +
                                "\t- Heap mem:" + "\n" +
                                "\t\t- Now: " + ss.heapMem + "\n" +
                                "\t\t- Avg: " + ss.avgHeapMem + "\n" +
                                "\t\t- Max: " + ss.maxHeapMem + "\n" +
                                "\t- Non-heap mem:" + "\n" +
                                "\t\t- Now: " + ss.nonHeapMem + "\n" +
                                "\t\t- Avg: " + ss.avgNonHeapMem + "\n" +
                                "\t\t- Max: " + ss.maxNonHeapMem + "\n" +
                                "\t- GCs:" + "\n" +
                                "\t\t- Count: " + ss.totalGCs + "\n" +
                                "\t\t- Avg: " + ss.avgGCTimeMS + "\n" +
                                "\t\t- Max: " + ss.maxGCTimeMS + (ss.missedGCs ? " (WARN: missed GCs)" : "") + "\n"
                            : "") + "\n"
                );
            }

        }
    }
}
