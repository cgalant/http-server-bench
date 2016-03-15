package co.paralleluniverse.text;

public interface TextSource {
    void accept(TextVisitor v) throws Exception;
}
