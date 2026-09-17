package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Production implements Comparable<Production> {
    public final Grammar.NonTerminal head;
    private final List<Grammar.Symbol> body;

    Production(Grammar.NonTerminal h, List<Grammar.Symbol> b) {
        head = h;
        body = List.copyOf(b); // Make a new list
    }

    public List<Grammar.Symbol> body() { // For encapsulation
        return Collections.unmodifiableList(body);
    }
    public int bodySize() { return body.size(); }

    @Override
    public int compareTo(Production p) {
        if (p == null) { throw new NullPointerException("Cannot compare with null"); }

        int headComp = Integer.compare(this.head.code(), p.head.code());
        if (headComp != 0) return headComp; // Unequal heads

        int sizeComp = Integer.compare(this.body.size(), p.body.size());
        if (sizeComp != 0) return sizeComp; // Unequal amounts of symbols in bodies

        for (int i = 0; i < this.body.size(); i++) {
            int symbolComp = Integer.compare(this.body.get(i).code(), p.body.get(i).code());
            if (symbolComp != 0) return symbolComp; // Unequal body symbol
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Production other)) return false;
        return this.head == other.head && Objects.equals(this.body, other.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, body);
    }

    @Override
    public String toString() {
        return head + " -> " + body;
    }
}
