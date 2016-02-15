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

public final class Netty {
    // cf.sync();
    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public ChannelFuture handlerServer(int port, SimpleChannelInboundHandler<Object> handler) {
        final ServerBootstrap b = new ServerBootstrap();

        b.option(ChannelOption.SO_BACKLOG, 65535);
        b.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 0);
        b.childOption(ChannelOption.TCP_NODELAY, true);
        b.childOption(ChannelOption.SO_REUSEADDR, true);
        b.childOption(ChannelOption.SO_LINGER, 0);
        // b.childOption(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        // b.childOption(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

        final ChannelInitializer<SocketChannel> childHandler = new SocketChannelChannelInitializer(handler);
        final NioEventLoopGroup group = new NioEventLoopGroup();
        return b.group(group)
            .channel(NioServerSocketChannel.class)
            .childHandler(childHandler)
            .bind(port);
    }

    private static class SocketChannelChannelInitializer extends ChannelInitializer<SocketChannel> {
        private final SimpleChannelInboundHandler<Object> handler;

        public SocketChannelChannelInitializer(SimpleChannelInboundHandler<Object> handler) {
            this.handler = handler;
        }

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpObjectAggregator(65536));
            pipeline.addLast(handler);
        }
    }

    private Netty() {}
}
