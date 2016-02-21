import co.paralleluniverse.comsat.bench.http.server.UndertowLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.undertow.AutoWebActorHandler;

public final class Main extends UndertowLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return 100;
    }

    @Override
    protected final int getDefaultPort() {
        return 8023;
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleHandlerServer(port, backlog, maxIOP, maxProcessingP, new AutoWebActorHandler());
    }
}
