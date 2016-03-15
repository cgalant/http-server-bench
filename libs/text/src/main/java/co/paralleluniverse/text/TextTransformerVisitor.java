package co.paralleluniverse.text;

public abstract class TextTransformerVisitor extends DelegatingTextVisitor implements TextTransformer {
    public TextTransformerVisitor(TextVisitor delegate) {
        super(delegate);
    }

    public TextTransformerVisitor() {
        super();
    }

    @Override
    public void visitLine(String line) throws Exception {
        super.visitLine(transformLine(line));
    }
}
