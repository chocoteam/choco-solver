/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import gnu.trove.map.hash.THashMap;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/01/13
 */
public enum Operator {

    NONE(), EQ(), LT(), GT(), NQ(), LE(), GE(), PL(), MN();

	private static THashMap<String, Operator> operators = new THashMap<>();

    static {
        operators.put("@", Operator.NONE);
        operators.put("=", Operator.EQ);
        operators.put(">", Operator.GT);
        operators.put(">=", Operator.GE);
        operators.put("<", Operator.LT);
        operators.put("<=", Operator.LE);
        operators.put("!=", Operator.NQ);
        operators.put("+", Operator.PL);
        operators.put("-", Operator.MN);
    }

	public static Operator get(String name) {
        return operators.get(name);
	}

	@Override
	public String toString() {
		switch (this){
			case LT:return "<";
			case GT:return ">";
			case LE:return "<=";
			case GE:return ">=";
			case NQ:return "!=";
			case EQ:return "=";
			case PL:return "+";
			case MN:return "-";
			default:throw new UnsupportedOperationException();
		}
	}

	/**
	 * Flips the direction of an inequality
	 * @param operator op to flip
	 */
	public static String getFlip(String operator) {
		switch (get(operator)){
			case LT:return ">";
			case GT:return "<";
			case LE:return ">=";
			case GE:return "<=";
			default:return operator;
		}
	}

	public static Operator getOpposite(Operator operator) {
		switch (operator){
			case LT:return GE;
			case GT:return LE;
			case LE:return GT;
			case GE:return LT;
			case NQ:return EQ;
			case EQ:return NQ;
			case PL:return MN;
			case MN:return PL;
			default:throw new UnsupportedOperationException();
		}
	}
}
