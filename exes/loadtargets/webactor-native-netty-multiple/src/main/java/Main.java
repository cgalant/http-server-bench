import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.standalone.Netty;
import co.paralleluniverse.comsat.webactors.netty.AutoWebActorHandler;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.netty.channel.ChannelFuture;

import java.util.Map;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8022;
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
        final ChannelFuture cf = Netty.singleHandlerServer(port, backlog, maxIOP, () -> new AutoWebActorHandler() {
            @Override
            protected AutoContextProvider newContextProvider(ClassLoader userClassLoader, Map<Class<?>, Object[]> actorParams) {
                return new AutoContextProvider(userClassLoader, actorParams, 1_000_000L /* ms */);
            }
        });
        cf.sync();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }
}

