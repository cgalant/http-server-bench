import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

// 9033

public final class ComsatWebActorsServletTomcat {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);

        HelloWebActor.SERVER_NAME = "comsat-webactors-servlet-tomcat";

        final Tomcat tomcat = new Tomcat();
        final StandardContext context =
            (StandardContext) tomcat.addContext("/", new File("comsat-webactors/target").getAbsolutePath());

        context.addApplicationEventListener(WebActorInitializer.class.newInstance());
        context.setSessionTimeout(1);

        tomcat.setPort(9033);
        tomcat.getConnector().setAttribute("maxThreads", 100);
        tomcat.getConnector().setAttribute("acceptCount", 100000);

        tomcat.start();
        new Thread(() -> {
            tomcat.getServer().await();
        }).start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9103/hello");
    }
}
