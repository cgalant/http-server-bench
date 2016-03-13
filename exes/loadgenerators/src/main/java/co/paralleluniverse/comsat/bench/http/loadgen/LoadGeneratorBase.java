package co.paralleluniverse.comsat.bench.http.loadgen;

import co.paralleluniverse.fibers.DefaultFiberScheduler;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.StrandFactory;
import co.paralleluniverse.strands.SuspendableCallable;
import co.paralleluniverse.strands.SuspendableRunnable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import com.pinterest.jbender.JBender;
import com.pinterest.jbender.events.TimingEvent;
import com.pinterest.jbender.events.recording.HdrHistogramRecorder;
import com.pinterest.jbender.events.recording.LoggingRecorder;
import com.pinterest.jbender.executors.Validator;
import com.pinterest.jbender.intervals.ConstantIntervalGenerator;
import com.pinterest.jbender.intervals.ExponentialIntervalGenerator;
import com.pinterest.jbender.intervals.IntervalGenerator;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static co.paralleluniverse.comsat.bench.Utils.fmt;

import static com.pinterest.jbender.events.recording.Recorder.record;
import static java.util.Arrays.asList;

public abstract class LoadGeneratorBase<Req, Res, Exec extends AutoCloseableRequestExecutor<Req, Res>, E extends Env<Req, Exec>> {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final ExecutorService e = Executors.newCachedThreadPool();
    public static final StrandFactory DEFAULT_FIBERS_SF = DefaultFiberScheduler.getInstance();

    private static final String H = "h";
    private static final String L = "l";
    private static final String P = "p";
    private static final String SMSY = "smsy";

    private E env;

    protected abstract E setupEnv(OptionSet options);

