import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.xnio.Options;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

// 8001

public final class UtowTechem {

  public static final String TEXT_PLAIN = "text/plain";

  public static void main(String[] args) throws Exception {
    new UtowTechem();
  }

  public UtowTechem() throws ClassNotFoundException, IOException, SQLException {
    Undertow.builder()
        .addHttpListener(8001,"0.0.0.0")
        .setBufferSize(1024 * 16)
        .setIoThreads(Runtime.getRuntime().availableProcessors() * 2) //this seems slightly faster in some configurations
        .setSocketOption(Options.BACKLOG, 10000)
//        .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false) //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
        .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
        .setServerOption(UndertowOptions.ENABLE_CONNECTOR_STATISTICS, false)
        .setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false)
        .setHandler(Handlers.header(Handlers.path().addPrefixPath("/hello", new PlaintextHandler()), Headers.SERVER_STRING, "undertow-techem"))
        .setWorkerThreads(200)
        .build()
        .start();
  }
}


final class PlaintextHandler implements HttpHandler {
  private static final ByteBuffer buffer;
  private static final String MESSAGE = "Hello, World!";

  static {
      buffer = ByteBuffer.allocateDirect(MESSAGE.length());   
      try {
          buffer.put(MESSAGE.getBytes("US-ASCII"));
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
      buffer.flip();
  }
     
  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, UtowTechem.TEXT_PLAIN);
    exchange.getResponseSender().send(buffer.duplicate());
  }
}
