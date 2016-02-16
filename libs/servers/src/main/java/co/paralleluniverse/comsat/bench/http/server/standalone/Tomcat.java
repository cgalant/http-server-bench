package co.paralleluniverse.comsat.bench.http.server.standalone;
import co.paralleluniverse.comsat.bench.http.server.ServerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;

import javax.servlet.ServletContextListener;
import java.io.File;

public final class Tomcat {
    // tomcat.start();
    // new Thread(() -> {
    // tomcat.getServer().await();
    // }).start();
    // AbstractEmbeddedServer.waitUrlAvailable("...");

    public static org.apache.catalina.startup.Tomcat servletServer(int port, int backlog, int maxIOP, String servletClassName, String contextRoot) {
        final org.apache.catalina.startup.Tomcat t = getTomcat();
        final Context context = getContext(contextRoot, t);

        addServlet(context, servletClassName);

        configure(port, backlog, maxIOP, t);

        return t;
    }

    public static org.apache.catalina.startup.Tomcat applicationEventListenerServer(int port, int backlog, int maxIOP, Class<? extends ServletContextListener> c, String contextRoot) throws IllegalAccessException, InstantiationException {
        final org.apache.catalina.startup.Tomcat t = getTomcat();
        final StandardContext context = (StandardContext) getContext(contextRoot, t);

        addApplicationEventListener(c, context);
        context.setSessionTimeout(1); // minimum

        configure(port, backlog, maxIOP, t);

        return t;
    }

    private static void configure(int port, int backlog, int maxIOP, org.apache.catalina.startup.Tomcat t) {
        t.setPort(port);
        t.getConnector().setAttribute("acceptCount", backlog);
        t.getConnector().setAttribute("maxThreads", maxIOP);
    }

    private static void addApplicationEventListener(Class<? extends ServletContextListener> c, StandardContext context) throws InstantiationException, IllegalAccessException {
        context.addApplicationEventListener(c.newInstance());
    }

    private static void addServlet(Context context, String servletClassName) {
        final Wrapper w = org.apache.catalina.startup.Tomcat.addServlet(context, ServerUtils.SN, servletClassName);
        w.addMapping(HandlerUtils.CT);
    }

    private static Context getContext(String contextRoot, org.apache.catalina.startup.Tomcat t) {
        return t.addContext(ServerUtils.CP, new File(contextRoot).getAbsolutePath());
    }

    private static org.apache.catalina.startup.Tomcat getTomcat() {
        return new org.apache.catalina.startup.Tomcat();
    }

    private Tomcat() {}
}
