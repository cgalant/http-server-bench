import co.paralleluniverse.comsat.bench.http.server.TomcatLoadTargetBase;
import co.paralleluniverse.comsat.bench.http.server.standalone.Tomcat;
import co.paralleluniverse.comsat.webactors.servlet.WebActorInitializer;

public final class Main extends TomcatLoadTargetBase {
    public static void main(String[] args) throws Exception {
        new Main().run(args);
    }

    @Override
    protected final int getDefaultWorkParallelism() {
        return 100;
    }

    @Override
    protected final int getDefaultPort() {
        return 8025;
    }

    @Override
    protected final org.apache.catalina.startup.Tomcat getTomcatServer(int port, int backlog, int maxIOP, String contextRoot) throws Exception {
        WebActorInitializer.setUserClassLoader(ClassLoader.getSystemClassLoader()); // Seemingly necessary due to the container's intricated classloading structure
        return Tomcat.applicationListenerServer(port, backlog, maxIOP, WebActorInitializer.class, contextRoot);
    }
}
