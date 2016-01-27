
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatServletUndertow

import co.paralleluniverse.embedded.containers.EmbeddedServer;
import co.paralleluniverse.embedded.containers.UndertowServer;

// 9099

public final class ComsatServletUndertow {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        final EmbeddedServer server = new UndertowServer();
        server.setPort(9099);
	server.addServlet("plaintext", PlaintextServlet.class, "/plaintext");
        server.start();
    }
}
