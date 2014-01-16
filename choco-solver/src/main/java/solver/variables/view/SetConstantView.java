package solver.variables.view;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import solver.ICause;
import solver.Solver;
import solver.constraints.Propagator;
import solver.exception.ContradictionException;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.SetVar;
import solver.variables.Variable;
import solver.variables.delta.SetDelta;
import solver.variables.delta.monitor.SetDeltaMonitor;

import java.util.Arrays;

/**
 *
 * @author jimmy
 */
public class SetConstantView implements SetVar {

    private final Solver solver;
    private final int ID;
    protected final String name;
    private final int[] value;
    private int kerIndex;
    private int envIndex;

    public SetConstantView(String name, TIntSet value, Solver solver) {
        this.name = name;
        this.value = value.toArray();
        Arrays.sort(this.value);
        this.solver = solver;
        this.ID = solver.nextId();
    }

    public SetConstantView(String name, int[] value, Solver solver) {
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
    /*
     * Unforunately, unlike IIntDeltaMonitor and IntDelta, there isn't a "None"
     * delta for sets.
     */
    private SetDelta delta;
    private SetDeltaMonitor deltaMonitor;
    private boolean reactOnModification;

    @Override
    public SetDelta getDelta() {
        return delta;
    }

    @Override
    public void createDelta() {
        if (!reactOnModification) {
            reactOnModification = true;
            delta = new SetDelta(solver.getSearchLoop());
        }
    }

    @Override
    public SetDeltaMonitor monitorDelta(ICause propagator) {
        createDelta();
        if (deltaMonitor == null) {
            deltaMonitor = new SetDeltaMonitor(delta, propagator);;
        }
        return deltaMonitor;
    }

    @Override
    public boolean instantiated() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Propagator[] getPropagators() {
        return new Propagator[0];
    }

    @Override
    public Propagator getPropagator(int idx) {
        return null;
    }

    @Override
    public int getNbProps() {
        return 0;
    }

    @Override
    public int[] getPIndices() {
        return new int[0];
    }

    @Override
    public int getIndiceInPropagator(int pidx) {
        return 0;
    }

    @Override
    public void addMonitor(IVariableMonitor monitor) {
        //void
    }

    @Override
    public void removeMonitor(IVariableMonitor monitor) {
        //void
    }

    @Override
    public void subscribeView(IView view) {
        //void
    }

    @Override
    public void explain(VariableState what, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override
    public void explain(VariableState what, int val, Explanation to) {
        throw new UnsupportedOperationException("SetConstantView does not (yet) implement method explain(...)");
    }

    @Override
    public int link(Propagator propagator, int idxInProp) {
        return -1;
    }

    @Override
    public void recordMask(int mask) {
        //void
    }

    @Override
    public void unlink(Propagator propagator, int idxInProp) {
        //void
    }

    @Override
    public void notifyPropagators(EventType event, ICause cause) throws ContradictionException {
        //void
    }

    @Override
    public void notifyViews(EventType event, ICause cause) throws ContradictionException {
        //void
    }

    @Override
    public void notifyMonitors(EventType event) throws ContradictionException {
        //void
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
        solver.getEngine().fails(cause, this, message);
    }

    @Override
    public Solver getSolver() {
        return solver;
    }

    @Override
    public int getTypeAndKind() {
        return Variable.SET | Variable.CSTE;
    }

    @Override
    public <V extends Variable> V duplicate() {
        throw new UnsupportedOperationException("Cannot duplicate a constant view");
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public String toString() {
        return getName();
    }
}
