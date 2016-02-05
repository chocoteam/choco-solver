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
package org.chocosolver.solver;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.Environments;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.nary.cnf.PropFalse;
import org.chocosolver.solver.constraints.nary.cnf.PropTrue;
import org.chocosolver.solver.constraints.nary.cnf.SatConstraint;
import org.chocosolver.solver.constraints.nary.nogood.NogoodConstraint;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.reification.CondisConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.search.loop.SLF;
import org.chocosolver.solver.search.loop.Resolver;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;
import org.chocosolver.solver.search.solution.*;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.observers.FilteringMonitorList;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;

import java.io.*;
import java.util.*;

/**
 * The <code>Model</code> is the header component of Constraint Programming.
 * It embeds the list of <code>Variable</code> (and their <code>Domain</code>), the <code>Constraint</code>'s network,
 * and a <code>IPropagationEngine</code> to pilot the propagation.<br/>
 * <code>Model</code> includes a <code>AbstractSearchLoop</code> to guide the search loop: applying decisions and propagating,
 * running backups and rollbacks and storing solutions.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @version 0.01, june 2010
 * @see org.chocosolver.solver.variables.Variable
 * @see org.chocosolver.solver.constraints.Constraint
 * @since 0.01
 */
public class Model implements Serializable, IModeler{

    /** For serialization purpose */
    private static final long serialVersionUID = 1L;

    /** Settings to use with this solver */
    private Settings settings = new Settings() {};

    /** A map to cache constants (considered as fixed variables) */
    public TIntObjectHashMap<IntVar> cachedConstants;

    /** Variables of the model */
    Variable[] vars;

    /** Index of the last added variable */
    int vIdx;

    /** Constraints of the model */
    Constraint[] cstrs;

    /** Index of the last added constraint */
    int cIdx;

    /** Environment, based of the search tree (trailing or copying) */
    final IEnvironment environment;

    /** Resolver of the model */
    protected Resolver search;

    /** Array of variable to optimize. */
    protected Variable[] objectives;

    /** Precision to consider when optimizing a RealVariable */
    protected double precision = 0.00D;

    /** Model name */
    protected String name;

    /** Stores this model's creation time */
    protected long creationTime;

    /** Counter used to set ids to variables and propagators */
    protected int id = 1;

    /** Basic TRUE constraint, cached to avoid multiple useless occurrences */
    private Constraint TRUE;

    /** Basic FALSE constraint, cached to avoid multiple useless occurrences */
    private Constraint FALSE;

    /** Basic ZERO constant, cached to avoid multiple useless occurrences. */
    private BoolVar ZERO;

    /** Basic ONE constant, cached to avoid multiple useless occurrences. */
    private BoolVar ONE;

    /** A MiniSat instance, useful to deal with clauses*/
    protected SatConstraint minisat;

    /** A MiniSat instance adapted to nogood management */
    protected NogoodConstraint nogoods;

    /** A CondisConstraint instance adapted to constructive disjunction management */
    protected CondisConstraint condis;

    /** An Ibex (continuous constraint model) instance */
    private Ibex ibex;

    /** Enable attaching hooks to a model. */
    private Map<String,Object> hooks;

    /** The propagation engine to use */
    protected IPropagationEngine engine;

    protected ResolutionPolicy policy = ResolutionPolicy.SATISFACTION;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// CONSTRUCTORS ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a model object embedding a <code>environment</code>,  named <code>name</code> and with the specific set of
     * properties <code>solverProperties</code>.
     *
     * @param environment a backtracking environment
     * @param name        a name
     */
    public Model(IEnvironment environment, String name) {
        this.name = name;
        this.vars = new Variable[32];
        this.vIdx = 0;
        this.cstrs = new Constraint[32];
        this.cIdx = 0;
        this.environment = environment;
        this.creationTime -= System.nanoTime();
        this.cachedConstants = new TIntObjectHashMap<>(16, 1.5f, Integer.MAX_VALUE);
        this.engine = NoPropagationEngine.SINGLETON;
        this.getResolver().setObjectiveManager(ObjectiveManager.SAT());
        this.objectives = new Variable[0];
        this.hooks = new HashMap<>();
    }

    /**
     * Create a model object with default parameters, named <code>name</code>.
     *
     * @param name name attributed to this model
     * @see Model#Model(org.chocosolver.memory.IEnvironment, String)
     */
    public Model(String name) {
        this(Environments.DEFAULT.make(), name);
    }

