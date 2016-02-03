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
import org.chocosolver.util.tools.StringUtils;

/**
 * Interface to make variables
 *
 * @author Jean-Guillaume FAGES (www.cosling.com)
 */
public interface Vars {

    Solver _me();

    //*************************************************************************************
    // BOOLEAN VARIABLES
    //*************************************************************************************

    default BoolVar makeBoolVar(boolean CONSTANT) {
        return VariableFactory.fixed(CONSTANT, _me());
    }

    default BoolVar makeBoolVar() {
        return makeBoolVar(StringUtils.randomName());
    }

    default BoolVar makeBoolVar(String NAME) {
        return VariableFactory.bool(NAME, _me());
    }

    // ARRAY

    default BoolVar[] makeBoolVarArray(int SIZE) {
        return makeBoolVarArray(StringUtils.randomName(),SIZE);
    }

    default BoolVar[] makeBoolVarArray(String NAME, int SIZE) {
        BoolVar[] vars = new BoolVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeBoolVar(NAME + "[" + i + "]");
        }
        return vars;
    }

    // MATRIX

    default BoolVar[][] makeBoolVarMatrix(int DIM1, int DIM2) {
        return makeBoolVarMatrix(StringUtils.randomName(),DIM1,DIM2);
    }

    default BoolVar[][] makeBoolVarMatrix(String NAME, int DIM1, int DIM2) {
        BoolVar[][] vars = new BoolVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeBoolVarArray(NAME + "[" + i + "]", DIM2);
        }
        return vars;
    }

    //*************************************************************************************
    // INTEGER VARIABLES
    //*************************************************************************************

    // SINGLE

    default IntVar makeIntVar(int CONSTANT) {
        return makeIntVar(StringUtils.randomName(),CONSTANT);
    }

    default IntVar makeIntVar(int[] VALUES) {
        return makeIntVar(StringUtils.randomName(),VALUES);
    }

    default IntVar makeIntVar(int MIN, int MAX) {
        return makeIntVar(StringUtils.randomName(),MIN, MAX);
    }

    default IntVar makeIntVar(int MIN, int MAX, boolean boundedDomain) {
        return makeIntVar(StringUtils.randomName(), MIN, MAX, boundedDomain);
    }

    default IntVar makeIntVar(String NAME, int CONSTANT) {
        return VariableFactory.fixed(NAME, CONSTANT, _me());
    }

    default IntVar makeIntVar(String NAME, int MIN, int MAX, boolean boundedDomain) {
        if (boundedDomain) {
            return VariableFactory.bounded(NAME, MIN, MAX, _me());
        } else {
            return VariableFactory.enumerated(NAME, MIN, MAX, _me());
        }
    }

    default IntVar makeIntVar(String NAME, int MIN, int MAX) {
        boolean bounded = MAX - MIN + 1 >= _me().getSettings().getMaxDomSizeForEnumerated();
        return makeIntVar(NAME, MIN, MAX, bounded);
    }

    default IntVar makeIntVar(String NAME, int[] VALUES) {
        return VariableFactory.enumerated(NAME, VALUES, _me());
    }

    // ARRAY

    default IntVar[] makeIntVarArray(int SIZE, int[] VALUES) {
        return makeIntVarArray(StringUtils.randomName(), SIZE, VALUES);
    }

    default IntVar[] makeIntVarArray(int SIZE, int MIN, int MAX) {
        return makeIntVarArray(StringUtils.randomName(), SIZE, MIN, MAX);
    }

    default IntVar[] makeIntVarArray(int SIZE, int MIN, int MAX, boolean boundedDomain) {
        return makeIntVarArray(StringUtils.randomName(), SIZE, MIN, MAX, boundedDomain);
    }

    default IntVar[] makeIntVarArray(String NAME, int SIZE, int MIN, int MAX, boolean boundedDomain) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeIntVar(NAME + "[" + i + "]", MIN, MAX, boundedDomain);
        }
        return vars;
    }

    default IntVar[] makeIntVarArray(String NAME, int SIZE, int MIN, int MAX) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeIntVar(NAME + "[" + i + "]", MIN, MAX);
        }
        return vars;
    }

    default IntVar[] makeIntVarArray(String NAME, int SIZE, int[] VALUES) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeIntVar(NAME + "[" + i + "]", VALUES);
        }
        return vars;
    }

    // MATRIX

    default IntVar[][] makeIntVarMatrix(int DIM1, int DIM2, int[] VALUES) {
        return makeIntVarMatrix(StringUtils.randomName(), DIM1, DIM2, VALUES);
    }

    default IntVar[][] makeIntVarMatrix(int DIM1, int DIM2, int MIN, int MAX) {
        return makeIntVarMatrix(StringUtils.randomName(), DIM1, DIM2, MIN, MAX);
    }

    default IntVar[][] makeIntVarMatrix(int DIM1, int DIM2, int MIN, int MAX, boolean boundedDomain) {
        return makeIntVarMatrix(StringUtils.randomName(), DIM1, DIM2, MIN, MAX, boundedDomain);
    }

    default IntVar[][] makeIntVarMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, boolean boundedDomain) {
        IntVar[][] vars = new IntVar[DIM1][DIM2];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeIntVarArray(NAME + "[" + i + "]", DIM2, MIN, MAX, boundedDomain);
        }
        return vars;
    }

    default IntVar[][] makeIntVarMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX) {
        IntVar[][] vars = new IntVar[DIM1][DIM2];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeIntVarArray(NAME + "[" + i + "]", DIM2, MIN, MAX);
        }
        return vars;
    }

    default IntVar[][] makeIntVarMatrix(String NAME, int DIM1, int DIM2, int[] VALUES) {
        IntVar[][] vars = new IntVar[DIM1][DIM2];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeIntVarArray(NAME + "[" + i + "]", DIM2, VALUES);
        }
        return vars;
    }


    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    default RealVar makeRealVar(String NAME, double MIN, double MAX, double PRECISION) {
        return VariableFactory.real(NAME, MIN, MAX, PRECISION, _me());
    }

    default RealVar makeRealView(IntVar VAR, double PRECISION) {
        return VariableFactory.real(VAR, PRECISION);
    }

    // ARRAY

    default RealVar[] makeRealVarArray(String NAME, int SIZE, double MIN, double MAX, double PRECISION) {
        RealVar[] vars = new RealVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeRealVar(NAME + "[" + i + "]", MIN, MAX, PRECISION);
        }
        return vars;
    }

    default RealVar[] makeRealViewArray(IntVar[] VAR, double PRECISION) {
        return VariableFactory.real(VAR, PRECISION);
    }

    // MATRIX

    default RealVar[][] makeRealVarMatrix(String NAME, int DIM1, int DIM2, double MIN, double MAX, double PRECISION) {
        RealVar[][] vars = new RealVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeRealVarArray(NAME + "[" + i + "]", DIM2, MIN, MAX, PRECISION);
        }
        return vars;
    }

    default RealVar[][] makeRealViewMatrix(IntVar[][] VAR, double PRECISION) {
        RealVar[][] vars = new RealVar[VAR.length][VAR[0].length];
        for (int i = 0; i < VAR.length; i++) {
            vars[i] = makeRealViewArray(VAR[i], PRECISION);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    default SetVar makeSetVar(int[] KERNEL, int[] ENVELOPE) {
        return makeSetVar(StringUtils.randomName(), KERNEL, ENVELOPE);
    }

    default SetVar makeSetVar(int[] CONSTANT_SET) {
        return makeSetVar(StringUtils.randomName(), CONSTANT_SET);
    }

    default SetVar makeSetVar(String NAME, int[] KERNEL, int[] ENVELOPE) {
        return VariableFactory.set(NAME, ENVELOPE, KERNEL, _me());
    }

    default SetVar makeSetVar(String NAME, int[] CONSTANT_SET) {
        return VariableFactory.fixed(NAME, CONSTANT_SET, _me());
    }

    // ARRAY

    default SetVar[] makeSetVarArray(int SIZE, int[] KERNEL, int[] ENVELOPE) {
        return makeSetVarArray(StringUtils.randomName(), SIZE, KERNEL, ENVELOPE);
    }

    default SetVar[] makeSetVarArray(String NAME, int SIZE, int[] KERNEL, int[] ENVELOPE) {
        SetVar[] vars = new SetVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = makeSetVar(NAME + "[" + i + "]", KERNEL, ENVELOPE);
        }
        return vars;
    }

    // MATRIX

    default SetVar[][] makeSetVarMatrix(int DIM1, int DIM2, int[] KERNEL, int[] ENVELOPE) {
        return makeSetVarMatrix(StringUtils.randomName(), DIM1, DIM2, KERNEL, ENVELOPE);
    }

    default SetVar[][] makeSetVarMatrix(String NAME, int DIM1, int DIM2, int[] KERNEL, int[] ENVELOPE) {
        SetVar[][] vars = new SetVar[DIM1][DIM2];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = makeSetVarArray(NAME + "[" + i + "]", DIM2, KERNEL, ENVELOPE);
        }
        return vars;
    }
}
