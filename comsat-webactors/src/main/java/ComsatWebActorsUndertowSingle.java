
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.4-jdk8.jar ComsatWebActorsUndertowSingle

// 9104

public final class ComsatWebActorsUndertowSingle {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new UndertowActorServerSingle().start();
    }
}
