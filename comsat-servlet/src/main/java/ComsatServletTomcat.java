
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatServletTomcat

import co.paralleluniverse.embedded.containers.EmbeddedServer;
import co.paralleluniverse.embedded.containers.TomcatServer;

// 9100

public final class ComsatServletTomcat {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        final EmbeddedServer server = new TomcatServer("comsat-servlet/target");
        server.setPort(9100);
        server.addServlet("plaintext", PlaintextServlet.class, "/hello");
        server.start();
    }
}
