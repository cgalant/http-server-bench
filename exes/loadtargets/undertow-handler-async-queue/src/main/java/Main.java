import co.paralleluniverse.comsat.bench.http.server.UndertowAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.utow.async.UtowHandlerAsyncQueue;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;

public final class Main extends UndertowAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleHandlerServer(port, backlog, maxIOP, maxProcessingP, new UtowHandlerAsyncQueue());
    }
}
