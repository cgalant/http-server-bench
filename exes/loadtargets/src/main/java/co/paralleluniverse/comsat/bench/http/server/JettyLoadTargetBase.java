package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public abstract class JettyLoadTargetBase extends LoadTargetBase {
    @Override
    protected final int getDefaultIOParallelism() {
        return -1; // Not used
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 10000;
    }

    @Override
    protected final void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        System.err.println("WARNING: Jetty servers don't use the 'maxIOParallelism' parameter");

        final Server s = getJettyServer(port, backlog, maxProcessingP);
        s.start();

        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);

        System.err.println("SERVER UP");

        s.join();
    }

    protected abstract Server getJettyServer(int port, int backlog, int maxIOP) throws Exception;
}
