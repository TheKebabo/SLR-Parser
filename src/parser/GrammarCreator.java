package parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrammarCreator {
    public static Grammar createGrammar() {
        Map<Grammar.NonTerminal, List<Production>> productions = new HashMap<>();

        productions.put(Grammar.NonTerminal.START, List.of(
            new Production( // START -> EXPR
                Grammar.NonTerminal.START,
                List.of(
                    Grammar.NonTerminal.EXPR
                ),
                nodes -> new ParseNode("START", nodes))
        ));


        productions.put(Grammar.NonTerminal.EXPR, List.of(
                new Production( // EXPR -> A EXPR'
                    Grammar.NonTerminal.EXPR,
                    List.of(
                        Grammar.NonTerminal.A,
                        Grammar.NonTerminal.EXPR_
                    ),
                    nodes -> new ParseNode("EXPR", nodes))
        ));


        productions.put(Grammar.NonTerminal.EXPR_, List.of(
            new Production( // EXPR' -> + A EXPR'
                Grammar.NonTerminal.EXPR_,
                List.of(
                    Grammar.Terminal.PLUS,
                    Grammar.NonTerminal.A,
                    Grammar.NonTerminal.EXPR_
                ),
                nodes -> new ParseNode("EXPR'", nodes)),
            new Production( // EXPR' -> - A EXPR'
                Grammar.NonTerminal.EXPR_,
                List.of(
                    Grammar.Terminal.MINUS,
                    Grammar.NonTerminal.A,
                    Grammar.NonTerminal.EXPR_
                ),
                nodes -> new ParseNode("EXPR'", nodes)),
            new Production( // EXPR' -> ε
                Grammar.NonTerminal.EXPR_,
                List.of(),
                nodes -> new ParseNode(
                            "EXPR'",
                            List.of(new ParseNode("ε", List.of()))
                ))
        ));

        productions.put(Grammar.NonTerminal.A, List.of(
            new Production( // A -> B ^ A
                Grammar.NonTerminal.A,
                List.of(
                    Grammar.NonTerminal.B,
                    Grammar.Terminal.EXP,
                    Grammar.NonTerminal.A
                ),
                nodes -> new ParseNode("A", nodes)),
            new Production( // A -> B
                Grammar.NonTerminal.A,
                List.of(
                    Grammar.NonTerminal.B
                ),
                nodes -> new ParseNode("A", nodes))
        ));


        productions.put(Grammar.NonTerminal.B, List.of(
            new Production( // B -> cos B
                Grammar.NonTerminal.B,
                List.of(
                    Grammar.Terminal.COS,
                    Grammar.NonTerminal.B
                ),
                nodes -> new ParseNode("B", nodes)),
            new Production( // B -> C
                Grammar.NonTerminal.B,
                List.of(
                    Grammar.NonTerminal.C
                ),
                nodes -> new ParseNode("B", nodes))
        ));


        productions.put(Grammar.NonTerminal.C, List.of(
            new Production( // C -> D C'
                Grammar.NonTerminal.C,
                List.of(
                    Grammar.NonTerminal.D,
                    Grammar.NonTerminal.C_
                ),
                nodes -> new ParseNode("C", nodes))
        ));


        productions.put(Grammar.NonTerminal.C_, List.of(
            new Production( // C' -> ! C'
                Grammar.NonTerminal.C_,
                List.of(
                        Grammar.Terminal.FACT,
                        Grammar.NonTerminal.C_
                ),
                nodes -> new ParseNode("C'", nodes)),
            new Production( // C' -> ε
                Grammar.NonTerminal.C_,
                List.of(),
                nodes -> new ParseNode(
                    "C'",
                    List.of(new ParseNode("ε", List.of()))
                ))
        ));


        productions.put(Grammar.NonTerminal.D, List.of(
            new Production( // D -> ( EXPR )
                Grammar.NonTerminal.D,
                List.of(
                    Grammar.Terminal.LEFT_PAREN,
                    Grammar.NonTerminal.EXPR,
                    Grammar.Terminal.RIGHT_PAREN
                ),
                nodes -> new ParseNode("D", nodes)),
            new Production(  // D -> float
                Grammar.NonTerminal.D,
                List.of(
                        Grammar.Terminal.FLOAT
                ),
                nodes -> new ParseNode("D", nodes))
        ));


        return new Grammar(productions, Grammar.NonTerminal.START);
    }
}
