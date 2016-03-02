package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;

public final class ServerUtils {
    public static final String CP = "/";
    public static final String SN = "hello";
    public static final long TIMEOUT = HandlerUtils.asyncTimeout;

    private ServerUtils() {}
}
