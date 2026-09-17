package lexer;
import parser.Grammar;

public class Token {
    public final Grammar.Terminal terminal;
    public Token(Grammar.Terminal t) { terminal = t; }
    public String toString() { return "" + (char)terminal.code(); }
}
