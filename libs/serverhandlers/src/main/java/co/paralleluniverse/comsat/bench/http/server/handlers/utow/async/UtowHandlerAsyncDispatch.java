package co.paralleluniverse.comsat.bench.http.server.handlers.utow.async;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.ByteBuffer;

import java.util.Timer;
import java.util.TimerTask;

public final class UtowHandlerAsyncDispatch implements HttpHandler {
    int num = 0;
    final HttpServerExchange acv[] = new HttpServerExchange[1000000];

    synchronized final void store(HttpServerExchange async) {
        if (async == null) while (num > 0) {
            reply(acv[--num]);
            acv[num] = null;
        }
        else acv[num++] = async;
    }

    final byte[] TXT = HandlerUtils.TXT.getBytes(HandlerUtils.TXT_CR);
    final ByteBuffer buf = ByteBuffer.allocate(TXT.length).put(TXT);

    {
        buf.flip();
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

    {
        new Timer().schedule(new TimerTask() {
            public final void run() {
                UtowHandlerAsyncDispatch.this.store(null);
            }
        }, 10, 10);
    }

    @Override
    final public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(() -> store(exchange));
    }
}
