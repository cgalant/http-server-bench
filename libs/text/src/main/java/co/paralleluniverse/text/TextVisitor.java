package co.paralleluniverse.text;

public interface TextVisitor {
    void visitLine(String l) throws Exception;
}
