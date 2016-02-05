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
package org.chocosolver.solver.variables;

import org.chocosolver.solver.Solver;
import org.chocosolver.util.objects.setDataStructures.SetType;

/**
 * @deprecated : variable creation should be done through the {@link Solver} object
 * which extends {@link org.chocosolver.solver.variables.IVariableFactory}
 *
 * This class will be removed in versions > 3.4.0
 */
@Deprecated
public class VariableFactory {

    VariableFactory() {}

    /**
     * @deprecated : use {@link Solver#CSTE_NAME} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static final String CSTE_NAME = "cste -- ";

    /**
     * @deprecated : use {@link Solver#MIN_INT_BOUND} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static final int MIN_INT_BOUND = Integer.MIN_VALUE / 100;

    /**
     * @deprecated : use {@link Solver#MAX_INT_BOUND} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static final int MAX_INT_BOUND = Integer.MAX_VALUE / 100;

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    /**
     * @deprecated : use {@link Solver#boolVar(String)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar bool(String NAME, Solver SOLVER) {
        return SOLVER.boolVar(NAME);
    }

    /**
     * @deprecated : use {@link Solver#boolVarArray(String, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar[] boolArray(String NAME, int SIZE, Solver SOLVER) {
        return SOLVER.boolVarArray(NAME,SIZE);
    }

    /**
     * @deprecated : use {@link Solver#boolVarMatrix(String, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar[][] boolMatrix(String NAME, int DIM1, int DIM2, Solver SOLVER) {
        return SOLVER.boolVarMatrix(NAME,DIM1,DIM2);
    }

    /**
     * @deprecated : use {@link Solver#intVar(String, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar integer(String NAME, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVar(NAME,MIN,MAX);
    }

    /**
     * @deprecated : use {@link Solver#intVarArray(String, int, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[] integerArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarArray(NAME,SIZE,MIN,MAX);
    }

    /**
     * @deprecated : use {@link Solver#intVarMatrix(String, int, int, int, int)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[][] integerMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarMatrix(NAME,DIM1,DIM2,MIN,MAX);
    }


    /**
     * @deprecated : use {@link Solver#intVar(String, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar bounded(String NAME, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVar(NAME, MIN, MAX,true);
    }

    /**
     * @deprecated : use {@link Solver#intVarArray(int, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[] boundedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarArray(NAME,SIZE,MIN,MAX,true);
    }

    /**
     * @deprecated : use {@link Solver#intVarMatrix(String, int, int, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[][] boundedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarMatrix(NAME,DIM1,DIM2,MIN,MAX,true);
    }

    /**
     * @deprecated : use {@link Solver#intVar(String, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar enumerated(String NAME, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVar(NAME,MIN,MAX,false);
    }

    /**
     * @deprecated : use {@link Solver#intVarArray(int, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[] enumeratedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarArray(NAME,SIZE,MIN,MAX,false);
    }

    /**
     * @deprecated : use {@link Solver#intVarMatrix(String, int, int, int, int, boolean)} instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
        return SOLVER.intVarMatrix(NAME,DIM1,DIM2,MIN,MAX,false);
    }

    /**
     * @deprecated : use {@link Solver#intVar(String, int[])}  instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar enumerated(String NAME, int[] VALUES, Solver SOLVER) {
        return SOLVER.intVar(NAME,VALUES);
    }

    /**
     * @deprecated : use {@link Solver#intVarArray(String, int, int[])}  instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[] enumeratedArray(String NAME, int SIZE, int[] VALUES, Solver SOLVER) {
        return SOLVER.intVarArray(NAME,SIZE,VALUES);
    }

    /**
     * @deprecated : use {@link Solver#intVarMatrix(String, int, int, int[])}  instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int[] VALUES, Solver SOLVER) {
        return SOLVER.intVarMatrix(NAME,DIM1,DIM2,VALUES);
    }

    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * @deprecated : use {@link Solver#realVar(String, double, double, double)}   instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealVar real(String NAME, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        return SOLVER.realVar(NAME,MIN,MAX,PRECISION);
    }

    /**
     * @deprecated : use {@link Solver#realVarArray(String, int, double, double, double)}   instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealVar[] realArray(String NAME, int SIZE, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        return SOLVER.realVarArray(NAME,SIZE,MIN,MAX,PRECISION);
    }

    /**
     * @deprecated : use {@link Solver#realVarMatrix(String, int, int, double, double, double)}    instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealVar[][] realMatrix(String NAME, int DIM1, int DIM2, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        return SOLVER.realVarMatrix(NAME,DIM1,DIM2,MIN,MAX,PRECISION);
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    /**
     * @deprecated : use {@link Solver#setVar(String, int[], int[])}    instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetVar set(String NAME, int[] ENVELOPE, SetType ENV_TYPE, int[] KERNEL, SetType KER_TYPE, Solver SOLVER) {
        return SOLVER.setVar(NAME,KERNEL,ENVELOPE);
    }

    /**
     * @deprecated : use {@link Solver#setVar(String, int[], int[])}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetVar set(String NAME, int[] ENVELOPE, int[] KERNEL, Solver SOLVER) {
        return SOLVER.setVar(NAME,KERNEL,ENVELOPE);
    }

    /**
     * @deprecated : use {@link Solver#setVar(String, int[], int[])}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetVar set(String NAME, int[] ENVELOPE, Solver SOLVER) {
        return SOLVER.setVar(NAME,new int[]{},ENVELOPE);
    }

    /**
     * @deprecated : use {@link Solver#setVar(String, int[], int[])}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetVar set(String NAME, int MIN_ELEMENT, int MAX_ELEMENT, Solver SOLVER) {
        int[] ENVELOPE = new int[MAX_ELEMENT-MIN_ELEMENT+1];
        for (int i=0;i<ENVELOPE.length;i++){
            ENVELOPE[i] = MIN_ELEMENT+i;
        }
        return SOLVER.setVar(NAME,new int[]{},ENVELOPE);
    }

    //*************************************************************************************
    // TASKS
    //*************************************************************************************

    /**
     * @deprecated : use {@link new Task}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static Task task(IntVar START, IntVar DURATION, IntVar END) {
        return new Task(START, DURATION, END);
    }

    //*************************************************************************************
    // UTILITIES
    //*************************************************************************************

    /**
     * @deprecated : will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[] castToIntVar(Variable... VARIABLES) {
        IntVar[] ivars = new IntVar[VARIABLES.length];
        for (int i = 0; i < VARIABLES.length; i++) {
            ivars[i] = (IntVar) VARIABLES[i];
        }
        return ivars;
    }

    /**
     * @deprecated : will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar[][] toMatrix(IntVar[] VARS, int DIM1, int DIM2) {
        IntVar[][] mat = new IntVar[DIM1][DIM2];
        for (int i = 0; i < DIM1; i++) {
            for (int j = 0; j < DIM2; j++) {
                mat[i][j] = VARS[i + j * DIM1];
            }
        }
        return mat;
    }

    /**
     * @deprecated : use {@link Solver#intVar(int)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar fixed(int VALUE, Solver SOLVER) {
        return SOLVER.intVar(VALUE);
    }

    /**
     * @deprecated : use {@link Solver#ZERO()}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar zero(Solver SOLVER) {
        return SOLVER.ZERO();
    }

    /**
     * @deprecated : use {@link Solver#ONE()}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar one(Solver SOLVER) {
        return SOLVER.ONE();
    }

    //*************************************************************************************
    // CONSTANTS
    //*************************************************************************************

    /**
     * @deprecated : use {@link Solver#intVar(String, int)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar fixed(String NAME, int VALUE, Solver SOLVER) {
        return SOLVER.intVar(NAME, VALUE);
    }

    /**
     * @deprecated : use {@link Solver#boolVar(boolean)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar fixed(boolean VALUE, Solver SOLVER) {
        if (VALUE) {
            return SOLVER.ONE();
        } else {
            return SOLVER.ZERO();
        }
    }

    /**
     * @deprecated : use {@link Solver#setVar(String, int[])}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static SetVar fixed(String NAME, int[] VALUE, Solver SOLVER) {
        return SOLVER.setVar(NAME,VALUE);
    }

    //*************************************************************************************
    // VIEWS
    //*************************************************************************************

    /**
     * @deprecated : use {@link Solver#intOffsetView(IntVar, int)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar offset(IntVar VAR, int CSTE) {
        return VAR.getSolver().intOffsetView(VAR,CSTE);
    }

    /**
     * @deprecated : use {@link Solver#intEqView(IntVar)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar eq(IntVar VAR) {
        return VAR.getSolver().intEqView(VAR);
    }

    /**
     * @deprecated : use {@link Solver#boolEqView(BoolVar)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar eq(BoolVar VAR) {
        return eqbool(VAR);
    }

    private static BoolVar eqbool(BoolVar BOOL) {
        return BOOL.getSolver().boolEqView(BOOL);
    }

    /**
     * @deprecated : use {@link Solver#boolNotView(BoolVar)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static BoolVar not(BoolVar BOOL) {
        return BOOL.getSolver().boolNotView(BOOL);
    }

    /**
     * @deprecated : use {@link Solver#intMinusView(IntVar)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar minus(IntVar VAR) {
        return VAR.getSolver().intMinusView(VAR);
    }

    /**
     * @deprecated : use {@link Solver#intScaleView(IntVar, int)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar scale(IntVar VAR, int CSTE) {
        return VAR.getSolver().intScaleView(VAR,CSTE);
    }

    /**
     * @deprecated : use {@link Solver#intAbsView(IntVar)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static IntVar abs(IntVar VAR) {
        return VAR.getSolver().intAbsView(VAR);
    }

    /**
     * @deprecated : use {@link Solver#realIntView(IntVar, double)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealVar real(IntVar VAR, double PRECISION) {
        return VAR.getSolver().realIntView(VAR,PRECISION);
    }

    /**
     * @deprecated : use {@link Solver#realIntViewArray(IntVar[], double)}     instead
     * This will be removed in versions > 3.4.0
     */
    @Deprecated
    public static RealVar[] real(IntVar[] VARS, double PRECISION) {
        return VARS[0].getSolver().realIntViewArray(VARS,PRECISION);
    }
}
