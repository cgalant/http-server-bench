package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public abstract class JettyLoadTargetBase extends LoadTargetBase {
    @Override
    protected final int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected final int getDefaultWorkParallelism() {
        return 100; // Used only for waits
    }

    @Override
    protected final void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        System.err.println("WARNING: Jetty servers use the 'maxProcessingParallelism' parameter only for delayed responses");

        final Server s = getJettyServer(port, backlog, maxIOP);
        s.start();

        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);

        System.err.println("SERVER UP");

        s.join();
    }

    protected abstract Server getJettyServer(int port, int backlog, int maxIOP) throws Exception;
}
