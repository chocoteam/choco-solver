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
package solver.propagation.generator;

import choco.kernel.common.util.tools.ArrayUtils;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.SolverException;
import solver.propagation.PropagationEngine;
import solver.propagation.comparators.predicate.All;
import solver.propagation.comparators.predicate.Predicate;
import solver.recorders.IEventRecorder;
import solver.recorders.coarse.AbstractCoarseEventRecorder;
import solver.recorders.coarse.CoarseEventRecorder;
import solver.recorders.conditions.ICondition;
import solver.recorders.fine.*;
import solver.search.loop.monitors.VariableClearing;
import solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/12/11
 */
public class Primitive<E extends IEventRecorder> extends Generator<IEventRecorder> {

    // informations concerning the objects to generate
    protected static final int arc = 1 << 1;
    protected static final int var = 1 << 2;
    protected static final int prop = 1 << 3;
    protected static final int fine = arc + var + prop;
    protected static final int coarse = 1 << 4;

    protected static final Variable[] var0 = new Variable[0];
    protected static final Propagator[] prop0 = new Propagator[0];

    protected final int mymask; // the mask of external elements declared

    protected final Variable[] variables;
    protected final Propagator[] propagators;
    protected final Predicate<E> predicate;
    protected final ICondition<E> condition;

    public Primitive(int mask, Variable[] variables, Propagator[] propagators, Predicate<E> predicate, ICondition<E> conditon) {
        super();
        this.mymask = mask;
        this.variables = variables;
        this.propagators = propagators;
        this.predicate = predicate;
        this.condition = conditon;
    }

