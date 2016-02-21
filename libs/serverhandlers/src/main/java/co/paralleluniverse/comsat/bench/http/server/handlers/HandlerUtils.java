package co.paralleluniverse.comsat.bench.http.server.handlers;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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

    public static void printStats() {
        System.err.println("** First request started: " + fmt(firstRequestStart.get()));
        System.err.println("** Last request started: "  + fmt(lastRequestStart.get()));
        System.err.println("** Total requests started: " + startedRequests.get());
        System.err.println("** First request end: " + fmt(firstRequestEnd.get()));
        System.err.println("** Last request end: " + fmt(lastRequestEnd.get()));
        System.err.println("** Total requests completed: " + completedRequests.get());
    }

    public static void recordStart() {
        final Date now = new Date();
        firstRequestStart.compareAndSet(null, now);
        lastRequestStart.set(now);
        startedRequests.incrementAndGet();
    }

    public static void recordEnd() {
        final Date now = new Date();
        firstRequestEnd.compareAndSet(null, now);
        lastRequestEnd.set(now);
        completedRequests.incrementAndGet();
    }

    private static final AtomicReference<Date>
        firstRequestStart = new AtomicReference<>(null),
        lastRequestStart = new AtomicReference<>(null),
        firstRequestEnd = new AtomicReference<>(null),
        lastRequestEnd = new AtomicReference<>(null);
    private static final AtomicLong
        startedRequests = new AtomicLong(0L),
        completedRequests = new AtomicLong(0L);

    private static String fmt(Date d) {
        return dateFormat.format(d);
    }

    private HandlerUtils() {}

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
}
