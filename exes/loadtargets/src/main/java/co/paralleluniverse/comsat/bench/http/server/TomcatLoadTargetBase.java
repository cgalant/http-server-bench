package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.apache.catalina.LifecycleException;

import java.io.File;

public abstract class TomcatLoadTargetBase extends LoadTargetBase {

    @Override
    protected final int getDefaultConnectionsBacklog() {
        return 65535;
    }

    @Override
    protected final int getDefaultIOParallelism() {
        return 200;
    }

    @Override
    protected final int getDefaultWorkParallelism() {
        return -1; // Unused
    }

    @Override
    protected final void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        System.err.println("WARNING: Tomcat servers don't use the 'maxProcessingParallelism' parameter");

        final org.apache.catalina.startup.Tomcat t = getTomcatServer(port, backlog, maxIOP, new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                t.stop();
            } catch (LifecycleException ignored) {}
        }));

        t.start();

        new Thread(() -> {
            t.getServer().await();
        }).start();

        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);

        System.err.println("SERVER UP");
    }

    protected abstract org.apache.catalina.startup.Tomcat getTomcatServer(int port, int backlog, int maxIOP, String contextRoot) throws Exception;
}
