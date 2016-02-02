import co.paralleluniverse.embedded.containers.TomcatServer;

// 9103

public final class ComsatWebActorsServletTomcat {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new ServletActorServer(new TomcatServer("comsat-webactors/target"), 9103).start();
    }
}
