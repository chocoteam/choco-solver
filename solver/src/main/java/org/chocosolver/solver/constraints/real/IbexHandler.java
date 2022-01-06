/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.real;

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.tools.VariableUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Utility class to build the right Ibex instance.
 *
 * The main idea is to delegate filtering of full real constraints and partially real constraints to
 * Ibex.
 *
 * Because Ibex does not support removing a contractor, this class makes up for this lack.
 *
 * Anytime a contractor has to be removed from Ibex, the instance is killed and recreated lazily.
 * <p> Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 29/09/2017.
 */
public class IbexHandler {

    private static final Pattern p1 = Pattern.compile("\\{_");
    /**
     * Ibex instances.
     */
    private Ibex ibex;
    /**
     * Store for each function declared, the index of the corresponding contractor in Ibex. Since
     * Ibex instance is lazily created, the mapping can be dynamically updated.
     */
    private final TObjectIntHashMap<RealPropagator> ibexCtr = new TObjectIntHashMap<>(16, 0.5f, -1);
    /**
     * Store, for each function declared, indices of the associated variables.
     */
    private final TObjectIntHashMap<Variable> ibexVar = new TObjectIntHashMap<>(16, 0.5f, -1);
    /**
     * List of all variables known by Ibex
     */
    private final List<Variable> vars = new ArrayList<>();
    /**
     * Each boolean indicates whether a variable is integral or not.
     */
    private final TDoubleList precisions = new TDoubleArrayList();
    /**
     * Each boolean indicates whether a variable is integral or not.
     */
    private double[] domains;
    /**
     * Indicates whether the constraint store changed
     */
    private boolean hasChanged = false;
    /**
     * Indicates whether the resolution status 0: not started, 1: started, 2: search over.
     */
    private byte startSolve = 0;

    /**
     * To optimize running time ibex only considers domain contractions greater than
     * a given ratio during constraint propagation. The default value is 1% (0.01).
     * See issue #653.
     *
     * Given: x = [0.0, 100.0], y = [0.5,0.5] and CSTR(x > y)
     * - When the ratio is 1% (0.01) bounds of X are kept as [0.0, 100.0]
     *   because it's contraction is less than 1%.
     * - When the ratio is 0.1% (0.001) bounds of X are update to [0.5, 100.0]
     *   because it's contraction is greater than 0.1%.
     *
     * @implNote Supported since ibex-java version 1.2.0
     */
    private double contractionRatio = Ibex.RATIO;

    /**
     * To improve the running time, ibex changes the rounding system for double values
     * during contraction. In Linux/MACOS environments it leads to different results in
     * calculations like `Math.pow(10, 6)`, see issue #740.
     *
     * When preserveRounding is defined as true, after calling ibex, the default Java
     * rounding system is restored. At the price of a little loss of efficiency.
     *
     * @implNote Supported since ibex-java version 1.2.0
     */
    private boolean preserveRounding = Ibex.PRESERVE_ROUNDING;

    /**
     * build Ibex instance.
     * Since Ibex' parser is not thread safe, this method is synchronized
     * @param ibex ibex instance to build.
     */
    private synchronized static void build(Ibex ibex){
        if(!ibex.build()){
            throw new SolverException("Malformed Ibex function");
        }
    }

    /**
     * Add new functions to Ibex
     *
     * @param prop propagator that manages the function
     */
    public void declare(RealPropagator prop) {
        ibexCtr.put(prop, -1);
        hasChanged = true;
    }

    /**
     * Remove 'prop' and thus the function it managed.
     *
     * @param prop propagator that manages the function
     */
    public void remove(RealPropagator prop) {
        ibexCtr.remove(prop);
        hasChanged = true;
    }

