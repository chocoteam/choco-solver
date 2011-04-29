package solver.search.strategy.enumerations.values.heuristics.nary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.comparators.IntComparator;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.unary.Lookahead;

public class Join extends NaryHeuristicVal<Lookahead> {

    IntComparator f;

    private Join(Action action) {
        super(action);
    }

    public Join(IntComparator f, HeuristicVal left, HeuristicVal right) {
        super(new Lookahead[]{new Lookahead(left), new Lookahead(right)});
        this.f = f;
    }

    /**
     * Beware: action is NOT given in parameter to Lookahead constructor.
     * @param f comparator
     * @param left first heuristic val
     * @param right second heuristic val
     * @param action action of <code>this</code>
     */
    public Join(IntComparator f, HeuristicVal left, HeuristicVal right, Action action) {
        super(new Lookahead[]{new Lookahead(left, left.getAction()), new Lookahead(right, right.getAction())}, action);
        this.f = f;
    }

    public boolean hasNext() {
        return subs[0].hasNext() || subs[1].hasNext();
    }

    public int next() {
        if (!subs[0].hasNext()) {
            return subs[1].next();
        } else if (!subs[1].hasNext()) {
            return subs[0].next();
        } else if (f.compare(subs[0].peekNext(), subs[1].peekNext()) >= 0) {
            return subs[0].next();
        } else {
            return subs[1].next();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Join.remove not implemented");
    }

    @Override
    public void update(Action action) {
        subs[0].update(action);
        subs[1].update(action);
        f.update(action);
    }

    @Override
    protected void doUpdate(Action action) {}

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            Join duplicata = new Join(this.action);
            duplicata.subs = new Lookahead[]{
                    (Lookahead) this.subs[0].duplicate(map),
                    (Lookahead) this.subs[1].duplicate(map)
            };
            duplicata.f = f;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}

