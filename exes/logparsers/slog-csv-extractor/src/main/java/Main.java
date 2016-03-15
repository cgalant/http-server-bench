import co.paralleluniverse.text.TextVisitor;
import co.paralleluniverse.text.log.LogParser;

public final class Main {
    public static void main(String[] args) throws Exception {
        LogParser.transformAll(args, SlogTextTransformer::new);
    }

    private static final class SlogTextTransformer extends LogParser {
        private final static String SEP = ",";
        private final static String QUOTE = "\"";

        private final static String HEAD =
            quote(QUOTE, "Reqs started (#)") + SEP + quote(QUOTE, "Reqs completed (#)") + SEP +
            quote(QUOTE, "First req start") + SEP + quote(QUOTE, "First req end") + SEP +
            quote(QUOTE, "Last req start") + SEP + quote(QUOTE, "Last req end") + SEP +
            quote(QUOTE, "Concurrency at end (#)") + SEP + quote(QUOTE, "Concurrency avg (#)") + SEP +
            quote(QUOTE, "Concurrency max (#)") + SEP + quote(QUOTE, "OS load avg") + SEP +
            quote(QUOTE, "Total compilation time (ms)") + SEP + quote(QUOTE, "CPU at end (%)") + SEP +
            quote(QUOTE, "CPU avg (%)") + SEP + quote(QUOTE, "CPU max (%)") + SEP +
            quote(QUOTE, "Threads at end (#)") + SEP + quote(QUOTE, "Threads avg (#)") + SEP +
            quote(QUOTE, "Threads max (#)") + SEP + quote(QUOTE, "Daemon threads at end (#)") + SEP +
            quote(QUOTE, "Daemon threads avg (#)") + SEP + quote(QUOTE, "Daemon threads max (#)") + SEP +
            quote(QUOTE, "Heap mem at end (b)") + SEP + quote(QUOTE, "Heap mem avg (b)") + SEP +
            quote(QUOTE, "Heap mem max") + SEP + quote(QUOTE, "Non-heap mem at end (b)") + SEP +
            quote(QUOTE, "Non-heap mem avg (b)") + SEP + quote(QUOTE, "Non-heap mem max (b)") + SEP +
            quote(QUOTE, "GCs count (#)") + SEP + quote(QUOTE, "GC time avg (ms)") + SEP +
            quote(QUOTE, "GC time max (ms)");

        private static final String RESET = "========================== ";
        private static final String START_RECORDING = "Monitoring stopped, printing last sample";
        private final static String REQS_STARTED = "- # Reqs started:";
        private final static String REQS_COMPLETED = "- # Reqs completed:";

        private final static String AVG_OS_LOAD = "- Avg OS load:";
        private final static String TOTAL_COMPILATION_TIME = "- Total compilation time (ms):";

        private final static String GCS_CTX = "- GCs:";

        private final static String START = "- Start:";
        private final static String END = "- End:";
        private final static String NOW = "- Now:";
        private final static String AVG = "- Avg:";
        private final static String MAX = "- Max:";
        private final static String COUNT = "- Count:";

        private boolean extract, lastCtx, waitReset;

        private String csvLine = HEAD + System.lineSeparator();

        public SlogTextTransformer(String p, TextVisitor delegate) {
            super(p, delegate);
        }

        @Override
        public final String transformLine0(String l) throws Exception {
            if (l.startsWith(RESET))
                waitReset = false;

            if (!waitReset) {
                extract = extract || l.startsWith(START_RECORDING);

                if (extract) {
                    if (lastCtx && l.contains(MAX)) {
                        lastCtx = false;
                        extract = false;
                        waitReset = true;
                        final String ret = csvLine += quote(QUOTE, getString(l, MAX));
                        csvLine = "";
                        return ret;
                    } else if (l.contains(GCS_CTX)) {
                        lastCtx = true;
                    } else if (l.contains(REQS_STARTED)) {
                        csvLine += quote(QUOTE, getString(l, REQS_STARTED)) + SEP;
                    } else if (l.contains(REQS_COMPLETED)) {
                        csvLine += quote(QUOTE, getString(l, REQS_COMPLETED)) + SEP;
                    } else if (l.contains(START)) {
                        csvLine += quote(QUOTE, "'" + getString(l, START)) + SEP;
                    } else if (l.contains(END)) {
                        csvLine += quote(QUOTE, "'" + getString(l, END)) + SEP;
                    } else if (l.contains(NOW)) {
                        csvLine += quote(QUOTE, getString(l, NOW)) + SEP;
                    } else if (l.contains(AVG)) {
                        csvLine += quote(QUOTE, getString(l, AVG)) + SEP;
                    } else if (l.contains(MAX)) {
                        csvLine += quote(QUOTE, getString(l, MAX)) + SEP;
                    } else if (l.contains(AVG_OS_LOAD)) {
                        csvLine += quote(QUOTE, getString(l, AVG_OS_LOAD)) + SEP;
                    } else if (l.contains(TOTAL_COMPILATION_TIME)) {
                        csvLine += quote(QUOTE, getString(l, TOTAL_COMPILATION_TIME)) + SEP;
                    } else if (l.contains(COUNT)) {
                        csvLine += quote(QUOTE, getString(l, COUNT)) + SEP;
                    }
                }
            }

            return null;
        }
    }
}
