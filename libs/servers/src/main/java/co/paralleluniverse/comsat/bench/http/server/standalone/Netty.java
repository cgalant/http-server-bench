package co.paralleluniverse.comsat.bench.http.server.standalone;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.concurrent.Callable;

public final class Netty {
    // cf.sync();
    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public static ChannelFuture singleHandlerServer(int port, int backlog, int maxIOP, Callable<SimpleChannelInboundHandler<Object>> handlerProvider) {
        final ServerBootstrap b = new ServerBootstrap();

        b.option(ChannelOption.SO_BACKLOG, backlog);
        b.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);
        b.childOption(ChannelOption.TCP_NODELAY, true);
        b.childOption(ChannelOption.SO_REUSEADDR, true);
        // b.childOption(ChannelOption.SO_LINGER, 0);
        // b.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        // b.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        final ChannelInitializer<SocketChannel> childHandler = new SocketChannelChannelInitializer(handlerProvider);
        final NioEventLoopGroup group = new NioEventLoopGroup(maxIOP);
        b.group(group)
            .channel(NioServerSocketChannel.class)
            .childHandler(childHandler);
        return b.bind(port);
    }

    private static final class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {
        private final Callable<SimpleChannelInboundHandler<Object>> handlerProvider;

        public SocketChannelChannelInitializer(Callable<SimpleChannelInboundHandler<Object>> handlerProvider) {
            this.handlerProvider = handlerProvider;
        }

        @Override
        public final void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(handlerProvider.call());
        }
    }

    private Netty() {}
}
