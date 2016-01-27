
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatWebActorsServletUndertow

import co.paralleluniverse.embedded.containers.UndertowServer;

// 9102

public final class ComsatWebActorsServletUndertow {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new ServletActorServer(new UndertowServer(), 9102).start();
    }
}
