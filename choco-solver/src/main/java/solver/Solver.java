/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver;

import gnu.trove.map.hash.TIntObjectHashMap;
import memory.Environments;
import memory.IEnvironment;
import org.slf4j.LoggerFactory;
import solver.constraints.Constraint;
import solver.constraints.nary.cnf.PropFalse;
import solver.constraints.nary.cnf.PropTrue;
import solver.constraints.nary.cnf.SatConstraint;
import solver.constraints.real.Ibex;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.ExplanationEngine;
import solver.objective.ObjectiveManager;
import solver.propagation.IPropagationEngine;
import solver.propagation.NoPropagationEngine;
import solver.propagation.hardcoded.SevenQueuesPropagatorEngine;
import solver.search.loop.AbstractSearchLoop;
import solver.search.measure.IMeasures;
import solver.search.measure.MeasuresRecorder;
import solver.search.solution.LastSolutionRecorder;
import solver.search.solution.Solution;
import solver.search.strategy.ISF;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.*;
import solver.variables.graph.GraphVar;
import solver.variables.impl.FixedBoolVarImpl;
import solver.variables.impl.FixedIntVarImpl;
import sun.reflect.Reflection;
import util.ESat;

import java.io.*;
import java.util.Arrays;

/**
 * The <code>Solver</code> is the header component of Constraint Programming.
 * It embeds the list of <code>Variable</code> (and their <code>Domain</code>), the <code>Constraint</code>'s network,
 * and a <code>IPropagationEngine</code> to pilot the propagation.<br/>
 * It reads default properties in {@link SolverProperties} (it can be overriden).<br/>
 * <code>Solver</code> includes a <code>AbstractSearchLoop</code> to guide the search loop: apply decisions and propagate,
 * run backups and rollbacks and store solutions.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.Constraint
 * @since 0.01
 */
public class Solver implements Serializable {

    private static final long serialVersionUID = 3L;

    private ExplanationEngine explainer;

    /**
     * Variables of the solver
     */
    Variable[] vars;
    int vIdx;

    /**
     * Constraints of the solver
     */
    Constraint[] cstrs;
    int cIdx;

    public TIntObjectHashMap<FixedIntVarImpl> cachedConstants;

    /**
     * Environment, based of the search tree (trailing or copying)
     */
    final IEnvironment environment;

    /**
     * Search loop of the solver
     */
    protected AbstractSearchLoop search;

    protected IPropagationEngine engine;

    /**
     * Solver's measures
     */
    protected final IMeasures measures;

    /**
     * Solver name
     */
    protected String name;

    /**
     * Problem feasbility:
     * - UNDEFINED if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    ESat feasible = ESat.UNDEFINED;

    protected long creationTime;

    protected int id = 1;

    /**
     * Two basic constraints TRUE and FALSE, cached to avoid multiple useless occurrences
     */
    public final Constraint TRUE, FALSE;

    /**
     * Two basic constants ZERO and ONE, cached to avoid multiple useless occurrences.
     */
    public final FixedBoolVarImpl ZERO, ONE;


    protected SatConstraint minisat;
    private Ibex ibex;

    /**
     * Create a solver object embedding a <code>environment</code>,  named <code>name</code> and with the specific set of
     * properties <code>solverProperties</code>.
     *
     * @param environment      a backtracking environment
     * @param name             a name
     * @param solverProperties default properties to load
     */
    public Solver(IEnvironment environment, String name, ISolverProperties solverProperties) {
        this.name = name;
        this.vars = new Variable[32];
        vIdx = 0;
        this.cstrs = new Constraint[32];
        cIdx = 0;
        this.environment = environment;
        this.measures = new MeasuresRecorder(this);
        solverProperties.loadPropertiesIn(this);
        this.creationTime -= System.nanoTime();
        this.cachedConstants = new TIntObjectHashMap<FixedIntVarImpl>(16, 1.5f, Integer.MAX_VALUE);
        this.engine = NoPropagationEngine.SINGLETON;
        ZERO = new FixedBoolVarImpl("0", 0, this);
        ONE = new FixedBoolVarImpl("1", 1, this);
        ZERO._setNot(ONE);
        ONE._setNot(ZERO);
        TRUE = new Constraint("TRUE cstr",new PropTrue(ONE));
        FALSE = new Constraint("FALSE cstr",new PropFalse(ZERO));
    }

    /**
     * Create a solver object with default parameters.
     * Default settings are declared in {@link SolverProperties}.
     */
    public Solver() {
        this(Environments.DEFAULT.make(),
                Reflection.getCallerClass(2).getSimpleName(),
                SolverProperties.DEFAULT);
    }

