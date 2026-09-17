package lexer;
import parser.Grammar;

public class Float extends Token {
    public final float val;
    public Float(float v) { super(Grammar.Terminal.FLOAT); val = v; }
    @Override
    public String toString() { return "" + val; }
}
