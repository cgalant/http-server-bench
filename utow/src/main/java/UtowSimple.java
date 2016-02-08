import io.undertow.Undertow;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

// 8000

public class UtowSimple {
    public static void main(final String[] args) {
        final Undertow server = Undertow.builder()
            .addHttpListener(8000, "0.0.0.0")
            .setHandler(exchange ->
            {
                final HeaderMap headers = exchange.getResponseHeaders();
                headers.add(Headers.SERVER, "undertow-simple");

                exchange.getResponseSender().send("Hello, World!");
            }).build();
        server.start();
    }
}
