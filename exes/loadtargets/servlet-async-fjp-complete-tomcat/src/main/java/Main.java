import co.paralleluniverse.comsat.bench.http.server.TomcatLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.handlers.servlet.async.complete.ServletAsyncFJPComplete;
import co.paralleluniverse.comsat.bench.http.server.standalone.Tomcat;

public final class Main extends TomcatLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultPort() {
        return 8007;
    }

    @Override
    protected final org.apache.catalina.startup.Tomcat getTomcatServer(int port, int backlog, int maxIOP, String contextRoot) throws Exception {
        return Tomcat.singleServletServer(port, backlog, maxIOP, ServletAsyncFJPComplete.class.getName(), contextRoot, true);
    }
}
