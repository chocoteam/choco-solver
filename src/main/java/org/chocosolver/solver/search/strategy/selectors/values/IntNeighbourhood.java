package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

import java.util.List;

public class IntNeighbourhood implements IntValueSelector {

    private IntValueSelector selector;
    private List<Integer> initialValue;

    /**
     *
     * @param selector defines how we select the value if the initial values has been removed from the domain
     * @param initialValue get the initial value if any
     */
    public IntNeighbourhood(IntValueSelector selector, List<Integer> initialValue) {
        this.selector = selector;
        this.initialValue = initialValue;
    }

    @Override
    public int selectValue(IntVar var) {
        if(initialValue.contains(var.getId())){
            int initial = initialValue.get(var.getId());
            if(var.contains(initial)){
                return initial;
            }
        }
        return selector.selectValue(var);
    }

}
