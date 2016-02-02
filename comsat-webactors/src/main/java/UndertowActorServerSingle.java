import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.undertow.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;

public final class UndertowActorServerSingle {
    private static final Actor actor = new HelloWebActorOne();
    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

    public UndertowActorServerSingle() {
        server = Undertow.builder()
            .setDirectBuffers(true)
            .setIoThreads(100)
            .setWorkerThreads(100)
            .setBufferSize(1024)
            .setBuffersPerRegion(100)
            .addHttpListener(9104, "localhost")
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
        }
    }
}
