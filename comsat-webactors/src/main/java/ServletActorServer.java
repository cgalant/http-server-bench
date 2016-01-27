import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.EmbeddedServer;

public final class ServletActorServer {
    public ServletActorServer(EmbeddedServer server, int port) {
        this.server = server;
        this.port = port;
    }

    public void start() throws Exception {
        server.setPort(port);
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        server.addServletContextListener(WebActorInitializer.class);
        server.enableWebsockets();
        server.start();
        System.err.println("Server is up.");
    }

    private final EmbeddedServer server;
    private final int port;
}
