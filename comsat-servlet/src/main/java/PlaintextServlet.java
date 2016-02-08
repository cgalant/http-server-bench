import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class PlaintextServlet extends FiberHttpServlet {
    private static final byte[] HELLO_WORLD = "Hello, World!".getBytes(StandardCharsets.ISO_8859_1);

    static String SERVER_NAME = "";

    private final long DELAY = Long.parseLong(System.getProperty("delay", "0"));

    @Override
    @Suspendable
    protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (DELAY > 0) {
            try {
                Fiber.sleep(DELAY);
            } catch (final InterruptedException ie) {
                ie.printStackTrace(System.err);
                throw new RuntimeException(ie);
            } catch (final SuspendExecution se) {
                throw new AssertionError(se);
            }
        }
        resp.setContentType("text/plain");
        resp.setHeader("Server", SERVER_NAME);
        resp.getOutputStream().write(HELLO_WORLD);
    }
}