    /**
     * Call Ibex to contract the function managed by 'prop'
     *
     * @param prop propagator to propagate
     */
    void contract(RealPropagator prop) throws ContradictionException {
        Ibex mIbex = getIbexInstance();
        boolean filter;
        int reif;
        do {
            filter = false;
            extractDomains();
            reif = getReif(prop);
            int result;
            if (contractionRatio == Ibex.RATIO) {
                // Compatibility with ibex version previous to 2.8.8
                result = mIbex.contract(ibexCtr.get(prop), domains, reif);
            } else {
                // What's the best way to inform the user to update to ibex 2.8.8?
                result = mIbex.contract(ibexCtr.get(prop), domains, reif, contractionRatio);
            }
            switch (result) {
                case Ibex.FAIL:
                    // "Ibex failed"
                    if (reif == Ibex.FALSE_OR_TRUE) {
                        prop.reified.setToFalse(prop);
                        filter = true;
                    } else if (reif == Ibex.TRUE) {
                        prop.fails();
                    }
                    break;
                case Ibex.CONTRACT:
                    assert reif != Ibex.FALSE_OR_TRUE;
                    injectDomains(prop);
                    filter = true;
                    break;
                case Ibex.ENTAILED:
                    if (reif == Ibex.FALSE_OR_TRUE) {
                        prop.reified.setToTrue(prop);
                        filter = true;
                    } else if (reif == Ibex.FALSE) {
                        prop.fails();
                    } else {
                        injectDomains(prop);
                        // Validated by G. Chabert:
                        // When contracting returns ENTAIL, Ibex does not passivate the contractor.
                        // Indeed, even if all combinations are satisfiable the contractor may filter
                        // in the future. This happens when the search is delegated to Ibex,
                        // when calling {@link Ibex#start_solve(double[])}.
                        // so, we can set passivate the propagator in choco.
                    }
                    break;
                case Ibex.NOTHING:
                    break;
                default:
                    throw new SolverException("Bad return " + result);
            }
        } while (filter);

    }

    // NOT TESTED: it has to deal with environment backup, like a move.
    private void start_solve() {
        startSolve = 0;
        Ibex mIbex = getIbexInstance();
        extractDomains();
        int result = mIbex.start_solve(domains);
        switch (result) {
            case Ibex.STARTED: // success, a solution was found
                break;
            case Ibex.DISCRETE_NOT_INSTANCIATED:
            case Ibex.NOT_BUILT:
            case Ibex.BAD_DOMAIN:
                throw new IllegalStateException("Ibex cannot initialize the solving, error #" + result + " is thrown");
        }
    }

    public boolean nextSolution(boolean reinit){
        if (startSolve == 0 || (startSolve == 2 && reinit)) {
            this.start_solve();
            startSolve = 1;
        }
        if (startSolve == 1) {
            Ibex mIbex = getIbexInstance();
            int result = mIbex.next_solution(domains);
            switch (result) {
                case Ibex.SOLUTION:
                case Ibex.UNKNOWN:
                    return true;
                case Ibex.SEARCH_OVER:
                    startSolve = 2;
                    return false;
                case Ibex.NOT_STARTED:
                case Ibex.BAD_DOMAIN:
                    throw new IllegalStateException("Ibex cannot terminate the solving, error #" + result + " is thrown");
            }
        }
        return false;
    }

    public void injectDomain() throws ContradictionException {
        injectDomains(Cause.Null);
    }

    public double getContractionRatio() {
        return contractionRatio;
    }

    /**
     * Defines the ratio that real domains must be contract by ibex
     * to compute the constraint. A contraction is considered as significant
     * when at least {@param ratio} of a domain has been reduced.
     * If the contraction is not meet, then it is considered as insufficient
     * and therefore ignored.
     *
     * @param ratio defines the ratio that a domains must be contract to
     *              compute the constraint.
     */
    public void setContractionRatio(double ratio) {
        this.contractionRatio = ratio;
    }

    public boolean isPreserveRounding() {
        return preserveRounding;
    }

    /**
     * If preserve_rounding is true, Ibex will restore the default
     * Java rounding method when coming back from Ibex, which is
     * transparent for Java but causes a little loss of efficiency.
     *
     * @param preserveRounding
     */
    public void setPreserveRounding(boolean preserveRounding) {
        this.preserveRounding = preserveRounding;
    }

