package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.predicates.Predicate;

public class Filter extends UnaryHeuristicVal<Lookahead> {
    Predicate pred;

    private Filter() {
        super(Action.none);
    }

    public Filter(Predicate p, HeuristicVal sub) {
        super(new Lookahead(sub), Action.none);
        this.pred = p;
    }

    public boolean hasNext() {
        while (sub.hasNext() && !pred.eval(sub.peekNext())) {
            sub.next();
        }
        return sub.hasNext();
    }

    public int next() {
        return sub.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("Filter.remove not implemented");
    }

    @Override
    public void update(Action action) {
        sub.update(action);
        pred.update(action);
    }

    @Override
    public UnaryHeuristicVal duplicate() {
        Filter duplicata = new Filter();
        duplicata.pred = this.pred;
        return duplicata;
    }
}
