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
package org.chocosolver.solver.constraints;

import gnu.trove.map.hash.THashMap;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 21/01/13
 */
public enum Operator {

    NONE(-1), EQ(0), LT(1), GT(2), NQ(3), LE(4), GE(5), PL(6), MN(7);

	private int num;

    Operator(int num) {
        this.num = num;
    }

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
