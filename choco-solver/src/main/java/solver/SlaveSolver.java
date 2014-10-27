/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

import solver.objective.ObjectiveManager;
import solver.search.loop.monitors.IMonitorSolution;
import solver.thread.AbstractParallelSlave;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 27/10/14
 */
public class SlaveSolver extends AbstractParallelSlave<MasterSolver> {

    Solver solver;
    ResolutionPolicy policy;
    IntVar objective;

    /**
     * Create a slave born to be mastered and work in parallel
     *
     * @param master
     * @param id     slave unique name
     */
    public SlaveSolver(MasterSolver master, int id, Solver solver) {
        this(master, id, solver, ResolutionPolicy.SATISFACTION, null);
    }

    /**
     * Create a slave born to be mastered and work in parallel
     *
     * @param master    the driver
     * @param id        slave unique name
     * @param solver    the driven solver
     * @param policy    the resolution policy
     * @param objective the objective variable (can be null)
     */
    public SlaveSolver(MasterSolver master, int id, Solver solver, ResolutionPolicy policy, IntVar objective) {
        super(master, id);
        this.solver = solver;
        this.policy = policy;
        this.objective = objective;
    }


    @Override
    public void work() {
        solver.plugMonitor((IMonitorSolution) () -> {
            ObjectiveManager om = solver.getSearchLoop().getObjectiveManager();
            int val = om.getPolicy() == ResolutionPolicy.SATISFACTION ? 1 : om.getBestSolutionValue().intValue();
            master.onSolution(val);
        });
        if (policy.equals(ResolutionPolicy.SATISFACTION)) {
            solver.findSolution();
            if (!solver.hasReachedLimit()) {
                master.closeWithSuccess();
            }
        } else {
            solver.findOptimalSolution(policy, objective);
            if (!solver.hasReachedLimit()) {
                master.closeWithSuccess();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void findBetterThan(int val, ResolutionPolicy policy) {
        if (solver == null) return;// can happen if a solution is found before this thread is fully ready
        ObjectiveManager<IntVar, Integer> iom = solver.getSearchLoop().getObjectiveManager();
        if (iom == null) return;// can happen if a solution is found before this thread is fully ready
        switch (policy) {
            case MAXIMIZE:
                iom.updateBestLB(val);
                break;
            case MINIMIZE:
                iom.updateBestUB(val);
                break;
            case SATISFACTION:
                // nothing to do
                break;
        }
    }

    public void stop(){
        solver.getSearchLoop().forceAlive(false);
    }
}
