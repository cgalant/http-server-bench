import co.paralleluniverse.fibers.SuspendExecution;

public final class HelloWebActorOne extends HelloWebActor {
    @Override
    protected final Void doRun() throws InterruptedException, SuspendExecution {
        //noinspection InfiniteLoopStatement
        for (;;) {
            handleOne();
        }
    }
}
