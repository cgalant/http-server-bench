package co.paralleluniverse.comsat.bench.http.server.handlers.jetty.sync;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class JettyHandlerSync extends AbstractHandler {
    private static final ByteBuffer TXT = BufferUtil.toBuffer(HandlerUtils.TXT);

    @Override
    public final void handle(String target, Request br, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HandlerUtils.handleDelayWithThread();

        br.setHandled(true);
        br.getResponse().getHttpFields().add(HandlerUtils.CTJ);
        br.getResponse().getHttpFields().add(HandlerUtils.jettyServer);
        if (HandlerUtils.URL.equals(target))
            br.getResponse().getHttpOutput().sendContent(TXT.slice());
    }
}
