import co.paralleluniverse.comsat.bench.http.server.UndertowLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.complete.ServletAsyncFJPComplete;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;

public final class Main extends UndertowLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultPort() {
        return 8008;
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleServletServer(port, backlog, maxIOP, maxProcessingP, ServletAsyncFJPComplete.class, true);
    }
}
