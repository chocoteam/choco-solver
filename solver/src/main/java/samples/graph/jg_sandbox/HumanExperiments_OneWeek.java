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

package samples.graph.jg_sandbox;

import solver.Cause;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.IntLinComb;
import solver.constraints.nary.cnf.ConjunctiveNormalForm;
import solver.constraints.nary.cnf.Literal;
import solver.constraints.nary.cnf.Node;
import solver.constraints.reified.ReifiedConstraint;
import solver.exception.ContradictionException;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.*;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import solver.variables.view.Views;

import java.io.*;

public class HumanExperiments_OneWeek {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 5000;
	private final static String repo = "/Users/jfages07/Documents/JGTL/BenchmarkCompleted";
	private static Solver solver;
	private static String outFile = "tl.csv";

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		clearFile(outFile);
		writeTextInto("week;solved;time(ms);infeasibleDay\n",outFile);
		File folder = new File(repo);
		String[] list = folder.list();
//		boolean b = false;
		for(String file : list){
//			if(file.contains("Day0_Ta100_Ti130_SkCR_i018.txt"))
//				b = true;
//			if(b)
			if(file.contains("Day0_") && isMaybeFeasible(file)){
				solveWeek(file,list);
			}
		}
	}

	private static void solveWeek(String file, String[] list) {
		int day;
		String week = file.substring(5);
		System.out.println("solving instance "+week);
		//--------------------------------------
		// test day per day
		//--------------------------------------
		long dayTL = 3000;
		for(String f : list){
			if(f.substring(5).equals(week)){
				solver = new Solver();
				day = Character.getNumericValue(f.charAt(3));
				HE_DayModel model = new HE_DayModel(repo+"/"+f,solver);
				solver.getSearchLoop().getLimitsBox().setTimeLimit(dayTL);
				solver.set(StrategyFactory.graphStrategy(model.graph, null, model.getHeuristic(), GraphStrategy.NodeArcPriority.ARCS));
				solver.findSolution();
				if(solver.getMeasures().getSolutionCount()==0
						&& solver.getMeasures().getTimeCount()<dayTL){
					writeTextInto(file+";0;0;"+day+"\n","tl.csv");
					System.out.println("day "+day+" INFEASIBLE");
					return;
				}else if(solver.getMeasures().getTimeCount()>=dayTL){
					System.out.println("day "+day+" UNDERTERMINED");
				}
			}
		}
		System.gc();
		solver = new Solver();
		System.gc();
		//--------------------------------------
		// load week
		//--------------------------------------
		final HE_DayModel[] weekModel = new HE_DayModel[7];
		final AbstractStrategy[] weekStrategies = new AbstractStrategy[7];
		final UndirectedGraphVar[] vars = new UndirectedGraphVar[7];
		for(String f : list){
			if(f.substring(5).equals(week)){
				day = Character.getNumericValue(f.charAt(3));
				weekModel[day] = new HE_DayModel(repo+"/"+f,solver);
				vars[day] = weekModel[day].graph;
				weekStrategies[day] = StrategyFactory.graphStrategy(weekModel[day].graph, null,weekModel[day].getHeuristic(),GraphStrategy.NodeArcPriority.ARCS);
			}
		}
		final int ne = weekModel[0].ne;
		//--------------------------------------
		// add sliding constraints
		//--------------------------------------
		sleepingTime(ne,weekModel);// 11h de repos entre 2 jours travailles
		repos35h(ne,weekModel);	// 35h de repos tous les 7 jours 
		noMoreThanSixDays(ne,weekModel); // pas plus de six jours sans conge
		//--------------------------------------
		// configuration
		//--------------------------------------
		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		solver.set(new AbstractStrategy<Variable>(vars) {
			@Override
			public void init() {
				for (AbstractStrategy a : weekStrategies)
					a.init();
			}

			@Override
			public Decision getDecision() {
				for (int i = 0; i < 7; i++)
					if (!vars[i].instantiated())
						return weekStrategies[i].getDecision();
				return null;
			}
		});
		//--------------------------------------
		// resolution
		//--------------------------------------
		solver.findSolution();
		//--------------------------------------
		// output
		//--------------------------------------
		writeTextInto(file+";"+solver.getMeasures().getSolutionCount()+";"+(int)(solver.getMeasures().getTimeCount())+"\n","tl.csv");
	}

	private static void sleepingTime(final int ne, final HE_DayModel[] weekModel){
		for(int i=0;i<ne;i++){
			int minBegin = 660-weekModel[0].LW[i];
			try {
				weekModel[0].start[i].updateLowerBound(minBegin, Cause.Null);
			} catch (ContradictionException e) {e.printStackTrace(); System.exit(0);}
			for(int j=0;j<6;j++){
				IntVar view = Views.offset(weekModel[j].end[i],660);
				solver.post(ConstraintFactory.geq(weekModel[j+1].start[i], view, solver));
			}
		}
	}

	private static void reifiedSleepingTime(final int ne, final HE_DayModel[] weekModel){
		for(int i=0;i<ne;i++){
			int minBegin = 660-weekModel[0].LW[i];
			BoolVar b0 = VariableFactory.bool("b0,"+i+","+0,solver);
			solver.post(new ConjunctiveNormalForm(
					Node.implies(
							Literal.pos(weekModel[0].workToday[i]),
							Literal.pos(b0)
					),
					solver
			));
			solver.post(new ReifiedConstraint(b0,
					ConstraintFactory.geq(weekModel[0].start[i], minBegin, solver),
					ConstraintFactory.leq(weekModel[0].start[i], minBegin-1, solver),
					solver));
			for(int j=0;j<6;j++){
				IntVar view = Views.offset(weekModel[j].end[i],660);
				BoolVar bj = VariableFactory.bool("b0,"+i+","+j,solver);
				solver.post(new ConjunctiveNormalForm(
						Node.implies(
								Literal.pos(weekModel[j+1].workToday[i]),
								Literal.pos(bj)
						),
						solver
				));
				solver.post(new ReifiedConstraint(bj,
						ConstraintFactory.geq(weekModel[j+1].start[i], view, solver),
						ConstraintFactory.lt( weekModel[j+1].start[i], view, solver),
						solver));
			}
		}
	}

	private static void repos35h(final int ne, final HE_DayModel[] weekModel) {
		for(int i=0;i<ne;i++){
			int k = 7*24*60-weekModel[0].LB[i]-35*60;
			IntVar breakStart = VariableFactory.bounded("break"+i,0,k,solver);
			IntVar breakEnd = Views.offset(breakStart,2100);
			for(int d=0;d<7;d++){
				solver.post(new ReifiedConstraint(VariableFactory.bool("",solver),
						ConstraintFactory.geq(weekModel[d].start[i], breakEnd, solver),
						ConstraintFactory.leq(weekModel[d].end[i], breakStart, solver),
						solver));
			}
		}
	}

	private static void noMoreThanSixDays(final int ne, final HE_DayModel[] weekModel){
		int sundayBefore20h = 24*60*6+20*60;
		for(int i=0;i<ne;i++){
			IntVar nbC = VariableFactory.bounded("nbconge_"+i,1,7,solver);
			BoolVar atLeastTwoConge = VariableFactory.bool("atleast2conges_" + i, solver);
			solver.post(new ReifiedConstraint(atLeastTwoConge,
					ConstraintFactory.geq(nbC, 2, solver),
					ConstraintFactory.eq(nbC, 1, solver),
					solver));
			BoolVar congeOnNextMonday = VariableFactory.bool("congeOnNextMonday_"+i,solver);
			solver.post(new ReifiedConstraint(congeOnNextMonday,
					ConstraintFactory.leq(weekModel[6].end[i],sundayBefore20h,solver),
					ConstraintFactory.geq(weekModel[6].end[i], sundayBefore20h + 1, solver),
					solver));
			solver.post(new ConjunctiveNormalForm(
					Node.implies(
							Literal.pos(weekModel[0].conge[i]),
							Node.or(Literal.pos(atLeastTwoConge), Literal.pos(congeOnNextMonday))
					),solver
			));
			int lo = weekModel[0].LO[i];
			IntVar[] p = new IntVar[7];
			IntVar[] p1 = new IntVar[7-lo];
			for(int d=0;d<7;d++){
				if(d<7-lo){
					p1[d] = weekModel[d].conge[i];
				}
				p[d] = weekModel[d].conge[i];
			}
			solver.post(ConstraintFactory.sum(p,IntLinComb.Operator.EQ,nbC,1,solver));
			solver.post(ConstraintFactory.sum(p1,IntLinComb.Operator.GEQ,1,solver));
			if(weekModel[0].LW[i]<4*60){
				try {
					weekModel[0].conge[i].instantiateTo(0, Cause.Null);
				} catch (ContradictionException e) {
					e.printStackTrace();System.exit(0);
				}
			}
			for(int d=1;d<7;d++){
				// fini avant 20h la veille
				BoolVar before20h = VariableFactory.bool("before20h,"+i+","+d,solver);
				solver.post(new ReifiedConstraint(before20h,
						ConstraintFactory.leq(weekModel[d-1].end[i], (d-1)*24*60+20*60,   solver),
						ConstraintFactory.geq(weekModel[d-1].end[i], (d-1)*24*60+20*60+1, solver),
						solver));
				BoolVar dontWork = VariableFactory.bool("dontWork,"+i+","+d,solver);
				solver.post(new ReifiedConstraint(dontWork,
						ConstraintFactory.eq(weekModel[d].start[i], weekModel[d].end[i], solver),
						ConstraintFactory.neq(weekModel[d].start[i], weekModel[d].end[i],solver),
						solver));
				solver.post(new ConjunctiveNormalForm(
						Node.ifOnlyIf(
								Literal.pos(weekModel[d].conge[i]),
								Node.and(Literal.pos(before20h),Literal.pos(dontWork))
						),
						solver
				));
			}
		}
	}

	//***********************************************************************************
	// RECORDING RESULTS
	//***********************************************************************************

	public static void writeTextInto(String text, String file) {
		try {
			FileWriter out = new FileWriter(file, true);
			out.write(text);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearFile(String file) {
		try {
			FileWriter out = new FileWriter(file, false);
			out.write("");
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//***********************************************************************************
	// BLACK LIST --- trivially infeasible instances (because of at least one day)
	//***********************************************************************************

	private static boolean isMaybeFeasible(String inst){
//		for(String s:blackList){
//			if(inst.contains(s)){
//				return false;
//			}
//		}
		return true;
	}

	private final static String[] blackList = new String[]{};
}