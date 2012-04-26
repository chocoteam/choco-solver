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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 22/04/12
 * Time: 16:00
 */

package samples;

import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.AtLeastNValues;
import solver.constraints.nary.AtMostNValues;
import solver.constraints.nary.IntLinComb;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Created by IntelliJ IDEA.
 */
public class Dobble {

//    public static void nvalueWithIntersectOne(IntVar[] x, int n, int nbsymboles) {
//        m.addConstraint(Choco.atMostNValue(Choco.makeIntVar("atm", n, n, Options.V_NO_DECISION), x));
//        IntegerVariable[] b = Choco.makeIntVarArray("b", nbsymboles  + 1, 0, 1, Options.V_NO_DECISION);
//        IntegerVariable[] c = Choco.makeIntVarArray("c", nbsymboles  + 1, 0, 1, Options.V_NO_DECISION);
//        m.addConstraint(Choco.eq(b[0], 0));
//        m.addConstraint(Choco.eq(c[0], 0));
//        for (int i = 1 ; i <= nbsymboles ; i++) {
//            IntegerVariable occ = Choco.makeIntVar("occ" + i, 0, 2, Options.V_NO_DECISION);
//            m.addConstraint(Choco.occurrence(occ, x, i));
//            m.addConstraint(Choco.reifiedConstraint(b[i], Choco.gt(occ, 0)));
//            m.addConstraint(Choco.reifiedConstraint(c[i], Choco.eq(occ, 2)));
//        }
//        m.addConstraint(Choco.eq(Choco.sum(b), n));
//        m.addConstraint(Choco.eq(Choco.sum(c), 1));
//    }

    public static void main(String[] args) {
		Solver solver = new Solver();
        int nbcartes = 18;
        int nbsymboles = 57;
        int nbsymcarte = 8;
		int k = 2*nbsymcarte-1;
		int off = 2;
		IntVar nValue = VariableFactory.bounded("nv",k, k, solver);
		IntVar nV2 = VariableFactory.bounded("nv",k-off, k-off, solver);
		IntVar nV3 = VariableFactory.bounded("nv",k, k, solver);
		IntVar[][] cartes = new IntVar[nbcartes][];
		for(int i=0;i<nbcartes;i++){
			cartes[i] = VariableFactory.enumeratedArray("cartes", nbsymcarte, 1, nbsymboles, solver);
			//symmetry breaking on each card
            for (int sb = 0; sb <nbsymcarte - 1; sb++) {
				solver.post(ConstraintFactory.lt(cartes[i][sb],cartes[i][sb+1],solver));
            }
        }
		//symmetry breaking (incomplete) between cards
		//stronger filtering would involve Choco.lexChain(cartes)
//		IntVar[] vector = VariableFactory.boundedArray("vector",nbcartes,0,nbsymcarte*nbsymcarte*nbsymboles*nbsymboles,solver);
//		int[] coefs = new int[nbsymcarte];
//		for(int i=0;i<nbsymcarte;i++){
//			coefs[i] = i*nbsymboles;
//		}
//		for(int i=0;i<nbcartes;i++){
//			solver.post(ConstraintFactory.scalar(cartes[i],coefs, IntLinComb.Operator.EQ,vector[i],1,solver));
//		}
//		solver.post(new AllDifferent(vector,solver));
		for(int i=0;i<nbcartes;i++){
			for(int j=i+1;j<nbcartes;j++){
				solver.post(ConstraintFactory.leq(cartes[i][0],cartes[j][0],solver));
			}
        }
		for(int i=0;i<nbcartes;i++){
			for(int j=i+1;j<nbcartes;j++){
//				solver.post(ConstraintFactory.leq(cartes[i][0],cartes[j][0],solver));
//				solver.post(ConstraintFactory.gt(vector[i],vector[j],solver));
			}
        }
		// exactly one single color in common for each pair of cards
        for (int i = 0; i < cartes.length - 1; i++){
            IntVar[] x = cartes[i];
            for (int j =  i + 1; j < cartes.length ; j++) {
                IntVar[] y = cartes[j];
//				solver.post(new AtMostNValues(ArrayUtils.append(x,y),nValue,solver, AtMostNValues.Algo.BC));
				solver.post(new AtMostNValues(ArrayUtils.append(x,y),nValue,solver, AtMostNValues.Algo.Greedy));
//				solver.post(new AtLeastNValues(ArrayUtils.append(x,y),nV2,solver));
				solver.post(new AtLeastNValues(ArrayUtils.append(x,y),nV3,solver));
                //nvalueWithIntersectOne(ArrayUtils.append(x, y), 2 * nbsymcarte - 1, model, nbsymboles);
//                model.addConstraint(integer.SumOfWeightsOfDistinctValues.nvalue(ArrayUtils.append(x, y), Choco.constant(2 * nbsymcarte - 1)));
            }
        }

		SearchMonitorFactory.log(solver, true, false);

		solver.getSearchLoop().getLimitsBox().setTimeLimit(30000);
//		solver.set(StrategyFactory.forceInputOrderMinVal(ArrayUtils.flatten(cartes),solver.getEnvironment()));
		solver.set(StrategyFactory.minDomMinVal(ArrayUtils.flatten(cartes),solver.getEnvironment()));

        solver.findSolution();

//		for(int i=0;i<nbcartes;i++)
//		System.out.println(vector[i]);
    }
}
