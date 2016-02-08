import java.io.EOFException;
import java.io.IOException;

import kilim.Pausable;
import kilim.Task;
import kilim.http.HttpRequest;
import kilim.http.HttpResponse;
import kilim.http.HttpServer;
import kilim.http.HttpSession;

// 9093

public final class KilimHello extends HttpSession {
    final byte [] bytes = "hello world".getBytes();
    private static int delay = 0;
    
    public static void main(String[] args) throws IOException {
        if (args.length > 0) delay = Integer.valueOf(args[0]);
        new HttpServer(9040, KilimHello.class);
    }
    
    public final void execute() throws Pausable, Exception {
        try {
            final HttpRequest req = new HttpRequest();
            final HttpResponse resp = new HttpResponse();
            while (true) {
                super.readRequest(req);
                if (req.keepAlive())
                    resp.addField("Connection", "Keep-Alive");
                if (delay > 0) Task.sleep(delay);
                resp.getOutputStream().write(bytes);
                sendResponse(resp);
                if (!req.keepAlive()) 
                    break;
            }
        } catch (final EOFException ignored) {}
        super.close();
    }
}
