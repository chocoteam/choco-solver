/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver;

import choco.kernel.ESat;
import choco.kernel.ResolutionPolicy;
import choco.kernel.memory.Environments;
import choco.kernel.memory.IEnvironment;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.LoggerFactory;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.explanations.ExplanationEngine;
import solver.objective.MaxObjectiveManager;
import solver.objective.MinObjectiveManager;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.PropagationStrategies;
import solver.search.loop.AbstractSearchLoop;
import solver.search.measure.IMeasures;
import solver.search.measure.MeasuresRecorder;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.VariableFactory;
import solver.variables.view.ConstantView;
import sun.reflect.Reflection;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

/**
 * The <code>Solver</code> is the header component of Constraint Programming.
 * It embeds the list of <code>Variable</code> (and their <code>Domain</code>), the <code>Constraint</code>'s network,
 * and a <code>IPropagationEngine</code> to pilot the propagation.<br/>
 * It reads default properties in <code>/solver.properties</code> (it can be overriden).<br/>
 * <code>Solver</code> includes a <code>AbstractSearchLoop</code> to guide the search loop: apply decisions and propagate,
 * run backups and rollbacks and store solutions.
 *
 * @author Xavier Lorca
 * @author Charles Prud'homme
 * @version 0.01, june 2010
 * @see solver.variables.Variable
 * @see solver.constraints.Constraint
 * @see solver.propagation.PropagationEngine
 * @see choco.kernel.memory.IEnvironment
 * @see solver.search.loop.AbstractSearchLoop
 * @since 0.01
 */
public class Solver implements Serializable {


    private static final long serialVersionUID = 3L;

    /**
     * Properties of the solver
     */
    public Properties properties;

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

    public TIntObjectHashMap<ConstantView> cachedConstants;

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
     * - NULL if unknown,
     * - TRUE if satisfiable,
     * - FALSE if unsatisfiable
     */
    Boolean feasible;

    protected long creationTime;

    protected int id = 1;

    public Solver(IEnvironment environment, String name, ISolverProperties solverProperties) {
        loadProperties();
        this.name = name;
        this.vars = new Variable[32];
        vIdx = 0;
        this.cstrs = new Constraint[32];
        cIdx = 0;
        this.environment = environment;
        this.measures = new MeasuresRecorder(this);
        solverProperties.loadPropertiesIn(this);
        this.creationTime -= System.nanoTime();
        this.cachedConstants = new TIntObjectHashMap<ConstantView>(16, 1.5f, Integer.MAX_VALUE);
    }

    public Solver() {
        this(Environments.DEFAULT.make(),
                Reflection.getCallerClass(2).getSimpleName(),
                SolverProperties.DEFAULT);
    }

    public Solver(String name) {
        this(Environments.DEFAULT.make(), name, SolverProperties.DEFAULT);
    }


    private void loadProperties() {
        try {
            properties = new Properties();
            InputStream is = getClass().getResourceAsStream("/solver.properties");
            properties.load(is);
        } catch (IOException e) {
            throw new SolverException("Could not open solver.properties");
        }
    }

    public void setSearch(AbstractSearchLoop searchLoop) {
        this.search = searchLoop;
    }

