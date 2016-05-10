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
package org.chocosolver.solver.constraints.reification;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSetUtils;
import org.chocosolver.util.ESat;

import java.util.*;

/**
 * A propagator for constructive disjunction.
 * The propagator forces reifying variables, in sequence, and maintains the domain union of each variable modified by all
 * reified constraint.
 * This propagator declares an internal propagation engine which run propagation triggered by forcing a reifying variable.
 * <p>
 * <p>
 * There can be only one instance of this propagator in a {@link Model} to avoid unexpected side-effects, this is maintained
 * by the factory
 * <p>
 * <p>
 * Project: choco.
 * <p>
 *
 * todo: deal with variable addition / constraint addition in internalEngine
 * todo: store for each boolvar the disjunctions it belongs (may be helpful to react on fine event and/or reaching fix point smartly
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 25/01/2016.
 */
public class PropConDis extends Propagator<IntVar> {

    /**
     * List of known boolvars
     */
    private TIntHashSet declared;

    /**
     * List of disjunctions to deal with
     * todo: link boolvar to index in this
     */
    private List<BoolVar[]> disjunctions;
    /**
     * An internal propagation engine to try each boolean variables
     */
    private IPropagationEngine internalEngine;
    /**
     * Original propagation engine, set temporary off while this builds deductions
     */
    private IPropagationEngine masterEngine;
    /**
     * Set to <tt>true</tt> to indicate that this builds deductions
     */
    private boolean isworking;
    /**
     * Set to <tt>false</tt> before the first propagation of this, <tt>true</tt> then.
     */
    private boolean firstAwake;
    /**
     * All integer variables (INT and BOOL) extract from this model.
     */
    private IntVar[] allvars;
    /**
     * Store cardinality of variables before a try
     */
    private int[] cardinalities;
    /**
     * Store the union of domain of modified variables
     */
    private TIntObjectHashMap<IntIterableRangeSet> domains;
    /**
     * Cardinality of domains (external to limit GC)
     */
    private BitSet toUnion;
    /**
     * To get boolean variables to set to false
     */
    private BitSet toZero;

    /**
     * Store new added variables when {@link #initialized} is <i>false</i>
     */
    private ArrayList<IntVar> add_var;

    /**
     * Indicates if this is initialized or not
     */
    private boolean initialized = false;

    /**
     * A propagator to deal with constructive disjunction
     * @param model a model
     */
    public PropConDis(Model model) {
        super(new IntVar[]{model.ONE()}, PropagatorPriority.VERY_SLOW, false);// adds model.ONE to fit to the super constructor
        this.vars = new IntVar[0];    // erase model.ONE from the variable scope
        domains = new TIntObjectHashMap<>();
        toUnion = new BitSet();
        toZero = new BitSet();
        declared = new TIntHashSet();
        disjunctions = new ArrayList<>();
        add_var = new ArrayList<>(16);
    }

    /**
     * Add a new disjunctions to the list of disjunctions
     *
     * @param cstrs constraints in disjunction
     */
    public void addDisjunction(Constraint... cstrs) {
        BoolVar[] bvars = new BoolVar[cstrs.length];
        for(int i  = 0; i < cstrs.length; i++){
            Arrays.stream(cstrs[i].getPropagators()).forEach(
                    p -> Arrays.stream(p.getVars()).forEach(v -> addOneVariable((IntVar) v)));
            bvars[i] = cstrs[i].reify();
            addOneVariable(bvars[i]);
        }
        disjunctions.add(bvars);
    }

    private void addOneVariable(final IntVar var) {
        if (!declared.contains(var.getId())) {
            declared.add(var.getId());
            if (initialized) {
                addVariable(var);
            } else {
                add_var.add(var);
            }
        }
    }

    /**
     * Initializes this propagator
     */
    public void initialize(){
        if (!initialized) {
            if (add_var.size() > 0) {
                addVariable(add_var.toArray(new IntVar[add_var.size()]));
            }
            add_var.clear();
            this.initialized = true;
        }
    }


    @Override
    public boolean isActive() {
        return !isworking && super.isActive();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        assert initialized:"PropConDis is not initialized";
        try {
            if (!isworking) {
                isworking = true;
                if (!firstAwake && !firstPropagation()) {
                    fails();
                }
                boolean change = true;
                while (change) {
                    change = false;
                    // a fix point needs to be reached
                    // indeed, some deductions from disjunction j may impact disjunction i, i < j.
                    for (int i = 0; i < allvars.length; i++) {
                        cardinalities[i] = allvars[i].getDomainSize();
                        if (domains.get(i) != null) {
                            domains.get(i).clear();
                        }
                    }
                    for (int i = 0; i < disjunctions.size(); i++) {
                        BoolVar[] boolVars = disjunctions.get(i);
                        // change the propagation engine
                        model.getSolver().set(internalEngine);
                        toZero.clear();
                        toUnion.clear();
                        for (int b = 0; b < boolVars.length; b++) {
                            if (!boolVars[b].isInstantiated()) {
                                forceReification(boolVars[b], b);
                                // if no variable is modified, or intersection of modified variable is empty
                                if (toUnion.cardinality() == 0) {
                                    break;
                                }
                            } else if (boolVars[b].isInstantiatedTo(1)) {
                                if (b > 0) { // swap it with the first element, for next iterations
                                    BoolVar tmp = boolVars[b];
                                    boolVars[b] = boolVars[0];
                                    boolVars[0] = tmp;
                                }
                                break;
                            }
                        }
                        // restore the propagation before applying the deductions
                        model.getSolver().set(masterEngine);
                        change |= applyDeductions(boolVars);
                    }
                }
            } // else: nothing to do, to avoid undesirable side effects or too-depth propagation
        } finally {
            isworking = false;
        }
    }

