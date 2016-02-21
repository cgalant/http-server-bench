package co.paralleluniverse.comsat.bench.http.server.handlers.utow.sync;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.ByteBuffer;

public class UndertowHandlerSyncTechem implements HttpHandler {
    private static final ByteBuffer buffer;
    private static final String MESSAGE = HandlerUtils.TXT;

    static {
        buffer = ByteBuffer.allocateDirect(MESSAGE.length());
        try {
            buffer.put(MESSAGE.getBytes(HandlerUtils.TXT_CR));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        buffer.flip();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        HandlerUtils.recordStart();
        HandlerUtils.handleDelayWithThread();
        try {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, HandlerUtils.CT);
            exchange.getResponseHeaders().put(Headers.SERVER, HandlerUtils.server);
            exchange.getResponseSender().send(buffer.duplicate());
        } finally {
            HandlerUtils.recordEnd();
        }
    }
}
