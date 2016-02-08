import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 9011

public final class JettyAsyncDispatchServlet extends HttpServlet {
    private static final byte[] HELLO_WORLD = "Hello, World!".getBytes(StandardCharsets.ISO_8859_1);
    private final static String RESULTS_ATTR = "org.eclipse.jetty.demo.client";

    private static int num = 0;
    private static final AsyncContext acv[] = new AsyncContext[1000000];
    private static Lock l = new ReentrantLock();

    static void store(AsyncContext async) {
        try {
            l.lock();
            if (async == null)
                while (num > 0) {
                    final AsyncContext ac = acv[--num];
                    acv[num] = null;
                    if (ac != null) {
                        try {
                            ac.dispatch();
                        } catch (final Throwable ignored) {
                        }
                    }
                }
            else acv[num++] = async;
        } finally {
            l.unlock();
        }
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Object results = request.getAttribute(RESULTS_ATTR);

        if (results == null) {
            request.setAttribute(RESULTS_ATTR, new Object());
            final AsyncContext async = request.startAsync();
            async.setTimeout(120000);
            store(async);
            return;
        }
        response.setContentType("text/plain");
        response.setHeader("Server", "jetty-async-dispatch-servlet");
        response.getOutputStream().write(HELLO_WORLD);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    static Server setup(int port) throws Exception {
        final JettyAsyncDispatchServlet rest = new JettyAsyncDispatchServlet();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public final void run() {
                store(null);
            }
        }, 10, 10);

        final Server server = new Server(port);
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        final ServletHolder holder = new ServletHolder(rest);
        context.addServlet(holder, "/hello");
        holder.setAsyncSupported(true);
        server.setHandler(context);
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        setup(9011);
    }
}
