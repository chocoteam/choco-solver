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
import org.chocosolver.solver.constraints.reification.ConDisConstraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.propagation.PropagationTrigger;
import org.chocosolver.solver.search.loop.monitors.ISearchMonitor;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.*;
import org.chocosolver.util.ESat;
import org.chocosolver.util.criteria.Criterion;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
public class Model implements Serializable, IModel {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// PRIVATE FIELDS /////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** For serialization purpose */
    private static final long serialVersionUID = 1L;

    /** Settings to use with this solver */
    private Settings settings = new Settings() {};

    /** A map to cache constants (considered as fixed variables) */
    private TIntObjectHashMap<IntVar> cachedConstants;

    /** Variables of the model */
    private Variable[] vars;

    /** Index of the last added variable */
    private int vIdx;

    /** Constraints of the model */
    private Constraint[] cstrs;

    /** Index of the last added constraint */
    private int cIdx;

    /** Environment, based of the search tree (trailing or copying) */
    private final IEnvironment environment;

    /** Resolver of the model, controls propagation and search */
    private final Solver solver;

    /** Variable to optimize, possibly null. */
    private Variable objective;

    /** Precision to consider when optimizing a RealVariable */
    private double precision = 0.0001D;

    /** Model name */
    private String name;

    /** Stores this model's creation time */
    private long creationTime;

    /** Counter used to set ids to variables and propagators */
    private int id = 1;

    /** Basic TRUE constraint, cached to avoid multiple useless occurrences */
    private Constraint TRUE;

    /** Basic FALSE constraint, cached to avoid multiple useless occurrences */
    private Constraint FALSE;

    /** Basic ZERO constant, cached to avoid multiple useless occurrences. */
    private BoolVar ZERO;

    /** Basic ONE constant, cached to avoid multiple useless occurrences. */
    private BoolVar ONE;

    /** A MiniSat instance, useful to deal with clauses*/
    private SatConstraint minisat;

    /** A MiniSat instance adapted to nogood management */
    private NogoodConstraint nogoods;

    /** A CondisConstraint instance adapted to constructive disjunction management */
    private ConDisConstraint condis;

    /** An Ibex (continuous constraint model) instance */
    private Ibex ibex;

    /** Enable attaching hooks to a model. */
    private Map<String,Object> hooks;

    /** Resolution policy (sat/min/max) */
    private ResolutionPolicy policy = ResolutionPolicy.SATISFACTION;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// CONSTRUCTORS ///////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a Model object to formulate a decision problem by declaring variables and posting constraints.
     * The model is named <code>name</code> and it uses a specific backtracking <code>environment</code>.
     *
     * @param environment a backtracking environment to allow search
     * @param name        The name of the model (for logging purpose)
     */
    public Model(IEnvironment environment, String name) {
        this.name = name;
        this.vars = new Variable[32];
        this.vIdx = 0;
        this.cstrs = new Constraint[32];
        this.cIdx = 0;
        this.environment = environment;
        this.creationTime = System.currentTimeMillis();
        this.cachedConstants = new TIntObjectHashMap<>(16, 1.5f, Integer.MAX_VALUE);
        this.objective = null;
        this.hooks = new HashMap<>();
        this.solver = new Solver(this);
    }

    /**
     * Creates a Model object to formulate a decision problem by declaring variables and posting constraints.
     * The model is named <code>name</code> and uses the default (trailing) backtracking environment.
     *
     * @param name        The name of the model (for logging purpose)
     * @see Model#Model(org.chocosolver.memory.IEnvironment, String)
     */
    public Model(String name) {
        this(Environments.DEFAULT.make(), name);
    }

    /**
     * Creates a Model object to formulate a decision problem by declaring variables and posting constraints.
     * The model uses the default (trailing) backtracking environment.
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
     * Get the creation time (in milliseconds) of the model (to estimate modeling duration)
     * @return the time (in ms) of the creation of the model
     */
    public long getCreationTime(){
        return creationTime;
    }

    /**
     * Get the resolution policy of the model
     * @return the resolution policy of the model
     * @see ResolutionPolicy
     */
    public ResolutionPolicy getResolutionPolicy(){
        return policy;
    }

    /**
     * Get the map of constant IntVar the have default names to avoid creating multiple identical constants.
     * Should not be called by the user.
     * @return the map of constant IntVar having default names.
     */
    public TIntObjectHashMap<IntVar> getCachedConstants() {
        return cachedConstants;
    }

    /**
     * The basic constant "0" used as an integer variable or a (false) boolean variable
     * @return a BoolVar set to 0
     */
    public BoolVar ZERO() {
        if (ZERO == null) {
            _zeroOne();
        }
        return ZERO;
    }

