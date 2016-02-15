package co.paralleluniverse.comsat.bench.http.server.handlers.utow.sync;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

public class UndertowHandlerSyncSimple implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final HeaderMap headers = exchange.getResponseHeaders();
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, HandlerUtils.CT);
        headers.add(Headers.SERVER, HandlerUtils.server);
        exchange.getResponseSender().send(HandlerUtils.TXT);
    }
}
