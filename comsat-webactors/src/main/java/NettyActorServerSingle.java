import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorImpl;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.comsat.webactors.WebMessage;
import co.paralleluniverse.comsat.webactors.netty.WebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
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

public final class NettyActorServerSingle {
    private static final Actor actor = new HelloWebActorOne();
    @SuppressWarnings("unchecked")
    private static final ActorRef<? extends WebMessage> actorRef = actor.spawn();

    private static final WebActorHandler.DefaultContextImpl context = new MyDefaultContextImpl();

    public final void start() throws Exception {
        b.bind(9105).sync();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9105");
        System.err.println("Server is up.");
    }

    final ServerBootstrap b = new ServerBootstrap();

    public NettyActorServerSingle() {
        b.option(ChannelOption.SO_BACKLOG, 65535);
        b.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);
        b.childOption(ChannelOption.TCP_NODELAY, true);
        b.childOption(ChannelOption.SO_REUSEADDR, true);
        b.childOption(ChannelOption.SO_LINGER, 0);
        // b.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        // b.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        final ChannelInitializer<SocketChannel> childHandler = new SocketChannelChannelInitializer();
        final NioEventLoopGroup group = new NioEventLoopGroup();
        b.group(group)
            .channel(NioServerSocketChannel.class)
            .childHandler(childHandler);
    }

    private static class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(new WebActorHandler(new MyWebActorContextProvider()));
        }

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
    }
}
