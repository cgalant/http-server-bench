import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.dispatch.ServletAsyncDispatch;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8003;
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
        final Server s = Jetty.servletServer(port, backlog, maxIOP, new ServletAsyncDispatch(), true);
        System.err.println("WARNING: Jetty servers don't use the 'maxProcessingParallelism' parameter");
        s.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
        s.join();
    }
}
