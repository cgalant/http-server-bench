// 9105

public final class ComsatWebActorsNettySingle {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        HelloWebActor.SERVER_NAME = "comsat-webactors-netty-single";
        new NettyActorServerSingle().start();
    }
}