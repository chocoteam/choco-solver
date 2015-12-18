/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.chocosolver.parser.flatzinc.ast.expression;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for int expressions definition based on flatzinc-like objects.
*/
public final class EInt extends Expression {

    public final int value;

    private static EInt[] cache;
    final static int low = -128;

    // high value may be configured by property
    final static int high = 127;

    static {
        cache = new EInt[(high - low) + 1];
        int j = low;
        for (int k = 0; k < cache.length; k++)
            cache[k] = new EInt(j++);
    }


//    public EInt(String sign, String svalue) {
//        super(EType.INT);
//        value = Integer.parseInt((sign.equals("-")?sign:"") + svalue);
//    }

    public static EInt make(String svalue) {
        int value = Integer.parseInt(svalue);
        return make(value);
    }

    public static EInt make(int value) {
        if (value >= -128 && value <= high)
            return cache[value + 128];
        else
            return new EInt(value);
    }

    private EInt(int value) {
        super(EType.INT);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public int[] toIntArray() {
        return new int[]{intValue()};
    }

    @Override
    public int[][] toIntMatrix() {
        return new int[][]{{intValue()}};
    }


    @Override
    public IntVar intVarValue(Solver solver) {
        return VariableFactory.fixed(intValue(), solver);
    }

    @Override
    public IntVar[] toIntVarArray(Solver solver) {
        return new IntVar[]{intVarValue(solver)};
    }
}
