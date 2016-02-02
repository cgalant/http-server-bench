import co.paralleluniverse.embedded.containers.JettyServer;

// 9101

public final class ComsatWebActorsServletJetty {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new ServletActorServer(new JettyServer(), 9101).start();
    }
}
