import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.comsat.ServletComsatSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Tomcat;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8023;
    }

    @Override
    protected int getDefaultConnectionsBacklog() {
        return 65535;
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 200;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return -1; // Unused
    }

    @Override
    protected void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        final org.apache.catalina.startup.Tomcat t = Tomcat.applicationEventListenerServer(port, backlog, maxIOP, WebActorInitializer.class, "build");
        System.err.println("WARNING: Tomcat servers don't use the 'maxProcessingParallelism' parameter");
        t.start();
        new Thread(() -> {
            t.getServer().await();
        }).start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }
}
