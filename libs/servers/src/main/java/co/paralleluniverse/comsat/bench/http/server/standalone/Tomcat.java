package co.paralleluniverse.comsat.bench.http.server.standalone;
import co.paralleluniverse.comsat.bench.http.server.ServerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;

import javax.servlet.ServletContextListener;
import java.io.File;

public final class Tomcat {
    // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    //     try {
    //         t.stop();
    //     } catch (LifecycleException ignored) {}
    // }));
    //
    // tomcat.start();
    //
    // new Thread(() -> {
    //   tomcat.getServer().await();
    // }).start();

    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public static org.apache.catalina.startup.Tomcat singleServletServer(int port, int backlog, int maxProcessingP, String servletClassName, String contextRoot, boolean async) {
        final org.apache.catalina.startup.Tomcat t = getTomcat();
        final Context context = getContext(contextRoot, t);

        addServlet(context, servletClassName, async);

        configure(port, backlog, maxProcessingP, t);

        return t;
    }

    public static org.apache.catalina.startup.Tomcat applicationListenerServer(int port, int backlog, int maxProcessingP, Class<? extends ServletContextListener> c, String contextRoot) throws IllegalAccessException, InstantiationException {
        final org.apache.catalina.startup.Tomcat t = getTomcat();
        final StandardContext context = (StandardContext) getContext(contextRoot, t);

        addApplicationListener(c, context);
        context.setSessionTimeout(1 /* Minimum = 1 minute */);

        configure(port, backlog, maxProcessingP, t);

        return t;
    }

    private static void configure(int port, int backlog, int maxProcessingP, org.apache.catalina.startup.Tomcat t) {
        t.setPort(port);
        t.getConnector().setAttribute("maxThreads", maxProcessingP);
        t.getConnector().setAttribute("acceptCount", backlog);

        t.getConnector().setAttribute("connectionLinger", 0);
        t.getConnector().setAttribute("connectionTimeout", 3600000);
    }

    private static void addApplicationListener(Class<? extends ServletContextListener> c, StandardContext context) throws InstantiationException, IllegalAccessException {
        context.addApplicationListener(c.getName());
    }

    private static void addServlet(Context context, String servletClassName, boolean async) {
        final Wrapper w = org.apache.catalina.startup.Tomcat.addServlet(context, ServerUtils.SN, servletClassName);
        w.addMapping(HandlerUtils.URL);
        w.setAsyncSupported(async);
    }

    private static Context getContext(String contextRoot, org.apache.catalina.startup.Tomcat t) {
        return t.addContext(ServerUtils.CP, new File(contextRoot).getAbsolutePath());
    }

    private static org.apache.catalina.startup.Tomcat getTomcat() {
        return new org.apache.catalina.startup.Tomcat();
    }

    private Tomcat() {}
}
