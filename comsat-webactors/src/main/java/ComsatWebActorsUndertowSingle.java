import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.undertow.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpServerExchange;
import org.xnio.Options;

// 9031

import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

public final class ComsatWebActorsUndertowSingle {
    private static final Actor actor = new HelloWebActorOne();

    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

    public ComsatWebActorsUndertowSingle() {
        server = Undertow.builder()
            .addHttpListener(9031, "localhost")

            .setDirectBuffers(true)

            .setIoThreads(100)
            .setWorkerThreads(100)

            .setBufferSize(1024)
            .setBuffersPerRegion(100)

            // .setSocketOption(Options.ALLOW_BLOCKING, true)
            .setSocketOption(Options.REUSE_ADDRESSES, true)
            // .setSocketOption(Options.CORK, true)
            // .setSocketOption(Options.USE_DIRECT_BUFFERS, true)
            // .setSocketOption(Options.BACKLOG, Integer.MAX_VALUE)
            // .setSocketOption(Options.RECEIVE_BUFFER, 2048)
            // .setSocketOption(Options.SEND_BUFFER, 2048)
            // .setSocketOption(Options.CONNECTION_HIGH_WATER, Integer.MAX_VALUE)
            // .setSocketOption(Options.CONNECTION_LOW_WATER, Integer.MAX_VALUE)
            // .setSocketOption(Options.READ_TIMEOUT, Integer.MAX_VALUE)
            // .setSocketOption(Options.WRITE_TIMEOUT, Integer.MAX_VALUE)
            // .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required

            // .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
            .setServerOption(UndertowOptions.ENABLE_CONNECTOR_STATISTICS, false)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)

            .setHandler(new WebActorHandler(new MyContextProvider())).build();
    }

    public final void start() throws Exception {
        server.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9104");
        System.err.println("Server is up.");
    }

    private final Undertow server;

    private static class MyContextProvider implements WebActorHandler.ContextProvider {
        @Override
        public final WebActorHandler.Context get(HttpServerExchange xch) {
            return new MyDefaultContextImpl();
        }

        private static class MyDefaultContextImpl extends WebActorHandler.DefaultContextImpl {
            @SuppressWarnings("unchecked")
            @Override
            public final ActorRef<? extends WebMessage> getRef() {
                return actorRef;
            }

            @Override
            public final boolean handlesWithWebSocket(String uri) {
                return false;
            }

            @Override
            public final boolean handlesWithHttp(String uri) {
                return true;
            }

            @Override
            public final boolean watch() {
                return false;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        HelloWebActor.SERVER_NAME = "comsat-webactors-undertow-single";
        new ComsatWebActorsUndertowSingle().start();
    }
}
