package solver.search.strategy.enumerations.values.comparators;

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.metrics.Metric;

public class Distance extends IntComparator {
    int value;
    Metric metric;

    public Distance(Metric metric) {
        super();
        this.metric = metric;
        this.value = metric.getValue();
    }

    public Distance(Metric metric, Action action) {
        super(action);
        this.metric = metric;
        this.value = metric.getValue();
    }

    public int compare(int arg0, int arg1) {
        int d0 = Math.abs(value - arg0);
        int d1 = Math.abs(value - arg1);
        return d1 - d0;
    }

    @Override
    public void update(Action action) {
        metric.update(action);
        this.value = metric.getValue();
    }

    @Override
    protected void doUpdate() {
    }
}
