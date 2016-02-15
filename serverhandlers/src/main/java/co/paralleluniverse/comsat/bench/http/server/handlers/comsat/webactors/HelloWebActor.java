package co.paralleluniverse.comsat.bench.http.server.handlers.comsat.webactors;

import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.webactors.HttpRequest;
import co.paralleluniverse.comsat.webactors.HttpResponse;
import co.paralleluniverse.fibers.SuspendExecution;

import java.nio.ByteBuffer;

import static co.paralleluniverse.comsat.webactors.HttpResponse.error;
import static co.paralleluniverse.comsat.webactors.HttpResponse.ok;

public abstract class HelloWebActor extends BasicActor<Object, Void> {
    private static final byte[] TXT = HandlerUtils.TXT.getBytes();

    protected final Void handleOne() throws InterruptedException, SuspendExecution {
        final Object message = receive();
        if (message instanceof HttpRequest) {
            final HttpRequest req = (HttpRequest) message;
            HttpResponse.Builder res;
            if (HandlerUtils.URL.equals(req.getRequestURI())) {
                HandlerUtils.handleDelayWithStrand();

                final ByteBuffer b = ByteBuffer.wrap(TXT);
                res = ok(self(), req, b).setContentType(HandlerUtils.CT);
            } else {
                res = error(self(), req, 404, "Not found");
            }
            req.getFrom().send(res.addHeader(HandlerUtils.HEAD_SERVER_KEY, HandlerUtils.server).build());
        }
        return null;
    }
}
