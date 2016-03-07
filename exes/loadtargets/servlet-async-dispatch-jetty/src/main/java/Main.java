import co.paralleluniverse.comsat.bench.http.server.JettyLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.dispatch.ServletAsyncDispatch;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import org.eclipse.jetty.server.Server;

public final class Main extends JettyLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final Server getJettyServer(int port, int backlog, int maxIOP) throws Exception {
        return Jetty.singleServletServer(port, backlog, maxIOP, new ServletAsyncDispatch(), true);
    }
}
