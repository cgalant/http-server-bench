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

public final class HandlerUtils {
    public static final String HEAD_SERVER_KEY = "Server";
    public static final String CONTENT_TYPE_KEY = "Content-Type";

    public static final String URL = "/hello";

    public static final String TXT = "Hello, World!";
    public static final Charset TXT_CR = StandardCharsets.ISO_8859_1;

    public static final String CT = "text/plain";
    public static final HttpField CTJ = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_PLAIN.asString());

    public static final long ASYNC_TIMEOUT = 120000;

    public static String server = "<UNSET>";
    public static HttpField jettyServer = new PreEncodedHttpField(HttpHeader.SERVER, server);

    private static final long DELAY = Long.parseLong(System.getProperty("delay", "0"));

    public static void handleDelayWithThread() {
        if (DELAY > 0) {
            try {
                Thread.sleep(DELAY);
            } catch (final InterruptedException ie) {
                ie.printStackTrace(System.err);
                throw new RuntimeException(ie);
            }
        }
    }

    @Suspendable
    public static void handleDelayWithStrand() {
        if (DELAY > 0) {
            try {
                Strand.sleep(DELAY);
            } catch (final InterruptedException ie) {
                ie.printStackTrace(System.err);
                throw new RuntimeException(ie);
            } catch (final SuspendExecution se) {
                throw new AssertionError(se);
            }
        }
    }

    private HandlerUtils() {}
}
