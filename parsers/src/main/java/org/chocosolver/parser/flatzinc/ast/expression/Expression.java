/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.expression;


import org.chocosolver.parser.Exit;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
*
* Class for expression definition based on flatzinc-like objects.
*/
public abstract class Expression {

    public enum EType {
        ANN, ARR, BOO, IDA, IDE, INT, SET_B, SET_L, STR
    }

    @SuppressWarnings({"InstantiatingObjectToGetClassObject"})
    protected static final Class int_arr = new int[0].getClass();
    @SuppressWarnings({"InstantiatingObjectToGetClassObject"})
    protected static final Class bool_arr = new boolean[0].getClass();

    final EType typeOf;

    protected Expression(EType typeOf) {
        this.typeOf = typeOf;
    }

    public final EType getTypeOf() {
        return typeOf;
    }

    /**
     * Get the int value of the {@link Expression}
     *
     * @return int
     */
    public int intValue() {
        Exit.log();
        return 0;
    }

    /**
     * Get array of int of the {@link Expression}
     *
     * @return int[]
     */
    public int[] toIntArray() {
        Exit.log();
        return null;
    }

    /**
     * Get array of int of the {@link Expression}
     *
     * @return int[]
     */
    public int[][] toIntMatrix() {
        Exit.log();
        return null;
    }

    /**
     * Get the boolean value of the {@link Expression}
     *
     * @return boolean
     */
    public boolean boolValue() {
        Exit.log();
        return true;
    }

    /**
     * Get array of int of the {@link Expression}
     *
     * @return int[]
     */
    public boolean[] toBoolArray() {
        Exit.log();
        return null;
    }

    /**
     * Get the {@link BoolVar} of the {@link Expression}
     *
     * @param model the Model
     * @return {@link BoolVar}
     */
    public BoolVar boolVarValue(Model model) {
        Exit.log();
        return null;
    }

    /**
     * Get an array of {@link BoolVar}[] of the {@link Expression}
     *
     * @param model the Model
     * @return {@link BoolVar}[]
     */
    public BoolVar[] toBoolVarArray(Model model) {
        Exit.log();
        return null;
    }

    /**
     * Get the {@link IntVar} of the {@link Expression}
     *
     * @param model the Model
     * @return {@link IntVar}
     */
    public IntVar intVarValue(Model model) {
        Exit.log();
        return null;
    }

    /**
     * Get an array of {@link IntVar}[] of the {@link Expression}
     *
     * @param solver the Model
     * @return {@link IntVar}[]
     */
    public IntVar[] toIntVarArray(Model solver) {
        Exit.log();
        return null;
    }

    /**
     * Get the {@link SetVar} of the {@link Expression}
     *
     * @param solver the Model
     * @return {@link Variable} or {@link Variable}
     */
    public SetVar setVarValue(Model solver) {
        Exit.log();
        return null;
    }

    /**
     * Get an array of {@link SetVar}[] of the {@link Expression}
     *
     * @param solver the Model
     * @return {@link SetVar}[]
     */
    public SetVar[] toSetVarArray(Model solver) {
        Exit.log();
        return null;
    }

}
