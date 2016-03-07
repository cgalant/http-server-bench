package co.paralleluniverse.comsat.bench.http.server;

public abstract class TomcatAsyncLoadTargetBase extends TomcatLoadTargetBase {
    @Override
    protected final int getDefaultWorkParallelism() {
        return 100;
    }
}
