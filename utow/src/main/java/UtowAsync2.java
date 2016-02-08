import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.SameThreadExecutor;

import java.nio.ByteBuffer;
import java.util.Arrays;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

// 9001

public final class UtowAsync2 implements HttpHandler {
    final HttpServerExchange[]
        ac1 = new HttpServerExchange[100000], ac2 = new HttpServerExchange[100000];
    final byte[] bytes = "Hello, World!".getBytes();
    final ByteBuffer buf = ByteBuffer.allocate(bytes.length).put(bytes);

    HttpServerExchange[]acv = ac1, copy = ac2;

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


    public final void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(SameThreadExecutor.INSTANCE, () -> store(exchange));
    }

    final void reply(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "undertow-async2");
        exchange.getResponseSender().send(buf.duplicate());
    }

    final void reply(HttpServerExchange[] wrap) {
        for (int ii = 0; ii < wrap.length; ii++)
            reply(wrap[ii]);
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
        final int delta = 10, nt = 3;
        new Timer().schedule(new TimerTask() {
            public final void run() {
                reply();
            }
        }, delta, delta);

        for (int ii = 0; ii < nt; ii++) {
            new Thread(() -> {
                while (true) poll();
            }).start();
        }
    }

    {
        timers();
    }

    public static void main(String[] args) throws Exception {
        Undertow.builder()
            .addHttpListener(9001, "0.0.0.0")
            .setHandler(Handlers.path().addPrefixPath("/hello", new UtowAsync2()))
            .build()
            .start();
    }
}
