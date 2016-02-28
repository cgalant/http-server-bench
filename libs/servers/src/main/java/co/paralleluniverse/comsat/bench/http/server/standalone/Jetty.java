package co.paralleluniverse.comsat.bench.http.server.standalone;

import co.paralleluniverse.comsat.bench.http.server.ServerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

public final class Jetty {
    // s.start();
    // s.join();
    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public static Server singleHandlerServer(int port, int backlog, int maxProcessingP, AbstractHandler h) {
        final Server s = server(port, backlog, maxProcessingP);
        s.setHandler(h);
        return s;
    }

    public static Server singleServletServer(int port, int backlog, int maxProcessingP, HttpServlet hs, boolean async) throws Exception {
        final Server s = server(port, backlog, maxProcessingP);
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(ServerUtils.CP);
        addServlet(hs, async, s);
        return s;
    }

    public static Server applicationEventListenerServer(int port, int backlog, int maxProcessingP, Class<? extends ServletContextListener> c, boolean async) throws Exception {
        final Server s = server(port, backlog, maxProcessingP);
        addApplicationEventListener(c, s);
        return s;
    }

    private static void addApplicationEventListener(Class<? extends ServletContextListener> c, Server s) throws IllegalAccessException, InstantiationException {
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.getSessionHandler().getSessionManager().setMaxInactiveInterval(1 /* secs */);
        context.addEventListener(c.newInstance());
        s.setHandler(context);
    }

    private static void addServlet(HttpServlet hs, boolean async, Server s) {
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath(ServerUtils.CP);
        final ServletHolder holder = new ServletHolder(hs);
        context.addServlet(holder, HandlerUtils.URL);
        holder.setAsyncSupported(async);
        s.setHandler(context);
    }

    private static Server server(int port, int backlog, int maxProcessingP) {
        final Server s = new Server(new QueuedThreadPool(maxProcessingP, Runtime.getRuntime().availableProcessors()));
        final ServerConnector http = new ServerConnector(s);
        http.setAcceptQueueSize(backlog);
        http.setPort(port);
        s.addConnector(http);
        return s;
    }

    private Jetty() {}
}