    /**
     * Create a model object with default parameters.
     *
     * @see Model#Model(org.chocosolver.memory.IEnvironment, String)
     */
    public Model() {
        this("Model-" + nextModelNum());
    }

    /** For autonumbering anonymous models. */
    private static int modelInitNumber;

    /** @return next model's number, for anonymous models. */
    private static synchronized int nextModelNum() {
        return modelInitNumber++;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The basic constant "0"
     *
     * @return a boolean variable set to 0
     */
    public BoolVar ZERO() {
        if (ZERO == null) {
            _zeroOne();
        }
        return ZERO;
    }

    /**
     * The basic constant "1"
     *
     * @return a boolean variable set to 1
     */
    public BoolVar ONE() {
        if (ONE == null) {
            _zeroOne();
        }
        return ONE;
    }

    private void _zeroOne(){
        ZERO = (BoolVar) this.intVar(0);
        ONE = (BoolVar) this.intVar(1);
        ZERO._setNot(ONE);
        ONE._setNot(ZERO);
    }

    /**
     * The basic "true" constraint
     *
     * @return a "true" constraint
     */
    public Constraint TRUE() {
        if (TRUE == null) {
            TRUE = new Constraint("TRUE cstr", new PropTrue(ONE()));
        }
        return TRUE;
    }

    /**
     * The basic "false" constraint
     *
     * @return a "false" constraint
     */
    public Constraint FALSE() {
        if (FALSE == null) {
            FALSE = new Constraint("FALSE cstr", new PropFalse(ZERO()));
        }
        return FALSE;
    }


    /**
     * Returns the unique and internal search loop.
     * Set to null when this model is created,
     * it is lazily created (if needed) when a resolution is asked.
     *
     * @return the unique and internal <code>SearchLoop</code> object.
     */
    public Resolver getResolver() {
        if(search == null){
            SLF.dfs(this, null);
            search.set(new LastSolutionRecorder(new Solution(), this));
        }
        return search;
    }

    /**
     * @deprecated use {@link #getResolver()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public Resolver getSearchLoop() {
        return getResolver();
    }

    /**
     * Returns the array of declared <code>Variable</code> objects defined in this <code>Model</code>.
     *
     * @return array of variables
     */
    public Variable[] getVars() {
        return Arrays.copyOf(vars, vIdx);
    }


    /**
     * Returns the number of variables involved in <code>this</code>.
     *
     * @return number of variables
     */
    public int getNbVars() {
        return vIdx;
    }

    /**
     * Returns the i<sup>th</sup> variables within the array of variables defined in <code>this</code>.
     *
     * @param i index of the variables to return.
     * @return a variable
     */
    public Variable getVar(int i) {
        return vars[i];
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains all the IntVar of the model.
     * <b>excludes</b> BoolVar if includeBoolVar=false.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @param includeBoolVar indicates whether or not to include BoolVar
     * @return array of IntVars of <code>this</code>
     */
    public IntVar[] retrieveIntVars(boolean includeBoolVar) {
        IntVar[] ivars = new IntVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            int kind = (vars[i].getTypeAndKind() & Variable.KIND);
            if (kind == Variable.INT || (includeBoolVar && kind == Variable.BOOL)) {
                ivars[k++] = (IntVar) vars[i];
            }
        }
        return Arrays.copyOf(ivars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the BoolVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of BoolVars of <code>this</code>
     */
    public BoolVar[] retrieveBoolVars() {
        BoolVar[] bvars = new BoolVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.BOOL) {
                bvars[k++] = (BoolVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the SetVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of SetVars of <code>this</code>
     */
    public SetVar[] retrieveSetVars() {
        SetVar[] bvars = new SetVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.SET) {
                bvars[k++] = (SetVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the RealVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of RealVars of <code>this</code>
     */
    public RealVar[] retrieveRealVars() {
        RealVar[] bvars = new RealVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.KIND) == Variable.REAL) {
                bvars[k++] = (RealVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Returns the array of declared <code>Constraint</code> objects defined in this <code>Model</code>.
     *
     * @return array of constraints
     */
    public Constraint[] getCstrs() {
        return Arrays.copyOf(cstrs, cIdx);
    }

    /**
     * Return the number of constraints declared in <code>this</code>.
     *
     * @return number of constraints.
     */
    public int getNbCstrs() {
        return cIdx;
    }

    /**
     * Return the name, if any, of <code>this</code>.
     * @return this model's name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the backtracking environment of <code>this</code>.
     * @return the backtracking environment of this model
     */
    public IEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Returns the propagation engine used in <code>this</code>.
     *
     * @return a propagation engine.
     */
    public IPropagationEngine getEngine() {
        return engine;
    }

    /**
     * Return the objective variables
     *
     * @return a variable
     */
    public Variable[] getObjectives() {
        assert objectives!=null;
        return objectives;
    }

    /**
     * In case of real variable to optimize, a precision is required.
     *
     * @return the precision used
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Returns the object associated with the named <code>hookName</code>
     * @param hookName the name of the hook to return
     * @return the object associated to the name <code>hookName</code>
     */
    public Object getHook(String hookName){
        return hooks.get(hookName);
    }

    /**
     * Returns the map containing declared hooks.
     * This map is mutable.
     * @return the map of hooks.
     */
    public Map<String, Object> getHooks(){
        return hooks;
    }


    /**
     * Return a constraint embedding a minisat model.
     * It is highly recommended that there is only once instance of this constraint in a model.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public SatConstraint getMinisat() {
        if (minisat == null) {
            minisat = new SatConstraint(this);
            minisat.post();
        }
        return minisat;
    }

    /**
     * Return a constraint embedding a nogood store (based on a sat model).
     * It is highly recommended that there is only once instance of this constraint in a model.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public NogoodConstraint getNogoodStore() {
        if (nogoods == null) {
            nogoods = new NogoodConstraint(this);
            nogoods.post();
        }
        return nogoods;
    }

    /**
     * Return a constraint embedding a constructive disjunction store.
     * There can be only on instance of such a constraint in a solver to avoid undesirable side effects.
     * @return the condis constraint
     */
    public CondisConstraint getCondisStore(){
        if (condis == null) {
            condis = new CondisConstraint(this);
            condis.post();
        }
        return condis;
    }

    /**
     * Return the current settings for the solver
     *
     * @return a {@link org.chocosolver.solver.Settings}
     */
    public Settings getSettings() {
        return this.settings;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Override the default search loop to use in <code>this</code>.
     *
     * @param resolver the search loop to use
     */
    public void set(Resolver resolver) {
        this.search = resolver;
    }

    /**
     * Define the variables to optimize
     *
     * @param objectives one or more variables
     */
    public void setObjectives(ResolutionPolicy policy, Variable... objectives) {
        if(objectives == null){
            resetObjectives();
        }else {
            this.objectives = objectives;
            this.policy = policy;
        }
    }

    /**
     * @deprecated use {@link #setObjectives(ResolutionPolicy, Variable...)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void setObjectives(Variable... objectives) {
        if(objectives == null){
            resetObjectives();
        }else {
            this.objectives = objectives;
        }
    }

    /**
     * Removes any objective and set problem to a satisfaction problem
     */
    public void resetObjectives() {
        this.objectives = new Variable[0];
        this.policy = ResolutionPolicy.SATISFACTION;
    }

    /**
     * In case of real variable to optimize, a precision is required.
     *
     * @param p the precision (default is 0.00D)
     */
    public void setPrecision(double p) {
        this.precision = p;
    }

    /**
     * Attach a propagation engine <code>this</code>.
     * It overrides the previously defined one, if any.
     *
     * @param propagationEngine a propagation strategy
     */
    public void set(IPropagationEngine propagationEngine) {
        this.engine = propagationEngine;
    }

    /**
     * Override the default {@link org.chocosolver.solver.Settings} object.
     *
     * @param defaults new settings
     */
    public void set(Settings defaults) {
        this.settings = defaults;
    }

    /**
     * Adds the <code>hookObject</code> to store in this model, associated with the name <code>hookName</code>.
     * A hook is a simple map "hookName" <-> hookObject.
     * @param hookName name of the hook
     * @param hookObject hook to store
     */
    public void addHook(String hookName, Object hookObject){
        this.hooks.put(hookName, hookObject);
    }

    /**
     * Removes the hook named <code>hookName</code>
     * @param hookName name of the hookObject to remove
     */
    public void removeHook(String hookName){
        this.hooks.remove(hookName);
    }

    /**
     * Empties the hooks attaches to this model.
     */
    public void removeAllHooks(){
        this.hooks.clear();
    }

    /**
     * Changes the name of this model to be equal to the argument <code>name</code>.
     * @param name the new name of this model.
     */
    public void setName(String name){
        this.name = name;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RELATED TO VAR AND CSTR DECLARATION ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Link a variable to <code>this</code>.
     * This is executed AUTOMATICALLY in variable constructor, so no checked are done on multiple occurrences of
     * the very same variable.
     *
     * @param variable a newly created variable, not already added
     */
    public void associates(Variable variable) {
        if (vIdx == vars.length) {
            Variable[] tmp = vars;
            vars = new Variable[tmp.length * 2];
            System.arraycopy(tmp, 0, vars, 0, vIdx);
        }
        vars[vIdx++] = variable;
    }

    /**
     * Unlink the variable from <code>this</code>.
     *
     * @param variable variable to un-associate
     */
    public void unassociates(Variable variable) {
        if (variable.getNbProps() > 0) {
            throw new SolverException("Try to remove a variable (" + variable.getName() + ")which is still involved in at least one constraint");
        }
        int idx = 0;
        for (; idx < vIdx; idx++) {
            if (variable == vars[idx]) break;
        }
        System.arraycopy(vars, idx + 1, vars, idx + 1 - 1, vIdx - (idx + 1));
        vars[--vIdx] = null;
    }

    /**
     * Post permanently a constraint <code>c</code> in the constraints network of <code>this</code>:
     * - add it to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param c a Constraint
     */
    public void post(Constraint c) {
        _post(true, c);
    }

    /**
     * Post constraints <code>cs</code> permanently in the constraints network of <code>this</code>:
     * - add them to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param cs Constraints
     */
    public void post(Constraint... cs) {
        _post(true, cs);
    }

    /**
     * Post a constraint temporary, that is, it will be unposted upon backtrack.
     *
     * @param c constraint to add
     * @throws ContradictionException if the addition of the constraint <code>c</code> detects inconsistency.
     */
    public void postTemp(Constraint c) throws ContradictionException {
        _post(false, c);
        if (engine == NoPropagationEngine.SINGLETON || !engine.isInitialized()) {
            throw new SolverException("Try to post a temporary constraint while the resolution has not begun.\n" +
                    "A call to Model.post(Constraint) is more appropriate.");
        }
        for (Propagator propagator : c.getPropagators()) {
            if(settings.debugPropagation()){
                IPropagationEngine.Trace.printFirstPropagation(propagator, settings.outputWithANSIColors());
            }
            PropagationTrigger.execute(propagator, engine);
        }
    }


    /**
     * Add constraints to the model.
     *
     * @param permanent specify whether the constraints are added permanently (if set to true) or temporary (ie, should be removed on backtrack)
     * @param cs        list of constraints
     */
    private void _post(boolean permanent, Constraint... cs) {
        boolean dynAdd = false;
        // check if the resolution already started -> if true, dynamic addition
        if (engine != NoPropagationEngine.SINGLETON && engine.isInitialized()) {
            dynAdd = true;
        }
        // then store the constraints
        if (cIdx + cs.length >= cstrs.length) {
            int nsize = cstrs.length;
            while (cIdx + cs.length >= nsize) {
                nsize *= 3 / 2 + 1;
            }
            Constraint[] tmp = cstrs;
            cstrs = new Constraint[nsize];
            System.arraycopy(tmp, 0, cstrs, 0, cIdx);
        }
        System.arraycopy(cs, 0, cstrs, cIdx, cs.length);
        cIdx += cs.length;
        // specific behavior for dynamic addition and/or reified constraints
        for (int i = 0; i < cs.length; i++) {
            if (dynAdd) {
                engine.dynamicAddition(permanent, cs[i].getPropagators());
            }
            if (cs[i].isReified()) {
                try {
                    cs[i].reify().setToTrue(Cause.Null);
                } catch (ContradictionException e) {
                    throw new SolverException("post a constraint whose reification BoolVar is already set to false: no solution can exist");
                }
            }
        }
    }

    /**
     * Remove permanently the constraint <code>c</code> from the constraint network.
     *
     * @param c the constraint to remove
     */
    public void unpost(Constraint c) {
        // 1. look for the constraint c
        int idx = 0;
        while (idx < cIdx && cstrs[idx] != c) {
            idx++;
        }
        // 2. remove it from the network
        while (idx < cIdx) {
            Constraint cm = cstrs[--cIdx];
            cstrs[idx] = cm;
            cstrs[cIdx] = null;
            // 3. check if the resolution already started -> if true, dynamic deletion
            if (engine != NoPropagationEngine.SINGLETON && engine.isInitialized()) {
                engine.dynamicDeletion(c.getPropagators());
            }
            // 4. remove the propagators of the constraint from its variables
            for (Propagator prop : c.getPropagators()) {
                for (int v = 0; v < prop.getNbVars(); v++) {
                    prop.getVar(v).unlink(prop);
                }
            }
            // the constraint can have been posted more than once "accidentally" (but that's not a big deal, expect for
            // performance issue) but all occurrences should be removed now.
            while (idx < cIdx && cstrs[idx] != c) {
                idx++;
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// RELATED TO I/O ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return a string describing the CSP defined in <code>this</code>.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("\n Model[%s]\n", name));
        st.append(String.format("\n[ %d vars -- %d cstrs ]\n", vIdx, cIdx));
        st.append(String.format("Feasability: %s\n", isFeasible()));
        st.append("== variables ==\n");
        for (int v = 0; v < vIdx; v++) {
            st.append(vars[v].toString()).append('\n');
        }
        st.append("== constraints ==\n");
        for (int c = 0; c < cIdx; c++) {
            st.append(cstrs[c].toString()).append('\n');
        }
        return st.toString();
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into the given {@code file}.
     *
     * @param model to flatten
     * @param file   scope file
     * @throws java.io.IOException if an I/O exception occurs.
     */
    public static void writeInFile(final Model model, final File file) throws IOException {
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(model);
        out.close();
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into a file
     * in the default temporary-file directory.
     *
     * @param model to flatten
     * @return output file
     * @throws IOException if an I/O exception occurs.
     */
    public static File writeInFile(final Model model) throws IOException {
        final File file = File.createTempFile("SOLVER_", ".ser");
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(model);
        out.close();
        return file;
    }


    /**
     * Restore flatten {@link Model} from the given {@code file}.
     *
     * @param file input file
     * @return a {@link Model}
     * @throws IOException            if an I/O exception occurs.
     * @throws ClassNotFoundException if wrong flattened object.
     */
    @SuppressWarnings("unused")
    public static Model readFromFile(final String file) throws IOException, ClassNotFoundException {
        FileInputStream fis;
        ObjectInputStream in;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Model model = (Model) in.readObject();
        in.close();
        return model;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// RELATED TO IBEX ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>This methods should not be called by the user.</b>
     * @return a free id to use
     */
    public int nextId() {
        return id++;
    }

    /**
     * Get the ibex reference
     * Creates one if none
     *
     * @return the ibex reference
     */
    public Ibex getIbex() {
        if (ibex == null) ibex = new Ibex();
        return ibex;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// RELATED TO MODELING FACTORIES /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Model _me(){
        return this;
    }






















    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONTENT TO MOVE INSIDE RESOLVER
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////






























    /**
     * Attempts to find the first solution of the declared problem.
     * Then, following solutions can be found using {@link Model#nextSolution()}.
     * <p>
     * An alternative is to call {@link Model#isFeasible()} which tells, whether or not, a solution has been found.
     *
     * @return <code>true</code> if and only if a solution has been found.
     */
    public boolean findSolution() {
        solve(true);
        return getMeasures().getSolutionCount() > 0;
    }

    /**
     * Once {@link Model#findSolution()} has been called once, other solutions can be found using this method.
     * <p>
     * The search is then resume to the last found solution point.
     *
     * If {@link Model#findSolution()} has not been called yet, call it instead.
     *
     * @return a boolean stating whereas a new solution has been found (<code>true</code>), or not (<code>false</code>).
     */
    public boolean nextSolution() {
        if(getResolver() != null && getResolver().hasResolutionBegun()){
            long nbsol = getMeasures().getSolutionCount();
            getResolver().launch(true);
            return (getMeasures().getSolutionCount() - nbsol) > 0;
        }else{
            return findSolution();
        }
    }

    /**
     * Attempts to find all solutions of the declared problem.
     *
     * @return the number of found solutions.
     */
    public long findAllSolutions() {
        resetObjectives();
        solve(false);
        return getMeasures().getSolutionCount();
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>.
     * If <i>restoreLastSolution</i> is set to <tt>true</tt> and at least one solution has been found,
     * the last solution found so far is restored on exit.
     *
     * @param policy optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param restoreLastSolution set to <tt>true</tt> to automatically restore the last (presumably best) solution
     *                            found in this solver (i.e., {@link #restoreLastSolution()} is called) on exit,
     *                            set to <tt>false</tt> otherwise.
     */
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution) {
        if (objectives == null || objectives.length == 0) {
            throw new SolverException("No objective variable has been defined");
        }
        if (!getObjectiveManager().isOptimization()) {
            if (objectives.length == 1) {
                if ((objectives[0].getTypeAndKind() & Variable.KIND) == Variable.REAL) {
                    set(new ObjectiveManager<RealVar, Double>((RealVar) objectives[0], policy, 0.00d, true));
                } else {
                    set(new ObjectiveManager<IntVar, Integer>((IntVar) objectives[0], policy, true));
                }
            } else {
                // BEWARE the usual optimization manager is only defined for mono-objective optimization
                // so we use a satisfaction manager by default (which does nothing)
                // with a pareto solution recorder that dynamically adds constraints to skip dominated solutions
                if (!getObjectiveManager().isOptimization()) {
                    set(new ObjectiveManager<IntVar, Integer>(null, ResolutionPolicy.SATISFACTION, false));
                }
                IntVar[] _objectives = new IntVar[objectives.length];
                for (int i = 0; i < objectives.length; i++) {
                    _objectives[i] = (IntVar) objectives[i];
                }
                set(new ParetoSolutionsRecorder(policy, _objectives));
            }
        }
        solve(false);
        if (restoreLastSolution) {
            try {
                restoreLastSolution();
            } catch (ContradictionException e) {
                throw new UnsupportedOperationException("restoring the last solution ended in a failure");
            } finally {
                getEngine().flush();
            }
        }
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>
     * and restores the last solution found (if any) on exit.
     * <p>
     * Equivalent to {@link #findOptimalSolution(ResolutionPolicy, boolean)} where <i>boolean</i> is set to <tt>true</tt>.
     *
     * @param policy optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @see #findOptimalSolution(ResolutionPolicy, boolean)
     */
    public void findOptimalSolution(ResolutionPolicy policy) {
        findOptimalSolution(policy, true);
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>.
     * If <i>restoreLastSolution</i> is set to <tt>true</tt> and at least one solution has been found,
     * the last solution found so far is restored on exit.
     * <p>
     * Indeed, it calls in sequence:
     * <pre>
     *     <code>setObjectives(objectives);
     *     findOptimalSolution(policy, restoreLastSolution);
     *     </code>
     * </pre>
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param restoreLastSolution set to <tt>true</tt> to automatically restore the last (presumably best) solution
     *                            found in this solver (i.e., {@link #restoreLastSolution()} is called) on exit,
     *                            set to <tt>false</tt> otherwise.
     * @param objective the variable to optimize
     * @see #setObjectives(Variable...)
     * @see #findOptimalSolution(ResolutionPolicy, boolean)
     */
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution, IntVar objective) {
        setObjectives(objective);
        findOptimalSolution(policy, restoreLastSolution);
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>
     * and restores the last solution found (if any) on exit.
     * <p>
     * Equivalent to {@link #findOptimalSolution(ResolutionPolicy, boolean, IntVar)} where <i>boolean</i> is set to <tt>true</tt>.
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     * @see #findOptimalSolution(ResolutionPolicy, boolean, IntVar)
     */
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        findOptimalSolution(policy, true, objective);
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Finds and stores all optimal solution.
     * Calling this method does not restore solution on exit
     * since multiple equivalent (wrt objective value) solutions may exist.
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     * @param twoSteps  When set to true it calls two resolution:
     *                  1) It finds and prove the optimum
     *                  2) It reset search and enumerates all solutions of optimal cost
     *                  When set to false, it performs only one resolution but which does impose to find strictly
     *                  better solutions. This means it will spend time enumerating intermediary solutions equal to the
     *                  the best cost found so far (but not necessarily optimal).
     */
    public void findAllOptimalSolutions(ResolutionPolicy policy, IntVar objective, boolean twoSteps) {
        if (twoSteps) {
            findOptimalSolution(policy, objective);
            if (getMeasures().getSolutionCount() > 0) {
                int opt = getObjectiveManager().getBestSolutionValue().intValue();
                getEngine().flush();
                getResolver().reset();
                arithm(objective, "=", opt).post();
                set(new AllSolutionsRecorder(this));
                findAllSolutions();
            }
        } else {
            if (policy == ResolutionPolicy.SATISFACTION) {
                throw new SolverException("Model.findAllOptimalSolutions(...) cannot be called with ResolutionPolicy.SATISFACTION.");
            }
            if (objective == null) {
                throw new SolverException("No objective variable has been defined");
            }
            if (!getObjectiveManager().isOptimization()) {
                set(new ObjectiveManager<IntVar, Integer>(objective, policy, false));
            }
            set(new BestSolutionsRecorder(objective));
            solve(false);
        }
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>.
     * Finds and stores all optimal solution.
     * If <i>restoreLastSolution</i> is set to <tt>true</tt> and at least one solution has been found,
     * the last solution found so far is restored on exit.
     * <p>
     * Indeed, it calls in sequence:
     * <pre>
     *     <code>setObjectives(objectives);
     *     findOptimalSolution(policy, restoreLastSolution);
     *     </code>
     * </pre>
     *
     * @param policy     optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param restoreLastSolution set to <tt>true</tt> to automatically restore the last (presumably best) solution
     *                            found in this solver (i.e., {@link #restoreLastSolution()} is called) on exit,
     *                            set to <tt>false</tt> otherwise.
     * @param objectives the variables to optimize. BEWARE they should all respect the SAME optimization policy
     * @see #setObjectives(Variable...)
     * @see #findOptimalSolution(ResolutionPolicy, boolean)
     */
    public void findParetoFront(ResolutionPolicy policy, boolean restoreLastSolution, IntVar... objectives) {
        setObjectives(objectives);
        findOptimalSolution(policy, restoreLastSolution);
    }

    /**
     * Attempts optimize the value of the <i>objective</i> variable w.r.t. to the optimization <i>policy</i>.
     * It finds and stores all optimal solution
     * and restores the last solution found (if any) on exit.
     * <p>
     * Equivalent to {@link #findParetoFront(ResolutionPolicy, boolean, IntVar...)} where <i>boolean</i> is set to <tt>true</tt>.
     *
     * @param policy     optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objectives the variables to optimize. BEWARE they should all respect the SAME optimization policy
     * @see #findParetoFront(ResolutionPolicy, boolean, IntVar...)
     */
    public void findParetoFront(ResolutionPolicy policy, IntVar... objectives) {
        findParetoFront(policy, true, objectives);
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * If <i>restoreLastSolution</i> is set to <tt>true</tt> and at least one solution has been found,
     * the last solution found so far is restored on exit.
     * <p>
     * Indeed, it calls in sequence:
     * <pre>
     *     <code>setObjectives(objectives);
     *     setPrecision(precision);
     *     findOptimalSolution(policy, restoreLastSolution);
     *     </code>
     * </pre>
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param restoreLastSolution set to <tt>true</tt> to automatically restore the last (presumably best) solution
     *                            found in this solver (i.e., {@link #restoreLastSolution()} is called) on exit,
     *                            set to <tt>false</tt> otherwise.
     * @param objective the variable to optimize
     * @param precision to consider that <code>objective</code> is instantiated.
     * @see #setObjectives(Variable...)
     * @see #setPrecision(double)
     * @see #findOptimalSolution(ResolutionPolicy, boolean)
     */
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution, RealVar objective, double precision) {
        setObjectives(objective);
        setPrecision(precision);
        findOptimalSolution(policy, restoreLastSolution);
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>
     * and restores the last solution found (if any) on exit.
     * <p>
     * Equivalent to {@link #findOptimalSolution(ResolutionPolicy, boolean, RealVar, double)}
     * where <i>boolean</i> is set to <tt>true</tt>.
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     * @param precision to consider that <code>objective</code> is instantiated.
     * @see #findOptimalSolution(ResolutionPolicy, boolean, RealVar, double)
     */
    public void findOptimalSolution(ResolutionPolicy policy, RealVar objective, double precision) {
        setObjectives(objective);
        setPrecision(precision);
        findOptimalSolution(policy, true);
    }

    /**
     * This method should not be called externally. It launches the resolution process.
     * @param stopAtFirst set to <tt>true</tt> to stop the search when the first solution is found, <tt>false</tt> otherwise.
     */
    protected void solve(boolean stopAtFirst) {
        if (engine == NoPropagationEngine.SINGLETON) {
            this.set(PropagationEngineFactory.DEFAULT.make(this));
        }
        if (!engine.isInitialized()) {
            engine.initialize();
        }
        getMeasures().setReadingTimeCount(creationTime + System.nanoTime());
        getResolver().launch(stopAtFirst);
    }
















    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////   MOVED IN RESOLVER   //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////











    /**
     * @deprecated use {@link Resolver#getSolutionRecorder()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ISolutionRecorder getSolutionRecorder() {
        return getResolver().getSolutionRecorder();
    }

    /**
     * @deprecated use {@link Resolver#set(ISolutionRecorder)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(ISolutionRecorder sr) {
        getResolver().set(sr);
    }

    /**
     * @deprecated use {@link Resolver#restoreLastSolution()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean restoreLastSolution() throws ContradictionException {
        return getResolver().restoreLastSolution();
    }

    /**
     * @deprecated use {@link Resolver#restoreSolution(Solution)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean restoreSolution(Solution solution) throws ContradictionException {
        return getResolver().restoreSolution(solution);
    }

    /**
     * @deprecated use {@link Resolver#isFeasible()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ESat isFeasible() {
        return getResolver().isFeasible();
    }

    /**
     * @deprecated use {@link Resolver#setFeasible(ESat)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void setFeasible(ESat feasible) {
        getResolver().setFeasible(feasible);
    }

    /**
     * @deprecated use {@link Resolver#getExplainer()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ExplanationEngine getExplainer() {
        return getResolver().getExplainer();
    }

    /**
     * @deprecated use {@link Resolver#set(ExplanationEngine)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(ExplanationEngine explainer) {
        getResolver().set(explainer);
    }

    /**
     * @deprecated use {@link Resolver#plugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void plugMonitor(ISearchMonitor sm) {
        getResolver().plugMonitor(sm);
    }

    /**
     * @deprecated use {@link Resolver#unplugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void unplugMonitor(ISearchMonitor sm){
        getResolver().unplugMonitor(sm);
    }

    /**
     * @deprecated use {@link Resolver#unplugAllMonitors()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void unplugAllMonitors(){
        getResolver().unplugAllMonitors();
    }

    /**
     * @deprecated use {@link Resolver#plugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void plugMonitor(FilteringMonitor filteringMonitor) {
        getResolver().plugMonitor(filteringMonitor);
    }

    /**
     * @deprecated use {@link Resolver#getEventObserver()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public FilteringMonitor getEventObserver() {
        return getResolver().getEventObserver();
    }

    /**
     * @deprecated use {@link Resolver#addStopCriterion(Criterion)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void addStopCriterion(Criterion criterion){
        getResolver().addStopCriterion(criterion);
    }

    /**
     * @deprecated use {@link Resolver#removeStopCriterion(Criterion)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeStopCriterion(Criterion criterion){
        getResolver().removeStopCriterion(criterion);
    }

    /**
     * @deprecated use {@link Resolver#removeAllStopCriteria()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeAllStopCriteria(){
        getResolver().removeAllStopCriteria();
    }

    /**
     * @deprecated use {@link Resolver#getMeasures()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public IMeasures getMeasures() {
        return getResolver().getMeasures();
    }

    /**
     * @deprecated use {@link Resolver#isSatisfied()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ESat isSatisfied() {
        return getResolver().isSatisfied();
    }

    /**
     * @deprecated use {@link Resolver#propagate()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void propagate() throws ContradictionException {
        getResolver().propagate();
    }

    /**
     * @deprecated use {@link Resolver#hasReachedLimit()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean hasReachedLimit() {
        return getResolver().hasReachedLimit();
    }

    /**
     * @deprecated use {@link Resolver#getObjectiveManager()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ObjectiveManager getObjectiveManager() {
        return getResolver().getObjectiveManager();
    }

    /**
     * @deprecated use {@link Resolver#getStrategy()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public AbstractStrategy getStrategy() {
        return getResolver().getStrategy();
    }

    /**
     * @deprecated use {@link Resolver#set(AbstractStrategy[])} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(AbstractStrategy... strategies) {
        if (strategies == null || strategies.length == 0) {
            throw new UnsupportedOperationException("no search strategy has been specified");
        }
        if (strategies.length == 1) {
            getResolver().set(strategies[0]);
        } else {
            getResolver().set(ISF.sequencer(strategies));
        }
    }

    /**
     * @deprecated use {@link Resolver#setObjectiveManager(ObjectiveManager)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(ObjectiveManager om) {
        getResolver().setObjectiveManager(om);
    }

    /**
     * @deprecated use {@link Resolver#makeCompleteStrategy(boolean)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void makeCompleteSearch(boolean isComplete) {
        getResolver().makeCompleteStrategy(isComplete);
    }
}
