import co.paralleluniverse.comsat.bench.http.server.UndertowAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;

public final class Main extends UndertowAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader());
        return Undertow.applicationEventListenerServer(port, backlog, maxIOP, maxProcessingP, WebActorInitializer.class);
    }
}