    public final void run(final String[] args, final StrandFactory sf) throws SuspendExecution, InterruptedException, ExecutionException, IOException {
        final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) logger;
            logbackLogger.setLevel(ch.qos.logback.classic.Level.INFO);
        }

        final OptionParser parser = new OptionParser();
        final OptionSpec<Integer> j = parser.acceptsAll(asList("j", "repetitions")).withOptionalArg().ofType(Integer.class).describedAs("The number of benchmark repetitions").defaultsTo(1);
        final OptionSpec<Integer> r = parser.acceptsAll(asList("r", "rate")).withOptionalArg().ofType(Integer.class).describedAs("The desired throughput, in requests per second");
        final OptionSpec<Integer> v = parser.acceptsAll(asList("v", "interval")).withOptionalArg().ofType(Integer.class).describedAs("The interval between requests, in nanoseconds");
        final OptionSpec<Integer> n = parser.acceptsAll(asList("n", "maxConcurrency")).withOptionalArg().ofType(Integer.class).describedAs("Maximum concurrency level");
        final OptionSpec<Integer> w = parser.acceptsAll(asList("w", "warmup")).withOptionalArg().ofType(Integer.class).describedAs("The number of requests used to warm up the load tester").defaultsTo(1_000);
        final OptionSpec<Integer> c = parser.acceptsAll(asList("c", "count")).withRequiredArg().ofType(Integer.class).describedAs("Requests count").defaultsTo(11_000);
        final OptionSpec<Boolean> k = parser.acceptsAll(asList("k", "cooKies")).withRequiredArg().ofType(Boolean.class).describedAs("Accept cookies (e.g. sessions)").defaultsTo(false);
        parser.acceptsAll(asList(P, "preGenerateRequests"));

        final OptionSpec<String> u = parser.acceptsAll(asList("u", "url")).withRequiredArg().ofType(String.class).describedAs("HTTP URL").defaultsTo("http://localhost:8000");
        final OptionSpec<String> z = parser.acceptsAll(asList("z", "monitorURL")).withRequiredArg().ofType(String.class).describedAs("Monitor control base HTTP URL").defaultsTo("http://localhost:9000/monitor");

        final OptionSpec<Long> x = parser.acceptsAll(asList("x", "hdrHistHighest")).withRequiredArg().ofType(Long.class).describedAs("HDR Histogram highest trackable value").defaultsTo(3_600_000L * 1_000_000_000L);
        final OptionSpec<Integer> d = parser.acceptsAll(asList("d", "hdrHistDigits")).withRequiredArg().ofType(Integer.class).describedAs("HDR Histogram number of significant value digits").defaultsTo(3);
        final OptionSpec<Double> s = parser.acceptsAll(asList("s", "hdrHistScalingRatio")).withRequiredArg().ofType(Double.class).describedAs("HDR Histogram output value unit scaling ratio").defaultsTo(1_000_000.0d);

        final OptionSpec<Integer> i = parser.acceptsAll(asList("i", "ioParallelism")).withRequiredArg().ofType(Integer.class).describedAs("Number of OS threads performing actual I/O").defaultsTo(Runtime.getRuntime().availableProcessors());
        final OptionSpec<Integer> m = parser.acceptsAll(asList("m", "maxConnections")).withRequiredArg().ofType(Integer.class).describedAs("Maximum number of concurrent connections").defaultsTo(Integer.MAX_VALUE);
        final OptionSpec<Integer> t = parser.acceptsAll(asList("t", "timeout")).withRequiredArg().ofType(Integer.class).describedAs("Connection timeout (ms)").defaultsTo(3_600_000);

        final OptionSpec<Integer> cmpi = parser.accepts("cmpi").withRequiredArg().ofType(Integer.class).describedAs("Client monitoring print interval (ms)").defaultsTo(100);
        parser.accepts(SMSY);
        final OptionSpec<Integer> smsi = parser.accepts("smsi").withRequiredArg().ofType(Integer.class).describedAs("Server monitoring sample interval (ms)").defaultsTo(100);
        final OptionSpec<Integer> smpi = parser.accepts("smpi").withRequiredArg().ofType(Integer.class).describedAs("Server monitoring print interval (ms)").defaultsTo(1000);

        final OptionSpec<Integer> rbs = parser.accepts("rbs").withRequiredArg().ofType(Integer.class).describedAs("Buffer size for generated requests").defaultsTo(-1);
        final OptionSpec<Integer> ebs = parser.accepts("ebs").withRequiredArg().ofType(Integer.class).describedAs("Buffer size for request completion events").defaultsTo(-1);

        final OptionSpec<Boolean> ss = parser.accepts("ss").withRequiredArg().ofType(Boolean.class).describedAs("Shutdown server after run").defaultsTo(true);

        parser.acceptsAll(asList(L, "logging"));

        parser.acceptsAll(asList(H, "?", "help"), "Show help").forHelp();

        final OptionSet options = parser.parse(args);

        int status = 0;

        if (!options.has(v) && !options.has(r) && !options.has(n)) {
            status = -1;
            System.out.println("ERROR: one of '-v', '-r', '-n' must be provided.\n");
        }

        if (status != 0 || options.has(H)) {
            parser.printHelpOn(System.err);
            System.exit(status);
        }

        final int reqs = options.valueOf(c);
        final int rbsV = options.valueOf(rbs) < 0 ? reqs : options.valueOf(rbs);
        final int ebsV = options.valueOf(ebs) < 0 ? reqs : options.valueOf(ebs);

        System.err.println(
            "\n=============== LOAD GENERATOR SETTINGS ==============\n" +
                "\t* Repetitions (-j): " + options.valueOf(j) + "\n" +
                "\t* Server HTTP URL (-u): GET " + options.valueOf(u) + "\n" +
                "\t* Sever monitor control base HTTP URL (-z): " + options.valueOf(z) + "\n" +
                "\t* Shutdown server after run (-ss): " + options.valueOf(ss) + "\n" +
                "\t" +
                (!options.has(v) ?
                    (!options.has(r) ?
                        "* Maximum concurrency level (-n): " + options.valueOf(n) :
                        "* Desired throughput for exponential interval generator (rps, -r): " + options.valueOf(r)
                    ) :
                    "* Constant interval between requests (ns, -v): " + options.valueOf(v)
                ) +
                "\n" +
                "\t* Maximum open connections (-m): " + options.valueOf(m) + "\n" +
                "\t* IO Parallelism (async only, -i): " + options.valueOf(i) + "\n" +
                "\t* Request timeout (-t): " + options.valueOf(t) + " ms\n" +
                "\t* Requests count (-c): " + options.valueOf(c) + "\n" +
                "\t\t- Warmup requests (-w): " + options.valueOf(w) + "\n" +
                "\t* Handle cookies (e.g. session, -k): " + options.valueOf(k) + "\n" +
                "\t* HDR histogram settings:\n" +
                "\t\t- Maximum (-x): " + options.valueOf(x) + "\n" +
                "\t\t- Digits (-d): " + options.valueOf(d) + "\n" +
                "\t\t- Scaling ratio (-s): " + options.valueOf(s) + "\n" +
                "\t* Monitoring settings (-1 = N/A):\n" +
                "\t\t- Client print interval (-cmpi): " + options.valueOf(cmpi) + " ms\n" +
                "\t\t- Server system monitoring (-smsy): " + options.has(SMSY) + "\n" +
                "\t\t- Server sample interval (-smsi): " + options.valueOf(smsi) + " ms\n" +
                "\t\t- Server print interval (-smpi): " + options.valueOf(smpi) + " ms\n" +
                "\t* Buffer size settings:\n" +
                "\t\t- Generated requests (-rbs, equals count if pre-generating): " + rbsV + "\n" +
                "\t\t- Request completion events (-ebs): " + ebsV + "\n" +
                "\t* Pre-generate requests (-p): " + options.has(P) + "\n" +
                "\t* Logging (-l): " + options.has("l") +
            "\n======================================================\n"
        );

        env = setupEnv(options);
        final int runs = options.valueOf(j);

        final AtomicBoolean statsPrinted = new AtomicBoolean(false), serverShutdown = new AtomicBoolean(false);
        final AtomicReference<Histogram> histogram = new AtomicReference<>(null);
        final AtomicReference<ProgressLogger<Res, Exec>> resExecProgressLogger = new AtomicReference<>(null);
        final AtomicReference<Date> start = new AtomicReference<>(null);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!statsPrinted.get()) {
                printStats(options, s, histogram.get(), start.get(), resExecProgressLogger.get());
            }
            if (options.valueOf(ss) && !serverShutdown.get()) {
                try {
                    System.err.println("Shutting down server");
                    simpleBlockingGET(options.valueOf(z) + "/exit");
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }));
        for (int jj = 0 ; jj < runs ; jj++) {
            statsPrinted.set(false);
            System.err.println("======================= (" + (jj + 1) + "/" + runs + ") ========================");

            histogram.set(new Histogram(options.valueOf(x), options.valueOf(d)));
            try (final Exec requestExecutor =
                     env.newRequestExecutor(options.valueOf(i), options.valueOf(m), options.valueOf(t), options.valueOf(k))) {

                final int warms = options.valueOf(w);
                final int recordedReqs = reqs - warms;
                final Channel<Req> requestCh = Channels.newChannel(options.has(P) ? reqs : rbsV);
                final Channel<TimingEvent<Res>> eventCh = Channels.newChannel(ebsV);

                final String url = options.valueOf(u);
                final String monitoringURL = options.valueOf(z);
                final Fiber<Void> reqGen = startReqGen(reqs, requestCh, url);

                if (options.has(P))
                    reqGen.join();

                restartMonitoring(monitoringURL, monitoringURL + "/start?sampleIntervalMS=" + options.valueOf(smsi) + "&printIntervalMS=" + options.valueOf(smpi) + "&sysMon=" + options.has(SMSY));

                // Event recording, both HistHDR and logging
                resExecProgressLogger.set(new ProgressLogger<>(requestExecutor, recordedReqs, options.valueOf(cmpi)));
                HdrHistogramRecorder hdrHistogramRecorder = new HdrHistogramRecorder(histogram.get(), 1);
                final Fiber recorder;
                if (options.has("l"))
                    recorder = record(eventCh, hdrHistogramRecorder, new LoggingRecorder(LOG), resExecProgressLogger.get());
                else
                    recorder = record(eventCh, hdrHistogramRecorder, resExecProgressLogger.get());

                final Fiber<Void> jbender = startJBender(sf, r, v, n, options, requestExecutor, warms, requestCh, eventCh);
                start.set(new Date()); // Real start

                if (!options.has(P))
                    reqGen.join();

                jbender.join();

                System.err.println("Terminating iteration");
                new Fiber() {
                    @Override
                    public Object run() throws InterruptedException, SuspendExecution {
                        for (int i1 = 0 ; i1<100 ; i1++) {
                            Strand.sleep(100L);
                            System.err.print('.');
                        }
                        return null;
                    }
                }.start().join();
                recorder.join();

                System.err.println("\n======================================================");

                if (resExecProgressLogger.get() != null) {
                    System.err.println("Stopping progress logging...");
                    resExecProgressLogger.get().stopProgressLog();
                }

                System.err.println("Stopping server monitoring...");
                // Stop server monitoring
                try {
                    simpleBlockingGET(options.valueOf(z) + "/stop");
                } catch (final IOException e1) {
                    System.err.println("WARNING: couldn't stop monitoring (exception follows)");
                    e1.printStackTrace(System.err);
                }

                printStats(options, s, histogram.get(), start.get(), resExecProgressLogger.get());
                statsPrinted.set(true);
            } catch (final Throwable e) {
                statsPrinted.set(false);
                System.err.println("WARNING: repetition aborted (exception follows)");
                e.printStackTrace(System.err);
            }
        }

        if (options.valueOf(ss)) {
            System.err.println("Shutting down server");
            simpleBlockingGET(options.valueOf(z) + "/exit");
            serverShutdown.set(true);
        }

        System.err.println("Shutting down load generator");
        e.shutdown();
    }

    private void printStats(OptionSet options, OptionSpec<Double> s, Histogram histogram, Date start, ProgressLogger<Res, Exec> resExecProgressLogger) {
        // More stats
        final Date now = new Date();
        System.err.println("[" + fmt(now) + "] Run stats:");
        histogram.outputPercentileDistribution(System.err, options.valueOf(s));
        System.err.println();
        System.err.println("* Load started: " + (start != null ? dateFormat.format(start) : "N/A"));
        if (resExecProgressLogger != null) {
            System.err.println("* Successful requests: " + resExecProgressLogger.succ.get());
            System.err.println("* Failed requests: " + resExecProgressLogger.err.get());
            final Date firstReqEnd = resExecProgressLogger.start.get();
            System.err.println("* First request ended: " + (firstReqEnd != null ? dateFormat.format(firstReqEnd) : "N/A"));
            final Date lastReqEnd = resExecProgressLogger.end.get();
            System.err.println("* Last request ended: " + (lastReqEnd != null ? dateFormat.format(lastReqEnd) : "N/A"));
            System.err.println("* Seconds from load start: " + (lastReqEnd != null && start != null ? ((lastReqEnd.getTime() - start.getTime()) / 1_000.0D) : "N/A"));
            System.err.println("* Seconds from first request completed: " + (lastReqEnd != null && firstReqEnd != null ? ((lastReqEnd.getTime() - firstReqEnd.getTime()) / 1_000.0D) : "N/A") + "\n");
        } else {
            System.err.println("WARN: progress logger didn't start, no further stats available");
        }
    }

    private void restartMonitoring(String baseMonitoringURL, String startMonitoringURL) throws IOException {
        // Reset and start server monitoring
        simpleBlockingGET(baseMonitoringURL + "/reset");
        simpleBlockingGET(startMonitoringURL);
    }

    private Fiber<Void> startJBender(final StrandFactory sf, final OptionSpec<Integer> r, final OptionSpec<Integer> v, final OptionSpec<Integer> n, final OptionSet options, final Exec requestExecutor, final int warms, final Channel<Req> requestCh, final Channel<TimingEvent<Res>> eventCh) {
        //noinspection Convert2Lambda
        return new Fiber<>("jbender", new SuspendableCallable<Void>() {
            @Override
            public Void run() throws SuspendExecution, InterruptedException {
                if (options.has(n)) {
                    System.err.println("================== CONCURRENCY TEST ==================");
                    JBender.loadTestConcurrency(options.valueOf(n), warms, requestCh, requestExecutor, eventCh, sf);
                } else {
                    System.err.println("===================== RATE TEST ======================");
                    IntervalGenerator intervalGen = null;

                    if (options.has(r))
                        intervalGen = new ExponentialIntervalGenerator(options.valueOf(r));
                    else if (options.has(v))
                        intervalGen = new ConstantIntervalGenerator(options.valueOf(v));

                    JBender.loadTestThroughput(intervalGen, warms, requestCh, requestExecutor, eventCh, sf);
                }
                return null;
            }
        }).start();
    }

    private Fiber<Void> startReqGen(final int reqs, final Channel<Req> requestCh, final String uri) {
        // Requests generator
        //noinspection Convert2Lambda
        return new Fiber<Void>("req-gen", new SuspendableRunnable() {
            @Override
            public void run() throws SuspendExecution, InterruptedException {
                for (int ii = 0; ii < reqs; ++ii) {
                    final Req req;
                    try {
                        req = env.newRequest(uri);
                        requestCh.send(req);
                    } catch (final Exception e) {
                        System.err.println("Got exception when constructing request: " + e.getMessage());
                    }
                }

                requestCh.close();
            }
        }).start();
    }

    public static <X> void validate(Validator<X> validator, X v) {
        if (v != null)
            validator.validate(v);
    }

    private static void simpleBlockingGET(String httpUrl) throws IOException {
        HttpURLConnection _c = null;
        try {
            _c = (HttpURLConnection) new URL(httpUrl).openConnection();
        } finally {
            if (_c != null) {
                _c.getInputStream().close();
                _c.disconnect();
            }
            LOG.info("Performed GET " + httpUrl);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(LoadGeneratorBase.class);
}
