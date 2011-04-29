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

package parser.flatzinc.ast;

import gnu.trove.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.flatzinc.FZNException;
import parser.flatzinc.ast.constraints.IBuilder;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.Expression;
import parser.flatzinc.parser.FZNParser;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.binary.Element;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.IntLinComb;
import solver.constraints.nary.InverseChanneling;
import solver.constraints.reified.ReifiedConstraint;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static solver.constraints.ConstraintFactory.*;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Constraint builder from flatzinc-like object.
*/
public final class PConstraint {

    static Logger LOGGER = LoggerFactory.getLogger("fzn");

    private static final String ERROR_MSG = "Cant load manager by reflection: ";

    static Properties properties = new Properties();

    private static THashMap<String, IBuilder> builders = new THashMap<String, IBuilder>();

    static {
        InputStream is = PConstraint.class.getResourceAsStream("/fzn_manager.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("Could not open application.properties");
            throw new FZNException("Could not open fzn_manager.properties");
        }
    }


    // VARIABLE TYPE
    private static final String _int = "int";
    private static final String _float = "float";
    private static final String _bool = "bool";
    private static final String _set = "set";
    private static final String _var = "_var";

    // COMPARISONS OPERATIONS
    private static final String _eq = "_eq";
    private static final String _ne = "_ne";
    private static final String _lt = "_lt";
    private static final String _gt = "_gt";
    private static final String _ge = "_ge";
    private static final String _le = "_le";

    private static final String _lin = "_lin";

    private static final String _reif = "_reif";

    // ARITHMETIC OPERATIONS
    private static final String _plus = "_plus";
    private static final String _minus = "_minus";
    private static final String _times = "_times";
    private static final String _negate = "_negate";
    private static final String _div = "_div";
    private static final String _mod = "_mod";
    private static final String _min = "_min";
    private static final String _max = "_max";
    private static final String _abs = "_abs";

    // LOGICAL OPERATIONS
    // CONJUNCTIONS
    private static final String _and = "_and";
    private static final String _or = "_or";
    private static final String _left_imp = "_left_imp";
    private static final String _right_imp = "_right_imp";
    private static final String _xor = "_xor";
    private static final String _not = "_not";
    // N-ARY CONJUNCTIONS
    private static final String _array = "array";
    // CLAUSES
    private static final String _clause = "_clause";

    // SET OPERATIONS
    private static final String _in = "_in";
    private static final String _subset = "_subset";
    private static final String _superset = "_superset";
    private static final String _union = "_union";
    private static final String _intersect = "_intersect";
    private static final String _diff = "_diff";
    private static final String _symdiff = "_symdiff";
    private static final String _card = "_card";

    // ARRAY OPERATIONS
    private static final String _element = "_element";

    // COERCION OPERATIONS
    private static final String _int2float = "int2float";
    private static final String _bool2int = "bool2int";

    // GLOBAL CONSTRAINTS
    private static final String _global = "global";
    private static final String _allDifferent = "_allDifferent";
    private static final String _allDisjoint = "_allDisjoint";
    private static final String _among = "_among";
    private static final String _cumulative = "_cumulative";
    private static final String _exactly = "_exactly";
    private static final String _setDisjoint = "_setDisjoint";
    private static final String _globalCardinalityLowUp = "_globalCardinalityLowUp";
    private static final String _globalCardinality = "_globalCardinality";
    private static final String _inverseChanneling = "_inverseChanneling";
    private static final String _inverseSet = "_inverseSet";
    private static final String _lexEq = "_lexEq";
    private static final String _lex = "_lex";
    private static final String _member = "_member";
    private static final String _sorting = "_sorting";


    public PConstraint(FZNParser parser, String id, List<Expression> exps, List<EAnnotation> annotations) {
        //TODO: manage annotations
//        build(id, exps, parser.solver);
        Solver solver = parser.solver;
        IBuilder builder = null;
        if (builders.containsKey(id)) {
            builder = builders.get(id);
        } else {
            String name = properties.getProperty(id);
            if(name==null){
                throw new FZNException("Unkown constraint: "+id);
            }
            builder = (IBuilder) loadManager(name);
            builders.put(id, builder);
        }
        Constraint cstr = builder.build(solver, id, exps, annotations);
        solver.post(cstr);
        readAnnotations(annotations);
    }


