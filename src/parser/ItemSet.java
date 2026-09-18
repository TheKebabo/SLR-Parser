package parser;

import java.util.*;

// This will also function as a 'state' in the parser
public class ItemSet {
    private final Set<Item> items;

    ItemSet() { items = new HashSet<>(); }
    ItemSet(Set<Item> is) { items = is; }

    void addItem(Item i) throws IllegalArgumentException {
        if (hasItem(i)) throw new IllegalArgumentException("Cannot add duplicate item to ItemSet");
        items.add(i);
    }
    boolean hasItem(Item i) { return items.contains(i); }
    public Set<Item> items() { // For encapsulation
        return Collections.unmodifiableSet(items);
    }
    public int size() { return items.size(); }

    // Compute the closure of an item set
    // For now this computes all the kernel and non-kernel items, this could be amended to save space
    public static ItemSet closure(ItemSet I, Grammar G) {
        // Initially add every item from I, since I is a subset of closure(I)
        ItemSet res = new ItemSet();
        for (Item i : I.items) { res.addItem(i); }

        boolean itemAdded = true; // Have at least one initial loop
        while (itemAdded) { // Loop until no items added in a round
            itemAdded = false;
            // We r updating res so need to iterate over old version
            Set<Item> currentRes = Set.copyOf(res.items());
            for (Item i : currentRes) { // A -> a . B b
                if (i.isComplete()) { continue; }

                Grammar.Symbol BSym = i.getNext();
                if (!(BSym instanceof Grammar.NonTerminal B)) { continue; }

                // We need items of the form B -> . c
                for (Production p : G.productions()) {
                    if (p.head != B) { continue; }

                    Item newItem = new Item(p, 0);
                    if (!res.hasItem(newItem)) {
                        res.addItem(newItem);
                        itemAdded = true;
                    }
                }
            }
        }
        return res;
    }

    // Compute the goto of an item set
    // i.e. the closure of the set of all items A -> a X . b such that A -> a . X b is in I
    public static ItemSet goTo(ItemSet I, Grammar.Symbol X, Grammar G) {
        ItemSet res = new ItemSet();

        for (Item i : I.items()) { // A -> a . Y b
            if (!i.isComplete() && i.getNext().equals(X)) { res.addItem(i.advance()); }
        }

        return closure(res, G);
    }

    // Compute the canonical set (will be an indexed list) of item sets, a set of LR(0) item sets for the LR(0) automata
    // This should be an augmented grammar G as described in the textbook, i.e. there is an additional starting symbol
    public static List<ItemSet> canonItemSet(Grammar G) {
        Item startItem = new Item(G.startProduction(), 0);
        ItemSet startSet = new ItemSet(Set.of(startItem));

        Set<ItemSet> C = new HashSet<>(); // This is to speed up 'C.contains'
        List<ItemSet> CList = new ArrayList<>(); // This is more useful, since state i is at index i

        // Initial closure
        ItemSet initClosure = closure(startSet, G);
        C.add(initClosure);
        CList.add(initClosure);

        boolean itemSetAdded = true; // Have at least one initial loop
        while (itemSetAdded) {
            itemSetAdded = false;
            for (int i = 0; i < CList.size(); i++) {
                ItemSet I = CList.get(i);
                // Loop over all grammar symbols
                for (Grammar.Terminal X : Grammar.Terminal.values()) {
                    ItemSet gotoSet = goTo(I, X, G);
                    if (gotoSet.size() > 0 && !C.contains(gotoSet)) { // New non-empty set created that isn't in C
                        C.add(gotoSet);
                        CList.add(gotoSet);
                        itemSetAdded = true;
                    }
                }
                for (Grammar.NonTerminal X : Grammar.NonTerminal.values()) {
                    ItemSet gotoSet = goTo(I, X, G);
                    if (gotoSet.size() > 0 && !C.contains(gotoSet)) { // New non-empty set created that isn't in C
                        C.add(gotoSet);
                        CList.add(gotoSet);
                        itemSetAdded = true;
                    }
                }
            }
        }

        return CList;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemSet other)) return false;
        return Objects.equals(this.items, other.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
