package co.paralleluniverse.comsat.bench.http.server;

public abstract class UndertowAsyncLoadTargetBase extends UndertowLoadTargetBase {
    @Override
    protected final int getDefaultWorkParallelism() {
        return 50;
    }

    @Override
    protected final int getDefaultIOParallelism() {
        return 50;
    }
}
