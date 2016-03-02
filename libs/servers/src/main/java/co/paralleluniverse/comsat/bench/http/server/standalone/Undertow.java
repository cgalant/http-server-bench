package co.paralleluniverse.comsat.bench.http.server.standalone;

import co.paralleluniverse.comsat.bench.http.server.ServerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import org.xnio.Options;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public final class Undertow {
    // new Thread(u::start).start();
    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public static io.undertow.Undertow singleHandlerServer(int port, int backlog, int maxIOP, int maxWorkP, HttpHandler handler) {
        //noinspection deprecation
        return io.undertow.Undertow.builder()
            .setHandler(handler)

            .setIoThreads(maxIOP)
            .setWorkerThreads(maxWorkP)

            .setDirectBuffers(true)
            .setBufferSize(1024)
            .setBuffersPerRegion(100)

            // .setSocketOption(Options.ALLOW_BLOCKING, true)
            .setSocketOption(Options.REUSE_ADDRESSES, true)
            // .setSocketOption(Options.CORK, true)
            // .setSocketOption(Options.USE_DIRECT_BUFFERS, true)
            .setSocketOption(Options.BACKLOG, backlog)
            // .setSocketOption(Options.RECEIVE_BUFFER, 2048)
            // .setSocketOption(Options.SEND_BUFFER, 2048)
            // .setSocketOption(Options.CONNECTION_HIGH_WATER, Integer.MAX_VALUE)
            // .setSocketOption(Options.CONNECTION_LOW_WATER, Integer.MAX_VALUE)
            // .setSocketOption(Options.READ_TIMEOUT, Integer.MAX_VALUE)
            // .setSocketOption(Options.WRITE_TIMEOUT, Integer.MAX_VALUE)
            // .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required

            // .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
            .setServerOption(UndertowOptions.ENABLE_CONNECTOR_STATISTICS, false)
            .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)

            .addHttpListener(port, "0.0.0.0")
            .build();
    }

    public static io.undertow.Undertow singleServletServer(int port, int backlog, int maxIOP, int maxWorkP, Class<? extends HttpServlet> c, boolean async) throws ServletException {
        final DeploymentInfo deployment = getDeploymentInfo();
        final HttpHandler h  = addSingleServletServletsContainer(deployment, c, async);
        return singleHandlerServer(port, backlog, maxIOP, maxWorkP, h);
    }

    public static io.undertow.Undertow applicationEventListenerServer(int port, int backlog, int maxIOP, int maxWorkP, Class<? extends ServletContextListener> c) throws ServletException {
        final DeploymentInfo deployment = getDeploymentInfo();
        final DeploymentManager servletsContainer = addApplicationEventListenerContainer(deployment, c);
        return singleHandlerServer(port, backlog, maxIOP, maxWorkP, servletsContainer.start());
    }

    private static HttpHandler addSingleServletServletsContainer(DeploymentInfo deployment, Class<? extends HttpServlet> c, boolean async) throws ServletException {
        final ServletInfo info = Servlets.servlet(ServerUtils.SN, c).addMapping(HandlerUtils.URL).setAsyncSupported(async);
        deployment.addServlet(info);
        // The code below must follow the code above
        final DeploymentManager servletsContainer = Servlets.defaultContainer().addDeployment(deployment);
        servletsContainer.deploy();
        return servletsContainer.start();
    }

    private static DeploymentManager addApplicationEventListenerContainer(DeploymentInfo deployment, Class<? extends ServletContextListener> c) {
        final ListenerInfo li = Servlets.listener(c);
        deployment.addListener(li);
        // The code below must follow the code above
        final DeploymentManager servletsContainer = Servlets.defaultContainer().addDeployment(deployment.setDefaultSessionTimeout(60) /* secs */);
        servletsContainer.deploy();
        return servletsContainer;
    }

    private static DeploymentInfo getDeploymentInfo() {
        return Servlets.deployment().setDeploymentName("")
            .setClassLoader(ClassLoader.getSystemClassLoader())
            .setContextPath(ServerUtils.CP);
    }
}
