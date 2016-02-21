package co.paralleluniverse.comsat.bench.http.server;

import co.paralleluniverse.comsat.bench.http.server.handlers.HandlerUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;

import static java.util.Arrays.asList;

public abstract class LoadTargetBase {
    protected abstract int getDefaultPort();

    protected abstract int getDefaultIOParallelism();

    protected abstract int getDefaultWorkParallelism();

    protected final int getDefaultConnectionsBacklog() {
        return 10000;
    }

    protected abstract void start(int port, int backlog, int maxIOP, int maxProcessingP) throws Exception;

    final protected void run(final String[] args) throws Exception {
        final OptionParser parser = new OptionParser();

        final OptionSpec<Long> d = parser.acceptsAll(asList("d", "delay")).withRequiredArg().ofType(Long.class).describedAs("Per-request delay").defaultsTo(HandlerUtils.delay);
        final OptionSpec<Long> t = parser.acceptsAll(asList("t", "timeout")).withRequiredArg().ofType(Long.class).describedAs("Async connection timeout (ms)").defaultsTo(HandlerUtils.asyncTimeout);

        final OptionSpec<Integer> p = parser.acceptsAll(asList("p", "port")).withRequiredArg().ofType(Integer.class).describedAs("Server port").defaultsTo(getDefaultPort());
        final OptionSpec<Integer> i = parser.acceptsAll(asList("i", "ioParallelism")).withRequiredArg().ofType(Integer.class).describedAs("Maximum number of OS threads performing actual I/O").defaultsTo(getDefaultIOParallelism());
        final OptionSpec<Integer> w = parser.acceptsAll(asList("w", "workParallelism")).withRequiredArg().ofType(Integer.class).describedAs("Maximum number of OS threads performing processing tasks").defaultsTo(getDefaultWorkParallelism());
        final OptionSpec<Integer> m = parser.acceptsAll(asList("m", "maxConnections")).withRequiredArg().ofType(Integer.class).describedAs("Maximum number of queued incoming connections").defaultsTo(getDefaultConnectionsBacklog());

        parser.acceptsAll(asList("h", "?", "help"), "Show help").forHelp();

        final OptionSet options = parser.parse(args);

        if (options.has("h")) {
            parser.printHelpOn(System.err);
            return;
        }

        System.err.println (
            "\n=============== SERVER SETTINGS ==============\n\n" +

              "- HANDLER\n" +
                "\t* Per-request delay (millis, -d): " + options.valueOf(d) + "\n" +
                "\t* Async request timeout (async only, -t): " + options.valueOf(t) + " ms\n\n" +

              "- SERVER\n" +
                "\t* Port (-p): " + options.valueOf(p) + "\n" +
                "\t* Maximum queued incoming connections (-m): " + options.valueOf(m) + "\n" +
                "\t* Maximum IO Parallelism (-i): " + options.valueOf(i) + "\n" +
                "\t* Maximum Processing Parallelism (-w): " + options.valueOf(w) + "\n" +

            "\n==============================================\n"
        );

        HandlerUtils.delay = options.valueOf(d);
        HandlerUtils.asyncTimeout = options.valueOf(t);
        HandlerUtils.workers = options.valueOf(w);

        Runtime.getRuntime().addShutdownHook(new Thread(HandlerUtils::printStats));

        start(options.valueOf(p), options.valueOf(m), options.valueOf(i), options.valueOf(w));
    }
}
