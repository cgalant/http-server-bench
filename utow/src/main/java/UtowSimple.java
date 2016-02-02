import io.undertow.Undertow;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;

// 9094

public class UtowSimple {
    public static void main(final String[] args) {
        final Undertow server = Undertow.builder()
            .addHttpListener(9094, "0.0.0.0")
            .setHandler(exchange ->
            {
                final HeaderMap headers = exchange.getResponseHeaders();
                headers.add(Headers.SERVER, "utow-simple");

                exchange.getResponseSender().send("Hello, world!");
            }).build();
        server.start();
    }
}
