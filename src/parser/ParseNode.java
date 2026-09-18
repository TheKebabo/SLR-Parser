package parser;


import java.util.List;

public class ParseNode {
    public final String symbol;
    List<ParseNode> children;
    public ParseNode(String symbol, List<ParseNode> children) {
        this.symbol = symbol;
        this.children = List.copyOf(children);
    }

    public void print(String linePadding) { // Display subtree rooted at this node
        System.out.println(linePadding + symbol);

        for (int i = 0; i < children.size(); ++i) {
            printNode(
                    children.get(i),
                    linePadding,
                    i == children.size() - 1
            );
        }
    }

    protected static void printNode(ParseNode node, String linePadding, boolean lastChild) {
        System.out.println(linePadding + "∟——— " + node.symbol);

        for (int i = 0; i < node.children.size(); ++i) {
            String childPrefix = linePadding + (lastChild ? "     " : "│    ");
            printNode(
                    node.children.get(i),
                    childPrefix,
                    i == node.children.size() - 1
            );
        }
    }
}
