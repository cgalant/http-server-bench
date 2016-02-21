package co.paralleluniverse.comsat.bench.http.server.handlers.jetty.async.complete;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public final class JettyHandlerAsyncTimerComplete extends AbstractHandler {
    private static final ByteBuffer TXT = BufferUtil.toBuffer(HandlerUtils.TXT);
    private static final LinkedBlockingQueue<AsyncContext[]> q = new LinkedBlockingQueue<>();

    private static final AsyncContext[] ac1 = new AsyncContext[100000], ac2 = new AsyncContext[100000];

    private static AsyncContext[] acv = ac1, copy = ac2;

    private static int num = 0;

    final synchronized int swap() {
        final int n2 = num;
        copy = acv;
        acv = (acv == ac1) ? ac2 : ac1;
        num = 0;
        return n2;
    }

    final AsyncContext[] wrap() {
        final int n2 = swap();
        final AsyncContext a2[] = new AsyncContext[n2];
        System.arraycopy(copy, 0, a2, 0, n2);
        Arrays.fill(copy, 0, n2, null);
        return a2;
    }

    final synchronized void store(AsyncContext async) {
        acv[num++] = async;
    }

    final void reply(AsyncContext async) {
        HandlerUtils.handleDelayWithTimer(() -> {
            try {
                final Request br = (Request) async.getRequest();
                br.setHandled(true);
                br.getResponse().getHttpFields().add(HandlerUtils.CTJ);
                br.getResponse().getHttpFields().add(HandlerUtils.jettyServer);
                if (HandlerUtils.URL.equals(br.getPathInfo()))
                    br.getResponse().getHttpOutput().sendContent(TXT.slice());
                async.complete();
            } catch (final IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    final void reply(AsyncContext[] wrap) {
        for (final AsyncContext aWrap : wrap) reply(aWrap);
    }

    final void reply() {
        final AsyncContext[] wrap = wrap();
        if (wrap.length == 0 || q.add(wrap)) return;
        reply(wrap);
    }

    final void poll() {
        try {
            reply(q.take());
        } catch (final Exception ignored) {}
    }

    final void timers() {
        final int delta = 10, nt = 3;
        new Timer().schedule(new TimerTask() {
            public final void run() {
                reply();
            }
        }, delta, delta);

        for (int ii = 0; ii < nt; ii++)
            new Thread(() -> { //noinspection InfiniteLoopStatement
                while (true) poll();
            }).start();
    }

    {
        timers();
    }

    @Override
    final public void handle(String target, Request br, HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext async = request.startAsync();
        async.setTimeout(HandlerUtils.asyncTimeout);
        store(async);
    }
}
