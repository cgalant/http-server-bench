import co.paralleluniverse.comsat.bench.http.server.UndertowLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;

public final class Main extends UndertowLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultPort() {
        return 8024;
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        return Undertow.applicationEventListenerServer(port, backlog, maxIOP, maxProcessingP, WebActorInitializer.class);
    }
}
