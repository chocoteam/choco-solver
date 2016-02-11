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

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.loop.monitors.IMonitorClose;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * <p>
 *     A parallel resolution helper.
 * </p>
 * <p>
 *     The parallel resolution of a problem is made of four steps:
 *      <ol>
 *          <li>adding models to be run in parallel,</li>
 *          <li>running resolution in parallel,</li>
 *          <li>getting the model which finds a solution (or the best one), if any.</li>
 *      </ol>
 *      Each of the four steps is needed and the order is imposed too.
 *      In particular, in step 1. each model should be populated individually with a model of the problem
 *      (presumably the same model, but not required).
 *      Populating model is not managed by this class and should be done before applying step 2.,
 *      with a dedicated method for instance.
 *      </br>
 *      Note also that there should not be pending resolution process in any models.
 *      Otherwise, unexpected behaviors may occur.
 * </p>
 * <p>
 *     The resolution process is synchronized. As soon as one model ends (naturally or by hitting a limit)
 *     the other ones are eagerly stopped.
 *     Moreover, when dealing with an optimization problem, cut on the objective variable's value is propagated
 *     to all models on solution.
 *     It is essential to eagerly declare the objective variable(s) with {@link Model#setObjectives(ResolutionPolicy, Variable...)}.
 *
 * </p>
 * <p>
 *     Note that the similarity of the models declared is not required.
 *     However, when dealing with an optimization problem, keep in mind that the cut on the objective variable's value
 *     is propagated among all models, so different objectives may lead to wrong results.
 * </p>
 * <p>
 *     Since there is no condition on the similarity of the models, this API does not rely on
 *     shared {@link org.chocosolver.solver.search.solution.ISolutionRecorder}.
 *     So then, once the resolution ends, the model which finds the (best) solution is internally stored.
 * </p>
 * <p>
 *     Example of use.
 *
 * <pre>
 * <code>ParallelResolution pares = new ParallelResolution();
 * int n = 4; // number of models to use
 * for (int i = 0; i < n; i++) {
 *      pares.addModel(modeller());
 * }
 * pares.solve();
 * Chatterbox.printSolutions(pares.getBestModel());
 * </code>
 * </pre>
 *
 * </p>
 * <p>
 *     This class uses Java 8 streaming feature, and may be not compliant with older versions.
 * </p>
 *
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 23/12/2015.
 */
public class ParallelResolution {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       VARIABLES       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /** List of {@link Model}s to be executed in parallel. */
    private final List<Model> models;

    /** Stores whether or not prepare() method has been called */
    private boolean isPrepared = false;

    /** Integer which stores the number of ending models. Needed for synchronization purpose. */
    private final AtomicInteger finishers = new AtomicInteger(0);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////      CONSTRUCTOR      //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new instance of this parallel resolution helper.
     * This class stores the models to be executed in parallel in a {@link LinkedList} initially empty.
     */
    public ParallelResolution() {
        this.models = new LinkedList<>();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////          API          //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * <p>
     * Adds a model to the list of models to run in parallel.
     * The model can either be a fresh one, ready for populating, or a populated one.
     * </p>
     * <p>
     *     <b>Important:</b>
     *  <ul>
     *      <li>the populating process is not managed by this parallel resolution helper
     *  and should be done externally, with a dedicated method for example.
     *  </li>
     *  <li>
     *      when dealing with optimization problems, the objective variables <b>HAVE</b> to be declared eagerly with
     *      {@link Model#setObjectives(ResolutionPolicy, Variable...)}.
     *  </li>
     *  </ul>
     *
     * </p>
     * @param model a model to add
     */
    public void addModel(Model model){
        this.models.add(model);
    }

    /**
     * Run the solve() instruction of every model of the portfolio in parallel.
     *
     * <p>
     * Note that a call to {@link #getBestModel()} returns a model which has found the best solution.
     * </p>
     * @return <code>true</code> if and only if at least one new solution has been found.
     * @throws SolverException if no model or only model has been added.
     */
    public boolean solve() {
        if(!isPrepared){ prepare(); }
        long nsol = 0;
        for (Model s : models) { nsol -= s.getResolver().getMeasures().getSolutionCount(); }
        models.parallelStream().forEach(Model::solve);
        for (Model s : models) { nsol += s.getResolver().getMeasures().getSolutionCount(); }
        return nsol > 0;
    }

    /**
     * Returns the first model from the list which, either :
     * <ul>
     *     <li>
     *         finds a solution when dealing with a satisfaction problem,
     *     </li>
     *     <li>
     *         or finds (and possibly proves) the best solution when dealing with an optimization problem.
     *     </li>
     * </ul>
     * or <tt>null</tt> if no such model exists.
     * Note that there can be more than one "finder" in the list, yet, this method returns the index of the first one.
     *
     * @return the first model which finds a solution (or the best one) or <tt>null</tt> if no such model exists.
     */
    public Model getBestModel(){
        ResolutionPolicy policy = models.get(0).getResolver().getObjectiveManager().getPolicy();
        check(policy);
        if (policy == ResolutionPolicy.SATISFACTION) {
            for (Model s : models) {
                if (s.getResolver().getMeasures().getSolutionCount() > 0) {
                    return s;
                }
            }
            return null;
        }else{
            boolean min = models.get(0).getResolver().getObjectiveManager().getPolicy() == ResolutionPolicy.MINIMIZE;
            Model best = null;
            int cost = 0;
            for (Model s : models) {
                if (s.getResolver().getMeasures().getSolutionCount() > 0) {
                    int solVal = s.getSolutionRecorder().getLastSolution().getIntVal((IntVar)s.getObjectives()[0]);
                    if (best == null
                            || (cost > solVal && min)
                            || (cost < solVal && !min)) {
                        best = s;
                        cost = solVal;
                    }
                }
            }
            return best;
        }
    }

    /**
     * @return the (mutable!) list of models used in this parallel resolution helper.
     */
    public List<Model> getModels(){
        return models;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////   INTERNAL METHODS    //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void prepare(){
        isPrepared = true;
        ResolutionPolicy policy = models.get(0).getResolutionPolicy();
        check(policy);
        models.stream().forEach(s -> s.getResolver().addStopCriterion(()->finishers.get()>0));
        models.stream().forEach(s -> s.getResolver().plugMonitor(new IMonitorClose() {
            @Override
            public void afterClose() {
                int count = finishers.addAndGet(1);
                if(count == models.size()){
                    finishers.set(0); //reset the counter to 0
                }
            }
        }));
        if(policy != ResolutionPolicy.SATISFACTION){
            // share the best known bound
            models.stream().forEach(s -> s.getResolver().plugMonitor(
                    (IMonitorSolution) () -> {
                        synchronized (s.getResolver().getObjectiveManager()) {
                            switch (s.getResolver().getObjectiveManager().getPolicy()) {
                                case MAXIMIZE:
                                    Number lb = s.getResolver().getObjectiveManager().getBestSolutionValue();
                                    models.forEach(s1 -> s1.getResolver().getObjectiveManager().updateBestLB(lb));
                                    break;
                                case MINIMIZE:
                                    int ub = s.getResolver().getObjectiveManager().getBestSolutionValue().intValue();
                                    models.forEach(s1 -> s1.getResolver().getObjectiveManager().updateBestUB(ub));
                                    break;
                                case SATISFACTION:
                                    break;
                            }
                        }
                    }
            ));
        }
    }

    private void check(ResolutionPolicy policy){
        if (models.size() <= 1) {
            throw new SolverException("Try to run " + models.size() + " model in parallel (should be >1).");
        }
        if(policy != ResolutionPolicy.SATISFACTION) {
            Variable[] os = models.get(0).getObjectives();
            if (os == null) {
                throw new UnsupportedOperationException("No objective has been defined");
            }
            if (!(os.length == 1 && (os[0].getTypeAndKind() & Variable.INT) != 0)) {
                throw new UnsupportedOperationException("ParallelResolution cannot deal with multi-objective or " +
                        "real variable objective optimization problems");
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////       TO REMOVE       //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
     * @deprecated use {@link #solve()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public boolean findSolution() {
        return solve();
    }

    /**
     * @deprecated use {@link #solve()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public void findOptimalSolution(ResolutionPolicy policy) {
        solve();
    }

    /**
     * @deprecated use {@link #addModel(Model)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void addSolver(Solver s){
        addModel(s);
    }

    /**
     * @deprecated use {@link #getModel(int)}  instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public Solver getSolver(int i){
        return (Solver)getModel(i);
    }

    /**
     * @deprecated use {@link #removeModel(Model)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeSolver(Solver s){
        removeModel(s);
    }

    /**
     * @deprecated use {@link #getModels().get(int)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public Model getModel(int index){
        return models.get(index);
    }

    /**
     * @deprecated use {@link #getModels().getSize()} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public int size(){
        return models.size();
    }

    /**
     * @deprecated use {@link #getModels().remove(Model)} instead
     * Will be removed in version > 3.4.0
     */
    @Deprecated
    public void removeModel(Model model){
        this.models.remove(model);
    }
}
