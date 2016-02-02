import co.paralleluniverse.comsat.webactors.WebActor;
import co.paralleluniverse.fibers.SuspendExecution;

@WebActor(httpUrlPatterns = {"/hello"})
public final class HelloWebActorPerSession extends HelloWebActor {
    @Override
    protected final Void doRun() throws InterruptedException, SuspendExecution {
        return handleOne();
    }
}
