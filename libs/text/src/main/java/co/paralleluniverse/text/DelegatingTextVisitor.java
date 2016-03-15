package co.paralleluniverse.text;

public abstract class DelegatingTextVisitor implements TextVisitor {
    private final TextVisitor delegate;

    public DelegatingTextVisitor(TextVisitor delegate) {
        this.delegate = delegate;
    }

    public DelegatingTextVisitor() {
        this(null);
    }

    @Override
    public void visitLine(String line) throws Exception {
        if (delegate != null)
            delegate.visitLine(line);
    }
}
