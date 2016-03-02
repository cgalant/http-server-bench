package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public abstract class UndertowLoadTargetBase extends LoadTargetBase {
    @Override
    protected int getDefaultIOParallelism() {
        return 5000;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 5000;
    }

    @Override
    protected final void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        final io.undertow.Undertow u = getUndertowServer(port, backlog, maxIOP, maxProcessingP);
        new Thread(u::start).start();

        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);

        System.err.println("SERVER UP");
    }

    protected abstract io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception;
}
