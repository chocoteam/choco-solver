/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;

/**
 * Class which stores the value of each variable in a solution
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 05/06/2013
 */
public class Solution implements ICause {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    /** No entry value for maps */
    private static final int NO_ENTRY = Integer.MAX_VALUE;

    // SOLUTION
    /** Set to <tt>true</tt> when this object is empty */
    private boolean empty;
    /** Maps of value for integer variable (id - value) */
    private TIntIntHashMap intmap;
    /** Maps of value for real variable (id - value) */
    private TIntObjectHashMap<double[]> realmap;
    /** Maps of value for set variable (id - values) */
    private TIntObjectHashMap<int[]> setmap;

    // INPUT
    /** Model to store */
    private Model model;
    /** Variables to store; */
    private Variable[] varsToStore;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    /**
     * Create an empty solution object
     * able to store the value of each variable in <code>varsToStore</code> when calling <code>record()</code>
     *
     * Stores all variables by default, when <code>varsToStore</code> is empty
     *
     * @param model model of the solution
     * @param varsToStore variables to store in this object
     */
    public Solution(Model model, Variable... varsToStore) {
        this.varsToStore = varsToStore;
        empty = true;
        this.model = model;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Records the current solution of the solver
     * clears all previous recordings
     * @return this object
     */
    public Solution record() {
        empty = false;
        boolean warn = false;
        if (varsToStore.length == 0) {
            varsToStore = model.getVars();
        }
        assert varsToStore.length > 0;
        if (intmap != null) {
            intmap.clear();
        }
        if (realmap != null) {
            realmap.clear();
        }
        if (setmap != null) {
            setmap.clear();
        }
        for (Variable var : varsToStore) {
            if ((var.getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = var.getTypeAndKind() & Variable.KIND;
                if (var.isInstantiated()) {
                    switch (kind) {
                        case Variable.INT:
                        case Variable.BOOL:
                            if (intmap == null) {
                                intmap = new TIntIntHashMap(16, .5f, Solution.NO_ENTRY, Solution.NO_ENTRY);
                            }
                            IntVar v = (IntVar) var;
                            intmap.put(v.getId(), v.getValue());
                            break;
                        case Variable.REAL:
                            if (realmap == null) {
                                realmap = new TIntObjectHashMap<>(16, 05f, Solution.NO_ENTRY);
                            }
                            RealVar r = (RealVar) var;
                            realmap.put(r.getId(), new double[]{r.getLB(), r.getUB()});
                            break;
                        case Variable.SET:
                            if (setmap == null) {
                                setmap = new TIntObjectHashMap<>(16, 05f, Solution.NO_ENTRY);
                            }
                            SetVar s = (SetVar) var;
                            setmap.put(s.getId(), s.getValue().toArray());
                            break;
                        default:
                            // do not throw exception to allow extending the solver with other variable kinds (e.g. graph)
                            // that should then be stored externally to this object
                            break;
                    }
                } else {
                    warn = true;
                }
            }
        }
        if (warn && varsToStore[0].getModel().getSettings().warnUser()) {
            model.getSolver().getOut().printf("Some non decision variables are not instantiated in the current solution.");
        }
        return this;
    }

    @Override
    public String toString() {
        if (empty) {
            return "Empty solution. No solution recorded yet";
        }
        StringBuilder st = new StringBuilder("Solution: ");
        for (Variable var : varsToStore) {
            if ((var.getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = var.getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        IntVar v = (IntVar) var;
                        st.append(v.getName()).append("=").append(intmap.get(v.getId())).append(", ");
                        break;
                    case Variable.REAL:
                        RealVar r = (RealVar) var;
                        double[] bounds = realmap.get(r.getId());
                        st.append(r.getName()).append("=[").append(bounds[0]).append(",").append(bounds[1]).append("], ");
                        break;
                    case Variable.SET:
                        SetVar s = (SetVar) var;
                        st.append(s.getName()).append("=").append(Arrays.toString(setmap.get(s.getId()))).append(", ");
                        break;
                    default:
                        // do not throw exception to allow extending the solver with other variable kinds (e.g. graph)
                        // that should then be stored externally to this object
                        break;
                }
            }
        }
        return st.toString();
    }

    public Solution copySolution() {
        Solution ret = new Solution(model, varsToStore);
        ret.empty = empty;
        ret.intmap = new TIntIntHashMap(intmap);
        ret.realmap = new TIntObjectHashMap<>(realmap);
        ret.setmap = new TIntObjectHashMap<>(setmap);
        return ret;
    }

    /**
     * Get the value of variable v in this solution.
     * If <i>v</i> was not instantiated during solution recording, calling this method will throw an exception.
     *
     * @param v IntVar (or BoolVar)
     * @return the value of variable v in this solution, or null if the variable is not instantiated in the solution
     * @exception SolverException if <i>v</i> was not instantiated during solution recording.
     */
    public int getIntVal(IntVar v) {
        if (empty) {
            throw new SolverException("Cannot access value of " + v + ": No solution has been recorded yet (empty solution). Make sure this.record() has been called.");
        }
        if (intmap != null && intmap.containsKey(v.getId())) {
            return intmap.get(v.getId());
        } else {
            if ((v.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
                return v.getValue();
            } else {
                throw new SolverException("Cannot access value of " + v + ": This variable has not been declared to be recorded in the Solution object (see Solution constructor).");
            }
        }
    }

    /**
     * Set the value of variable v in this solution.
     *
     * @param var IntVar (or BoolVar)
     * @param val its value
     */
    public void setIntVal(IntVar var, int val) {
        empty = false;
        if (intmap == null) {
            intmap = new TIntIntHashMap(16, .5f, Solution.NO_ENTRY, Solution.NO_ENTRY);
        }
        intmap.put(var.getId(), val);
    }

    /**
     * Get the value of variable s in this solution.
     * If <i>v</i> was not instantiated during solution recording, calling this method will throw an exception.
     *
     * @param s SetVar
     * @return the value of variable s in this solution, or null if the variable is not instantiated in the solution.
     * @exception SolverException if <i>v</i> was not instantiated during solution recording.
     */
    public int[] getSetVal(SetVar s) {
        if (empty) {
            throw new SolverException("Cannot access value of " + s + ": No solution has been recorded yet (empty solution). Make sure this.record() has been called.");
        }
        if (setmap != null && setmap.containsKey(s.getId())) {
            return setmap.get(s.getId());
        } else if ((s.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
            return s.getValue().toArray();
        } else {
            throw new SolverException("Cannot access value of " + s + ": This variable has not been declared to be recorded in the Solution object (see Solution constructor).");
        }
    }

    /**
     * Set the value of variable v in this solution
     *
     * @param var SetVar
     * @param val its value
     */
    public void setSetVal(SetVar var, int[] val) {
        empty = false;
        if (setmap == null) {
            setmap = new TIntObjectHashMap<>(16, 05f, Solution.NO_ENTRY);
        }
        setmap.put(var.getId(), val);
    }

    /**
     * Get the bounds of r in this solution.
     * If <i>v</i> was not instantiated during solution recording, calling this method will throw an exception.
     *
     * @param r RealVar
     * @return the bounds of r in this solution, or null if the variable is not instantiated in the solution
     * @exception SolverException if <i>v</i> was not instantiated during solution recording.
     */
    public double[] getRealBounds(RealVar r) {
        if (empty) {
            throw new SolverException("Cannot access value of " + r + ": No solution has been recorded yet (empty solution). Make sure this.record() has been called.");
        }
        if (realmap != null && realmap.containsKey(r.getId())) {
            return realmap.get(r.getId());
        } else {
            if ((r.getTypeAndKind() & Variable.TYPE) == Variable.CSTE) {
                return new double[]{r.getLB(), r.getUB()};
            } else {
                throw new SolverException("Cannot access value of " + r + ": This variable has not been declared to be recorded in the Solution object (see Solution constructor).");
            }
        }
    }

    /**
     * Set the value of variable v in this solution
     *
     * @param var RealVar
     * @param val its value
     */
    public void setRealBounds(RealVar var, double[] val) {
        empty = false;
        if (realmap == null) {
            realmap = new TIntObjectHashMap<>(16, 05f, Solution.NO_ENTRY);
        }
        if (val.length != 2) {
            throw new SolverException("wrong array size");
        }
        realmap.put(var.getId(), val);
    }

    /**
     * Restore the solution in {@link #model}.
     * Restoring a solution in a model consists in iterating over model's variables and
     * forcing each of them to be instantiated to the value recorded in this solution.
     * <p>
     * If a variable was not instantiated while this solution was recorded,
     * then a {@link SolverException} will be thrown (indeed, forcing this instantiation will call
     * {@link #getIntVal(IntVar)}, {@link #getSetVal(SetVar)} and/or {@link #getRealBounds(RealVar)}.
     * </p>
     * <p>
     * When instantiating all variables to their value in the solution,
     * a propagation loop will be achieved to ensure that the correctness and completeness of the model.
     * If the propagation detects a failure, a {@link ContradictionException} will be thrown.
     * If so, the propagation engine is not flushed automatically,
     * and a call to {@link IPropagationEngine#flush()} may be needed.
     *
     * However, the satisfaction of the solution status is not check
     * (see {@link Settings#checkModel(Solver)} to check satisfaction).
     * </p>
     * <p>
     * Restoring a solution is permanent except if a backtrack occurs.
     * Note that, for a backtrack to be feasible, it needs to be anticipated, by calling
     * {@link IEnvironment#worldPush()}:
     *
     * <pre>
     *     {@code
     *     // optional: for assertion only
     *     int wi = model.getEnvironment().getWorldIndex();
     *     // prepare future backtrack, in order to forget solution
     *     model.getEnvironment().worldPush();
     *     // restore the solution in `model`
     *     solution.restore();
     *     // ... do something
     *     // backtrack to before solution restoration
     *     model.getEnvironment().worldPop();
     *     // optional: for assertion only
     *     assert wi == model.getEnvironment().getWorldIndex();
     *     }
     * </pre>
     * </p>
     *
     *  @exception SolverException if a variable was not instantiated during solution recording.
     *  @exception ContradictionException if restoring the solution leads to failure
     */
    public void restore() throws ContradictionException{
        for (Variable var : varsToStore) {
            if ((var.getTypeAndKind() & Variable.TYPE) != Variable.CSTE) {
                int kind = var.getTypeAndKind() & Variable.KIND;
                switch (kind) {
                    case Variable.INT:
                    case Variable.BOOL:
                        IntVar v = (IntVar) var;
                        v.instantiateTo(intmap.get(v.getId()), this);
                        break;
                    case Variable.REAL:
                        RealVar r = (RealVar) var;
                        double[] bounds = realmap.get(r.getId());
                        r.updateBounds(bounds[0], bounds[1], this);
                        break;
                    case Variable.SET:
                        SetVar s = (SetVar) var;
                        s.instantiateTo(setmap.get(s.getId()), this);
                        break;
                    default:
                        // do not throw exception to allow extending the solver with other variable kinds (e.g. graph)
                        // that should then be stored externally to this object
                        break;
                }
            }
        }
        model.getSolver().propagate();
    }
}
