import co.paralleluniverse.comsat.bench.http.server.UndertowLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.comsat.ServletComsatSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Undertow;

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
        return 8011;
    }

    @Override
    protected final io.undertow.Undertow getUndertowServer(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        return Undertow.singleServletServer(port, backlog, maxIOP, maxProcessingP, ServletComsatSync.class, true);
    }
}
