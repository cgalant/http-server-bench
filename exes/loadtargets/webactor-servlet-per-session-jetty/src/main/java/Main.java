import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.ServletSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8022;
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
        final Server s = Jetty.applicationEventListenerServer(port, backlog, maxIOP, WebActorInitializer.class, true);
        System.err.println("WARNING: jetty servers don't use the 'maxProcessingParallelism' parameter");
        s.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
        s.join();
    }
}
