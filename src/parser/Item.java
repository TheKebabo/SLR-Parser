package parser;

import java.util.Objects;

public final class Item implements Comparable<Item> {
    public final Production production;
    public final int parsePos;

    Item(Production p, int pos) throws IllegalArgumentException {
        if (pos >= 0 && pos <= p.bodySize()) {
            production = p;
            parsePos = pos;
        }
        else throw new IllegalArgumentException("Invalid item parse position");
    }

    public boolean isComplete() { return parsePos == production.bodySize(); }
    public Item advance() { // A -> a.Xb becomes A -> aX.b, returning an new Item
        if (isComplete()) { throw new IllegalStateException("Cannot advance past end of production body"); }
        return new Item(production, parsePos + 1);
    }
    public Grammar.Symbol getNext() {
        return production.body().get(parsePos);
    }

    @Override
    public int compareTo(Item that) {
        if (that == null) { throw new NullPointerException("Cannot compare with null"); }

        int prodComp = this.production.compareTo(that.production);
        if (prodComp != 0) return prodComp;

        return Integer.compare(this.parsePos, that.parsePos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item other)) return false;
        return this.parsePos == other.parsePos && Objects.equals(this.production, other.production);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, parsePos);
    }
}
