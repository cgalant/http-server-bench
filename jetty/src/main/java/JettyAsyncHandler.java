import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.AsyncContext;


// 9091 async
// 9092 not async

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;

public class JettyAsyncHandler extends AbstractHandler {
    private static final ByteBuffer helloWorld = BufferUtil.toBuffer("Hello, world!");
    private static final HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE, MimeTypes.Type.TEXT_PLAIN.asString());
    private static final HttpField server = new PreEncodedHttpField(HttpHeader.SERVER, "jetty-async-handler");
    private static final LinkedBlockingQueue<AsyncContext[]> q = new LinkedBlockingQueue<>();

    private static int num = 0;
    private static AsyncContext[] ac1 = new AsyncContext[100000], ac2 = new AsyncContext[100000],
        acv = ac1, copy = ac2;

    final synchronized int swap() {
        int n2 = num;
        copy = acv;
        acv = (acv == ac1) ? ac2 : ac1;
        num = 0;
        return n2;
    }

    final AsyncContext[] wrap() {
        final int n2 = swap();
        AsyncContext a2[] = new AsyncContext[n2];
        System.arraycopy(copy, 0, a2, 0, n2);
        Arrays.fill(copy, 0, n2, null);
        return a2;
    }

    final synchronized void store(AsyncContext async) {
        acv[num++] = async;
    }

    final public void handle(String target, Request br, HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext async = request.startAsync();
        async.setTimeout(30000);
        store(async);
    }

    final void reply(AsyncContext async) {
        try {
            final Request br = (Request) async.getRequest();
            br.setHandled(true);
            br.getResponse().getHttpFields().add(contentType);
            br.getResponse().getHttpFields().add(server);
            if ("/hello".equals(br.getPathInfo()))
                br.getResponse().getHttpOutput().sendContent(helloWorld.slice());
            async.complete();
        } catch (final IOException ignored) {
        }
    }

    final void reply(AsyncContext[] wrap) {
        for (AsyncContext aWrap : wrap) reply(aWrap);
    }

    final void reply() {
        AsyncContext[] wrap = wrap();
        if (wrap.length == 0 || q.add(wrap)) return;
        reply(wrap);
    }

    final void poll() {
        try {
            reply(q.take());
        } catch (final Exception ignored) {
        }
    }

    final void timers() {
        final int delta = 10, nt = 3;
        new Timer().schedule(new TimerTask() {
            public void run() {
                reply();
            }
        }, delta, delta);

        for (int ii = 0; ii < nt; ii++)
            new Thread(() -> { //noinspection InfiniteLoopStatement
                while (true) poll();
            }).start();
    }

    {
        timers();
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(9091);
        server.setHandler(new JettyAsyncHandler());
        server.start();
    }
}