    /**
     * Based on deductions made before, filters domain of variables.
     *
     * @param disjunction list of boolvars in disjunction
     * @throws ContradictionException domain wiper out
     */
    private boolean applyDeductions(BoolVar[] disjunction) throws ContradictionException {
        boolean change = false;
        // push domains
        for (int p = toUnion.nextSetBit(0); p >= 0; p = toUnion.nextSetBit(p + 1)) {
            change |= allvars[p].removeAllValuesBut(domains.get(p), this);
        }
        int one = 0, zero = 0, last = -1;
        for (int b = 0; b < disjunction.length; b++) {
            if (disjunction[b].isInstantiated()) {
                if (disjunction[b].getValue() == 0) {
                    zero++;
                } else {
                    one++;
                }
            } else if (toZero.get(b)) {
                change |= disjunction[b].setToFalse(this);
                zero++;
            } else {
                last = b;
            }
        }
        // then force boolean variables, if needed
        if (one == 0 && zero >= disjunction.length - 1) {
            if (last > -1) {
                change |= disjunction[last].instantiateTo(1, this);
                // failure is expected if disjunction[last] was not free
            } else {
                this.fails(); // not very helpful for explanations ...
            }
        }// if at least one boolean is set to true
        if (one > 0) {
            //todo: deal with entailed disjunctions
        }
        return change;
    }

    /**
     * Force boolean variable <i>b</i> to <<tt>true</tt> and collect modified domains.
     *
     * @param bvar    boolean variable to force
     * @param idxDisj index of bvar in the current disjunction
     */
    private void forceReification(BoolVar bvar, int idxDisj) {
        // make a backup world
        model.getEnvironment().worldPush();
//        System.out.printf("%sTry %s for %s\n", pad("", model.getEnvironment().getWorldIndex(), "."), vars[b].getName(), this);
        try {
            bvar.instantiateTo(1, this);
            internalEngine.propagate();
            // find modified variables and copy their domain
            readDomains();
        } catch (ContradictionException cex) {
            // if failure occurs, then we consider all domains as empty
            // and union is maintained as is
            internalEngine.flush();
            toZero.set(idxDisj);
        }
        // restore backup world
        model.getEnvironment().worldPop();
    }

    /**
     * Set up data structures
     *
     * @return <tt>true</tt> if the initial propagation is ok, <tt>false</tt> otherwise
     */
    private boolean firstPropagation() {
        firstAwake = true;
        // initialize this
        allvars = new IntVar[model.getNbVars()];
        int k = 0;
        for (int i = 0; i < model.getNbVars(); i++) {
            if (((model.getVar(i).getTypeAndKind() & Variable.KIND) == Variable.INT
                    || (model.getVar(i).getTypeAndKind() & Variable.KIND) == Variable.BOOL)
                    && (model.getVar(i).getTypeAndKind() & Variable.TYPE) == Variable.VAR) {
                allvars[k++] = (IntVar) model.getVar(i);
            }
        }
        allvars = Arrays.copyOf(allvars, k);
        cardinalities = new int[k];
        // get a copy of the current propagation engine
        masterEngine = model.getSolver().getEngine();
        // create internalEngine
        internalEngine = new SevenQueuesPropagatorEngine(model);
        internalEngine.initialize();

        model.getSolver().set(internalEngine);
        boolean ok = true;
        try {
            internalEngine.propagate();
        } catch (ContradictionException c) {
            internalEngine.flush();
            ok = false;
        }
        model.getSolver().set(masterEngine);
        return ok;
    }


    /**
     * Find modified domains and compute unions from one propagation to the other.
     */
    private void readDomains() {
        if (toUnion.cardinality() == 0) {
            for (int i = 0; i < allvars.length; i++) {
                if (cardinalities[i] > allvars[i].getDomainSize()) {
                    IntIterableRangeSet rs = domains.get(i);
                    if (rs == null) {
                        rs = new IntIterableRangeSet();
                        domains.put(i, rs);
                    }
                    IntIterableSetUtils.copyIn(allvars[i], rs);
                    toUnion.set(i);
                }
            }
        } else { // only iterate over previously modified variables
            for (int p = toUnion.nextSetBit(0); p >= 0; p = toUnion.nextSetBit(p + 1)) {
                // check if domain has changed
                if (cardinalities[p] > allvars[p].getDomainSize()) {
                    IntIterableSetUtils.union(domains.get(p), allvars[p]);
                    if (domains.get(p).size() == cardinalities[p]) {
                        toUnion.clear(p);
                    }
                } else {
                    toUnion.clear(p);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < disjunctions.size(); i++) {
            BoolVar[] boolVars = disjunctions.get(i);
            int zero = 0, one = 0;
            for (int b = 0; b < boolVars.length; b++) {
                if (boolVars[b].isInstantiated()) {
                    if (vars[b].isInstantiatedTo(0)) {
                        zero++;
                    } else {
                        one++;
                    }
                }
            }
            if (zero == boolVars.length) {
                return ESat.FALSE;
            } else if (one == 0) {
                return ESat.UNDEFINED;
            }
        }
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        return "ConstructiveDisjunction";
    }
}
