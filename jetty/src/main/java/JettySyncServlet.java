import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

// 8011

public final class JettySyncServlet extends HttpServlet {
    private static final byte[] HELLO_WORLD = "Hello, World!".getBytes(StandardCharsets.ISO_8859_1);

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setHeader("Server", "jetty-sync-servlet");
        response.getOutputStream().write(HELLO_WORLD);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    static Server setup(int port) throws Exception {
        final JettySyncServlet rest = new JettySyncServlet();
        final Server server = new Server(port);
        final ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        final ServletHolder holder = new ServletHolder(rest);
        context.addServlet(holder, "/hello");
        holder.setAsyncSupported(false);
        server.setHandler(context);
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        setup(8011);
    }
}
