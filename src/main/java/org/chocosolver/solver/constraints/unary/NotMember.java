/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class NotMember extends Constraint {

	private final IntVar var;
	private final int[] values;
	private final int lb, ub;

    public NotMember(IntVar var, int[] values) {
        super("NotMember",new PropNotMemberEnum(var, values));
		this.var = var;
        this.values = values;
        lb = 0;
        ub = 0;
    }

    public NotMember(IntVar var, int lowerbound, int upperbound) {
        super("NotMember",new PropNotMemberBound(var, lowerbound, upperbound));
        this.values = null;
		this.var = var;
        this.lb = lowerbound;
        this.ub = upperbound;
    }

	@Override
	public Constraint makeOpposite(){
		if(values==null){
			return new Member(var,lb,ub);
		}else{
			return new Member(var,values);
		}
	}
}
