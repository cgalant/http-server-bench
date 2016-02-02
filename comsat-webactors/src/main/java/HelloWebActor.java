import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.comsat.webactors.HttpRequest;
import co.paralleluniverse.comsat.webactors.HttpResponse;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;

import java.nio.ByteBuffer;

import static co.paralleluniverse.comsat.webactors.HttpResponse.error;
import static co.paralleluniverse.comsat.webactors.HttpResponse.ok;

public abstract class HelloWebActor extends BasicActor<Object, Void> {
    static String SERVER_NAME;

    private static final String HELLO_WORLD = "Hello, world!";
    private static final byte[] HELLO_WORLD_A = HELLO_WORLD.getBytes();
    private static final long DELAY = Long.parseLong(System.getProperty("delay", "0"));

    protected final Void handleOne() throws InterruptedException, SuspendExecution {
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
            req.getFrom().send(res.addHeader("Server", SERVER_NAME).build());
        }
        return null;
    }
}
