package co.paralleluniverse.comsat.bench.http.server.handlers.utow.async;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.SameThreadExecutor;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public final class UtowHandlerAsyncQueue implements HttpHandler {
    final HttpServerExchange[] ac1 = new HttpServerExchange[100000], ac2 = new HttpServerExchange[100000];
    final byte[] TXT = HandlerUtils.TXT.getBytes(HandlerUtils.TXT_CR);
    final ByteBuffer buf = ByteBuffer.allocate(TXT.length).put(TXT);

    HttpServerExchange[] acv = ac1, copy = ac2;

    int num = 0;

    {
        buf.flip();
    }

    final LinkedBlockingQueue<HttpServerExchange[]> q = new LinkedBlockingQueue<>();

    synchronized final int swap() {
        final int n2 = num;
        copy = acv;
        acv = (acv == ac1) ? ac2 : ac1;
        num = 0;
        return n2;
    }

    final HttpServerExchange[] wrap() {
        final int n2 = swap();
        final HttpServerExchange a2[] = new HttpServerExchange[n2];
        System.arraycopy(copy, 0, a2, 0, n2);
        Arrays.fill(copy, 0, n2, null);
        return a2;
    }

    synchronized final void store(HttpServerExchange async) {
        acv[num++] = async;
    }

    final void reply(HttpServerExchange exchange) {
        HandlerUtils.reqStart();
        HandlerUtils.handleDelayWithTimer(() -> {
            try {
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, HandlerUtils.CT);
                exchange.getResponseHeaders().put(Headers.SERVER, HandlerUtils.server);
                exchange.getResponseSender().send(buf.duplicate());
            } finally {
                HandlerUtils.reqEnd();
            }
        });
    }

    final void reply(HttpServerExchange[] wrap) {
        for (HttpServerExchange aWrap : wrap) reply(aWrap);
    }

    final void reply() {
        final HttpServerExchange[] wrap = wrap();
        if (wrap.length == 0 || q.add(wrap)) return;
        reply(wrap);
    }

    final void poll() {
        try {
            reply(q.take());
        } catch (final Exception ignored) {}
    }

    final void timers() {
        final int delta = 10, nt = Runtime.getRuntime().availableProcessors();
        new Timer().schedule(new TimerTask() {
            public final void run() {
                reply();
            }
        }, delta, delta);

        for (int ii = 0; ii < nt; ii++) {
            new Thread(() -> {
                //noinspection InfiniteLoopStatement
                while (true) poll();
            }).start();
        }
    }

    {
        timers();
    }

    @Override
    public final void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(SameThreadExecutor.INSTANCE, () -> store(exchange));
    }
}
