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
 * Date: 14/11/12
 * Time: 16:13
 */

package samples;

import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.NValues;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * @author Alban DERRIEN
 */
public class Dobble {

	public static void main(String[] args) {
		int nbSymbCarte=5;
		int nbCarte=21;
		int nbSymbTotal=21;

		Solver solver = new Solver();
		
		IntVar[][] jeu = new IntVar[nbCarte][];
		IntVar nValTotal = VariableFactory.bounded("total",0,nbSymbTotal,solver);
		for(int i=0;i<nbCarte;i++) {
			jeu[i] = VariableFactory.enumeratedArray("Carte_"+i, nbSymbCarte, 1, nbSymbTotal, solver);
		}
		IntVar nbNValues = VariableFactory.enumerated("nbEnCommun", nbSymbCarte * 2 - 1, nbSymbCarte * 2 - 1, solver);
		for(int i=0;i<nbCarte;i++) {
			solver.post(new AllDifferent(jeu[i], solver,AllDifferent.Type.AC));//chaque carte a des symboles distinctes
			for(int j=0; j<nbSymbCarte-1;j++){
				solver.post(ConstraintFactory.lt(jeu[i][j], jeu[i][j + 1], solver));
			}
			for(int j=i+1;j<nbCarte;j++) {//pour tout couple de carte :
				//au total de 2 cartes, il doit y avoir 2n - 1 valeurs, car 1 est en commun, et une seule.
				solver.post(new NValues(ArrayUtils.append(jeu[i], jeu[j]), nbNValues, solver));
			}
		}
		for(int i=0;i<nbCarte-1;i++) {
			solver.post(ConstraintFactory.leq(jeu[i][0], jeu[i + 1][0], solver));
		}
		solver.post(new NValues(ArrayUtils.flatten(jeu),nValTotal,solver));
		// TODO conjonction differences par carte / nvalue => passer par des graphes!!!

//		solver.set(StrategyFactory.minDomMinVal(ArrayUtils.flatten(jeu), solver.getEnvironment()));
		solver.set(StrategyFactory.inputOrderMinVal(ArrayUtils.flatten(jeu),solver.getEnvironment()));
		SearchMonitorFactory.log(solver, true, false);
		solver.getSearchLoop().getLimitsBox().setTimeLimit(600*1000);
		solver.findSolution();
	}
}