package org.chocosolver.solver.variables.impl;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.explanations.VariableState;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.delta.ISetDeltaMonitor;
import org.chocosolver.solver.variables.delta.NoDelta;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.tools.StringUtils;

import java.util.Arrays;

/**
 * @author jimmy
 */
public class FixedSetVarImpl extends AbstractVariable implements SetVar {

    private final int[] values;
    private int kerIndex;
    private int envIndex;

    public FixedSetVarImpl(String name, TIntSet values, Solver solver) {
        super(name, solver);
        this.values = values.toArray();
        Arrays.sort(this.values);
    }

    public FixedSetVarImpl(String name, int[] values, Solver solver) {
        // Remove duplicates
        this(name, new TIntHashSet(values), solver);
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
            contradiction(cause, SetEventType.ADD_TO_KER, "");
        }
        return false;
    }

    @Override
    public boolean removeFromEnvelope(int element, ICause cause) throws ContradictionException {
        if (envelopeContains(element)) {
            contradiction(cause, SetEventType.REMOVE_FROM_ENVELOPE, "");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        if (value.length != this.values.length) {
            contradiction(cause, null, "");
        }
        for (int v : value) {
            if (!kernelContains(v)) {
                contradiction(cause, null, "");
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

    @Override
    public void explain(VariableState what, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override//void (a constant receives no event)
    public void recordMask(int mask) {
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
    public void contradiction(ICause cause, IEventType event, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.SET | Variable.CSTE;
    }

    @Override
    public SetVar duplicate() {
        return new FixedSetVarImpl(StringUtils.randomName(), this.getValues(), this.getSolver());
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            FixedSetVarImpl clone = new FixedSetVarImpl(this.name, this.values, solver);
            identitymap.put(this, clone);
        }
    }
}
