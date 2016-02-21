package co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.dispatch;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ServletAsyncDispatch extends HttpServlet {
    private static final byte[] TXT = HandlerUtils.TXT.getBytes(HandlerUtils.TXT_CR);

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
    public void init() throws ServletException {
        super.init();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                store(null);
            }
        }, 10, 10);
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Object results = request.getAttribute(RESULTS_ATTR);

        if (results == null) {
            request.setAttribute(RESULTS_ATTR, new Object());
            final AsyncContext async = request.startAsync();
            async.setTimeout(HandlerUtils.asyncTimeout);
            store(async);
            return;
        }

        HandlerUtils.handleDelayWithThread();
        {
            response.setContentType(HandlerUtils.CT);
            response.setHeader(HandlerUtils.HEAD_SERVER_KEY, HandlerUtils.server);
            try {
                response.getOutputStream().write(TXT);
            } catch (final IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }
}
