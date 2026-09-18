package lexer;
import parser.Grammar;

public class FloatToken extends Token {
    public final float val;
    public FloatToken(float v) { super(Grammar.Terminal.FLOAT); val = v; }
    @Override
    public String toString() { return "" + val; }
}
