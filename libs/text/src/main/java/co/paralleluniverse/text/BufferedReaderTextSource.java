package co.paralleluniverse.text;

import java.io.BufferedReader;

public final class BufferedReaderTextSource implements TextSource {
    private final BufferedReader br;

    public BufferedReaderTextSource(BufferedReader br) {
        this.br = br;
    }

    @Override
    public final void accept(TextVisitor v) throws Exception {
        String line;
        while ((line = br.readLine()) != null) {
            v.visitLine(line);
        }
    }
}
