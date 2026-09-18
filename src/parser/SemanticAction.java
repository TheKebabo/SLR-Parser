package parser;

import java.util.List;

public interface SemanticAction {
    ParseNode action(List<ParseNode> nodes);
}
