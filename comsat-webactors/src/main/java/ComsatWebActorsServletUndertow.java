import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletSessionConfig;
import io.undertow.servlet.core.InMemorySessionManagerFactory;
import org.xnio.Options;

// 9034

public final class ComsatWebActorsServletUndertow {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);

        HelloWebActor.SERVER_NAME = "comsat-webactors-servlet-undertow";

        final DeploymentInfo deployment = Servlets.deployment()
            .setDefaultSessionTimeout(1).setDeploymentName("")
            .setClassLoader(ClassLoader.getSystemClassLoader())
            .setContextPath("/");

        final ListenerInfo li = Servlets.listener(WebActorInitializer.class);
        deployment.addListener(li);

        final DeploymentManager servletsContainer = Servlets.defaultContainer().addDeployment(deployment);

        servletsContainer.deploy();

        final HttpHandler handler = servletsContainer.start();

        final Undertow server = Undertow.builder()
            .setHandler(handler)
            .setDirectBuffers(false) // With servlet-based actors there's a leak if using DIRECT

            .setIoThreads(100)
            .setWorkerThreads(100)

            .setBufferSize(1024)
            .setBuffersPerRegion(100)

            // .setSocketOption(Options.ALLOW_BLOCKING, true)
            .setSocketOption(Options.REUSE_ADDRESSES, true)
            // .setSocketOption(Options.CORK, true)
            // .setSocketOption(Options.USE_DIRECT_BUFFERS, true)
            // .setSocketOption(Options.BACKLOG, Integer.MAX_VALUE)
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

            .addHttpListener(9034, "localhost")
            .build();

        server.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9102/hello");
    }
}
