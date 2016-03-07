package co.paralleluniverse.comsat.bench.http.server.handlers.servlet.sync;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class ServletSync extends HttpServlet {
    private static final byte[] TXT = HandlerUtils.TXT.getBytes(HandlerUtils.TXT_CR);

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HandlerUtils.reqStart();
        HandlerUtils.handleDelayWithThread();

        try {
            resp.setContentType(HandlerUtils.CT);
            resp.setHeader(HandlerUtils.HEAD_SERVER_KEY, HandlerUtils.server);
            resp.getOutputStream().write(TXT);
        } finally {
            HandlerUtils.reqEnd();
        }
    }
}
