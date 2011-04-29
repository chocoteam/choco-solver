package solver.search.strategy.enumerations.values.predicates;

import solver.search.strategy.enumerations.values.heuristics.Action;

public class Greater extends Predicate {
    int value;

    public Greater(int value) {
        super();
        this.value = value;
    }

    @Override
    public void update(Action action) {}

    public boolean eval(int x) {
        return x > value;
    }

}
