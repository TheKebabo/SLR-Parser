package lexer;

import parser.Grammar;

public class Lexer {
    public final String input;
    private int inputLoc = 0;
    private char peek = ' ';
    private Token curToken;

    public Lexer(String input) { this.input = input; }

    public int inputLoc() { return inputLoc; }

    void readChar() {
        if (inputLoc == input.length()) { peek = '$'; }
        else { peek = input.charAt(inputLoc++); }
    }
    void unread(int offset) { inputLoc-=offset; }

    // Consume a single token
    public Token scan() throws LexException {
        readChar();
        if (peek == '$') { return new Token(Grammar.Terminal.SENTINEL); }

        // Handle whitespace
        while (peek == ' ' || peek == '\t' || peek == '\n') { readChar(); }
        if (peek == '$') { return new Token(Grammar.Terminal.SENTINEL); }

        // Try each terminal sequentially
        if (getPlus()) return curToken;
        if (getFloat()) return curToken; // Have to do this first because we could have a unary minus
        if (getMinus()) return curToken;
        if (getExp()) return curToken;
        if (getFact()) return curToken;
        if (getLeftParen()) return curToken;
        if (getRightParen()) return curToken;
        if (getCos()) return curToken;

        throw new LexException(inputLoc);
    }

    // Automaton transitions for the float construct
    static enum FLOAT_TRANSITIONS {
        ZERO(0), DIG_POS(1), DOT(2), SIGN(3), EXP(4), FAIL(-1);
        public final int v;
        FLOAT_TRANSITIONS(int v) { this.v = v; }
    }
    private static int getCharTransition(char c) {
        if (c == '0') return FLOAT_TRANSITIONS.ZERO.v;
        if (c >= '1' && c <= '9') return FLOAT_TRANSITIONS.DIG_POS.v;
        if (c == '.') return FLOAT_TRANSITIONS.DOT.v;
        if (c == '+' || c == '-') return FLOAT_TRANSITIONS.SIGN.v;
        if (c == 'e' || c == 'E') return FLOAT_TRANSITIONS.EXP.v;
        return FLOAT_TRANSITIONS.FAIL.v; // Unrecognized character
    }
    static final int[][] FLOAT_TRANSITION_TABLE = {
            // C_ZERO, C_DIG_POS, C_DOT, C_SIGN, C_EXP
            {       2,         3,     4,      1,    -1 }, // S0
            {       2,         3,     4,     -1,    -1 }, // S1 read mantissa sign
            {      -1,        -1,     7,     -1,     8 }, // S2 read 0, accepting
            {       3,         3,     7,     -1,     8 }, // S3 read 1..9, accepting
            {       5,         6,    -1,     -1,    -1 }, // S4 read .
            {      -1,        -1,    -1,     -1,     8 }, // S5 read .0, accepting
            {       6,         6,    -1,     -1,     8 }, // S6 read .1-9, accepting
            {       7,         7,    -1,     -1,     8 }, // S7 read int., accepting
            {      10,        11,    -1,      9,    -1 }, // S8 read e
            {      10,        11,    -1,     -1,    -1 }, // S9 read exp sign
            {      -1,        -1,    -1,     -1,    -1 }, // S10 read exp 0, accepting
            {      11,        11,    -1,     -1,    -1 }  // S11 read exp 1-9, accepting
    };
    private static final boolean[] FLOAT_ACCEPTING_STATES = {
            false, // S0
            false, // S1
            true,  // S2
            true,  // S3
            false, // S4
            true,  // S5
            true,  // S6
            true,  // S7
            false, // S8
            false, // S9
            true,  // S10
            true   // S11
    };

    // Simulators for transition diagrams corresponding to each terminal type
    boolean getFloat() { // This is a simulation of the transition diagram
        int state = 0;
        int charsRead = 1;
        char firstChar = peek;

        float mantissa = 0.0f;
        float fractionScale = 0.1f;
        int mantissaSign = 1;
        int expSign = 1;
        int expValue = 0;

        while (true) { // While not accepting
            // Transition
            int charTransition = getCharTransition(peek);
            if (charTransition == -1) { // Invalid character
                if (FLOAT_ACCEPTING_STATES[state]) { break; } // We have a correct float
                else { // Not a valid float
                    unread(charsRead - 1);
                    peek = firstChar;
                    return false;
                }
            }

            int nextState = FLOAT_TRANSITION_TABLE[state][charTransition];

            if (nextState == -1) { // INVALID state reached
                if (FLOAT_ACCEPTING_STATES[state]) { break; } // We have a correct float
                else { // Not a valid float
                    unread(charsRead - 1);
                    peek = firstChar;
                    return false;
                }
            }

            state = nextState;

            // Augment float value
            switch (state) {
                case 1: // Mantissa sign
                    if (peek == '-') mantissaSign = -1;
                    break;
                case 2: // Mantissa 0
                    mantissa = 0.0f;
                    break;
                case 3: // Mantissa 1-9
                    mantissa = (mantissa * 10.0f) + (peek - '0');
                    break;
                case 5:
                case 6:
                case 7: // Fraction 0-9
                    if (peek >= '0' && peek <= '9') {
                        mantissa += (peek - '0') * fractionScale;
                        fractionScale /= 10.0f;
                    }
                    break;
                case 9: // Exp sign
                    if (peek == '-') expSign = -1;
                    break;
                case 10:
                case 11: // Exp 0--9
                    if (peek >= '0' && peek <= '9') {
                        expValue = (expValue * 10) + (peek - '0');
                    }
                    break;
            }

            // Read a character
            readChar();
            charsRead++;
        }

        // We are in an accepting state
        // A non-EOF char has already been read, leave it for next scan.
        if (peek != '$') {
            unread(1);
        }
        float val = mantissaSign * mantissa * ((float)Math.pow(10.0, (double)(expSign * expValue)));
        curToken = new FloatToken(val);
        return true;
    }

    boolean getCos() {
        if (peek != 'c') {
            return false;
        }
        readChar();
        if (peek != 'o') {
            unread(1);
            return false;
        }
        readChar();
        if (peek != 's'){
            unread(2);
            return false;
        }
        curToken = new Token(Grammar.Terminal.COS);
        return true;
    }

    boolean getPlus() {
        if (peek == '+') {
            curToken = new Token(Grammar.Terminal.PLUS);
            return true;
        }
        return false;
    }
    boolean getMinus() {
        if (peek == '-') {
            curToken = new Token(Grammar.Terminal.MINUS);
            return true;
        }
        return false;
    }
    boolean getExp() {
        if (peek == '^') {
            curToken = new Token(Grammar.Terminal.EXP);
            return true;
        }
        return false;
    }
    boolean getFact() {
        if (peek == '!') {
            curToken = new Token(Grammar.Terminal.FACT);
            return true;
        }
        return false;
    }

    boolean getLeftParen() {
        if (peek == '(') {
            curToken = new Token(Grammar.Terminal.LEFT_PAREN);
            return true;
        }
        return false;
    }

    boolean getRightParen() {
        if (peek == ')') {
            curToken = new Token(Grammar.Terminal.RIGHT_PAREN);
            return true;
        }
        return false;
    }
}