    /**
     * Create a solver object with default parameters, named <code>name</code>.
     * Default settings are declared in {@link SolverProperties}.
     */
    public Solver(String name) {
        this(Environments.DEFAULT.make(), name, SolverProperties.DEFAULT);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// GETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Returns the unique and internal seach loop.
     *
     * @return the unique and internal <code>AbstractSearchLoop</code> object.
     */
    public AbstractSearchLoop getSearchLoop() {
        return search;
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
     * Returns the array of declared <code>Variable</code> objects defined in this <code>Solver</code>.
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
     * Iterate over the variable of <code>this</code> and build an array that contains the IntVar only (including BoolVar).
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of IntVars of <code>this</code>
     */
    public IntVar[] retrieveIntVars() {
        IntVar[] ivars = new IntVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.INT) != 0) {
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
            if ((vars[i].getTypeAndKind() & Variable.BOOL) != 0) {
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
            if ((vars[i].getTypeAndKind() & Variable.SET) != 0) {
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
            if ((vars[i].getTypeAndKind() & Variable.REAL) != 0) {
                bvars[k++] = (RealVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Iterate over the variable of <code>this</code> and build an array that contains the GraphVar only.
     * It also contains FIXED variables and VIEWS, if any.
     *
     * @return array of SetVars of <code>this</code>
     */
    public GraphVar[] retrieveGraphVars() {
        GraphVar[] bvars = new GraphVar[vIdx];
        int k = 0;
        for (int i = 0; i < vIdx; i++) {
            if ((vars[i].getTypeAndKind() & Variable.GRAPH) != 0) {
                bvars[k++] = (GraphVar) vars[i];
            }
        }
        return Arrays.copyOf(bvars, k);
    }

    /**
     * Returns the array of declared <code>Constraint</code> objects defined in this <code>Solver</code>.
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
     */
    public String getName() {
        return name;
    }

    /**
     * Return the backtracking environment of <code>this</code>.
     */
    public IEnvironment getEnvironment() {
        return environment;
    }

    /**
     * Return a reference to the measures recorder.
     * This enables to get, for instance, the number of solutions found, time count, etc.
     */
    public IMeasures getMeasures() {
        return measures;
    }


    /**
     * Return the explanation engine plugged into <code>this</code>.
     */
    public ExplanationEngine getExplainer() {
        return explainer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// SETTERS ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Override the default search loop to use in <code>this</code>.
     *
     * @param searchLoop the search loop to use
     */
    public void set(AbstractSearchLoop searchLoop) {
        this.search = searchLoop;
    }

	/**
	 * Override the default search strategies to use in <code>this</code>.
	 * In case many strategies are given, they will be called in sequence:
	 * The first strategy in parameter is first called to compute a decision, if possible.
	 * If it cannot provide a new decision, the second strategy is called ...
	 * and so on, until the last strategy.
	 *
	 * <p/>
	 * <b>BEWARE:</b> the default strategy requires variables to be integer.
	 *
	 * @param strategies the search strategies to use.
	 */
	public void set(AbstractStrategy... strategies) {
		if(strategies==null || strategies.length==0){
			throw new UnsupportedOperationException("no search strategy has been specified");
		}
		if(strategies.length==1){
			search.set(strategies[0]);
		}else{
			search.set(ISF.sequencer(strategies));
		}

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
     * Override the explanation engine.
     */
    public void set(ExplanationEngine explainer) {
        this.explainer = explainer;
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
        if (getEngine() != null && getEngine().isInitialized()) {
            throw new SolverException("Solver does not support dynamic variable addition");
        }
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
     * @param variable
     */
    public void unassociates(Variable variable) {
        int idx = 0;
        for (; idx < vIdx; idx++) {
            if (variable == vars[idx]) break;
        }
        if (idx == vIdx) return;
        vars[idx] = vars[--vIdx];
    }

    /**
     * Post a constraint <code>c</code> in the constraints network of <code>this</code>:
     * - add it to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param c a Constraint
     */
    public void post(Constraint c) {
        _post(false, c);
    }

    /**
     * Post constraints <code>cs</code> in the constraints network of <code>this</code>:
     * - add them to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param cs Constraints
     */
    public void post(Constraint... cs) {
        _post(false, cs);
    }

    /**
     * Post a cut (permanent constraint) during the search has started, .
     *
     * @param c constraint to add
     */
    public void postCut(Constraint c) {
        _post(true, c);
    }


    private void _post(boolean cut, Constraint... cs) {
        boolean dynAdd = false;
        if (engine != NoPropagationEngine.SINGLETON && engine.isInitialized()) {
            dynAdd = true;
        }

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
        for (int i = 0; i < cs.length; i++) {
            if (dynAdd) {
                engine.dynamicAddition(cs[i], cut);
            }
            if (cs[i].isReified()) {
                try {
                    cs[i].reif().setToTrue(Cause.Null);
                } catch (ContradictionException e) {
                    throw new SolverException("post a constraint whose reification BoolVar is already set to false: no solution can exist");
                }
            }
        }
    }

    /**
     * Return a constraint embedding a minisat solver.
     * It is highly recommanded that there is only once instance of this constraint in a solver.
     * So a call to this method will create and post the constraint if it does not exist.
     *
     * @return the minisat constraint
     */
    public SatConstraint getMinisat() {
        if (minisat == null) {
            minisat = new SatConstraint(this);
            this.post(minisat);
        }
        return minisat;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// RELATED TO RESOLUTION //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns information on the feasibility of the current problem defined by the solver.
     * <p/>
     * Possible back values are:
     * <br/>- {@link ESat#TRUE}: a solution has been found,
     * <br/>- {@link ESat#FALSE}: the CSP has been proven to have no solution,
     * <br/>- {@link ESat#UNDEFINED}: no solution has been found so far (within given limits)
     * without proving the unfeasibility, though.
     *
     * @return an {@link ESat}.
     */
    public ESat isFeasible() {
        return feasible;
    }

    /**
     * Changes the current feasibility state of the <code>Solver</code> object.
     * <p/>
     * <b>Commonly called by the search loop, should not used without any knowledge of side effects.</b>
     *
     * @param feasible new state
     */
    public void setFeasible(ESat feasible) {
        this.feasible = feasible;
    }

    /**
     * Returns information on the completeness of the search process.
     * <p/>
     * A call to {@link #isFeasible()} may provide complementary information.
     * <p/>
     * Possible back values are:
     * <p/>
     * <br/>- <code>true</code> : the resolution is complete and
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findSolution()}: a solution has been found or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #nextSolution()}: a new solution has been found, or no more solutions exist.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findAllSolutions()}: all solutions have been found, or the CSP has been proven to be unsatisfiable.
     * <br/>&nbsp;&nbsp;&nbsp;* {@link #findOptimalSolution(ResolutionPolicy, solver.variables.IntVar)}: the optimal solution has been found and
     * proven to be optimal, or the CSP has been proven to be unsatisfiable.
     * <br/>- <code>false</code>: the resolution stopped after reaching a limit.
     */
    public boolean hasReachedLimit() {
        return search.hasReachedLimit();
    }

    /**
     * Attempts to find the first solution of the declared problem.
     * Then, following solutions can be found using {@link solver.Solver#nextSolution()}.
     * <p/>
     * An alternative is to call {@link solver.Solver#isFeasible()} which tells, whether or not, a solution has been found.
     *
     * @return <code>true</code> if and only if a solution has been found.
     */
    public boolean findSolution() {
        this.search.setObjectivemanager(new ObjectiveManager(null, ResolutionPolicy.SATISFACTION, false));
        solve(true);
        return measures.getSolutionCount() > 0;
    }


    /**
     * Once {@link Solver#findSolution()} has been called once, other solutions can be found using this method.
     * <p/>
     * The search is then resume to the last found solution point.
     *
     * @return a boolean stating whereas a new solution has been found (<code>true</code>), or not (<code>false</code>).
     */
    public boolean nextSolution() {
        long nbsol = measures.getSolutionCount();
        search.resume();
        return (measures.getSolutionCount() - nbsol) > 0;
    }

    /**
     * Attempts to find all solutions of the declared problem.
     *
     * @return the number of found solutions.
     */
    public long findAllSolutions() {
        this.search.setObjectivemanager(new ObjectiveManager(null, ResolutionPolicy.SATISFACTION, false));
        solve(false);
        return measures.getSolutionCount();
    }

    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     */
    public void findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) can not be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        this.search.setObjectivemanager(new ObjectiveManager<IntVar,Integer>(objective, policy, true));
        search.plugSearchMonitor(new LastSolutionRecorder(new Solution(), true, this));
        solve(false);
    }


    /**
     * Attempts optimize the value of the <code>objective</code> variable w.r.t. to the optimization <code>policy</code>.
     * Restores the best solution found so far (if any)
     *
     * @param policy    optimization policy, among ResolutionPolicy.MINIMIZE and ResolutionPolicy.MAXIMIZE
     * @param objective the variable to optimize
     */
    public void findOptimalSolution(ResolutionPolicy policy, RealVar objective, double precision) {
        if (policy == ResolutionPolicy.SATISFACTION) {
            throw new SolverException("Solver.findOptimalSolution(...) can not be called with ResolutionPolicy.SATISFACTION.");
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        this.search.setObjectivemanager(new ObjectiveManager<RealVar,Double>(objective, policy, precision, true));
        search.plugSearchMonitor(new LastSolutionRecorder(new Solution(), true, this));
        solve(false);
    }

    /**
     * This method should not be called externally. It launches the resolution process.
     */
    protected void solve(boolean stopAtFirst) {
        if (engine == NoPropagationEngine.SINGLETON) {
            this.set(new SevenQueuesPropagatorEngine(this));
        }
        measures.setReadingTimeCount(creationTime + System.nanoTime());
        search.launch(stopAtFirst);
    }

    /**
     * Propagate constraints and related events through the constraint network until a fix point is find, or a contradiction
     * is detected.
     *
     * @throws ContradictionException
     */
    public void propagate() throws ContradictionException {
        if (engine == NoPropagationEngine.SINGLETON) {
            this.set(new SevenQueuesPropagatorEngine(this));
        }
        engine.propagate();
    }

	/**
	 * Return the current state of the CSP.
	 * <p/>
	 * Given the current domains, it can return a value among:
	 * <br/>- {@link ESat#TRUE}: all constraints of the CSP are satisfied for sure,
	 * <br/>- {@link ESat#FALSE}: at least one constraint of the CSP is not satisfied.
	 * <br/>- {@link ESat#UNDEFINED}: neither satisfiability nor  unsatisfiability could be proven so far.
	 * <p/>
	 * Presumably, not all variables are instantiated.
	 */
    public ESat isSatisfied() {
        int OK = 0;
        for (int c = 0; c < cIdx; c++) {
            ESat satC = cstrs[c].isSatisfied();
            if (ESat.FALSE == satC) {
                if (LoggerFactory.getLogger("solver").isErrorEnabled()) {
                    LoggerFactory.getLogger("solver").error("FAILURE >> {} ({})", cstrs[c].toString(), satC);
                }
                return ESat.FALSE;
            } else if (ESat.TRUE == satC) {
                OK++;
            }
        }
        if (OK == cIdx) {
            return ESat.TRUE;
        } else {
            return ESat.UNDEFINED;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Return a string describing the CSP defined in <code>this</code>.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(256);
        st.append(String.format("\n Solver %s\n", name));
        st.append(String.format("\n[ %d vars -- %d cstrs ]\n", vIdx, cIdx));
        st.append(String.format("Feasability: %s\n", feasible));
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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// RELATED TO I/O ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Kicks off the serialization mechanism and flatten the {@code solver} into the given {@code file}.
     *
     * @param solver to flatten
     * @param file   scope file
     * @throws java.io.IOException if an I/O exception occurs.
     */
    public static void writeInFile(final Solver solver, final File file) throws IOException {
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(solver);
        out.close();
    }

    /**
     * Kicks off the serialization mechanism and flatten the {@code model} into a file
     * in the default temporary-file directory.
     *
     * @param solver to flatten
     * @return output file
     * @throws IOException if an I/O exception occurs.
     */
    public static File writeInFile(final Solver solver) throws IOException {
        final File file = File.createTempFile("SOLVER_", ".ser");
        FileOutputStream fos;
        ObjectOutputStream out;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(solver);
        out.close();
        return file;
    }


    /**
     * Restore flatten {@link Solver} from the given {@code file}.
     *
     * @param file input file
     * @return a {@link Solver}
     * @throws IOException            if an I/O exception occurs.
     * @throws ClassNotFoundException if wrong flattened object.
     */
    public static Solver readFromFile(final String file) throws IOException, ClassNotFoundException {
        FileInputStream fis;
        ObjectInputStream in;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Solver model = (Solver) in.readObject();
        in.close();
        return model;
    }

    /**
     * Cloning process based on serialization.
     * <p/>
     * Return a clone of <code>solver</code>.
     *
     * @param solver solver to clone.
     */
    public static Solver serializeClone(Solver solver) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(solver);
            out.close();
            byte[] buf = baos.toByteArray();

            ByteArrayInputStream bin = new ByteArrayInputStream(buf);
            ObjectInputStream in = new ObjectInputStream(bin);
            return (Solver) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <b>This methods should be called by the user.</b>
     */
    public int getNbIdElt() {
        return id;
    }

    /**
     * <b>This methods should be called by the user.</b>
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
}
