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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 9012

public final class JettyAsyncCompleteServlet extends HttpServlet {
    private static final byte[] HELLO_WORLD = "Hello, World!".getBytes(StandardCharsets.ISO_8859_1);

    private static int num = 0;
    private static final AsyncContext acv[] = new AsyncContext[1000000];
    private static Lock l = new ReentrantLock();

    private static final ForkJoinPool pool = new ForkJoinPool();

    static void store(AsyncContext async) {
        try {
            l.lock();
            if (async == null)
                while (num > 0) {
                    final AsyncContext ac = acv[--num];
                    acv[num] = null;
                    if (ac != null) {
                        try {
                            exec(ac);
                        } catch (final Throwable ignored) {
                        }
                    }
                }
            else acv[num++] = async;
        } finally {
            l.unlock();
        }
    }

    private static void exec(AsyncContext ac) {
        pool.execute(() -> {
            final HttpServletResponse sr = (HttpServletResponse) ac.getResponse();
            sr.setContentType("text/plain");
            sr.setHeader("Server", "jetty-async-complete-servlet");
            try {
                sr.getOutputStream().write(HELLO_WORLD);
                ac.complete();
            } catch (final IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final AsyncContext async = request.startAsync();
        async.setTimeout(120000);
        store(async);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    static Server setup(int port) throws Exception {
        final JettyAsyncCompleteServlet rest = new JettyAsyncCompleteServlet();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
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
        setup(9012);
    }
}
