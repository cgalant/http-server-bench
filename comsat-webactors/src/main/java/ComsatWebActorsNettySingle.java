import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.netty.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

// 9030

public final class ComsatWebActorsNettySingle {
    private static final Actor actor = new HelloWebActorOne();
    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

    private static final WebActorHandler.DefaultContextImpl context = new MyDefaultContextImpl();
        private static class MyWebActorContextProvider implements WebActorHandler.WebActorContextProvider {
            @Override
            public WebActorHandler.Context get(ChannelHandlerContext ctx, FullHttpRequest req) {
                return context;
            }
        }
    }

    private static class MyDefaultContextImpl extends WebActorHandler.DefaultContextImpl {
        @SuppressWarnings("unchecked")
        @Override
        public ActorRef<? extends WebMessage> getRef() {
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
