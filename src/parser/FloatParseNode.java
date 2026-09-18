package parser;

import java.util.List;

public class FloatParseNode extends ParseNode {
    public final float val;
    public FloatParseNode(String symbol, List<ParseNode> children, float val) {
        super(symbol, children);
        this.val = val;
    }

    @Override
    public void print(String linePadding) { // Display subtree rooted at this node
        System.out.println(linePadding + val);
        for (int i = 0; i < children.size(); ++i) {
            printNode(
                    children.get(i),
                    linePadding,
                    i == children.size() - 1
            );
        }
    }
}
