package co.paralleluniverse.comsat.bench.http.server.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.util.concurrent.AtomicDouble;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class HandlerUtils {
    public static final String HEAD_SERVER_KEY = "Server";
    public static final String CONTENT_TYPE_KEY = "Content-Type";

    public static final String URL = "/hello";

    public static final String TXT = "Hello, World!";
    public static final Charset TXT_CR = StandardCharsets.ISO_8859_1;

    public static final String CT = "text/plain";
    public static final HttpField CTJ = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_PLAIN.asString());

    public static String server = "<UNSET>";
    public static HttpField jettyServer = new PreEncodedHttpField(HttpHeader.SERVER, server);

    public static long delay = 0L;
    public static long asyncTimeout = 3_600_000L; // 1h

    public static int workers = Runtime.getRuntime().availableProcessors();

    public static ScheduledExecutorService timerPool = Executors.newScheduledThreadPool(workers);

    public static void handleDelayWithThread() {
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (final InterruptedException ie) {
                ie.printStackTrace(System.err);
                throw new RuntimeException(ie);
            }
        }
    }

    @Suspendable
    public static void handleDelayWithStrand() {
        if (delay > 0) {
            try {
                Strand.sleep(delay);
            } catch (final InterruptedException ie) {
                ie.printStackTrace(System.err);
                throw new RuntimeException(ie);
            } catch (final SuspendExecution se) {
                throw new AssertionError(se);
            }
        }
    }

    public static void handleDelayWithTimer(Runnable r) {
        if (delay > 0) {
            timerPool.schedule(r, delay, TimeUnit.MILLISECONDS);
        } else {
            r.run();
        }
    }

    public static void reqStart() {
        final Date now = new Date();
        firstRequestStart.compareAndSet(null, now);
        lastRequestStart.set(now);
        startedRequests.incrementAndGet();

        concurrency.incrementAndGet();
        totConcurrency.addAndGet(concurrency.get());
        maxConcurrency.updateAndGet(val -> {
            long curr = concurrency.get();
            if (val < curr)
                return curr;
            else
                return val;
        });
        avgConcurrency.set(Math.round(totConcurrency.get() / startedRequests.get() * 100.D) / 100.D);
    }

    public static void reqEnd() {
        final Date now = new Date();
        firstRequestEnd.compareAndSet(null, now);
        lastRequestEnd.set(now);
        completedRequests.incrementAndGet();

        concurrency.decrementAndGet();
    }

    public static final class HandlerStats {
        public Date firstRequestStart, firstRequestEnd, lastRequestStart, lastRequestEnd;
        public long startedRequests, completedRequests, concurrency, maxConcurrency;
        public double avgConcurrency;
    }

    private static final AtomicReference<Date>
        firstRequestStart = new AtomicReference<>(null),
        lastRequestStart = new AtomicReference<>(null),
        firstRequestEnd = new AtomicReference<>(null),
        lastRequestEnd = new AtomicReference<>(null);
    private static final AtomicLong
        startedRequests = new AtomicLong(0L),
        completedRequests = new AtomicLong(0L);
    private static final AtomicLong
        concurrency = new AtomicLong(0L),
        maxConcurrency = new AtomicLong(0L),
        totConcurrency = new AtomicLong(0L);
    private static final AtomicDouble
        avgConcurrency = new AtomicDouble(0.0D);

    public static void resetStats() {
        firstRequestStart.set(null);
        lastRequestStart.set(null);
        firstRequestEnd.set(null);
        lastRequestEnd.set(null);
        startedRequests.set(0L);
        completedRequests.set(0L);
        concurrency.set(0L);
        maxConcurrency.set(0L);
        totConcurrency.set(0L);
        avgConcurrency.set(0.0D);
    }

    public static HandlerStats getStats() {
        final HandlerStats ret = new HandlerStats();
        ret.avgConcurrency = avgConcurrency.get();
        ret.completedRequests = completedRequests.get();
        ret.concurrency = concurrency.get();
        ret.firstRequestEnd = firstRequestEnd.get();
        ret.firstRequestStart = firstRequestStart.get();
        ret.lastRequestEnd = lastRequestEnd.get();
        ret.lastRequestStart = lastRequestStart.get();
        ret.maxConcurrency = maxConcurrency.get();
        ret.startedRequests = startedRequests.get();
        return ret;
    }

    private HandlerUtils() {}
}
