package co.paralleluniverse.comsat.bench.http.server.handlers.spark;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import spark.Request;
import spark.Response;
import spark.Route;

public final class SparkHandlerSync implements Route {
    @Override
    public Object handle(Request request, Response response) throws Exception {
        HandlerUtils.reqStart();
        HandlerUtils.handleDelayWithThread();
        try {
            response.header(HandlerUtils.HEAD_SERVER_KEY, HandlerUtils.server);
            response.header(HandlerUtils.CONTENT_TYPE_KEY, HandlerUtils.CT);
            return HandlerUtils.TXT;
        } finally {
            HandlerUtils.reqEnd();
        }
    }
}
