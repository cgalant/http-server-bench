import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.bench.http.server.UndertowAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.comsat.webactors.HelloWebActorOne;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.undertow.WebActorHandler;
import io.undertow.server.HttpServerExchange;

public final class Main extends UndertowAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleHandlerServer(port, backlog, maxIOP, maxProcessingP, new WebActorHandler(new MyContextProvider()));
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
            @Override
            public String getId() {
                return "CONSTANT";
            }

            @Override
            public void restart(HttpServerExchange xch) {
                // Nothing to do
            }

            @SuppressWarnings("unchecked")
            @Override
            public final ActorRef<? extends WebMessage> getWebActor() {
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
            public final WebActorHandler.Context.WatchPolicy watch() {
                return WatchPolicy.DIE;
            }
        }
    }
}
