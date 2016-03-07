import co.paralleluniverse.comsat.bench.http.server.UndertowAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;
import co.paralleluniverse.comsat.webactors.undertow.AutoWebActorHandler;

public final class Main extends UndertowAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleHandlerServer(port, backlog, maxIOP, maxProcessingP, new AutoWebActorHandler());
    }
}
