import co.paralleluniverse.comsat.bench.http.server.JettyLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.ServletSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Jetty;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;

public final class Main extends JettyLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultPort() {
        return 8022;
    }

    @Override
    protected final Server getJettyServer(int port, int backlog, int maxIOP) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader()); // Seemingly necessary due to the container's intricated classloading structure
        return Jetty.applicationEventListenerServer(port, backlog, maxIOP, WebActorInitializer.class, true);
    }
}