    private Ibex getIbexInstance() {
        if (hasChanged && ibex != null) {
            ibex.release();
            ibex = null;
        }
        if (ibex == null) {
            createInstance();
        }
        return ibex;
    }

    /**
     * Extract domains of all variables (required by Ibex)
     */
    private void extractDomains() {
        for (int i = 0; i < vars.size(); i++) {
            if (VariableUtils.isReal(vars.get(i))) {
                RealVar rvar = (RealVar) vars.get(i);
                domains[2 * i] = rvar.getLB();
                domains[2 * i + 1] = rvar.getUB();
            } else {
                IntVar ivar = (IntVar) vars.get(i);
                domains[2 * i] = ivar.getLB();
                domains[2 * i + 1] = ivar.getUB();
            }
        }
    }

    /**
     * Extract domains of all variables (required by Ibex)
     *
     * @param cause propagator that trigger the filtering
     * @throws ContradictionException in case of a failure
     */
    private void injectDomains(ICause cause) throws ContradictionException {
        for (int i = 0; i < vars.size(); i++) {
            if (VariableUtils.isReal(vars.get(i))) {
                RealVar rvar = (RealVar) vars.get(i);
                rvar.updateBounds(domains[2 * i], domains[2 * i + 1], cause);
            } else {
                IntVar ivar = (IntVar) vars.get(i);
                ivar.updateBounds((int) ceil(domains[2 * i]), (int) floor(domains[2 * i + 1]), cause);
            }
        }
    }

    /**
     * Compute the reification indicator of 'prop'
     *
     * @param prop propagator to filter
     * @return {@link Ibex#TRUE} if 'prop' is not reified or reified to 'true', {@link Ibex#FALSE}
     * if 'prop' is reified to 'false' or {@link Ibex#FALSE_OR_TRUE} if 'prop' reified to
     * 'undefined'
     */
    private int getReif(RealPropagator prop) {
        if (prop.reified == null) {
            return Ibex.TRUE;
        } else if (prop.reified.isInstantiated()) {
            return prop.reified.isInstantiatedTo(1) ? Ibex.TRUE : Ibex.FALSE;
        } else {
            return Ibex.FALSE_OR_TRUE;
        }
    }

    /**
     * Create the instance of Ibex
     */
    private synchronized void createInstance() {
        RealPropagator[] props = ibexCtr.keySet()
                .stream()
                .sorted(Comparator.comparingInt(Propagator::getId))
                .toArray(RealPropagator[]::new);
        // first pass to collect variable
        vars.clear();
        precisions.clear();
        ibexVar.clear();
        for (int i = 0; i < props.length; i++) {
            for (int j = 0; j < props[i].getNbVars() - (props[i].reified != null ? 1 : 0); j++) {
                Variable var = props[i].getVar(j);
                if (!ibexVar.contains(var)) {
                    ibexVar.put(var, vars.size());
                    vars.add(var);
                    precisions.add(VariableUtils.isReal(var) ? ((RealVar) var).getPrecision() : -1);
                }
            }
        }
        if (preserveRounding == Ibex.PRESERVE_ROUNDING) {
            // For backwards compatibility
            ibex = new Ibex(precisions.toArray());
        } else {
            ibex = new Ibex(precisions.toArray(), preserveRounding);
        }
        int k = 0;
        // first pass to modify functions wrt to variables
        for (int i = 0; i < props.length; i++) {
            String fct = props[i].functions;
            for (int j = 0; j < props[i].getNbVars() - (props[i].reified != null ? 1 : 0); j++) {
                fct = fct.replaceAll(
                        "\\{" + j + "\\}",
                        "{_" + ibexVar.get(props[i].getVar(j)) + "}");
            }
            ibexCtr.put(props[i], k++);
            ibex.add_ctr(p1.matcher(fct).replaceAll("{"));
        }
        domains = new double[vars.size() * 2];
        hasChanged = false;
        // build is synchronized because Ibex parser is not thread safe
        IbexHandler.build(ibex);
    }


    @Override
    protected void finalize() throws Throwable {
        if (ibex != null) {
            ibex.release();
        }
        super.finalize();
    }
}


