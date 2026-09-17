package parser;

import lexer.LexException;
import lexer.Token;
import lexer.TokenRetriever;

import java.util.List;
import java.util.Map;

// This will be a shift-reduce parser, specifically LR
public class Parser {
    private final TokenRetriever tokenStream;
    private Token lookahead;

    private List<Integer> stateStack; // The top of the stack is index 0


    private Map<ParserCreator.ActionPair, ParserCreator.ActionType> actionTable;
    private Map<ParserCreator.GotoPair, Integer> gotoTable;

    public Parser(TokenRetriever t,
                  Map<ParserCreator.ActionPair, ParserCreator.ActionType> a,
                  Map<ParserCreator.GotoPair, Integer> g) throws LexException {
        tokenStream = t;
        actionTable = a;
        gotoTable = g;
        stateStack = List.of(0); // Start with the starting state
        retrieveToken();
    }
    private void retrieveToken() throws LexException { lookahead = tokenStream.retrieve(); }

    public void parse() throws LexException {
        while(true) {
            int state = stateStack.getFirst();
            Grammar.Terminal t = lookahead.terminal;

            ParserCreator.ActionType action = actionTable.get(new ParserCreator.ActionPair(state, t));
            // This isn't good polymorphism I know
            if (action instanceof ParserCreator.ShiftAction shift) {
                shiftAction(shift.state);
            } else if (action instanceof ParserCreator.ReduceAction reduce) {
                reduceAction(reduce.prod);
            } else if (action instanceof ParserCreator.AcceptAction accept) {
                // output
            } else if (action instanceof ParserCreator.ErrorAction error) {
                // error
            }
        }
    }

    private void shiftAction(int state) throws LexException {
        stateStack.addFirst(state); // Shift state onto stack
        retrieveToken();
    }

    private void reduceAction(Production p) throws LexException {
        // Pop the first r=|p.body| symbols off the stack, we went from state s_m to state s_{m-r}
        for (int i = 0; i < p.bodySize(); ++i) stateStack.removeFirst();
        // Push onto the stack the state corresponding to the goto from state s_{m-r} with p.head
        int newState = gotoTable.get(new ParserCreator.GotoPair(stateStack.getFirst(), p.head));
        stateStack.addFirst(newState);

//            p.action(); Call the semantic action to actually calculate
    }

    private void acceptAction() {}
    private void errorAction() throws SyntaxException {}
}