    /**
     * Creates event recorders and marks them as created in the propagation engine (removes water marks).
     *
     * @param propagationEngine a propagation engine
     * @param solver            a solver
     */
    @Override
    public List<IEventRecorder> populate(PropagationEngine propagationEngine, Solver solver) {
        List<IEventRecorder> elements = new ArrayList<IEventRecorder>();
        // first fine events
        if ((mymask & fine) != 0) {
            if ((mymask & arc) != 0) { // build arc event recorders
                elements.addAll(_arcs(propagationEngine, solver));
            } else if ((mymask & var) != 0) { // build var event recorders
                elements.addAll(_vars(propagationEngine, solver));
            } else if ((mymask & prop) != 0) { // build prop event recorders
                elements.addAll(_props(propagationEngine, solver));
            }
        }
        if ((mymask & coarse) != 0) {// build unary coarse event recorders
            elements.addAll(_unaries(propagationEngine, solver));
        }
        return elements;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<IEventRecorder> _arcs(PropagationEngine propagationEngine, final Solver solver) {
        final List<IEventRecorder> all = new ArrayList<IEventRecorder>();
        if (propagators.length > 0) {
            if (variables.length > 0) {
                throw new SolverException("Not yet implemented.");
            } else {
                for (int p = 0; p < propagators.length; p++) {
                    Propagator prop = propagators[p];
                    for (int v = 0; v < prop.getNbVars(); v++) {
                        Variable var = prop.getVar(v);
                        if (propagationEngine.isMarked(prop.getId(), var.getId(), v)) {
                            all.add(arc(var, prop, v, solver));
                            propagationEngine.clearWatermark(prop.getId(), var.getId(), v);
                        }
                    }
                }
            }
        } else {
            if (variables.length > 0) {
                for (int v = 0; v < variables.length; v++) {
                    Variable var = variables[v];
                    Propagator[] propagators = var.getPropagators();
                    int[] pindices = var.getPIndices();
                    for (int p = 0; p < propagators.length; p++) {
                        Propagator prop = propagators[p];
                        if (propagationEngine.isMarked(prop.getId(), var.getId(), pindices[p])) {
                            all.add(arc(var, prop, pindices[p], solver));
                            propagationEngine.clearWatermark(prop.getId(), var.getId(), pindices[p]);
                        }
                    }
                }
            } else {
                LoggerFactory.getLogger(Primitive.class).info("empty primitive creation");
            }
        }
        if (!ArcEventRecorder.LAZY) {
            solver.getSearchLoop().plugSearchMonitor(
                    new VariableClearing(solver, all.toArray(new IEventRecorder[all.size()]))
            );
        }
        return all;
    }

    private List<IEventRecorder> _vars(PropagationEngine propagationEngine, final Solver solver) {
        final List<IEventRecorder> all = new ArrayList<IEventRecorder>();
        if (propagators.length > 0) {
            if (variables.length > 0) {
                throw new SolverException("Not yet implemented.");
            } else {
                throw new SolverException("Not yet implemented.");
            }
        } else {
            if (variables.length > 0) {
                for (int v = 0; v < variables.length; v++) {
                    Variable var = variables[v];
                    Propagator[] propagators = var.getPropagators();
                    int[] pindices = var.getPIndices();
                    for (int p = 0; p < propagators.length; p++) {
                        Propagator prop = propagators[p];
                        if (propagationEngine.isMarked(prop.getId(), var.getId(), pindices[p])) {
                            propagationEngine.clearWatermark(prop.getId(), var.getId(), pindices[p]);
                        } else {
                            throw new SolverException("Double marking tuple!");
                        }
                    }
                    all.add(var(var, propagators, pindices, solver));
                }
            } else {
                LoggerFactory.getLogger(Primitive.class).info("empty primitive creation");
            }
        }
        if (!ArcEventRecorder.LAZY) {
            solver.getSearchLoop().plugSearchMonitor(
                    new VariableClearing(solver, all.toArray(new IEventRecorder[all.size()]))
            );
        }
        return all;
    }

    private List<IEventRecorder> _props(PropagationEngine propagationEngine, final Solver solver) {
        final List<IEventRecorder> all = new ArrayList<IEventRecorder>();
        if (propagators.length > 0) {
            if (variables.length > 0) {
                throw new SolverException("Not yet implemented.");
            } else {
                for (int p = 0; p < propagators.length; p++) {
                    Propagator prop = propagators[p];
                    int nbv = prop.getNbVars();
                    Variable[] vars = new Variable[nbv];
                    int[] pindices = new int[nbv];
                    for (int v = 0; v < nbv; v++) {
                        Variable var = prop.getVar(v);
                        pindices[v] = v;
                        vars[v] = var;
                        if (propagationEngine.isMarked(prop.getId(), var.getId(), v)) {
                            propagationEngine.clearWatermark(prop.getId(), var.getId(), v);
                        } else {
                            throw new SolverException("Double marking tuple!");
                        }
                    }
                    all.add(prop(vars, prop, pindices, solver));
                }
            }
        } else {
            if (variables.length > 0) {
                throw new SolverException("Not yet implemented.");
            } else {
                LoggerFactory.getLogger(Primitive.class).info("empty primitive creation");
            }
        }
        if (!ArcEventRecorder.LAZY) {
            solver.getSearchLoop().plugSearchMonitor(
                    new VariableClearing(solver, all.toArray(new IEventRecorder[all.size()]))
            );
        }
        return all;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected <V extends Variable> IEventRecorder arc(V var, Propagator<V> prop, int idVinP, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            return new ArcEventRecorderWithCondition<V>(var, prop, idVinP, condition, solver);
        } else {
            return new ArcEventRecorder<V>(var, prop, idVinP, solver);
        }
        //}
    }

    protected <V extends Variable> IEventRecorder var(V var, Propagator<V>[] prop, int[] pindices, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            throw new SolverException("var cannot handle condition");
        } else {
            return new VarEventRecorder<V>(var, prop, pindices, solver);
        }
        //}
    }