    /**
     * The basic constant "1" used as an integer variable or a (true) boolean variable
     * @return a BoolVar set to 1
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
     * The basic "true" constraint, which is always satisfied
     * @return a "true" constraint
     */
    public Constraint TRUE() {
        if (TRUE == null) {
            TRUE = new Constraint("TRUE cstr", new PropTrue(ONE()));
        }
        return TRUE;
    }

    /**
     * The basic "false" constraint, which is always violated
     * @return a "false" constraint
     */
    public Constraint FALSE() {
        if (FALSE == null) {
            FALSE = new Constraint("FALSE cstr", new PropFalse(ZERO()));
        }
        return FALSE;
    }

    /**
     * Returns the unique and internal propagation and search object to solve this model.
     * @return the unique and internal <code>Resolver</code> object.
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * Returns the array of <code>Variable</code> objects declared in this <code>Model</code>.
     * @return array of all variables in this model
     */
    public Variable[] getVars() {
        return Arrays.copyOf(vars, vIdx);
    }

    /**
     * Returns the number of variables involved in <code>this</code>.
     * @return number of variables in this model
     */
    public int getNbVars() {
        return vIdx;
    }

    /**
     * Returns the i<sup>th</sup> variable within the array of variables defined in <code>this</code>.
     * @param i index of the variable to return.
     * @return the i<sup>th</sup> variable of this model
     */
    public Variable getVar(int i) {
        return vars[i];
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains all the IntVar of the model.
     * <b>excludes</b> BoolVar if includeBoolVar=false.
     * It also contains FIXED variables and VIEWS, if any.
     * @param includeBoolVar indicates whether or not to include BoolVar
     * @return array of IntVars in <code>this</code> model
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
     * @return array of BoolVars in <code>this</code> model
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
     * @return array of SetVars in <code>this</code> model
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
     * @return array of RealVars in <code>this</code> model
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
     * Returns the array of <code>Constraint</code> objects posted in this <code>Model</code>.
     * @return array of posted constraints
     */
    public Constraint[] getCstrs() {
        return Arrays.copyOf(cstrs, cIdx);
    }

    /**
     * Return the number of constraints posted in <code>this</code>.
     * @return number of posted constraints.
     */
    public int getNbCstrs() {
        return cIdx;
    }

    /**
     * Return the name of <code>this</code> model.
     * @return this model's name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the backtracking environment of <code>this</code> model.
     * @return the backtracking environment of this model
     */
    public IEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Return the (possibly null) objective variable
     * @return a variable (null for satisfaction problems)
     */
    public Variable getObjective() {
        return objective;
    }

    /**
     * In case of real variable(s) to optimize, a precision is required.
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
     * Returns the unique constraint embedding a minisat model.
     * A call to this method will create and post the constraint if it does not exist already.
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
     * A call to this method will create and post the constraint if it does not exist already.
     * @return the no good constraint
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
     * A call to this method will create and post the constraint if it does not exist already.
     * @return the constructive disjunction constraint
     */
    public ConDisConstraint getConDisStore(){
        if (condis == null) {
            condis = new ConDisConstraint(this);
            condis.post();
        }
        return condis;
    }

    /**
     * Return the current settings for the solver
     * @return a {@link org.chocosolver.solver.Settings}
     */
    public Settings getSettings() {
        return this.settings;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Defines the variable to optimize according to <i>policy</i>.
     * By default, each solution forces either :
     * <ul>
     * <li> for {@link ResolutionPolicy#MAXIMIZE}: to increase by one for {@link IntVar} (or {@link #precision} for {@link RealVar}) the objective lower bound, or</li>
     * <li> for {@link ResolutionPolicy#MINIMIZE}:  to decrease by one {@link IntVar} (or {@link #precision} for {@link RealVar}) the objective upper bound.</li>
     * </ul>
     * @see ObjectiveManager#strictIntVarCutComputer(ResolutionPolicy)
     * @see ObjectiveManager#strictRealVarCutComputer(ResolutionPolicy, double)
     * @see ObjectiveManager#setCutComputer(Function)
     * @param policy optimisation policy (minimisation or maximisation)
     * @param objective variable to optimize
     */
    @SuppressWarnings("unchecked")
    public <V extends Variable, N extends Number> void setObjective(ResolutionPolicy policy, V objective) {
        if(objective == null){
            assert policy == ResolutionPolicy.SATISFACTION;
            clearObjective();
        }else {
            assert policy != ResolutionPolicy.SATISFACTION;
            this.objective = objective;
            this.policy = policy;
            if ((objective.getTypeAndKind() & Variable.KIND) == Variable.REAL) {
                getSolver().set(new ObjectiveManager<RealVar, Double>((RealVar) objective, policy, 0.00d,
                        ObjectiveManager.strictRealVarCutComputer(policy, precision)));
            } else {
                getSolver().set(new ObjectiveManager<IntVar, Integer>((IntVar) objective, policy,
                        ObjectiveManager.strictIntVarCutComputer(policy)));
            }
        }
    }

    /**
     * Removes any objective and set problem to a satisfaction problem
     */
    public void clearObjective() {
        this.objective = null;
        this.policy = ResolutionPolicy.SATISFACTION;
        getSolver().set(ObjectiveManager.SAT());
    }

    /**
     * In case of real variable to optimize, a precision is required.
     * @param p the precision (default is 0.0001D)
     */
    public void setPrecision(double p) {
        this.precision = p;
    }

    /**
     * Override the default {@link org.chocosolver.solver.Settings} object.
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
     * Empties the hooks attached to this model.
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
    ///////////////////////////////////////         RELATED TO VAR              ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Link a variable to <code>this</code>. This is executed AUTOMATICALLY in variable constructor,
     * so no checked are done on multiple occurrences of the very same variable.
     * Should not be called by the user.
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
     * Should not be called by the user.
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
     * Get a free id to idendity unically a new variable.
     * Should not be called by the user.
     * @return a free id to use
     */
    public int nextId() {
        return id++;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////     RELATED TO CSTR DECLARATION     ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Posts constraints <code>cs</code> permanently in the constraints network of <code>this</code>:
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
     * Add constraints to the model.
     *
     * @param permanent specify whether the constraints are added permanently (if set to true) or temporary (ie, should be removed on backtrack)
     * @param cs        list of constraints
     */
    private void _post(boolean permanent, Constraint... cs) {
        boolean dynAdd = false;
        // check if the resolution already started -> if true, dynamic addition
        IPropagationEngine engine = getSolver().getEngine();
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
     * Posts constraints <code>cs</code> temporary, that is, they will be unposted upon backtrack.
     * @param cs a set of constraints to add
     * @throws ContradictionException if the addition of constraints <code>cs</code> detects inconsistency.
     */
    public void postTemp(Constraint... cs) throws ContradictionException {
        for(Constraint c:cs) {
            _post(false, c);
            if (getSolver().getEngine() == NoPropagationEngine.SINGLETON || !getSolver().getEngine().isInitialized()) {
                throw new SolverException("Try to post a temporary constraint while the resolution has not begun.\n" +
                        "A call to Model.post(Constraint) is more appropriate.");
            }
            for (Propagator propagator : c.getPropagators()) {
                if (settings.debugPropagation()) {
                    IPropagationEngine.Trace.printFirstPropagation(propagator, settings.outputWithANSIColors());
                }
                PropagationTrigger.execute(propagator, getSolver().getEngine());
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
        IPropagationEngine engine = getSolver().getEngine();
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
     * Return a string describing the CSP defined in <code>this</code> model.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("\n Model[%s]\n", name));
        st.append(String.format("\n[ %d vars -- %d cstrs ]\n", vIdx, cIdx));
        st.append(String.format("Feasability: %s\n", getSolver().isFeasible()));
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
    ////////////////////////////////////////////  RELATED TO SOLVING PROCESS   /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Solves the model by executing {@link Solver#solve()}.
     *
     * Default configuration:
     * - SATISFACTION : Computes a feasible solution. Use while(solve()) to enumerate all solutions.
     * - OPTIMISATION : If an objective has been defined, searches an optimal solution
     * (and prove optimality by closing the search space). Then restores the best solution found after solving.
     * @return if at least one new solution has been found.
     * @see {@link Solver#solve()}
     */
    public boolean solve(){
        return getSolver().solve();
    }
















    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       TO DELETE       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @deprecated use {@link Solver#isFeasible()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ESat isFeasible() {
        return getSolver().isFeasible();
    }

    /**
     * @deprecated use {@link Solver#getExplainer()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ExplanationEngine getExplainer() {
        return getSolver().getExplainer();
    }

    /**
     * @deprecated use {@link Solver#set(ExplanationEngine)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(ExplanationEngine explainer) {
        getSolver().set(explainer);
    }

    /**
     * @deprecated use {@link Solver#plugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void plugMonitor(ISearchMonitor sm) {
        getSolver().plugMonitor(sm);
    }

    /**
     * @deprecated use {@link Solver#unplugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void unplugMonitor(ISearchMonitor sm){
        getSolver().unplugMonitor(sm);
    }

    /**
     * @deprecated use {@link Solver#unplugAllSearchMonitors()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void unplugAllMonitors(){
        getSolver().unplugAllSearchMonitors();
    }

    /**
     * @deprecated use {@link Solver#plugMonitor(ISearchMonitor)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void plugMonitor(FilteringMonitor filteringMonitor) {
        getSolver().plugMonitor(filteringMonitor);
    }

    /**
     * @deprecated use {@link Solver#getEventObserver()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public FilteringMonitor getEventObserver() {
        return getSolver().getEventObserver();
    }

    /**
     * @deprecated use {@link Solver#addStopCriterion(Criterion...)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void addStopCriterion(Criterion criterion){
        getSolver().addStopCriterion(criterion);
    }

    /**
     * @deprecated use {@link Solver#removeStopCriterion(Criterion...)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeStopCriterion(Criterion criterion){
        getSolver().removeStopCriterion(criterion);
    }

    /**
     * @deprecated use {@link Solver#removeAllStopCriteria()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeAllStopCriteria(){
        getSolver().removeAllStopCriteria();
    }

    /**
     * @deprecated use {@link Solver#getMeasures()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public IMeasures getMeasures() {
        return getSolver().getMeasures();
    }

    /**
     * @deprecated use {@link Solver#isSatisfied()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ESat isSatisfied() {
        return getSolver().isSatisfied();
    }

    /**
     * @deprecated use {@link Solver#getEngine()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public IPropagationEngine getEngine() {
        return getSolver().getEngine();
    }

    /**
     * @deprecated use {@link Solver#set(IPropagationEngine)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(IPropagationEngine propagationEngine) {
        getSolver().set(propagationEngine);
    }

    /**
     * @deprecated use {@link Solver#propagate()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void propagate() throws ContradictionException {
        getSolver().propagate();
    }

    /**
     * @deprecated use {@link Solver#isStopCriterionMet()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean hasReachedLimit() {
        return getSolver().isStopCriterionMet();
    }

    /**
     * @deprecated use {@link Solver#getObjectiveManager()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public ObjectiveManager getObjectiveManager() {
        return getSolver().getObjectiveManager();
    }

    /**
     * @deprecated use {@link Solver#getStrategy()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public AbstractStrategy getStrategy() {
        return getSolver().getStrategy();
    }

    /**
     * @deprecated use {@link Solver#set(AbstractStrategy[])} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(AbstractStrategy... strategies) {
        getSolver().set(strategies);
    }

    /**
     * @deprecated use {@link Solver#set(ObjectiveManager)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(ObjectiveManager om) {
        getSolver().set(om);
    }

    /**
     * @deprecated use {@link Solver#makeCompleteStrategy(boolean)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void makeCompleteSearch(boolean isComplete) {
        getSolver().makeCompleteStrategy(isComplete);
    }

    /**
     * @deprecated use {@link #getSolver()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public Solver getSearchLoop() {
        return getSolver();
    }

    /**
     * @deprecated use {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void setObjectives(Variable... objectives) {
        if(objectives == null){
            clearObjective();
        }else {
            throw new UnsupportedOperationException("please specify a resolution policy");
        }
    }

    /**
     * @deprecated use while({@link #solve()}) instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public long findAllSolutions() {
        clearObjective();
        long nbSols = 0;
        while (solve()){
            nbSols++;
        }
        return nbSols;
    }

    /**
     * @deprecated use {@link #solve()} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean findSolution() {
        clearObjective();
        return solve();
    }

    /**
     * @deprecated use {@link #solve()} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public boolean nextSolution() {
        return findSolution();
    }

    // INTS

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution) {
        setObjective(policy, getObjective());
        while(solve());
    }

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy) {
        findOptimalSolution(policy,true);
    }

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        findOptimalSolution(policy, true, objective);
    }

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution, IntVar objective) {
        setObjective(policy,objective);
        while(solve());
    }

    // REALS

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy, boolean restoreLastSolution, RealVar objective, double precision) {
        setObjective(policy,objective);
        setPrecision(precision);
        while(solve());
    }

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy, RealVar objective, double precision) {
        findOptimalSolution(policy,true,objective,precision);
    }

    // PARETO

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findParetoFront(ResolutionPolicy policy, boolean restoreLastSolution, IntVar... objectives) {
        throw new UnsupportedOperationException("Pareto not supported anymore");
    }

    /**
     * @deprecated use {@link #solve()} and {@link #setObjective(ResolutionPolicy, Variable)} instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findParetoFront(ResolutionPolicy policy, IntVar... objectives) {
        throw new UnsupportedOperationException("Pareto not supported anymore");
    }

    /**
     * @deprecated
     * if(twoSteps)
     *      do the two steps in your model (setObj / solve / reset / clearObj / while(solve))
     * else
     *      use change objective manager for non strict optimisation
     *      and use while({@link #solve()}) instead
     *
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void findAllOptimalSolutions(ResolutionPolicy policy, IntVar objective, boolean twoSteps) {
        throw new UnsupportedOperationException("Two-step resolution not supported anymore");
    }

    /**
     * @deprecated Will be removed in version > 3.4.0
     */
    @Deprecated
    public void set(Solver solver) {
        throw new UnsupportedOperationException();
    }
}
