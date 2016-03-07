import co.paralleluniverse.comsat.bench.http.server.TomcatAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.complete.ServletAsyncFJP;
import co.paralleluniverse.comsat.bench.http.server.standalone.Tomcat;

public final class Main extends TomcatAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final org.apache.catalina.startup.Tomcat getTomcatServer(int port, int backlog, int maxIOP, String contextRoot) throws Exception {
        return Tomcat.singleServletServer(port, backlog, maxIOP, ServletAsyncFJP.class.getName(), contextRoot, true);
    }
}
