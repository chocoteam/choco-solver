package solver.constraints.propagators.nary.alldifferent.proba;

import choco.kernel.common.util.procedure.IntProcedure;
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
import solver.recorders.coarse.CoarseEventRecorderWithCondition;
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
public class CondAllDiffBCProba implements IVariableMonitor<IntVar>, ICondition<CoarseEventRecorderWithCondition> {

    IEnvironment environment;

    Random rand = new Random();

    protected IUnion unionset; // the union of the domains
    protected IntVar[] vars;
    IStateInt nbNotInstVar;
    TIntObjectHashMap<IStateInt[]> oldBounds;
    double proba;

    protected final RemProc rem_proc;
    protected final TIntObjectHashMap<IDeltaMonitor> deltamon; // delta monitoring -- can be NONE
    protected final TIntIntHashMap idxVs; // index of this within the variables structure -- mutable
    protected TIntLongHashMap timestamps; // a timestamp lazy clear the event structures


    public CondAllDiffBCProba(IEnvironment environment, IntVar[] vars) {
        this.rem_proc = new RemProc(this);
        this.environment = environment;
        this.vars = vars;
        this.nbNotInstVar = environment.makeInt(vars.length);
        this.oldBounds = new TIntObjectHashMap<IStateInt[]>(vars.length);
        this.deltamon = new TIntObjectHashMap<IDeltaMonitor>(vars.length);
        this.idxVs = new TIntIntHashMap(vars.length, (float) 0.5, -2, -2);
        this.timestamps = new TIntLongHashMap(vars.length, (float) 0.5, -2, -2);
        for (int i = 0; i < vars.length; i++) {
            IntVar v = vars[i];
            v.analyseAndAdapt(EventType.REMOVE.mask); // to be sure delta is created and maintained
            int vid = v.getId();
            IStateInt[] iBounds = new IStateInt[2];
            iBounds[0] = environment.makeInt(v.getLB());
            iBounds[1] = environment.makeInt(v.getUB());
            oldBounds.put(vid, iBounds);
            deltamon.put(vid, v.getDelta().getMonitor(Cause.Null));
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
        unionset = new Union(vars, environment);
        //unionset = new BitSetUnion(vars, environment);
        assert checkUnion();
    }

    public void activate() {
        for (int i = 0; i < vars.length; i++) {
            vars[i].activate(this);
        }
        init();
    }

    private static class RemProc implements IntProcedure {
        private final CondAllDiffBCProba p;

        public RemProc(CondAllDiffBCProba p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            //System.out.println("remove one occ of value " + i);
            p.unionset.remove(i);
            assert p.checkOcc(i);
        }
    }

    @Override
    public void afterUpdate(IntVar var, EventType evt, ICause cause) {
        int vid = var.getId();
        /*System.out.printf("\n\nCND : %s (%d) on %s\n", var, vid, evt);
        for (IntVar vs : vars) {
            System.out.println(vs);
        }//*/
        ////////////////////// Sauce a Charles pour utiliser le delta domaine de var
        IDeltaMonitor dm = deltamon.get(var.getId());
        long t = timestamps.get(vid);
        if (t - AbstractSearchLoop.timeStamp != 0) {
            deltamon.get(vid).clear();
            timestamps.adjustValue(vid, AbstractSearchLoop.timeStamp - t);
        }
        dm.freeze();
        /////////////////////////////// Mise a jour des donnees dans le cas de l'instanciation
        int m = unionset.getSize();
        int n = nbNotInstVar.get();
        if (EventType.isInstantiate(evt.mask)) {
            /////////////////////// Memoire des bornes de la valeur qui vient d'etre instanciee, du nombre de variables pas encore instanciees
            int v = unionset.getLastInstValuePos();
            int al = unionset.getLastLowValuePos();
            int be = unionset.getLastUppValuePos();
            ///////////////// calcul de la proba avec donnees avant mise a jour
            proba = ProbaFunctions.probaAfterOther(m, n);
            //proba = ProbaFunctions.probaAfterInst(m, n, v, al, be);
            unionset.instantiatedValue(var.getValue(), oldBounds.get(vid)[0].get(), oldBounds.get(vid)[1].get()); // todo : not compatible with assert on union
            nbNotInstVar.add(-1);
            oldBounds.get(vid)[0].set(var.getLB());
            oldBounds.get(vid)[1].set(var.getUB());
        }
        else {
            ///////////////// calcul de la proba avec donnees avant mise a jour
            proba = ProbaFunctions.probaAfterOther(m, n);
        }
        ////////////////////// Mise a jour des retrait de valeur dans l'union
        try {
            dm.forEach(rem_proc, EventType.REMOVE);
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
        //////////////////////////////////  liberation du delta
        dm.unfreeze();
        assert checkUnion();
    }

    /*@Override
    public void afterUpdate(IntVar var, EventType evt, ICause cause) {
        try {
            int vid = var.getId();
            System.out.printf("\n\nCND : %s (%d) on %s\n", var, vid, evt);
            for (IntVar vs : vars) {
                System.out.println(vs);
            }
            ////////////////////// Sauce a Charles pour utiliser le delta domaine de var
            IDeltaMonitor dm = deltamon.get(var.getId());
            //int vid = var.getId();
            long t = timestamps.get(vid);
            if (t - AbstractSearchLoop.timeStamp != 0) {
                deltamon.get(vid).clear();
                timestamps.adjustValue(vid, AbstractSearchLoop.timeStamp - t);
            }
            dm.freeze();
            System.out.printf("%s\n", dm.toString());
            //////////////////////////////////
            int m = unionset.getSize();
            int n = nbNotInstVar.get();
            if (EventType.isInstantiate(evt.mask)) { // la proba sur l'instantiation
                nbNotInstVar.add(-1);
                /*dm.forEach(minMax_proc.init(), EventType.REMOVE);
                int low = minMax_proc.getMin();
                int upp = minMax_proc.getMax();
                //int low = oldBounds.get(vid)[0].get();
                //int upp = oldBounds.get(vid)[1].get();
                //unionset.instantiatedValue(var.getValue(), low, upp);
                //int v = unionset.getLastInstValuePos();
                //int al = unionset.getLastLowValuePos();
                //int be = unionset.getLastUppValuePos();
                dm.forEach(rem_proc, EventType.REMOVE);
                this.proba = probaAfterInst(m, n, v, al, be);  //je calcule la proba avant de prendre en compte les changements courrant
                this.proba = probaAfterOther(m, n);
            } else {   // la proba sur un autre type d'evenement
                System.out.println("traite un retrait");
                dm.forEach(rem_proc, EventType.REMOVE);
                this.proba = probaAfterOther(m, n);
            }
            //oldBounds.get(vid)[0].set(var.getLB());
            //oldBounds.get(vid)[1].set(var.getUB());
            ////////////////////////// liberation de l'iterateur sur le delta
            dm.unfreeze();
            assert proba >= 0.0 && proba <= 1.0 : proba;
            if (proba != 0.0f)
                System.out.printf("%.3f, %d, %d\n", proba, m, n);
            assert checkUnion();
        } catch (ContradictionException e) {
            throw new SolverException("CondAllDiffBCProba#update encounters an exception");
        }
    } */

    @Override
    public boolean validateScheduling(CoarseEventRecorderWithCondition recorder, Propagator propagator, EventType event) {
        double cut = rand.nextDouble();
        return (1 - proba > cut); //*/
        //return true;
    }

    @Override
    public ICondition next() {
        return null;
    }

    @Override
    public void linkRecorder(CoarseEventRecorderWithCondition recorder) {
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

    /**
     * test for unionset => has to be executed in the search loop at the beginning of downBranch method
     *
     * @return true if unionset is ok
     */
    public boolean checkUnion() {
        //System.out.print("\nUnion from sctrach: ");
        Set<Integer> allVals = computeUnion();
        if (allVals.size() != unionset.getSize()) {
            System.out.println("size problem " + unionset.getSize() + ", " + allVals.size());
            System.out.println(unionset + " -VS- " + allVals);
            return false;
        }
        for (Integer value : allVals) {
            //System.out.println("check value " + value + ", <" + unionset.getOccOf(value) + ">");
            if (!checkOcc(value)) {
                return false;
            }
        }
        //System.out.println();
        return true;
    }

    public boolean checkOcc(int value) {
        int occ = 0;
        for (IntVar v : vars) {
            if (v.contains(value)) {
                //System.out.println(v + ": y");
                occ++;
            } else {
                //System.out.println(v + ": n");
            }
        }
        //System.out.print("[" + value + "," + occ + "];");
        if (occ != unionset.getOccOf(value)) {
            System.out.println("value " + value + ": " + occ + " VS " + unionset.getOccOf(value));
            for (IntVar v : vars) {
                System.out.println(v);
            } //*/
            System.out.println("-------------");
            return false;
        } else {
            //System.out.println("-------------");
            return true;
        }
    }

    private Set<Integer> computeUnion() {
        //System.out.println("*********** calcul manuel de l'union");
        Set<Integer> vals = new HashSet<Integer>();
        for (IntVar var : vars) {
            //if (!var.instantiated()) {
                int ub = var.getUB();
                for (int i = var.getLB(); i <= ub; i = var.nextValue(i)) {
                    vals.add(i);
                }
            //}
        }
        //System.out.println(vals);
        //System.out.println("calcul manuel de l'union ***********");
        return vals;
    } //*/
}
