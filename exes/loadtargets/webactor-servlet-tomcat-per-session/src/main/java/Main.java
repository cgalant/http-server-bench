import co.paralleluniverse.comsat.bench.http.server.TomcatAsyncLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Tomcat;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;

public final class Main extends TomcatAsyncLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final org.apache.catalina.startup.Tomcat getTomcatServer(int port, int backlog, int maxIOP, String contextRoot) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader()); // Seemingly necessary due to the container's intricated classloading structure
        return Tomcat.applicationListenerServer(port, backlog, maxIOP, WebActorInitializer.class, contextRoot);
    }
}
