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
package org.chocosolver.solver.variables.impl;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.tools.StringUtils;

import java.util.Arrays;

/**
 * @author jimmy
 */
public class FixedSetVarImpl extends AbstractVariable implements SetVar {

    /**
     * Set of values.
     */
    private final int[] values;

    /**
     * Index to iterate over kernel values.
     */
    private int kerIndex;

    /**
     * Index to iterate over envelope values.
     */
    private int envIndex;

    public FixedSetVarImpl(String name, TIntSet values, Model model) {
        super(name, model);
        this.values = values.toArray();
        Arrays.sort(this.values);
    }

    public FixedSetVarImpl(String name, int[] values, Model model) {
        // Remove duplicates
        this(name, new TIntHashSet(values), model);
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(" = {");
		int s = values.length;
		for (int v:values) {
			sb.append(v);
			s--;
			if (s > 0) {
				sb.append(",");
			}
		}
		sb.append("}");
		return sb.toString();
	}

    @Override
    public int getKernelFirst() {
        return values.length == 0
                ? SetVar.END
                : values[kerIndex = 0];
    }

    @Override
    public int getKernelNext() {
        return ++kerIndex >= values.length
                ? SetVar.END
                : values[kerIndex];
    }

    @Override
    public int getKernelSize() {
        return values.length;
    }

    @Override
    public boolean kernelContains(int element) {
        return Arrays.binarySearch(values, element) >= 0;
    }

    @Override
    public int getEnvelopeFirst() {
        return values.length == 0
                ? SetVar.END
                : values[envIndex = 0];
    }

    @Override
    public int getEnvelopeNext() {
        return ++envIndex >= values.length
                ? SetVar.END
                : values[envIndex];
    }

    @Override
    public int getEnvelopeSize() {
        return values.length;
    }

    @Override
    public boolean envelopeContains(int element) {
        return Arrays.binarySearch(values, element) >= 0;
    }

    @Override
    public boolean addToKernel(int element, ICause cause) throws ContradictionException {
        if (!kernelContains(element)) {
            contradiction(cause, "");
        }
        return false;
    }

    @Override
    public boolean removeFromEnvelope(int element, ICause cause) throws ContradictionException {
        if (envelopeContains(element)) {
            contradiction(cause, "");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        if (value.length != this.values.length) {
            contradiction(cause, "");
        }
        for (int v : value) {
            if (!kernelContains(v)) {
                contradiction(cause, "");
            }
        }
        return false;
    }

    @Override
    public int[] getValues() {
        return values;
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {
    }

    @Override
    public ISetDeltaMonitor monitorDelta(ICause propagator) {
        return ISetDeltaMonitor.Default.NONE;
    }

    @Override
    public boolean isInstantiated() {
        return true;
    }

    @Override//void (a constant receives no event)
    public void addMonitor(IVariableMonitor monitor) {
    }

    @Override//void (a constant receives no event)
    public void removeMonitor(IVariableMonitor monitor) {
    }

    @Override//void (a constant receives no event)
    public void subscribeView(IView view) {
    }

    @Override//void (a constant receives no event)
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
    }

    @Override//void (a constant receives no event)
    public void notifyViews(IEventType event, ICause cause) throws ContradictionException {
    }

    @Override//void (a constant receives no event)
    public void notifyMonitors(IEventType event) throws ContradictionException {
    }

    @Override
    public void contradiction(ICause cause, String message) throws ContradictionException {
        model.getResolver().getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.SET | Variable.CSTE;
    }

    @Override
    public SetVar duplicate() {
        return new FixedSetVarImpl(StringUtils.randomName(), this.getValues(), model);
    }

}
