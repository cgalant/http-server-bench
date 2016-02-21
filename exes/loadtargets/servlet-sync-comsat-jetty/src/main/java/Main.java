import co.paralleluniverse.comsat.bench.http.server.JettyLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.comsat.ServletComsatSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import org.eclipse.jetty.server.Server;

public final class Main extends JettyLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 100;
    }

    @Override
    protected final int getDefaultPort() {
        return 8009;
    }

    @Override
    protected final Server getJettyServer(int port, int backlog, int maxIOP) throws Exception {
        return Jetty.singleServletServer(port, backlog, maxIOP, new ServletComsatSync(), true);
    }
}
