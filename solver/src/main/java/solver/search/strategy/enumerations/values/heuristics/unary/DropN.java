package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Metric;

public class DropN extends UnaryHeuristicVal<HeuristicVal> {

    Metric f;


    private DropN(Action action) {
        super(action);
    }

    public DropN(HeuristicVal sub) {
        this(sub, new Const(1));
    }

    public DropN(HeuristicVal sub, Action action) {
        this(sub, new Const(1), action);
    }

    public DropN(HeuristicVal sub, Metric metric) {
        super(sub);
        this.f = metric;
        doUpdate(action);
    }

    public DropN(HeuristicVal sub, Metric metric, Action action) {
        super(sub, action);
        this.f = metric;
        doUpdate(action);
    }


    public boolean hasNext() {
        return sub.hasNext();
    }

    public int next() {
        return sub.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("DropN.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        f.update(action);
        int _f = f.getValue();
        // discard the n first elements
        while (sub.hasNext() && _f > 0) {
            sub.next();
            _f--;
        }
    }

    @Override
    public UnaryHeuristicVal duplicate() {
        DropN duplicata = new DropN(this.action);
        duplicata.f = this.f;
        return duplicata;
    }
}
