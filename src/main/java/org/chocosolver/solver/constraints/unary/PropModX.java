/*
@author Arthur Godet <arth.godet@gmail.com>
@since 29/03/2019
*/
package org.chocosolver.solver.constraints.unary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;


/**
 * X % a = b
 * A propagator for the constraint b = X % a where X is an integer, possibly negative, variable and a and b are int
 * The filtering algorithm both supports bounded and enumerated integer variables
 */
public class PropModX extends Propagator<IntVar> {
    private IntVar x;
    private int a;
    private int b;
    private boolean alreadyPropagated;

    public PropModX(IntVar x, int a, int b) {
        super(new IntVar[]{x}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.a = a;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(a!= 0 && !alreadyPropagated) {
            alreadyPropagated = true;
            for(int v = x.getLB(); v<=x.getUB(); v = x.nextValue(v)) {
                if(v%a != b) {
                    x.removeValue(v, this);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(x.isInstantiated()) {
            return a==0 || x.getValue()%a==b ? ESat.TRUE : ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return x.getName()+" % "+a+" = "+b;
    }

}
