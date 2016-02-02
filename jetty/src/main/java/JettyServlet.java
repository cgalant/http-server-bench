import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

// 9091
// async by default, any cmd-line-arg for not async

public class JettyServlet extends HttpServlet {
    private static final byte[] HELLO_WORLD = "Hello, world!".getBytes(StandardCharsets.ISO_8859_1);
    private final static String RESULTS_ATTR = "org.eclipse.jetty.demo.client";

    private static boolean useasync;

    private static int num = 0;
    private static final AsyncContext acv[] = new AsyncContext[1000000];
    private static Lock l = new ReentrantLock();

    public JettyServlet(boolean async) {
        useasync = async;
    }

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

        if (useasync && results == null) {
            request.setAttribute(RESULTS_ATTR, new Object());
            final AsyncContext async = request.startAsync();
            async.setTimeout(120000);
            store(async);
            return;
        }
        response.setContentType("text/plain");
        response.setHeader("Server", useasync ? "jetty-servlet-async" : "jetty-servlet");
        response.getOutputStream().write(HELLO_WORLD);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    static Server setup(int port, boolean async) throws Exception {
        final JettyServlet rest = new JettyServlet(async);
        final Timer timer = new Timer();
        if (useasync) timer.schedule(new TimerTask() {
            public void run() {
                store(null);
            }
        }, 10, 10);

        final Server server = new Server(port);
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        final ServletHolder holder = new ServletHolder(rest);
        context.addServlet(holder, "/hello");
        holder.setAsyncSupported(useasync);
        server.setHandler(context);
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) setup(9091, true);
        else setup(9091, false);
    }
}