    private static Object loadManager(String name) {
        try {
            return Class.forName(name).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FZNException(ERROR_MSG + name);
        } catch (InstantiationException e) {
            throw new FZNException(ERROR_MSG + name);
        } catch (IllegalAccessException e) {
            throw new FZNException(ERROR_MSG + name);
        }
    }

    private static void readAnnotations(List<EAnnotation> annotations) {
        if(annotations.size()>0){
            LOGGER.trace("% unsupported operation: annotation");
        }
//        for (int i = 0; i < annotations.size(); i++) {
//            EAnnotation annotation = annotations.get(i);
//        }
    }

    /**
     * Builder of constraint defined with flatzinc-like object.
     *
     * @param name   predicate name
     * @param exps   constraint parameters
     * @param solver
     */
    private static void build(String name, List<Expression> exps, Solver solver) {
        if (name.startsWith(_global)) {
            buildGlobal(name, exps, solver);
            return;
        } else if (name.startsWith(_int)) {
            buildInt(name, exps, solver);
            return;
        } else if (name.startsWith(_float)) {
            buildFloat(name, exps, solver);
            return;
        } else if (name.startsWith(_bool)) {
            buildBool(name, exps, solver);
            return;
        } else if (name.startsWith(_set)) {
            buildSet(name, exps, solver);
            return;
        } else if (name.startsWith(_array)) {
            if (name.contains(_bool)) {
                buildBool(name, exps, solver);
                return;
            } else if (name.contains(_int)) {
                buildInt(name, exps, solver);
                return;
            }
        }
        LOGGER.error("buildCstr::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on int variables
     *
     * @param name   name of the constraint
     * @param exps   parameters of the constraint
     * @param solver
     */
    private static void buildInt(String name, List<Expression> exps, Solver solver) {
        Constraint c = null;
        if (name.contains(_lin)) {

            int[] coeffs = exps.get(0).toIntArray();
            IntVar[] vars = exps.get(1).toIntVarArray(solver);
            int result = exps.get(2).intValue();

            if (name.contains(_eq)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.EQ, result, solver));
            } else if (name.contains(_ne)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.NEQ, result, solver));
            } else if (name.contains(_gt)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.GEQ, result - 1, solver));
            } else if (name.contains(_lt)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.LEQ, result + 1, solver));
            } else if (name.contains(_ge)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.GEQ, result, solver));
            } else if (name.contains(_le)) {
                c = (scalar(vars, coeffs, IntLinComb.Operator.LEQ, result, solver));
            }
        } else if (name.contains(_array) && name.contains(_element)) {
            IntVar index = exps.get(0).intVarValue(solver);
            IntVar val = exps.get(2).intVarValue(solver);
            if (name.contains(_var)) {
                try {
                    IntVar[] values = exps.get(1).toIntVarArray(solver);
//                    c = new Element(index, values, val, 1, solver, propEngine);
                    Exit.log("Element 2D not yet implemented");
                } catch (ClassCastException e) {
                    int[] values = exps.get(1).toIntArray();
                    c = new Element(index, values, val, 1, solver);
                }
            } else {
                int[] values = exps.get(1).toIntArray();
                c = new Element(index, values, val, 1, solver);
            }
        } else {
            IntVar[] vars = new IntVar[exps.size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = exps.get(i).intVarValue(solver);
            }

            if (name.contains(_eq)) {
                c = (eq(vars[0], vars[1], solver));
            } else if (name.contains(_ne)) {
                c = (neq(vars[0], vars[1], solver));
            } else if (name.contains(_gt)) {
                c = (gt(vars[0], vars[1], solver));
            } else if (name.contains(_lt)) {
                c = (lt(vars[0], vars[1], solver));
            } else if (name.contains(_ge)) {
                c = (geq(vars[0], vars[1], solver));
            } else if (name.contains(_le)) {
                c = (leq(vars[0], vars[1], solver));
            }
            /*if (name.contains(_plus)) {
                c = (eq(plus(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_minus)) {
                c = (eq(minus(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_times)) {
                c = (times(vars[0], vars[1], vars[2]));
            } else if (name.contains(_negate)) {
                c = (eq(neg(vars[0]), vars[1]));
            } else if (name.contains(_div)) {
                c = (eq(div(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_mod)) {
                c = (eq(mod(vars[0], vars[1]), vars[2]));
            } else if (name.contains(_min)) {
                c = (min(vars[0], vars[1], vars[2]));
            } else if (name.contains(_max)) {
                c = (max(vars[0], vars[1], vars[2]));
            } else if (name.contains(_abs)) {
                c = (eq(abs(vars[0]), vars[1]));
            } else if (name.contains(_int2float)) {
                //TODO: to complete
            }*/
        }
        if (c != null) {
            if (!name.endsWith(_reif)) {
                solver.post(c);
                return;
            } else {
                BoolVar vr = exps.get(exps.size() - 1).boolVarValue(solver);
                solver.post(new ReifiedConstraint(vr, null, c, solver));
                return;
            }
        }
        LOGGER.error("buildInt::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on float variables
     *
     * @param name   name of the constraint
     * @param exps   parameters of the constraint
     * @param solver
     */
    private static void buildFloat(String name, List<Expression> exps, Solver solver) {
        //TODO: to complete
        LOGGER.error("buildFloat::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on bool variables
     * FYI : bool == 1 is true
     *
     * @param name   name of the constraint
     * @param exps   parameters of the constraint
     * @param solver
     */
    private static void buildBool(String name, List<Expression> exps, Solver solver) {
        Constraint c = null;
        if (name.contains((_array))) {
            if (name.contains(_element)) {
                if (name.contains(_var)) {
                    IntVar index = exps.get(0).intVarValue(solver);
                    IntVar[] values = exps.get(1).toIntVarArray(solver);
                    IntVar val = exps.get(2).intVarValue(solver);
//                    c = new Element(index, values, val, 1, solver, engine);
                } else {
                    IntVar index = exps.get(0).intVarValue(solver);
                    //TODO: must be change to smth like get_bools
                    int[] values = exps.get(1).toIntArray();
                    IntVar val = exps.get(2).intVarValue(solver);
                    c = new Element(index, values, val, 1, solver);
                }
            } else {
                Variable[] vars = exps.get(0).toIntVarArray(solver);
                Variable result = exps.get(1).intVarValue(solver);
                /*if (name.contains(_and)) {
                    c = (reifiedAnd(result, vars));
                } else if (name.contains(_or)) {
                    c = (reifiedOr(result, vars));
                }*/
            }
        } else if (name.contains(_clause)) {
            Variable[] posLits = exps.get(0).toIntVarArray(solver);
            Variable[] negLits = exps.get(1).toIntVarArray(solver);
//            c = (clause(posLits, negLits));
        } else {
            IntVar[] vars = new IntVar[exps.size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = exps.get(i).intVarValue(solver);
            }
            if (name.contains(_eq)) {
                c = ConstraintFactory.eq(vars[0], vars[1], solver);
            } else if (name.contains(_ne)) {
                c = ConstraintFactory.eq(vars[0], vars[1], solver);
            } else if (name.contains(_gt)) {
                c = ConstraintFactory.gt(vars[0], vars[1], solver);
            } else if (name.contains(_lt)) {
                c = ConstraintFactory.lt(vars[0], vars[1], solver);
            } else if (name.contains(_ge)) {
                c = ConstraintFactory.geq(vars[0], vars[1], solver);
            } else if (name.contains(_le)) {
                c = ConstraintFactory.leq(vars[0], vars[1], solver);
                /*} else if (name.contains(_and)) {
            c = (reifiedAnd(vars[2], vars[0], vars[1]));
        } else if (name.contains(_or)) {
            c = (reifiedOr(vars[2], vars[0], vars[1]));
        } else if (name.contains(_xor)) {
            c = (reifiedXor(vars[2], vars[0], vars[1]));
        } else if (name.contains(_not)) {
            //TODO: to check
            c = neq(vars[0], vars[1]);
        } else if (name.contains(_right_imp)) {
            c = (reifiedRightImp(vars[2], vars[0], vars[1]));
        } else if (name.contains(_left_imp)) {
            c = (reifiedLeftImp(vars[2], vars[0], vars[1]));*/
            } else if (name.contains(_bool2int)) {
                // beware... it is due to the fact that in choco, there are no explicit boolean variable
                // but integer variable with [0,1] domain.
                c = ConstraintFactory.eq(vars[0], vars[1], solver);
            }
        }
        if (c != null) {
            if (!name.endsWith(_reif)) {
                solver.post(c);
                return;
            } else {
                BoolVar vr = exps.get(exps.size() - 1).boolVarValue(solver);
                solver.post(new ReifiedConstraint(vr, null, c, solver));
                return;
            }
        }
        LOGGER.error("buildBool::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a basic constraint based on set variables
     *
     * @param name   name of the constraint
     * @param exps   parameters of the constraint
     * @param solver
     */
    private static void buildSet(String name, List<Expression> exps, Solver solver) {
        Constraint c = null;
        /*if(name.endsWith(_reif)){
            LOGGER.error("buildSet::ERROR:: unexepected reified call :" + name);
            throw new UnsupportedOperationException();
        }
        if (name.endsWith(_in)) {
            Variable iv = exps.get(0).intVarValue(solver);
            Variable sv = exps.get(1).setVarValue(solver);
            c = member(iv, sv);
        }else if (name.contains(_card)) {
            Variable sv = exps.get(0).setVarValue(solver);
            Variable iv = exps.get(1).intVarValue(solver);
            c = eqCard(sv, iv);
        }
        else{
                Variable sv1 = exps.get(0).setVarValue(solver);
                Variable sv2 = exps.get(1).setVarValue(solver);
            if (name.contains(_diff)) {
                //TODO: to complete
            }else if (name.contains(_eq)) {
                c = eq(sv1, sv2);
            } else if (name.contains(_ge)) {
                //TODO: to complete
            } else if (name.contains(_gt)) {
                //TODO: to complete
            } else if (name.contains(_intersect)) {
                Variable inter = exps.get(2).setVarValue(solver);
                c = setInter(sv1, sv2, inter);
            } else if (name.contains(_le)) {
                //TODO: to complete
            } else if (name.contains(_lt)) {
                //TODO: to complete
            } else if (name.contains(_ne)) {
                c = neq(sv1, sv2);
            } else if (name.contains(_subset)) {
                //TODO: to complete
            } else if (name.contains(_superset)) {
                //TODO: to complete
            } else if (name.contains(_symdiff)) {
                //TODO: to complete
            } else if (name.contains(_union)) {
                Variable union = exps.get(2).setVarValue(solver);
                c = setUnion(sv1, sv2, union);
            }
        }*/

        if (c != null) {
//            if (!name.endsWith(_reif)) {
            solver.post(c);
//            } else {
//                Variable vr = exps.get(exps.size()-1).intVarValue();
//                FZNParser.model.addConstraint(reifiedIntConstraint(vr, c));
//            }
            return;
        }
        LOGGER.error("buildSet::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

    /**
     * Build a global constraint
     *
     * @param name   name of the constraint
     * @param exps   parameters of the constraint
     * @param solver
     */
    private static void buildGlobal(String name, List<Expression> exps, Solver solver) {
        Constraint c = null;
        if (name.contains(_allDifferent)) {
            IntVar[] vars = exps.get(0).toIntVarArray(solver);
            c = new AllDifferent(vars, solver);
        } else
            /*if(name.contains(_allDisjoint)){
                SetVariable[] vars = exps.get(0).toSetVarArray(solver);
                c = setDisjoint(vars);
            }else*/
            /*if(name.contains(_among)){
                Variable nvar = exps.get(0).intVarValue(solver);
                Variable[] vars = exps.get(1).toIntVarArray(solver);
                SetVariable svar = exps.get(2).setVarValue(solver);

                c = among(nvar, vars, svar);
            }else*/
            /*if(name.contains(_cumulative)){
                Variable[] starts = exps.get(0).toIntVarArray(solver);
                Variable[] durations = exps.get(1).toIntVarArray(solver);
                // build task variables
                TaskVariable[] tvars = new TaskVariable[starts.length];
                for(int i = 0; i < tvars.length; i++){
                    tvars[i] = makeTaskVar("t_"+i, starts[i], durations[i]);
                }
                Variable[] heights = exps.get(2).toIntVarArray(solver);
                Variable capa = exps.get(3).intVarValue(solver);
                c = cumulative(name, tvars, heights, null, constant(0), capa, (Variable)null, "");
            }else*/
            /*if(name.contains(_setDisjoint)){
                SetVariable s1 = exps.get(0).setVarValue(solver);
                SetVariable s2 = exps.get(1).setVarValue(solver);
                c = setDisjoint(s1, s2);
            }else*/
            /*if(name.contains(_exactly)){
                int n = exps.get(0).intValue();
                Variable[] vars = exps.get(1).toIntVarArray(solver);
                int value = exps.get(2).intValue();
                c = occurrence(n, vars, value);
            }else*/
            /*if(name.contains(_element)){
                Variable index = exps.get(0).intVarValue(solver);
                Variable[] varArray = exps.get(1).toIntVarArray(solver);
                Variable val = exps.get(2).intVarValue(solver);
                c = nth(index, varArray, val,1);
            }else*/
            /*if(name.contains(_globalCardinalityLowUp)){
                Variable[] vars = exps.get(0).toIntVarArray(solver);
                int[] covers = exps.get(1).toIntArray();
                int[] lbound = exps.get(2).toIntArray();
                int[] ubound = exps.get(3).toIntArray();

                int min  = MathUtils.min(covers)-1;
                int max = MathUtils.max(covers)+1;

                int[] low = new int[max-min];
                int[] upp = new int[max-min];
                Arrays.fill(low, 0);
                Arrays.fill(upp, 0);
                for(int i = 0; i < covers.length; i++){
                    low[covers[i]] = lbound[i];
                    upp[covers[i]] = ubound[i];
                }
                c = globalCardinality(vars, low, upp, 0);
            }else*/
            /*if(name.contains(_globalCardinality)){
                Variable[] vars = exps.get(0).toIntVarArray(solver);
                Variable[] cards = exps.get(1).toIntVarArray(solver);
                c = globalCardinality(vars, cards, 0);
            }else*/
            if (name.contains(_inverseChanneling)) {
                IntVar[] ivars1 = exps.get(0).toIntVarArray(solver);
                IntVar[] ivars2 = exps.get(1).toIntVarArray(solver);
                c = new InverseChanneling(ivars1, ivars2, solver);
            }/* else */
        /*if(name.contains(_inverseSet)){
            Variable[] ivars = exps.get(0).toIntVarArray(solver);
            SetVariable[] svars = exps.get(1).toSetVarArray(solver);
            c = inverseSet(ivars, svars);
        }else*/
        /*if(name.contains(_lexEq)){
            Variable[] xs = exps.get(0).toIntVarArray(solver);
            Variable[] ys = exps.get(1).toIntVarArray(solver);
            c = lexEq(xs, ys);
        }else*/
        /*if(name.contains(_lex)){
            Variable[] xs = exps.get(0).toIntVarArray(solver);
            Variable[] ys = exps.get(1).toIntVarArray(solver);
            c = lex(xs, ys);
        }else*/
        /*if(name.contains(_max)){
            Variable[] xs = exps.get(0).toIntVarArray(solver);
            Variable max = exps.get(1).intVarValue(solver);
            c = max(xs, max);
        }else*/
        /*if(name.contains(_member)){
            Variable ivar = exps.get(0).intVarValue(solver);
            SetVariable svar = exps.get(1).setVarValue(solver);
            c = member(ivar, svar);
        }else*/
        /*if(name.contains(_min)){
            Variable[] xs = exps.get(0).toIntVarArray(solver);
            Variable min = exps.get(1).intVarValue(solver);
            c = min(xs, min);
        }else*/
        /*if(name.contains(_sorting)){
            Variable[] xs = exps.get(0).toIntVarArray(solver);
            Variable[] ys = exps.get(1).toIntVarArray(solver);
            c = sorting(xs, ys);
        }*/
        if (c != null) {
            if (!name.endsWith(_reif)) {
                solver.post(c);
                return;
            } else {
                BoolVar vr = exps.get(exps.size() - 1).boolVarValue(solver);
                solver.post(new ReifiedConstraint(vr, null, c, solver));
                return;
            }
        }
        LOGGER.error("buildGlob::ERROR:: unknown type :" + name);
        throw new UnsupportedOperationException();
    }

}
