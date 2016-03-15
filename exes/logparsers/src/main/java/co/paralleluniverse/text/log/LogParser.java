package co.paralleluniverse.text.log;

import co.paralleluniverse.common.util.Function2;
import co.paralleluniverse.text.BufferedReaderTextSource;
import co.paralleluniverse.text.PrintStreamTextWriterVisitor;
import co.paralleluniverse.text.TextTransformerVisitor;
import co.paralleluniverse.text.TextVisitor;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class LogParser extends TextTransformerVisitor {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void transformAll(String[] args, Function2<String, TextVisitor, TextVisitor> f) throws Exception {
        for (final String p : args) {
            try (
                final FileReader fr = new FileReader(p);
                final BufferedReader br = new BufferedReader(fr);
                final FileOutputStream fos = new FileOutputStream(p + ".csv");
                final PrintStream ps = new PrintStream(fos)
            ) {
                final BufferedReaderTextSource brts = new BufferedReaderTextSource(br);
                final PrintStreamTextWriterVisitor pswv = new PrintStreamTextWriterVisitor(ps);
                brts.accept(f.apply(p, pswv));
            }
        }
    }

    protected final String p;
    protected final TextVisitor delegate;
    private long count;

    public LogParser(String p, TextVisitor delegate) {
        super(delegate);
        this.delegate = delegate;
        this.p = p;
    }

    @Override
    public final String transformLine(String l) throws Exception {
        count++;
        return transformLine0(l);
    }

    protected abstract String transformLine0(String l) throws Exception;

    protected final Double parseDouble(String l, String startMarker, String endMarker) {
        try {
            final String str = getString(l, startMarker, endMarker);
            return Double.parseDouble(str);
        } catch (final NumberFormatException | IndexOutOfBoundsException e) {
            handleParseError(startMarker, endMarker, e);
            return null;
        }
    }

    protected final Double parseDouble(String l, String startMarker) {
        return parseDouble(l, startMarker, null);
    }

    protected final Long parseLong(String l, String startMarker, String endMarker) {
        try {
            final String str = getString(l, startMarker, endMarker);
            return Long.parseLong(str);
        } catch (final NumberFormatException | IndexOutOfBoundsException e) {
            handleParseError(startMarker, endMarker, e);
            return null;
        }
    }

    protected final Long parseLong(String l, String startMarker) {
        return parseLong(l, startMarker, null);
    }

    protected final Date parseDate(String l, String startMarker, String endMarker) {
        try {
            final String str = getString(l, startMarker, endMarker);
            return dateFormat.parse(str);
        } catch (final ParseException | IndexOutOfBoundsException e) {
            handleParseError(startMarker, endMarker, e);
            return null;
        }
    }

    protected final Date parseDate(String l, String startMarker) {
        return parseDate(l, startMarker, null);
    }

    protected static String formatDate(Date d) {
        return d != null ? dateFormat.format(d) : null;
    }

    private void handleParseError(String startMarker, String endMarker, Throwable t) {
        System.err.println(startMarker + ":" + endMarker + "@" + p + "[f]@" + count + "[l]");
        t.printStackTrace(System.err);
    }

    protected static String getString(String l, String startMarker, String endMarker) {
        int startIdx = l.indexOf(startMarker) + startMarker.length();
        final String startStr = l.substring(startIdx);
        return (endMarker != null ? startStr.substring(0, startStr.indexOf(endMarker)) : startStr).trim();
    }

    protected static String getString(String l, String startMarker) {
        return getString(l, startMarker, null);
    }
}
