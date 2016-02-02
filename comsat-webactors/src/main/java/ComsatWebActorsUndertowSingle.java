// 9104

public final class ComsatWebActorsUndertowSingle {
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            System.setProperty("delay", args[0]);
        new UndertowActorServerSingle().start();
    }
}
