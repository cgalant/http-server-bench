import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.comsat.webactors.HelloWebActorOne;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.undertow.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.undertow.server.HttpServerExchange;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8021;
    }

    @Override
    protected int getDefaultConnectionsBacklog() {
        return 65535;
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 100;
    }

    @Override
    protected void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        final io.undertow.Undertow u = Undertow.handlerServer(port, backlog, maxIOP, maxProcessingP, new WebActorHandler(new MyContextProvider()));
        u.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }

    private static final Actor actor = new HelloWebActorOne();
    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

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
}
