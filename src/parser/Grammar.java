package parser;

import java.util.*;

public final class Grammar { // Essentially a list of productions with a designated start symbol
    // Each non-terminal has its own list of productions
    private final Map<NonTerminal, List<Production>> productions;
    public final NonTerminal startSymbol;
    // The set of terminals that could be at the immediate start of any string that X derives
    public final Map<Symbol, Set<Terminal>> firsts;
    // The set of terminals that could immediately follow any string that X derives
    public final Map<Symbol, Set<Terminal>> follows;

    Grammar(Map<NonTerminal, List<Production>> p, NonTerminal s) {
        productions = p;
        startSymbol = s;
        firsts = new HashMap<>();
        follows = new HashMap<>();
    }

    public Production startProduction() {
        return productions.get(startSymbol).getFirst(); // We assume the entry production is at the 0th index
    }

    public List<Production> productions() { // For encapsulation
        List<Production> p = new ArrayList<>();
        for (List<Production> prods : productions.values()) {
            p.addAll(prods);
        }
        return Collections.unmodifiableList(p);
    }

    public void computeFirsts() {
        // Initialise for terminals
        for (Terminal t : Terminal.values()) { firsts.put(t, new HashSet<>(Set.of(t))); }

        // Initialise for non-terminals
        for (NonTerminal n : NonTerminal.values()) { firsts.put(n, new HashSet<>()); }

        boolean updated = true; // Repeat until no more updates
        while (updated) {
            updated = false;

            for (NonTerminal X : NonTerminal.values()) { // Loop over all non-terminals
                List<Production> prods = productions.get(X);
                if (prods == null) continue;

                Set<Terminal> Xfirsts = firsts.get(X);
                for (Production p : prods) { // Loop over all its productions
                    int prevXfirstsSize = Xfirsts.size();

                    // Case epsilon production
                    if (p.bodySize() == 0) { Xfirsts.add(null); } // Add epsilon / null
                    else {
                        boolean allYDeriveEpsilon = true;
                        // Check symbols in the body Y1 Y2 ... Yk
                        for (Symbol Y : p.body()) {
                            Set<Terminal> Yfirsts = firsts.get(Y);

                            // Add all terminals from first(Y) except epsilon
                            for (Terminal t : Yfirsts) {
                                if (t != null) { Xfirsts.add(t); }
                            }

                            // If Y doesn't derive epsilon, stop
                            if (!Yfirsts.contains(null)) {
                                allYDeriveEpsilon = false;
                                break;
                            }
                        }

                        // If all Y can derive epsilon then so can X
                        if (allYDeriveEpsilon) { Xfirsts.add(null); }
                    }

                    if (Xfirsts.size() > prevXfirstsSize) updated = true;
                }
            }
        }
    }

    private Set<Terminal> first(List<Symbol> string) {
        Set<Terminal> res = new HashSet<>();

        if (string.isEmpty()) {  // Empty string derives epsilon
            res.add(null);
            return res;
        }

        boolean allDeriveEpsilon = true;
        for (Symbol X : string) {
            Set<Terminal> Xfirsts = firsts.get(X);

            for (Terminal t : Xfirsts) {
                if (t != null) { res.add(t); }
            }

            if (!Xfirsts.contains(null)) { // There is no epsilon
                allDeriveEpsilon = false;
                break;
            }
        }

        if (allDeriveEpsilon) { res.add(null); }
        return res;
    }

    public void computeFollows() {
        // Initialise for non-terminals
        for (NonTerminal n : NonTerminal.values()) {
            follows.put(n, new HashSet<>());
        }

        // Place $ / sentinel in follow(startSymbol)
        follows.get(startSymbol).add(Terminal.SENTINEL);

        boolean updated = true;  // Repeat until no more updates
        while (updated) {
            updated = false;

            for (NonTerminal X : NonTerminal.values()) { // Loop over all non-terminals
                List<Production> prods = productions.get(X);
                if (prods == null) continue;

                for (Production p : prods) {
                    List<Symbol> body = p.body();

                    for (int i = 0; i < body.size(); i++) {  // Loop over production body Y1 Y2 ... Yk
                        Symbol Y = body.get(i);

                        // Only compute follow for non-terminals
                        if (Y instanceof NonTerminal) {
                            NonTerminal B = (NonTerminal)Y;

                            Set<Terminal> Bfollows = follows.get(B);
                            int BfollowsSize = Bfollows.size();

                            // We need the form X -> alpha B beta so we need to get beta
                            List<Symbol> beta = body.subList(i + 1, body.size());

                            // Rule 2. in textbook: add first(beta) without epsilon to follow(B)
                            Set<Terminal> betaFirsts = first(beta);
                            for (Terminal t : betaFirsts) {
                                if (t != null) { Bfollows.add(t); }
                            }

                            // Rule 3: If epsilon is in first(beta) (or beta is empty), add follow(X) to follow(B)
                            if (betaFirsts.contains(null)) {
                                Set<Terminal> Xfollows = follows.get(X);
                                if (Xfollows != null) { Bfollows.addAll(Xfollows); }
                            }

                            if (Bfollows.size() > BfollowsSize) { updated = true; }
                        }
                    }
                }
            }
        }
    }


    public interface Symbol {
        int code();
    }

    // For the calculation grammar (i.e. parsing)
    public enum Terminal implements Symbol {
        // Use ASCII code for single characters, else >=256 and derive toString()
        PLUS(43, "+"), MINUS(45, "-"), EXP(94, "^"), FACT(21, "!"), LEFT_PAREN(40, "("), RIGHT_PAREN(41, ")"),
        FLOAT(256, "float"), COS(257, "cos"), SENTINEL(258, "$");

        private final int code;
        private final String terminal;
        Terminal(int code, String terminal) { this.code = code; this.terminal = terminal; }
        public int code() { return this.code; }
        public String terminal() { return this.terminal; }
    }
    public enum NonTerminal implements Symbol {
        // A_ represents A' in the grammar
        START(512), EXPR(513), EXPR_(514), A(515), B(516), C(517), C_(518), D(519);

        private final int code;
        NonTerminal(int code) { this.code = code; }
        @Override public int code() { return this.code; }
    }
}
