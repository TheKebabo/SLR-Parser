package lexer;

import java.io.IOException;

// This needed since the parser will be using a lookahead, so we need to be able to access future tokens
// Essentially a lazy stream
public class TokenRetriever {
    private final Lexer lex;
    public TokenRetriever(Lexer l) { lex = l; }
    public Token retrieve() throws LexException { return lex.scan(); }
    public Token[] retrieve(int n) throws LexException {
        Token[] tokens = new Token[n];
        for (int i = 0; i < n; ++i) tokens[i] = lex.scan();
        return tokens;
    }
    public int getCurrentLoc() { return lex.inputLoc(); }
}
