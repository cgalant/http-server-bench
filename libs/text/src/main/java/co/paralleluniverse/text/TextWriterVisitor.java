package co.paralleluniverse.text;

public abstract class TextWriterVisitor extends DelegatingTextVisitor implements TextWriter {
    public TextWriterVisitor(TextVisitor delegate) {
        super(delegate);
    }

    public TextWriterVisitor() {
        this(null);
    }

    @Override
    public void visitLine(String line) throws Exception {
        writeLine(line);
        super.visitLine(line);
    }
}
