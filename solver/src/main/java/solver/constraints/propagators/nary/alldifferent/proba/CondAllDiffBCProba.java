package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.procedure.UnaryIntProcedure;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import solver.Cause;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.recorders.IEventRecorder;
import solver.recorders.conditions.ICondition;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.IVariableMonitor;
import solver.variables.IntVar;
import solver.variables.delta.IDeltaMonitor;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 22/11/11
 */
public class CondAllDiffBCProba implements IVariableMonitor<IntVar>, ICondition<IEventRecorder> {

    IEnvironment environment;

    Random rand;

    protected IntVar[] vars;
    protected BitSetUnion unionset; // the union of the domains
    IStateInt nbNotInstVar;
    double proba;
    int seed;

    protected final RemProc rem_proc;
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntIntHashMap idxVs; // index of this within the variables structure -- mutable
    protected TIntLongHashMap timestamps; // a timestamp lazy clear the event structures
	protected final AbstractSearchLoop loop;


    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars, int seed, AbstractSearchLoop loop) {
        this.seed = seed;
		this.loop = loop;
        this.rand = new Random();
        this.rem_proc = new RemProc(this);
        this.environment = environment;
        this.vars = vars;
        this.nbNotInstVar = environment.makeInt(vars.length);
        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(vars.length);
        this.idxVs = new TIntIntHashMap(vars.length, (float) 0.5, -2, -2);
        this.timestamps = new TIntLongHashMap(vars.length, (float) 0.5, -2, -2);
        for (IntVar v : vars) {
            v.analyseAndAdapt(EventType.REMOVE.mask); // to be sure delta is created and maintained
            int vid = v.getId();
            deltamon.put(vid, v.getDelta().createDeltaMonitor(Cause.Null));
            v.addMonitor(this); // attach this as a variable monitor
            timestamps.put(vid, -1);

        }
    }

    private void init() {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].instantiated()) {
                nbNotInstVar.add(-1);
            }
        }
        unionset = new BitSetUnion(vars, environment);
    }

    public void activate() {
        for (int i = 0; i < vars.length; i++) {
            vars[i].activate(this);
        }
        init();
    }

    private static class RemProc implements UnaryIntProcedure<IntVar> {
        private final CondAllDiffBCProba p;
        private IntVar var;

        public RemProc(CondAllDiffBCProba p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            p.unionset.remove(i, var);
        }

        @Override
        public UnaryIntProcedure set(IntVar var) {
            this.var = var;
            return this;
        }
    }

    private static class ShowDelta implements IntProcedure {

        String s = "";

        @Override
        public void execute(int i) throws ContradictionException {
            s += i + ",";
        }

        public String toString() {
            return s;
        }
    }// */

    @Override
    public final void afterUpdate(IntVar var, EventType evt, ICause cause) {
        int vid = var.getId();
//        ShowDelta delta = new ShowDelta();
//        System.out.printf("\n\nCND : %s (%d) on %s cause: %s\n", var, vid, evt, cause);
//        for (IntVar vs : vars) {
//            System.out.println(vs);
//        }
        ////////////////////// Sauce a Charles pour utiliser le delta domaine de var
        IDeltaMonitor dm = deltamon.get(var.getId());
        long t = timestamps.get(vid);
        if (t - loop.timeStamp != 0) {
            deltamon.get(vid).clear();
            timestamps.adjustValue(vid, loop.timeStamp - t);
        }
        dm.freeze();
        /////////////////////// afficher le delta
//        dm.forEach(delta, EventType.REMOVE);
//        String d = delta.toString();
//        assert !d.isEmpty() : "delta {" + d + "} is empty while an " + evt + " has been detected on variable " + var + " by "+ cause;
        /////////////////////////////// Mise a jour des donnees dans le cas de l'instanciation
        int m = unionset.getSize(); // taille de l'union avant evenement courrant
        int n = nbNotInstVar.get(); // nombre de vars non instanciees avant evenement courrant
        if (EventType.isInstantiate(evt.mask)) {
            //////////// mise a jour de l'union avec l'info d'instanciation
            int[] positions = unionset.instantiatedValue(var.getValue(), var);
            ///////////////// calcul de la proba avec donnees
            proba = ProbaFunctions.probaAfterInst(m, n, positions[0], positions[1], positions[2]);
        } else {
            ///////////////// calcul de la proba avec donnees avant mise a jour
            proba = ProbaFunctions.probaAfterOther(m, n);
        }
        ////////////////////// Mise a jour des retraits de valeurs dans l'union et des positions eventuelles
        try {
            dm.forEach(rem_proc.set(var), EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
        if (EventType.isInstantiate(evt.mask)) {
            nbNotInstVar.add(-1);
        }
        //////////////////////////////////  liberation du delta
        dm.unfreeze();
        assert test();
    } //*/

    @Override
    public boolean validateScheduling(IEventRecorder recorder, Propagator propagator, EventType event) {
        double cut = rand.nextDouble();
        return (1 - proba > cut); //*/
        //return true;
    }

    @Override
    public ICondition next() {
        return null;
    }

    @Override
    public void linkRecorder(IEventRecorder recorder) {
    }

    @Override
    public int getIdxInV(IntVar variable) {
        return idxVs.get(variable.getId());
    }

    @Override
    public void setIdxInV(IntVar variable, int idx) {
        idxVs.put(variable.getId(), idx);
    }

    @Override
    public void beforeUpdate(IntVar var, EventType evt, ICause cause) {
    }

    @Override
    public void contradict(IntVar var, EventType evt, ICause cause) {
    }

    public boolean test() {
        Set<Integer> union = computeUnion();
        //System.out.println("union fs: " + union);
        for (int i : union) {
            if (!checkOcc(i)) {
                System.out.println("union fs: " + union);
                return false;
            }
        }
        return true;
    }

    private boolean checkOcc(int value) {
        int occ = 0;
        for (IntVar v : vars) {
            if (v.contains(value)) occ++;
        }
        for (IntVar v : vars) {
            if (v.instantiatedTo(value)) occ--;
        }
        if (occ != unionset.getOccOf(value)) {
            System.out.println("value " + value + ": " + occ + " VS " + unionset.getOccOf(value));
            System.out.println(unionset);
            for (IntVar v : vars) System.out.println(v);
            System.out.println("-------------");
            checkOcc(value);
            return false;
        } else return true;
    }

    private Set<Integer> computeUnion() {
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            int ub = var.getUB();
            for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                vals.add(i);
            }
        }
        return vals;
    }
}
