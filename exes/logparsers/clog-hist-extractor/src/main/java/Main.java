import co.paralleluniverse.text.TextVisitor;
import co.paralleluniverse.text.log.LogParser;

public final class Main {
    public static void main(String[] args) throws Exception {
        LogParser.transformAll(args, ClogHistTextTransformer::new);
    }

    private static final class ClogHistTextTransformer extends LogParser {
        private final static String SEP = ",";
        private final static String QUOTE = "\"";

        private final static String HEAD =
            quote(QUOTE, "Histogram ID") + SEP +
            quote(QUOTE, "Time (ms)") + SEP +
            quote(QUOTE, "Percentile") + SEP +
            quote(QUOTE, "Requests #") + SEP +
            quote(QUOTE, "1/(1-Percentile)");

        private final static String HIST_START_LINE_PREFIX = "       Value     Percentile TotalCount 1/(1-Percentile)";
        private final static String HIST_END_LINE_PREFIX = "#[Mean";

        private String csvLine = HEAD + System.lineSeparator();

        public ClogHistTextTransformer(String p, TextVisitor delegate) {
            super(p, delegate);
        }

        private int skipLines = -1;
        private int histID = 1;

        @Override
        public final String transformLine0(String l) throws Exception {

            if (skipLines == 0) {
                if (l.startsWith(HIST_END_LINE_PREFIX)) {
                    csvLine = "";
                    histID++;
                    skipLines = -1;
                } else {
                    final String ret = csvLine + histID + SEP + parseHistogramLine(l);
                    csvLine = "";
                    return ret;
                }
            } else if (skipLines > 0) {
                skipLines--;
            } else if (l.startsWith(HIST_START_LINE_PREFIX)) {
                skipLines = 1;
            }

            return null;
        }

        private String parseHistogramLine(String l) {
            final String[] res = l.trim().split("\\s");
            String ret = "";
            boolean start = true;
            for (final String s : res) {
                if (s != null && !s.isEmpty()) {
                    final String prefix = start ? "" : SEP;
                    start = false;
                    ret += prefix + quote(QUOTE, s);
                }
            }
            return ret;
        }
    }
}
