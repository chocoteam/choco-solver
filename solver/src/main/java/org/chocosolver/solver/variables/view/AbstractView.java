package org.chocosolver.solver.variables.view;

import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.impl.AbstractVariable;

public abstract class AbstractView<V extends Variable> extends AbstractVariable implements IView<V> {

    protected V[] variables;

    /**
     * Default constructor for views.
     *
     * @param name name of the view
     * @param variables observed variables
     */
    protected AbstractView(String name, V... variables) {
        super(name, variables[0].getModel());
        this.variables = variables;
        for (int i = 0; i < variables.length; i++) {
            variables[i].subscribeView(this, i);
        }
    }

    @Override
    public V[] getVariables() {
        return variables;
    }
}
