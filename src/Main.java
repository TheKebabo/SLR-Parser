import lexer.*;
import parser.*;

void main() {
    Grammar G = GrammarCreator.createGrammar();
    try {
        Parser P = ParserCreator.createParsingTables(G, " ((cos 40)! + -3.5e2) - 5e3 ");
        ParseNode root = P.parse();
        root.print("");
    } catch (LexException | SyntaxException e) {
        System.out.println(e.getMessage());
    } catch (Exception e) {
        throw e;
    }
}
