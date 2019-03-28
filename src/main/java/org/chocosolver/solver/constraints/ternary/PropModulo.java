/*
@author Arthur Godet <arth.godet@gmail.com>
@since 28/03/2019
*/
package org.chocosolver.solver.constraints.ternary;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * X % Y = Z
 * A propagator for the constraint Z = X % Y where X, Y and Z are integer, possibly negative, variables
 * This propagator also supports constructor and filtering algorithm where Y is an int, and where both Y and Z are int
 * The filtering algorithm both supports bounded and enumerated integer variables
 */
public class PropModulo extends Propagator<IntVar> {
    private IntVar x;
    private IntVar y;
    private IntVar z;
    private Integer a;
    private Integer b;

    private boolean alreadyPropagated;

    public PropModulo(IntVar x, IntVar y, IntVar z) {
        super(new IntVar[]{x, y, z}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PropModulo(IntVar x, int a, IntVar z) {
        super(new IntVar[]{x, z}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.z = z;
        this.a = a;
    }

    public PropModulo(IntVar x, int a, int b) {
        super(new IntVar[]{x}, PropagatorPriority.LINEAR, false);
        this.x = x;
        this.a = a;
        this.b = b;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.boundAndInst();
    }

    private TIntArrayList usedValues = new TIntArrayList();

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if(a==null && b==null) {
            y.removeValue(0, this);
            usedValues.clear();
            for(int vx = x.getLB(); vx<=x.getUB(); vx=x.nextValue(vx)) {
                boolean toRemove = true;
                for(int vy = y.getLB(); vy<=y.getUB(); vy=y.nextValue(vy)) {
                    if(vy!=0 && z.contains(vx%vy)) {
                        usedValues.add(vx%vy);
                        toRemove = false;
                    }
                }
                if(toRemove) {
                    x.removeValue(vx, this);
                }
            }
            for(int v = z.getLB(); v<=z.getUB(); v=z.nextValue(v)) {
                if(!usedValues.contains(v)) {
                    z.removeValue(v, this);
                }
            }
        } else if(b == null) {
            usedValues.clear();
            for(int v = x.getLB(); v<=x.getUB(); v=x.nextValue(v)) {
                if(z.contains(v%a)) {
                    usedValues.add(v%a);
                } else {
                    x.removeValue(v, this);
                }
            }
            for(int v = z.getLB(); v<=z.getUB(); v=z.nextValue(v)) {
                if(!usedValues.contains(v)) {
                    z.removeValue(v, this);
                }
            }
        } else if(!alreadyPropagated) {
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
        if(a==null && b==null) {
            if(x.isInstantiated() && y.isInstantiated() && z.isInstantiated()) {
                return x.getValue()%y.getValue()==z.getValue() ? ESat.TRUE : ESat.FALSE;
            }
        } else if(b == null) {
            if(x.isInstantiated() && z.isInstantiated()) {
                return x.getValue()%a==z.getValue() ? ESat.TRUE : ESat.FALSE;
            }
        } else {
            if(x.isInstantiated()) {
                return x.getValue()%a==b ? ESat.TRUE : ESat.FALSE;
            }
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        if(a==null && b==null) {
            return x.getName()+" % "+y.getName()+" = "+z.getName();
        } else if(b == null) {
            return x.getName()+" % "+a+" = "+z.getName();
        } else {
            return x.getName()+" % "+a+" = "+b;
        }
    }

}
