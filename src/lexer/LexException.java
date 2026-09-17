package lexer;

public class LexException extends Exception {
    public LexException(int inputLoc) {
        super("Lexical syntax error at index " + inputLoc);
    }
}
