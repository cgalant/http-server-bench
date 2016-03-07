import co.paralleluniverse.comsat.bench.http.server.JettyAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import org.eclipse.jetty.server.Server;

public final class Main extends JettyAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final Server getJettyServer(int port, int backlog, int maxIOP) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader()); // Seemingly necessary due to the container's intricated classloading structure
        return Jetty.applicationEventListenerServer(port, backlog, maxIOP, WebActorInitializer.class, true);
    }
}
