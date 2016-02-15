package co.paralleluniverse.comsat.bench.http.server.handlers.comsat.webactors;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.comsat.webactors.WebActor;
import co.paralleluniverse.fibers.SuspendExecution;

@WebActor(httpUrlPatterns = {HandlerUtils.URL})
public final class HelloWebActorPerSession extends HelloWebActor {
    @Override
    protected final Void doRun() throws InterruptedException, SuspendExecution {
        return handleOne();
    }
}
