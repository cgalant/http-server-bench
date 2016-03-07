package co.paralleluniverse.comsat.bench;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Utils {
    public static String fmt(Date d) {
        return d != null ? dateFormat.format(d) : null;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private Utils() {}
}
