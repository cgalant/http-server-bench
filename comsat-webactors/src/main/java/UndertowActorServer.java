import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorImpl;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.undertow.WebActorHandler;
import io.undertow.Undertow;
import io.undertow.server.HttpServerExchange;

public final class UndertowActorServer {
	private static final Actor actor = new HelloWebActor();
	@SuppressWarnings("unchecked")
	private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

	public UndertowActorServer() {
			server = Undertow.builder()
					.addHttpListener(9104, "localhost")
					.setHandler(new WebActorHandler(new WebActorHandler.ContextProvider() {
			@Override
			public WebActorHandler.Context get(HttpServerExchange xch) {
				return new WebActorHandler.DefaultContextImpl() {
					@SuppressWarnings("unchecked")
					@Override
					public ActorRef<? extends WebMessage> getRef() {
						return actorRef;
					}

					@Override
					public Class<? extends ActorImpl<? extends WebMessage>> getWebActorClass() {
						return (Class<? extends ActorImpl<? extends WebMessage>>) actor.getClass();
					}
				};
			}
		})).build();
	}

	public final void start() throws Exception {
			server.start();
			System.err.println("Server is up.");
	}

	private final Undertow server;
}
