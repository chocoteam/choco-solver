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

import gnu.trove.map.hash.TIntIntHashMap;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.chocosolver.solver.constraints.ConstraintsName.REALCONSTRAINT;

/**
 * A constraint on real variables, solved using IBEX. <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18/07/12
 */
public class RealConstraint extends Constraint {

    private static final Pattern p0 = Pattern.compile("\\{\\d*\\}");
    private static final Pattern p1 = Pattern.compile("\\{_");

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Make a new RealConstraint defined as a set of RealPropagators
     *
     * @param name        name of the constraint
     * @param propagators set of propagators defining the constraint
     */
    private RealConstraint(String name, RealPropagator... propagators) {
        super(name, propagators);
    }

    /**
     * Make a new RealConstraint to model one or more continuous functions, separated with
     * semi-colon ";" <br/> A function is a string declared using the following format: <br/>- the
     * '{i}' tag defines a variable, where 'i' is an explicit index the array of variables
     * <code>vars</code>, <br/>- one or more operators :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max(
     * ),min( ),abs( ),cos( ), sin( ),...' <br/> A complete list is available in the documentation
     * of IBEX.
     * <p/>
     * <p/>
     * <blockquote><pre>
     *     model.realIbexGenericConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,
     * y).post();
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param contractionRatio defines the domain contraction significance
     * @param rvars     a list of real variables
     */
    public RealConstraint(String functions, double contractionRatio, Variable... rvars) {
        this(REALCONSTRAINT, createPropagator(functions, contractionRatio, rvars));
    }

    public RealConstraint(String functions, Variable... rvars) {
        this(functions, Ibex.RATIO, rvars);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    /**
     * Creates a RealPropagator to propagate one or more continuous functions, separated with
     * semi-colon ";" Each function is set to a single propagator. <br/> A function is a string
     * declared using the following format: <br/>- the '{i}' tag defines a variable, where 'i' is an
     * explicit index the array of variables <code>vars</code>, <br/>- one or more operators
     * :'+,-,*,/,=,<,>,<=,>=,exp( ),ln( ),max( ),min( ),abs( ),cos( ), sin( ),...' <br/> A complete
     * list is available in the documentation of IBEX.
     * <p/>
     * <p/>
     * <blockquote><pre>
     *     model.realIbexGenericConstraint("({0}*{1})+sin({0})=1.0;ln({0}+[-0.1,0.1])>=2.6", x,
     * y).post();
     * </pre>
     * </blockquote>
     *
     * @param functions list of functions, separated by a semi-colon
     * @param contractionRatio defines the domain contraction significance
     * @param rvars     a list of real variables
     * @return a RealPropagator to propagate the given functions over given variable domains
     */
    private static RealPropagator[] createPropagator(String functions, double contractionRatio, Variable... rvars) {
        // split functions to correctly maintain indices of contractors
        String[] theFunctions = functions.split(";");
        RealPropagator[] props = new RealPropagator[theFunctions.length];
        List<Variable> vars = new ArrayList<>();
        TIntIntHashMap sidx = new TIntIntHashMap();
        for (int i = 0; i < props.length; i++) {
            String fct = theFunctions[i];
            // determine the sub-scope of variables
            Matcher m = p0.matcher(fct);
            while (m.find()) {
                String g = m.group();
                int id = Integer.parseInt(g.substring(1, g.length() - 1));
                if (!sidx.contains(id)) {
                    sidx.put(id, vars.size());
                    vars.add(rvars[id]);
                }
            }
            // update the function
            for (int k : sidx.keys()) {
                fct = fct.replaceAll(
                        "\\{" + k + "\\}",
                        "{_" + sidx.get(k) + "}");
            }
            fct = p1.matcher(fct).replaceAll("{");
            RealPropagator realPropagator = new RealPropagator(fct, vars.toArray(new Variable[0]));
            realPropagator.setContractionRatio(contractionRatio);
            props[i] = realPropagator;
            sidx.clear();
            vars.clear();
        }
        return props;
    }

    /**
     * Reifies the constraint with a boolean variable
     * If the reified boolean variable already exists, an additional (equality) constraint is automatically posted.
     *
     * @param bool the variable to reify with
     */
    public void reifyWith(BoolVar bool) {
        Model s = propagators[0].getModel();
        getOpposite();
        if (boolReif == null) {
            this.post();
            boolReif = bool;
            if(propagators.length == 1) {
                ((RealPropagator) propagators[0]).reify(bool);
            }else {
                BoolVar[] bvars = bool.getModel().boolVarArray(propagators.length);
                for (int i = 0; i < propagators.length; i++) {
                    ((RealPropagator) propagators[i]).reify(bvars[i]);
                }
                bool.getModel().addClausesBoolAndArrayEqVar(bvars, bool);
            }
        } else if (bool != boolReif) {
            s.arithm(bool, "=", boolReif).post();
        }
    }


}
