import co.paralleluniverse.text.TextVisitor;
import co.paralleluniverse.text.log.LogParser;

public final class Main {
    public static void main(String[] args) throws Exception {
        LogParser.transformAll(args, ClogTextTransformer::new);
    }

    private static final class ClogTextTransformer extends LogParser {
        private final static String SEP = ",";
        private final static String QUOTE = "\"";

        private final static String HEAD =
            quote(QUOTE, "Mean (ms)") + SEP + quote(QUOTE, "Max (ms)") + SEP + quote(QUOTE, "Start") + SEP +
            quote(QUOTE, "Successful (#)") + SEP + quote(QUOTE, "Failed (#)") + SEP + quote(QUOTE, "First end") + SEP +
            quote(QUOTE, "Last end") + SEP + quote(QUOTE, "Time from start (s)") + SEP +
            quote(QUOTE, "Time from first end (s)");

        private final static String MEAN = "#[Mean    =";
        private final static String MAX = "#[Max     =";
        private final static String LOAD_STARTED = "* Load started: ";
        private final static String SUCCESSFUL_REQUESTS = "* Successful requests:";
        private final static String FAILED_REQUESTS = "* Failed requests:";
        private final static String FIRST_REQUEST_ENDED = "* First request ended:";
        private final static String LAST_REQUEST_ENDED = "* Last request ended:";
        private final static String SECONDS_FROM_LOAD_START = "* Seconds from load start:";
        private final static String SECONDS_FROM_FIRST_REQUEST_COMPLETED = "* Seconds from first request completed:";

        private String csvLine = HEAD + System.lineSeparator();

        public ClogTextTransformer(String p, TextVisitor delegate) {
            super(p, delegate);
        }

        @Override
        public final String transformLine0(String l) throws Exception {
            if (l.startsWith(MEAN)) {
                csvLine += quote(QUOTE, getString(l, MEAN, ",")) + SEP;
            } else if (l.startsWith(MAX)) {
                csvLine += quote(QUOTE, getString(l, MAX, ",")) + SEP;
            } else if (l.startsWith(LOAD_STARTED)) {
                csvLine += quote(QUOTE, "'" + getString(l, LOAD_STARTED)) + SEP;
            } else if (l.startsWith(SUCCESSFUL_REQUESTS)) {
                csvLine += quote(QUOTE, getString(l, SUCCESSFUL_REQUESTS)) + SEP;
            } else if (l.startsWith(FAILED_REQUESTS)) {
                csvLine += quote(QUOTE, getString(l, FAILED_REQUESTS)) + SEP;
            } else if (l.startsWith(FIRST_REQUEST_ENDED)) {
                csvLine += quote(QUOTE, "'" + getString(l, FIRST_REQUEST_ENDED)) + SEP;
            } else if (l.startsWith(LAST_REQUEST_ENDED)) {
                csvLine += quote(QUOTE, "'" + getString(l, LAST_REQUEST_ENDED)) + SEP;
            } else if (l.startsWith(SECONDS_FROM_LOAD_START)) {
                csvLine += quote(QUOTE, getString(l, SECONDS_FROM_LOAD_START)) + SEP;
            } else if (l.startsWith(SECONDS_FROM_FIRST_REQUEST_COMPLETED)) {
                csvLine += quote(QUOTE, getString(l, SECONDS_FROM_FIRST_REQUEST_COMPLETED));
                final String ret = csvLine;
                csvLine = "";
                return ret;
            }

            return null;
        }
    }
}
