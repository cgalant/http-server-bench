import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.comsat.webactors.HttpRequest;
import co.paralleluniverse.comsat.webactors.HttpResponse;
import co.paralleluniverse.comsat.webactors.WebActor;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static co.paralleluniverse.comsat.webactors.HttpResponse.error;
import static co.paralleluniverse.comsat.webactors.HttpResponse.ok;
// import java.nio.ByteBuffer;
// import java.nio.charset.StandardCharsets;

@WebActor(httpUrlPatterns = {"/hello"})
public final class HelloWebActorSingle extends BasicActor<Object, Void> {
    private static final String HELLO_WORLD = "Hello, World!";
    private static final byte[] HELLO_WORLD_A = HELLO_WORLD.getBytes();
    private static final long DELAY = Long.parseLong(System.getProperty("delay", "0"));

    @Override
    protected final Void doRun() throws InterruptedException, SuspendExecution {
        //noinspection InfiniteLoopStatement
        for (;;) {
            final Object message = receive();
            if (message instanceof HttpRequest) {
                final HttpRequest req = (HttpRequest) message;
                HttpResponse.Builder res;
                if ("/hello".equals(req.getRequestURI())) {
                    if (DELAY > 0)
                        Fiber.sleep(DELAY);
                    final ByteBuffer b = ByteBuffer.wrap(HELLO_WORLD_A);
                    res = ok(self(), req, b).setContentType("text/plain");
                } else {
                    res = error(self(), req, 404, "Not found");
                }
                req.getFrom().send(res.addHeader("Server", "comsat-webactors").build());
            }
        }
    }
}
