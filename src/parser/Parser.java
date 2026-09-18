package parser;

import lexer.FloatToken;
import lexer.LexException;
import lexer.Token;
import lexer.TokenRetriever;

import java.util.*;

// This will be a shift-reduce parser, specifically LR
public class Parser {
    private final TokenRetriever tokenStream;
    private Token lookahead;

    private final Deque<Integer> stateStack; // The top of the stack is index 0
    private final Deque<ParseNode> nodeStack; // Each node corresponds directly to a state on stateStack

    private final Map<ParserCreator.ActionPair, ParserCreator.ActionType> actionTable;
    private final Map<ParserCreator.GotoPair, Integer> gotoTable;

    public Parser(TokenRetriever t,
                  Map<ParserCreator.ActionPair, ParserCreator.ActionType> a,
                  Map<ParserCreator.GotoPair, Integer> g) throws LexException {
        tokenStream = t;
        actionTable = a;
        gotoTable = g;
        stateStack = new ArrayDeque<>();
        stateStack.addFirst(0); // Start with the starting state
        nodeStack = new ArrayDeque<>();
        retrieveToken();
    }
    private void retrieveToken() throws LexException { lookahead = tokenStream.retrieve(); }

    // Will build the parse tree
    public ParseNode parse() throws LexException, SyntaxException {
        while (true) {
            int state = stateStack.getFirst();
            Grammar.Terminal t = lookahead.terminal;

            ParserCreator.ActionType action = actionTable.get(new ParserCreator.ActionPair(state, t));
            // This isn't good polymorphism I know
            if (action == null || action instanceof ParserCreator.ErrorAction) { // error
                errorAction();
            } else if (action instanceof ParserCreator.ShiftAction shift) {
                shiftAction(shift.state);
            } else if (action instanceof ParserCreator.ReduceAction reduce) {
                reduceAction(reduce.prod);
            } else if (action instanceof ParserCreator.AcceptAction) {
                return acceptAction();
            } else {
                throw new ParserTableException("Unknown parser action: " + action);
            }
        }
    }

    private void shiftAction(int state) throws LexException {
        stateStack.addFirst(state); // Shift state onto stack

        // Add corresponding parse tree leaf node
        if (lookahead instanceof FloatToken floatToken) {
            nodeStack.addFirst(new ParseNode(Float.toString(floatToken.val), List.of()));
        } else {
        nodeStack.addFirst(new ParseNode(lookahead.terminal.toString(), List.of()));
        }

        retrieveToken();
    }

    private void reduceAction(Production p) throws LexException {
        // Pop the first r=|p.body| symbols off the stack, we went from state s_m to state s_{m-r}
        // We also want to get the corresponding values, and correct the order since we are popping in reverse
        ParseNode[] nodes = new ParseNode[p.bodySize()];
        for (int i = p.bodySize()-1; i >= 0; --i) { // Go in reverse for the values
            stateStack.removeFirst();
            nodes[i] = nodeStack.removeFirst();
        }

        ParseNode res = p.applySemanticAction(Arrays.asList(nodes)); // Build the tree

        // Push onto the stack the state corresponding to the goto from state s_{m-r} with p.head
        Integer newState = gotoTable.get(new ParserCreator.GotoPair(stateStack.getFirst(), p.head));
        if (newState == null) { // Table error
            throw new ParserTableException("Missing goto entry for state " + stateStack.getFirst()
                            + " and non-terminal " + p.head);
        }
        stateStack.addFirst(newState);
        nodeStack.addFirst(res);
    }

    private ParseNode acceptAction() {
        return nodeStack.getFirst();
    }
    private void errorAction() throws SyntaxException {
        throw new SyntaxException(tokenStream.getCurrentLoc());
    }
}