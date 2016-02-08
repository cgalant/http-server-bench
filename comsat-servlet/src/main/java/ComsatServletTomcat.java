import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;

// 9022

public final class ComsatServletTomcat {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);

        PlaintextServlet.SERVER_NAME = "comsat-servlet-tomcat";

        final Tomcat tomcat = new Tomcat();
        final Context context = tomcat.addContext("/", new File("comsat-servlet/target").getAbsolutePath());

        Wrapper w = Tomcat.addServlet(context, "plaintext", PlaintextServlet.class.getName());
        w.addMapping("/hello");

        tomcat.setPort(9022);
        tomcat.getConnector().setAttribute("maxThreads", 100);
        tomcat.getConnector().setAttribute("acceptCount", 100000);

        tomcat.start();
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:9022/hello");
    }
}
