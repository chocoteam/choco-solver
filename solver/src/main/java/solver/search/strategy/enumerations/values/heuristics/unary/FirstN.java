package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Metric;

public class FirstN extends UnaryHeuristicVal<HeuristicVal> {
    Metric f;
    int idx;

    private FirstN(Action action) {
        super(action);
    }

    public FirstN(HeuristicVal sub) {
        this(sub, new Const(1));
    }

    public FirstN(HeuristicVal sub, Action action) {
        this(sub, new Const(1), action);
    }

    public FirstN(HeuristicVal sub, Metric metric) {
        super(sub);
        this.f = metric;
        doUpdate(action);
    }

    public FirstN(HeuristicVal sub, Metric metric, Action action) {
        super(sub, action);
        this.f = metric;
        doUpdate(action);
    }

    public boolean hasNext() {
        return idx > 0 && sub.hasNext();
    }

    public int next() {
        idx--;
        return sub.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("FirstN.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        f.update(action);
        idx = f.getValue();
    }

    @Override
    public UnaryHeuristicVal duplicate() {
        FirstN duplicata = new FirstN(this.action);
        duplicata.f = this.f;
        duplicata.idx = this.idx;
        return duplicata;
    }
}
