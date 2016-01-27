
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatWebActorsServletJetty

import co.paralleluniverse.embedded.containers.JettyServer;

// 9101

public final class ComsatWebActorsServletJetty {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new ServletActorServer(new JettyServer(), 9101).start();
    }
}
