import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;
import java.io.IOException;
import java.nio.ByteBuffer;

// 8010

public final class JettyHandler
{
    public static void main(String[] args) throws Exception {
        final Server server = new Server(8010);
        final ServerConnector connector = server.getBean(ServerConnector.class);
        final HttpConfiguration config = connector.getBean(HttpConnectionFactory.class).getHttpConfiguration();
        config.setSendDateHeader(true);
        config.setSendServerVersion(true);

        final PathHandler pathHandler = new PathHandler();
        server.setHandler(pathHandler);

        server.start();
        server.join();
    }

    public static class PathHandler extends AbstractHandler {
        private static final ByteBuffer helloWorld = BufferUtil.toBuffer("Hello, World!");
        private static final HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_PLAIN.asString());
        private static final HttpField server = new PreEncodedHttpField(HttpHeader.SERVER, "jetty-handler");

        @Override
        public final void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            baseRequest.setHandled(true);
            baseRequest.getResponse().getHttpFields().add(contentType);
            baseRequest.getResponse().getHttpFields().add(server);
            if ("/hello".equals(target))
                baseRequest.getResponse().getHttpOutput().sendContent(helloWorld.slice());
        }
    }
}
