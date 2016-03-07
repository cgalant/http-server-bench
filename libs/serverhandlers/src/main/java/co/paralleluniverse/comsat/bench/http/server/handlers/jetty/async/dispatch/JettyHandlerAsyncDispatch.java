package co.paralleluniverse.comsat.bench.http.server.handlers.jetty.async.dispatch;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JettyHandlerAsyncDispatch extends AbstractHandler {
    private static final ByteBuffer TXT = BufferUtil.toBuffer(HandlerUtils.TXT);

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

    public JettyHandlerAsyncDispatch() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                store(null);
            }
        }, 10, 10);
    }

    @Override
    final public void handle(String target, Request br, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Object results = request.getAttribute(RESULTS_ATTR);

        if (results == null) {
            request.setAttribute(RESULTS_ATTR, new Object());
            final AsyncContext async = request.startAsync();
            async.setTimeout(120000);
            store(async);
            return;
        }

        HandlerUtils.reqStart();
        HandlerUtils.handleDelayWithThread();
        {
            br.setHandled(true);
            br.getResponse().getHttpFields().add(HandlerUtils.CTJ);
            br.getResponse().getHttpFields().add(HandlerUtils.jettyServer);
            if (HandlerUtils.URL.equals(br.getPathInfo()))
                try {
                    br.getResponse().getHttpOutput().sendContent(TXT.slice());
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    HandlerUtils.reqEnd();
                }
        };
    }
}
