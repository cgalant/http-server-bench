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
}
