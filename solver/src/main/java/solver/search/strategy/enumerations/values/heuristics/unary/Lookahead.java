package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class Lookahead extends UnaryHeuristicVal<HeuristicVal> {

    int next;
    boolean hasNext = true;

    private Lookahead(Action action) {
        super(action);
    }

    public Lookahead(HeuristicVal sub) {
        super(sub);
        doUpdate(action);
    }

    public Lookahead(HeuristicVal sub, Action action) {
        super(sub, action);
        doUpdate(action);
    }

    public boolean hasNext() {
        return hasNext;
    }

    public int next() {
        int result = next;
        if (sub.hasNext()) {
            next = sub.next();
        } else {
            hasNext = false;
        }
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("Lookahead.remove not implemented");
    }

    public int peekNext() {
        return next;
    }

    @Override
    protected void doUpdate(Action action) {
        hasNext = true;
        next();
    }

    @Override
    public UnaryHeuristicVal duplicate() {
        Lookahead duplicata = new Lookahead(this.action);
        duplicata.hasNext = this.hasNext;
        duplicata.next = this.next;
        return duplicata;
    }
}