    protected <V extends Variable> IEventRecorder f2c_var(V var, Propagator<V>[] props, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            throw new SolverException("f2c_var cannot handle condition");
        } else {
            return new Fine2CoarseVarEventRecorder<V>(var, props, solver);
        }
        //}
    }

    protected <V extends Variable> IEventRecorder prop(V[] vars, Propagator<V> prop, int[] pindices, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            throw new SolverException("var cannot handle condition");
        } else {
            return new PropEventRecorder<V>(vars, prop, pindices, solver);
        }
        //}
    }

    protected <V extends Variable> IEventRecorder f2c_prop(V[] vars, Propagator<V> propagator, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            throw new SolverException("f2c_prop cannot handle condition");
        } else {
            return new Fine2CoarsePropEventRecorder<V>(vars, propagator, solver);
        }
        //}
    }


    private List<AbstractCoarseEventRecorder> _unaries(PropagationEngine propagationEngine, Solver solver) {
        List<AbstractCoarseEventRecorder> all = new ArrayList<AbstractCoarseEventRecorder>();
        if (propagators.length > 0) {
            for (int p = 0; p < propagators.length; p++) {
                Propagator prop = propagators[p];
                if (propagationEngine.isMarked(prop.getId(), 0, 0)) {
                    all.add(_unary(prop, solver));
                    propagationEngine.clearWatermark(prop.getId(), 0, 0);
                }
            }
        } else {
            LoggerFactory.getLogger(Primitive.class).info("empty primitive creation");
        }
        return all;
    }

    protected AbstractCoarseEventRecorder _unary(Propagator prop, Solver solver) {
        //if(predicate.eval()){
        if (condition != null) {
            //cpru :
            return new CoarseEventRecorder(prop, solver);
        } else {
            return new CoarseEventRecorder(prop, solver);
        }
        //}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //<---- ARC-ORIENTED

    public static Primitive<ArcEventRecorder> arcs(Constraint... constraints) {
        Propagator[] propagators = prop0;
        for (int c = 0; c < constraints.length; c++) {
            propagators = ArrayUtils.append(propagators, constraints[c].propagators);
        }
        return new Primitive<ArcEventRecorder>(arc, var0, propagators, All.singleton, null);
    }

    public static Primitive<ArcEventRecorder> arcs(Propagator... propagators) {
        return new Primitive<ArcEventRecorder>(arc, var0, propagators, All.singleton, null);
    }

    public static Primitive<ArcEventRecorderWithCondition> arcs(ICondition<ArcEventRecorderWithCondition> condition, Constraint... constraints) {
        Propagator[] propagators = prop0;
        for (int c = 0; c < constraints.length; c++) {
            propagators = ArrayUtils.append(propagators, constraints[c].propagators);
        }
        return new Primitive<ArcEventRecorderWithCondition>(arc, var0, propagators, All.singleton, condition);
    }

    //---->
    //<---- VARIABLE-ORIENTED

    public static Primitive<VarEventRecorder> vars(Variable... variables) {
        return new Primitive<VarEventRecorder>(var, variables, prop0, All.singleton, null);
    }

    //---->
    //<---- PROPAGATOR-ORIENTED

    public static Primitive<PropEventRecorder> props(Constraint... constraints) {
        Propagator[] propagators = prop0;
        for (int c = 0; c < constraints.length; c++) {
            propagators = ArrayUtils.append(propagators, constraints[c].propagators);
        }
        return new Primitive<PropEventRecorder>(prop, var0, propagators, All.singleton, null);
    }

    public static Primitive<PropEventRecorder> props(Propagator... propagators) {
        return new Primitive<PropEventRecorder>(prop, var0, propagators, All.singleton, null);
    }

    //---->
    //<---- COARSE UNARY

    public static Primitive<CoarseEventRecorder> coarses(Constraint... constraints) {
        Propagator[] propagators = prop0;
        for (int c = 0; c < constraints.length; c++) {
            propagators = ArrayUtils.append(propagators, constraints[c].propagators);
        }
        return new Primitive<CoarseEventRecorder>(coarse, var0, propagators, All.singleton, null);
    }

    public static Primitive<CoarseEventRecorder> coarses(Propagator... propagators) {
        return new Primitive<CoarseEventRecorder>(coarse, var0, propagators, All.singleton, null);
    }

    //---->
    //<---- COARSE NARY
    //---->


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
