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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 15/05/13
 * Time: 18:50
 */

package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.variables.IntVar;

/**
 * Small example enumerating solutions of
 * (x>y && y>z && z>x) || not(AllDifferent(x,y,z)
 * (the left part being always false, it ensures that AllDifferent is violated
 * @author Jean-Guillaume Fages
 * @since 15/05/2013
 */
public class ReifSample extends AbstractProblem {

	IntVar x,y,z;

	@Override
	public void createSolver() {
		solver = new Solver();
	}

	@Override
	public void buildModel() {
		x = solver.intVar("x", 0, 3, false);
		y = solver.intVar("y", 0, 3, false);
		z = solver.intVar("z", 0, 3, false);
		Constraint imp = LCF.and(
				ICF.arithm(x, ">", y),
				ICF.arithm(y, ">", z),
				ICF.arithm(z, ">", x)
		);
		Constraint ad = ICF.alldifferent(new IntVar[]{x, y, z}, "DEFAULT");
		Constraint nad = LCF.not(ad);
		solver.post(LCF.or(imp, nad));
	}

	@Override
	public void configureSearch() {
		solver.set(ISF.lexico_LB(new IntVar[]{x, y, z}));
	}

	@Override
	public void solve() {
		solver.plugMonitor((IMonitorSolution) () -> {
            System.out.println("////////////////");
            System.out.println(x);
            System.out.println(y);
            System.out.println(z);
            System.out.println();
        });
		solver.findAllSolutions();
	}

	@Override
	public void prettyOut() {}

	public static void main(String[] args){
	    new ReifSample().execute();
	}
}
