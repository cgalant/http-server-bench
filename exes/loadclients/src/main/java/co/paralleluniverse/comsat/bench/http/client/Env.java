package co.paralleluniverse.comsat.bench.http.client;

public interface Env<R, E extends AutoCloseableRequestExecutor<R, ?>> {
  E newRequestExecutor(int ioParallelism, int maxConnections, int timeoutMS, boolean cookies) throws Exception;
  R newRequest(String address) throws Exception;
}
