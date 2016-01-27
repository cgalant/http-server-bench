import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.comsat.webactors.HttpRequest;
import co.paralleluniverse.comsat.webactors.HttpResponse;
import co.paralleluniverse.comsat.webactors.WebActor;
import co.paralleluniverse.fibers.SuspendExecution;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static co.paralleluniverse.comsat.webactors.HttpResponse.error;
import static co.paralleluniverse.comsat.webactors.HttpResponse.ok;
import co.paralleluniverse.fibers.Fiber;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@WebActor(httpUrlPatterns = {"/hello"})
public final class HelloWebActor extends BasicActor<Object, Void> {
    private static final String HELLO_WORLD = "Hello, World!";
    private static final byte[] HELLO_WORLD_A = HELLO_WORLD.getBytes(StandardCharsets.ISO_8859_1);
    private static final long DELAY = Long.parseLong(System.getProperty("delay", "0"));

    private final ByteBuffer b = ByteBuffer.allocateDirect(HELLO_WORLD_A.length);

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public HelloWebActor() {
        b.clear();
        b.put(HELLO_WORLD_A);
        b.flip();
    }

    @Override
    protected final Void doRun() throws InterruptedException, SuspendExecution {
        //noinspection InfiniteLoopStatement
        for (;;) {
            final Object message = receive();
            if (message instanceof HttpRequest) {
                final HttpRequest req = (HttpRequest) message;
                HttpResponse.Builder res;
                switch (req.getRequestURI()) {
                    case "/hello":
                        if (DELAY > 0)
                            Fiber.sleep(DELAY);
                        b.rewind();
                        res = ok(self(), req, HELLO_WORLD).setContentType("text/plain");
                        break;
                    default:
                        res = error(self(), req, 404, "Not found");
                        break;
                }
                req.getFrom().send (
                    res
                        .addHeader("Server", "comsat-webactors")
                        .addHeader("Date", DATE_FORMAT.format(CALENDAR.getTime()))
                        .build()
                );
            }
        }
    }
}
