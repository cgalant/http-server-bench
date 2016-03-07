package co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync.comsat;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ServletSyncComsat extends FiberHttpServlet {
    private static final byte[] TXT = HandlerUtils.TXT.getBytes(HandlerUtils.TXT_CR);

    @Override
    @Suspendable
    protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        HandlerUtils.reqStart();
        HandlerUtils.handleDelayWithStrand();

        try {
            resp.setContentType(HandlerUtils.CT);
            resp.setHeader(HandlerUtils.HEAD_SERVER_KEY, HandlerUtils.server);
            resp.getOutputStream().write(TXT);
        } finally {
            HandlerUtils.reqEnd();
        }
    }
}
