package solver.variables.impl;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.ISetDeltaMonitor;
import solver.variables.delta.NoDelta;
import solver.variables.view.IView;

import java.util.Arrays;

/**
 *
 * @author jimmy
 */
public class FixedSetVarImpl extends AbstractVariable implements SetVar {

    private final int[] value;
    private int kerIndex;
    private int envIndex;

    public FixedSetVarImpl(String name, TIntSet value, Solver solver) {
		super(name,solver);
        this.value = value.toArray();
        Arrays.sort(this.value);
    }

    public FixedSetVarImpl(String name, int[] value, Solver solver) {
        // Remove duplicates
        this(name, new TIntHashSet(value), solver);
    }

    @Override
    public int getKernelFirst() {
        return value.length == 0
                ? SetVar.END
                : value[kerIndex = 0];
    }

    @Override
    public int getKernelNext() {
        return ++kerIndex >= value.length
                ? SetVar.END
                : value[kerIndex];
    }

    @Override
    public int getKernelSize() {
        return value.length;
    }

    @Override
    public boolean kernelContains(int element) {
        return Arrays.binarySearch(value, element) >= 0;
    }

    @Override
    public int getEnvelopeFirst() {
        return value.length == 0
                ? SetVar.END
                : value[envIndex = 0];
    }

    @Override
    public int getEnvelopeNext() {
        return ++envIndex >= value.length
                ? SetVar.END
                : value[envIndex];
    }

    @Override
    public int getEnvelopeSize() {
        return value.length;
    }

    @Override
    public boolean envelopeContains(int element) {
        return Arrays.binarySearch(value, element) >= 0;
    }

    @Override
    public boolean addToKernel(int element, ICause cause) throws ContradictionException {
        if (!kernelContains(element)) {
            contradiction(cause, EventType.ADD_TO_KER, "");
        }
        return false;
    }

    @Override
    public boolean removeFromEnvelope(int element, ICause cause) throws ContradictionException {
        if (envelopeContains(element)) {
            contradiction(cause, EventType.REMOVE_FROM_ENVELOPE, "");
        }
        return false;
    }

    @Override
    public boolean instantiateTo(int[] value, ICause cause) throws ContradictionException {
        if (value.length != this.value.length) {
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
    public int[] getValue() {
        return value;
    }

    @Override
    public NoDelta getDelta() {
        return NoDelta.singleton;
    }

    @Override
    public void createDelta() {}

    @Override
    public ISetDeltaMonitor monitorDelta(ICause propagator) {
		return ISetDeltaMonitor.Default.NONE;
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override//void (a constant receives no event)
    public void addMonitor(IVariableMonitor monitor) {}

    @Override//void (a constant receives no event)
    public void removeMonitor(IVariableMonitor monitor) {}

    @Override//void (a constant receives no event)
    public void subscribeView(IView view) {}

    @Override
    public void explain(VariableState what, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override//void (a constant receives no event)
    public void recordMask(int mask) {}

    @Override//void (a constant receives no event)
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {}

    @Override//void (a constant receives no event)
    public void notifyViews(EventType event, ICause cause) throws ContradictionException {}

    @Override//void (a constant receives no event)
    public void notifyMonitors(EventType event) throws ContradictionException {}

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public int getTypeAndKind() {
        return Variable.SET | Variable.CSTE;
    }

    @Override
    public <V extends Variable> V duplicate() {
        throw new UnsupportedOperationException("Cannot duplicate a constant view");
    }
}
