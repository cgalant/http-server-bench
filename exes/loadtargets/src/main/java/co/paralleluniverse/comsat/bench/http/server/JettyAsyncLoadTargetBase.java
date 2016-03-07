package co.paralleluniverse.comsat.bench.http.server;

public abstract class JettyAsyncLoadTargetBase extends JettyLoadTargetBase {
    @Override
    protected final int getDefaultWorkParallelism() {
        return 100;
    }
}
