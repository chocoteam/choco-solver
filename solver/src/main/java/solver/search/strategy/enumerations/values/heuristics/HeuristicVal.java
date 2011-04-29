package solver.search.strategy.enumerations.values.heuristics;

import choco.kernel.common.util.iterators.IntIterator;
import gnu.trove.THashMap;

public abstract class HeuristicVal implements IntIterator {

    protected final Action action;

    /**
     * Specific constructor with default action (<code>Action.open_node</code>)
     */
    protected HeuristicVal() {
        this(Action.open_node);
    }

    protected HeuristicVal(Action action) {
        this.action = action;
    }

    /**
     * Duplicate <code>this</code> and its internal structure
     * (objects that do not extend {@link HeuristicVal} are shared, not duplicated)
     *
     * @param map map to store duplicated HeuristicVal
     * @return copy of <code>this</code>
     */
    public abstract HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map);

    /**
     * Update the internal structure of <code>this</code>, according to the given <code>action</code>.
     *
     * @param action action of the update order
     */
    public void update(Action action) {
        if (this.action.equals(action)) {
            doUpdate(action);
        }
    }

    protected abstract void doUpdate(Action action);

    public Action getAction() {
        return action;
    }
}
