import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

// 9021

public final class ComsatServletJetty {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);

        PlaintextServlet.SERVER_NAME = "comsat-servlet-jetty";

        final Server server = new Server(new QueuedThreadPool(100, 2));
        final ServerConnector http = new ServerConnector(server);
        http.setPort(9021);
        http.setAcceptQueueSize(100000);
        server.addConnector(http);

        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        final ServletHolder holder = new ServletHolder(new PlaintextServlet());
        context.addServlet(holder, "/hello");
        holder.setAsyncSupported(true);

        server.setHandler(context);
        server.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9021/hello");
    }
}
