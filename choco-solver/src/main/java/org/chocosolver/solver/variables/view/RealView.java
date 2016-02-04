/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.view;


import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.RealEventType;
import org.chocosolver.solver.variables.impl.AbstractVariable;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 20/07/12
 */
public class RealView extends AbstractVariable implements IView, RealVar {

    protected final IntVar var;

    protected final double precision;

    public RealView(IntVar var, double precision) {
        super("(real)" + var.getName(), var.getSolver());
        this.var = var;
        this.precision = precision;
        this.var.subscribeView(this);
    }

    @Override
    public IntVar getVariable() {
        return var;
    }

    @Override
    public void transformEvent(IEventType evt, ICause cause) throws ContradictionException {
		RealEventType realevt;
		IntEventType intevt = (IntEventType) evt;
		switch (intevt){
			case INSTANTIATE:
			case BOUND:
				realevt = RealEventType.BOUND;break;
			case INCLOW:realevt = RealEventType.INCLOW;break;
			case DECUPP:realevt = RealEventType.DECUPP;break;
			case REMOVE:return;
			default:throw new UnsupportedOperationException("unexpected event transformation in RealView");
		}
        notifyPropagators(realevt, this);
    }

    @Deprecated
    @Override
    public void recordMask(int mask) {
    }

    @Override
    public String toString() {
        return "(real)" + var.toString();
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////

    @Override
    public double getLB() {
        return var.getLB();
    }

    @Override
    public double getUB() {
        return var.getUB();
    }

    @Override
    public boolean updateLowerBound(double value, ICause cause) throws ContradictionException {
        if (var.updateLowerBound((int) Math.ceil(value - precision), this)) {
            notifyPropagators(RealEventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUpperBound(double value, ICause cause) throws ContradictionException {
        if (var.updateUpperBound((int) Math.floor(value + precision), this)) {
            notifyPropagators(RealEventType.INCLOW, cause);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateBounds(double lowerbound, double upperbound, ICause cause) throws ContradictionException {
        int c = 0;
        c += (var.updateLowerBound((int) Math.ceil(lowerbound - precision), this) ? 1 : 0);
        c += (var.updateUpperBound((int) Math.floor(upperbound + precision), this) ? 2 : 0);
        switch (c) {
            case 3:
                notifyPropagators(RealEventType.BOUND, cause);
                return true;
            case 2:
                notifyPropagators(RealEventType.DECUPP, cause);
                return true;
            case 1:
                notifyPropagators(RealEventType.INCLOW, cause);
                return true;
            default: //cas 0;
                return false;
        }
    }

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public boolean isInstantiated() {
        return var.isInstantiated();
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
    }

    @Override
    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return VIEW | REAL;
    }

    @Override
    public RealVar duplicate() {
        return solver.makeRealView(this.var, this.precision);
    }

}
