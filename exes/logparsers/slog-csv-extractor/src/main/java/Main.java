import co.paralleluniverse.text.TextVisitor;
import co.paralleluniverse.text.log.LogParser;

public final class Main {
    public static void main(String[] args) throws Exception {
        LogParser.transformAll(args, SlogTextTransformer::new);
    }

    private static final class SlogTextTransformer extends LogParser {
        private final static String SEP = "|";

        private final static String HEAD =
            "Reqs started (#)|Reqs completed (#)|First req start|First req end|Last req start|Last req end|" +
            "Concurrency at end (#)|Concurrency avg (#)|Concurrency max (#)|OS load avg|Total compilation time (ms)|CPU at end (%)|CPU avg (%)|CPU max (%)|" +
            "Threads at end (#)|Threads avg (#)|Threads max (#)|Daemon threads at end (#)|Daemon threads avg (#)|Daemon threads max (#)|" +
            "Heap mem at end (b)|Heap mem avg (b)|Heap mem max|Non-heap mem at end (b)|Non-heap mem avg (b)|Non-heap mem max (b)|" +
            "GCs count (#)|GC time avg (ms)|GC time max (ms)";

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
                        final String ret = csvLine += getString(l, MAX);
                        csvLine = "";
                        return ret;
                    } else if (l.contains(GCS_CTX)) {
                        lastCtx = true;
                    } else if (l.contains(REQS_STARTED)) {
                        csvLine += getString(l, REQS_STARTED) + SEP;
                    } else if (l.contains(REQS_COMPLETED)) {
                        csvLine += getString(l, REQS_COMPLETED) + SEP;
                    } else if (l.contains(START)) {
                        csvLine += getString(l, START) + SEP;
                    } else if (l.contains(END)) {
                        csvLine += getString(l, END) + SEP;
                    } else if (l.contains(NOW)) {
                        csvLine += getString(l, NOW) + SEP;
                    } else if (l.contains(AVG)) {
                        csvLine += getString(l, AVG) + SEP;
                    } else if (l.contains(MAX)) {
                        csvLine += getString(l, MAX) + SEP;
                    } else if (l.contains(AVG_OS_LOAD)) {
                        csvLine += getString(l, AVG_OS_LOAD) + SEP;
                    } else if (l.contains(TOTAL_COMPILATION_TIME)) {
                        csvLine += getString(l, TOTAL_COMPILATION_TIME) + SEP;
                    } else if (l.contains(COUNT)) {
                        csvLine += getString(l, COUNT) + SEP;
                    }
                }
            }

            return null;
        }
    }
}
