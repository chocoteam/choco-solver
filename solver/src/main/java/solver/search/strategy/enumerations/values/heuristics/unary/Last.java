package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class Last extends UnaryHeuristicVal<HeuristicVal> {

    int last;

    public Last() {
        super(Action.none);
    }

    public Last(HeuristicVal sub) {
        super(sub);
    }

    public boolean hasNext() {
        return sub.hasNext();
    }

    public int next() {
        last = sub.next();
        while (sub.hasNext()) {
            last = sub.next();
        }
        return last;
    }

    public void remove() {
        throw new UnsupportedOperationException("Last.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {

    }

    @Override
    public UnaryHeuristicVal duplicate() {
        Last duplicata = new Last();
        duplicata.last = this.last;
        return duplicata;
    }
}
