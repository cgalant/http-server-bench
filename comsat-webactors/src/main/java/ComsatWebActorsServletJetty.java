import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

// 9032

public final class ComsatWebActorsServletJetty {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);

        HelloWebActor.SERVER_NAME = "comsat-webactors-servlet-jetty";

        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());

        final Server server = new Server(new QueuedThreadPool(100, 2));
        final ServerConnector http = new ServerConnector(server);
        http.setPort(9032);
        http.setAcceptQueueSize(100000);
        server.addConnector(http);

        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(1);

        context.addEventListener(WebActorInitializer.class.newInstance());

        server.setHandler(context);
        server.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9101/hello");
    }
}