    public void set(AbstractStrategy strategies) {
        this.search.set(strategies);
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
     * Post a constraint <code>c</code> in the constraints network of <code>this</code>:
     * - add it to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param c a Constraint
     */
    public void post(Constraint c) {
        if (cIdx == cstrs.length) {
            Constraint[] tmp = cstrs;
            cstrs = new Constraint[tmp.length * 2];
            System.arraycopy(tmp, 0, cstrs, 0, cIdx);
        }
        cstrs[cIdx++] = c;
        c.declare();
    }

    /**
     * Post constraints <code>cs</code> in the constraints network of <code>this</code>:
     * - add them to the data structure,
     * - set the fixed idx,
     * - checks for restrictions
     *
     * @param cs Constraints
     */
    public void post(Constraint[] cs) {
        while (cIdx + cs.length >= cstrs.length) {
            Constraint[] tmp = cstrs;
            cstrs = new Constraint[tmp.length * 2];
            System.arraycopy(tmp, 0, cstrs, 0, cIdx);
        }
        System.arraycopy(cs, 0, cstrs, cIdx, cs.length);
        cIdx += cs.length;
        for (int i = 0; i < cs.length; i++) {
            cs[i].declare();
        }
    }

    public void post(Constraint c, Constraint... cs) {
        while (cIdx + cs.length + 1 >= cstrs.length) {
            Constraint[] tmp = cstrs;
            cstrs = new Constraint[tmp.length * 2];
            System.arraycopy(tmp, 0, cstrs, 0, cIdx);
        }
        cstrs[cIdx++] = c;
        c.declare();

        System.arraycopy(cs, 0, cstrs, cIdx, cs.length);
        cIdx += cs.length;
        for (int i = 0; i < cs.length; i++) {
            cs[i].declare();
        }
    }

    public IEnvironment getEnvironment() {
        return environment;
    }

    public IMeasures getMeasures() {
        return measures;
    }

    /**
     * Attempts to find the first solution of the declared problem.
     * Main steps are:
     * <ul>
     * <li>setting up the configuration,</li>
     * <li>initializing the <code>Environment</code>,</li>
     * <li>running the initial propagation,</li>
     * <li>if necessary, launching the search</li>
     * </ul>
     * <p/>
     * Then, following solutions can be found using <code>nextSolution()</code>.
     *
     * @return the number of found solutions.
     */
    public Boolean findSolution() {
        search.stopAtFirstSolution(true);
        return solve();
    }


    /**
     * Once <code>findSolution()</code> has been called once, other solutions can be found using
     * <code>nextSolution()</code>. The search is then resume to the last found solution point.
     * Beware: limits are ignored!
     *
     * @return a Boolean stating whereas a new solution has been found (<code>TRUE</code>),
     *         a limit has been encountered (<code>NULL</code>), ne new solution (<code>FALSE</code>).
     */
    public boolean nextSolution() {
        return search.resume();
    }

    /**
     * Attempts to find all solutions of the declared problem.
     * Main steps are:
     * <ul>
     * <li>setting up the configuration,</li>
     * <li>initializing the <code>Environment</code>,</li>
     * <li>running the initial propagation,</li>
     * <li>if necessary, launching the search</li>
     * </ul>
     *
     * @return the number of found solutions.
     */
    public Boolean findAllSolutions() {
        search.stopAtFirstSolution(false);
        return solve();
    }

    public Boolean findOptimalSolution(ResolutionPolicy policy, IntVar objective) {
        search.stopAtFirstSolution(false);
        //search.setSolutionPoolCapacity(1);
        if (search.getSolutionPoolCapacity() < 1) {
//            LoggerFactory.getLogger("solver").warn("Solver: capacity of solution pool is set to 1.");
            search.setSolutionPoolCapacity(1);
        }
        if (objective == null) {
            throw new SolverException("No objective variable has been defined");
        }
        switch (policy) {
            case MAXIMIZE:
                MaxObjectiveManager maom = new MaxObjectiveManager(objective);
                maom.setMeasures(this.measures);
                this.search.setObjectivemanager(maom);
                break;
            case MINIMIZE:
                MinObjectiveManager miom = new MinObjectiveManager(objective);
                miom.setMeasures(this.measures);
                this.search.setObjectivemanager(miom);
                break;
        }
        return solve();
    }

    public Boolean solve() {
        if (engine == null) {
            IPropagationEngine engine = new PropagationEngine(environment);
            PropagationStrategies.DEFAULT.make(this, engine);
            this.set(engine);
        }
        if (search.getStrategy() == null) {
            LoggerFactory.getLogger("solver").info("Set default search strategy: Dow/WDeg");
            set(StrategyFactory.domwdegMindom(VariableFactory.toIntVar(getVars()), this));
        }
        measures.setReadingTimeCount(creationTime + System.nanoTime());
        search.setup();
        return search.launch();
    }

    public void propagate() throws ContradictionException {
        if (engine == null) {
            IPropagationEngine engine = new PropagationEngine(environment);
            PropagationStrategies.DEFAULT.make(this, engine);
            this.set(engine);
        }
        if (!engine.initialized()) {
            engine.init(this);
        }
        engine.propagate();
    }

    /**
     * Returns the unique and internal seach loop.
     *
     * @return the unique and internal <code>AbstractSearchLoop</code> object.
     */
    public AbstractSearchLoop getSearchLoop() {
        return search;
    }

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


    public int getNbVars() {
        return vIdx;
    }

    public Variable getVar(int i) {
        return vars[i];
    }

    /**
     * Returns the array of declared <code>Constraint</code> objects defined in this <code>Solver</code>.
     *
     * @return array of constraints
     */
    public Constraint[] getCstrs() {
        return Arrays.copyOf(cstrs, cIdx);
    }

    public int getNbCstrs() {
        return cIdx;
    }

    /**
     * Returns information on the feasability of the current problem defined by the solver.
     * <p/>
     * Possible back values are:
     * <ul>
     * <li>TRUE : the problem is feasible, at least one solution has already been found,</li>
     * <li>FALSE: the problem has been proven to be infeasible,</li>
     * <li>NULL: nothing can state on the feasibility, no solution has been found yet
     * and the search is not ended.</li>
     * </ul>
     *
     * @return a Boolean
     */
    public Boolean isFeasible() {
        return feasible;
    }


    public String getName() {
        return name;
    }

    /**
     * Changes the current feasability state of the <code>Solver</code> object.
     *
     * @param feasible new state
     */
    public void setFeasible(Boolean feasible) {
        this.feasible = feasible;
    }

    public ESat isSatisfied() {
        ESat check = ESat.TRUE;
        for (int c = 0; c < cIdx; c++) {
            ESat satC = cstrs[c].isSatisfied();
            if (!ESat.TRUE.equals(satC)) {
                if (LoggerFactory.getLogger("solver").isErrorEnabled()) {
                    LoggerFactory.getLogger("solver").error("FAILURE >> {} ({})", cstrs[c].toString(), satC);
                }
                check = ESat.FALSE;
            }
        }
        return check;
    }

    public ESat isEntailed() {
        ESat check = ESat.TRUE;
        for (int c = 0; c < cIdx; c++) {
            ESat satC = cstrs[c].isEntailed();
            if (!ESat.TRUE.equals(satC)) {
                if (LoggerFactory.getLogger("solver").isErrorEnabled()) {
                    LoggerFactory.getLogger("solver").error("FAILURE >> {} ({})", cstrs[c].toString(), satC);
                }
                check = ESat.FALSE;
            }
        }
        return check;
    }

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

    public int getNbIdElt() {
        return id;
    }

    public int nextId() {
        return id++;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    /**
     * Explanation engine for the solver
     */
    public ExplanationEngine getExplainer() {
        return explainer;
    }

    public void setExplainer(ExplanationEngine explainer) {
        this.explainer = explainer;
    }

    public void set(ExplanationEngine explainer) {
        this.setExplainer(explainer);
    }


}
