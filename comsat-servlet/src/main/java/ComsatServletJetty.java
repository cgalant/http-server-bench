
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatServletJetty

import co.paralleluniverse.embedded.containers.EmbeddedServer;
import co.paralleluniverse.embedded.containers.JettyServer;

// 9096

public final class ComsatServletJetty {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        final EmbeddedServer server = new JettyServer();
        server.setPort(9096);
        server.addServlet("plaintext", PlaintextServlet.class, "/hello");
        server.start();
    }
}
