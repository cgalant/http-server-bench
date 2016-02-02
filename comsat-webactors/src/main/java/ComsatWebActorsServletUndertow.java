
import co.paralleluniverse.embedded.containers.UndertowServer;

// 9102

public final class ComsatWebActorsServletUndertow {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        HelloWebActor.SERVER_NAME = "comsat-webactors-servlet-undertow";
        new ServletActorServer(new UndertowServer(), 9102).start();
    }
}