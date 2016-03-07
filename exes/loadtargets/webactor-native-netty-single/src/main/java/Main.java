import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.comsat.webactors.HelloWebActorOne;
import co.paralleluniverse.comsat.bench.http.server.standalone.Netty;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.netty.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.FullHttpRequest;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return -1;
    }

    @Override
    protected void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        System.err.println("WARNING: Netty servers don't use the 'maxProcessingParallelism' parameter");
        final MyWebActorContextProvider ctxP = new MyWebActorContextProvider();
        final ChannelFuture cf = Netty.singleHandlerServer(port, backlog, maxIOP, () -> new WebActorHandler(ctxP));
        cf.sync();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }

    private static final Actor actor = new HelloWebActorOne();
    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

    private static final WebActorHandler.DefaultContextImpl context = new MyDefaultContextImpl();
    private static class MyWebActorContextProvider implements WebActorHandler.WebActorContextProvider {
        @Override
        public WebActorHandler.Context get(FullHttpRequest req) {
            return context;
        }
    }

    private static class MyDefaultContextImpl extends WebActorHandler.DefaultContextImpl {
        @Override
        public final String getId() {
            return "CONSTANT";
        }

        @Override
        public final void restart(FullHttpRequest r) {
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
            return WatchPolicy.DONT_WATCH;
        }
    }
}
