package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.variables.IntVar;

import java.util.Map;

/**
 * A Neighbourhood search to find a solution close to the original solution
 *
 * @author Pierre Tassel
 * @since 10/10/2019
 */
public class IntNeighbourhood implements IntValueSelector {

    private IntValueSelector selector;
    private Map<Integer ,Integer> initialValue;

    /**
     *
     * @param selector defines how we select the value if the initial values has been removed from the domain
     * @param initialValue get the initial value if any
     */
    public IntNeighbourhood(IntValueSelector selector, Map<Integer ,Integer> initialValue) {
        this.selector = selector;
        this.initialValue = initialValue;
    }

    @Override
    public int selectValue(IntVar var) {
        // if the variable is selected to have an initial value
        if(initialValue.containsKey(var.getId())){
            int initial = initialValue.get(var.getId());
            // if the domains contains the initial value, we select it
            if(var.contains(initial)){
                return initial;
            }
        }
        // otherwise we get the value selected by the defined search strategy
        return selector.selectValue(var);
    }

}
