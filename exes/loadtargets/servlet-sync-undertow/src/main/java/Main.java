import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.ServletSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8014;
    }

    @Override
    protected int getDefaultConnectionsBacklog() {
        return 65535;
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 100;
    }

    @Override
    protected void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        final io.undertow.Undertow u = Undertow.servletServer(port, backlog, maxIOP, maxProcessingP, ServletSync.class, true);
        u.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }
}
