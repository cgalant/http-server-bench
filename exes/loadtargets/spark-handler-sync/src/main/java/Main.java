import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.spark.SparkHandlerSync;
import co.paralleluniverse.comsat.bench.http.server.standalone.Spark;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultConnectionsBacklog() {
        return -1; // unused
    }

    @Override
    protected final int getDefaultIOParallelism() {
        return -1; // unused
    }

    @Override
    protected final int getDefaultWorkParallelism() {
        return 10000;
    }

    @Override
    protected final void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        Spark.startGet(port, maxProcessingP, new SparkHandlerSync());
        System.err.println("WARNING: Spark servers don't use the 'backlog' nor the 'maxIOParallelism' parameters");
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }
}
