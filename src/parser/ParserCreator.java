package parser;

import lexer.LexException;
import lexer.Lexer;
import lexer.TokenRetriever;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserCreator {
    // Alg 4.46 in textbook
    public static Parser createParsingTables(Grammar G, String input) throws LexException {
        Map<ActionPair, ActionType> actionTable = new HashMap<>();
        Map<GotoPair, Integer> gotoTable = new HashMap<>();

        G.computeFirsts();
        G.computeFollows();

        List<ItemSet> C = ItemSet.canonItemSet(G);

        for (int i = 0; i < C.size(); ++i) { // Loop over all states/itemsets i
            ItemSet itemSet_i = C.get(i);
            // Action table
            for (Item I : itemSet_i.items()) {
                // Determine reductions/accepting
                if (I.isComplete()) {
                    Grammar.NonTerminal A = I.production.head;
                    // Determine accepting
                    if (A.equals(G.startSymbol)) {
                        actionTable.put(new ActionPair(i, Grammar.Terminal.SENTINEL), new AcceptAction());
                    }
                    else { // This is a reduction
                        // Loop over all a that could follow A
                        for (Grammar.Terminal a : G.follows.get(A)) {
                            actionTable.put(new ActionPair(i, a), new ReduceAction(I.production));
                        }
                    }
                } // Determine shifts
                else {
                    Grammar.Symbol symbol = I.getNext();
                    if (symbol instanceof Grammar.Terminal) {
                        Grammar.Terminal a = (Grammar.Terminal) symbol;
                        ItemSet itemSet_j = ItemSet.goTo(itemSet_i, a, G); // This is alr computed in canonItemSet but oh well
                        int j = C.indexOf(itemSet_j);
                        actionTable.put(new ActionPair(i, a), new ShiftAction(j));
                    }
                }
            }

            // Goto table
            for (Grammar.NonTerminal A : Grammar.NonTerminal.values()) {
                ItemSet itemSet_j = ItemSet.goTo(itemSet_i, A, G);
                int j = C.indexOf(itemSet_j);
                gotoTable.put(new GotoPair(i, A), j);
            }
        }

        TokenRetriever t = new TokenRetriever(new Lexer(input));
        return new Parser(t, actionTable, gotoTable);
    }


    public interface ActionType {}
    public static final class ShiftAction implements ActionType {
        public final int state;
        ShiftAction(int state) { this.state = state; }
    }
    public static final class ReduceAction implements ActionType {
        public final Production prod;
        ReduceAction(Production p) { this.prod = p; }
    }
    public static final class AcceptAction implements ActionType {}
    public final class ErrorAction implements ActionType { // Implement error routines
    }

    // For the 2D maps
    public record ActionPair(Integer first, Grammar.Terminal second) {}
    public record GotoPair(Integer first, Grammar.NonTerminal second) {}
}
