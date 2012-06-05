/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver.variables;

import com.sun.istack.internal.NotNull;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.delta.NoDelta;
import solver.variables.view.IView;

public class MetaVariable<V extends Variable> extends AbstractVariable<NoDelta, IView, MetaVariable<V>> implements Variable<NoDelta, IView> {

    protected V[] components;
    protected int dim;

    public MetaVariable(String name, Solver sol, V[] vars) {
        super(name, sol);
        components = vars;
        dim = vars.length;
		solver.associates(this);
    }

    @Override
    public boolean instantiated() {
        for (int i = 0; i < dim; i++) {
            if (!components[i].instantiated()) {
                return false;
            }
        }
        return true;
    }

    public void notifyPropagators(EventType event, @NotNull ICause cause) throws ContradictionException {
        if ((modificationEvents & event.mask) != 0) {
            //records.forEach(afterModification.set(this, event, cause));
            solver.getEngine().onVariableUpdate(this, afterModification.set(this, event, cause));
        }
        notifyViews(event, cause);
        notifyMonitors(event, cause);
    }

    public void notifyMonitors(EventType event, @NotNull ICause cause) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event, cause);
        }
    }

    @Override
    public Explanation explain(VariableState what) {
        throw new UnsupportedOperationException("MetaVariable does not (yet) implement method explain(...)");
    }

    @Override
    public Explanation explain(VariableState what, int val) {
        throw new UnsupportedOperationException("GraphVar does not (yet) implement method explain(...)");
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    public String toString() {
        String s = this.name + " : {";
        for (int i = 0; i < dim - 1; i++) {
            s += components[i].toString() + ", ";
        }
        s += components[dim - 1].toString() + "}";
        return s;
    }

    public V[] getComponents() {
        return components;
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
//        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return VAR + META;
    }
}
