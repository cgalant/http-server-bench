
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatWebActorsNettySingle

// 9105

public final class ComsatWebActorsNettySingle {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new NettyActorServerSingle().start();
    }
}
