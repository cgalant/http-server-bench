package co.paralleluniverse.text;

import java.io.PrintStream;

public final class PrintStreamTextWriterVisitor extends TextWriterVisitor {
    private final PrintStream ps;

    public PrintStreamTextWriterVisitor(PrintStream ps, TextVisitor delegate) {
        super(delegate);
        this.ps = ps;
    }

    public PrintStreamTextWriterVisitor(PrintStream ps) {
        this(ps, null);
    }

    @Override
    public final void writeLine(String line) throws Exception {
        if (line != null)
            ps.println(line);
    }
}
