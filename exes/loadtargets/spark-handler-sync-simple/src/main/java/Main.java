import co.paralleluniverse.comsat.bench.http.server.LoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.bench.http.server.handlers.spark.SparkHandlerSyncSimple;
import co.paralleluniverse.comsat.bench.http.server.standalone.Spark;
import co.paralleluniverse.embedded.containers.AbstractEmbeddedServer;

public final class Main extends LoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected int getDefaultPort() {
        return 8015;
    }

    @Override
    protected int getDefaultConnectionsBacklog() {
        return -1; // Unused
    }

    @Override
    protected int getDefaultIOParallelism() {
        return 100;
    }

    @Override
    protected int getDefaultWorkParallelism() {
        return -1; // unused
    }

    @Override
    protected void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception {
        Spark.startGet(port, maxIOP, new SparkHandlerSyncSimple());
        System.err.println("WARNING: Spark servers don't use the 'maxIOParallelism' nor the 'maxProcessingParallelism' parameters");
        AbstractEmbeddedServer.waitUrlAvailable("http://localhost:" + port + HandlerUtils.URL);
        System.err.println("SERVER UP");
    }
}
