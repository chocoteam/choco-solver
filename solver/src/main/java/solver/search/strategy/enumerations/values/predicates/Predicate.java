package solver.search.strategy.enumerations.values.predicates;

import solver.search.strategy.enumerations.values.heuristics.Action;

/**
 * TODO: could have an internal structure that must be updated  ( pick x such as  x > max already used values) ?
 *
 *
 * BEWARE: this should not embaded side effect: during HeuristicVal.duplicate(),
 * BEWARE: Predicates are shared, not duplicated !
 */
public abstract class Predicate {

    final Action action;

    public static Predicate even = new Predicate(Action.none) {

        @Override
        public void update(Action action) {}

        @Override
        public boolean eval(int i) {
            return i % 2 == 0;
        }
    };

    public static Predicate odd = new Predicate(Action.none) {

        @Override
        public void update(Action action) {}

        @Override
        public boolean eval(int i) {
            return !Predicate.even.eval(i);
        }
    };

    protected Predicate() {
        this.action = Action.open_node;
    }

    protected Predicate(Action action) {
        this.action = action;
    }

    public abstract void update(Action action);

    public abstract boolean eval(int i);
}
