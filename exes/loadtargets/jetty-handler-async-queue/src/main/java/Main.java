import co.paralleluniverse.comsat.bench.http.server.JettyAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.jetty.async.complete.JettyHandlerAsyncQueue;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import org.eclipse.jetty.server.Server;

public final class Main extends JettyAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final Server getJettyServer(int port, int backlog, int maxIOP) {
        return Jetty.singleHandlerServer(port, backlog, maxIOP, new JettyHandlerAsyncQueue());
    }
}
