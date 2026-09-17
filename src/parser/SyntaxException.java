package parser;

public class SyntaxException extends Exception {
    public SyntaxException(int inputLoc) {
        super("Syntax error at index " + inputLoc);
    }
}
