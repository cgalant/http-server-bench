
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.nio.ByteBuffer;
import io.undertow.util.SameThreadExecutor;

import java.util.Timer;
import java.util.TimerTask;

// 9097
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" UtowAsync


public final class UtowAsync implements HttpHandler {

  public static void main(String[] args) throws Exception {
    Undertow.builder()
        .addHttpListener(9097,"0.0.0.0")
        .setHandler(Handlers.path().addPrefixPath("/hello",new UtowAsync()))
        .build()
        .start();
  }

// http://lists.jboss.org/pipermail/undertow-dev/2014-August/000898.html

// at 3000 concurrency
// sending while synchronized takes a bunch of reps to jit, but hits 80-84k req/s
// fast-flip 90k req/s

    int num = 0;
    HttpServerExchange acv[] = new HttpServerExchange[1000000];

    synchronized void store(HttpServerExchange async) {
        if (async==null) while (num > 0) {
            reply(acv[--num]);
            acv[num] = null;
        }
        else acv[num++] = async;
    }

    byte [] bytes = "Hello, world!".getBytes();
    ByteBuffer buf = ByteBuffer.allocate(bytes.length).put(bytes);
    { buf.flip(); }
    
    void reply(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseHeaders().put(Headers.SERVER, "undertow-async");
        exchange.getResponseSender().send(buf.duplicate());
    }
    
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(SameThreadExecutor.INSTANCE, () -> store(exchange));
    }
    
    {
        new Timer().schedule(new TimerTask() { public void run() {
            UtowAsync.this.store(null);
        } },10,10);
    }
}
