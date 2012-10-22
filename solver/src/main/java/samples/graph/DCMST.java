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

package samples.graph;

import choco.kernel.ESat;
import choco.kernel.ResolutionPolicy;
import samples.graph.input.DCMST_Utils;
import samples.graph.output.TextWriter;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.GraphConstraintFactory;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.constraints.propagators.gary.basic.PropKCC;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtLeast;
import solver.constraints.propagators.gary.degree.PropNodeDegree_AtMost;
import solver.constraints.propagators.gary.trees.PropTreeEvalObj;
import solver.constraints.propagators.gary.trees.PropTreeNoSubtour;
import solver.constraints.propagators.gary.trees.lagrangianRelaxation.*;
import solver.exception.ContradictionException;
import solver.objective.MinObjectiveManager;
import solver.objective.strategies.BottomUp_Minimization;
import solver.objective.strategies.Dichotomic_Minimization;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PArc;
import solver.propagation.generator.Sort;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.loop.monitors.VoidSearchMonitor;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.search.strategy.strategy.graph.ArcStrategy;
import solver.search.strategy.strategy.graph.GraphStrategy;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.setDataStructures.ISet;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;
import java.io.*;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Parse and solve an symmetric Traveling Salesman Problem instance of the TSPLIB
 */
public class DCMST {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	//	private static int upperBound;
	// input
	private static String dir = "/Users/jfages07/Desktop/ConstrainedTrees/instances";
	private static String testalagPath = "/Users/jfages07/Desktop/ConstrainedTrees/archive/codeAlex";// path of testalag
	private static String instanceName;
	private static int n,nMin,nMax;
	public static int[] dMax;
	private static int[][] dist;
	// model
	public static IntVar totalCost;
	private static Solver solver;
	private static int search;
	private static int lb,ub;
	public static int optimum;
	// other
	private static long TIMELIMIT = 10000000;
	private static String outFile;
	private static PropLagr_DCMST hk;
	private static boolean optGiven = false;
	static boolean moreHK;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		//DE,DR,instanciasT
		bench("instanciasT");
	}

	public static void bench(String type) {
		if(optGiven){
			search = 0;
		}else{
			search = 1;
		}
		TextWriter.clearFile(outFile = type + "botup_minCostTieBreak_s" + search + ".csv");
		TextWriter.writeTextInto("instance;sols;fails;nodes;time;obj;lb;ub;search;\n", outFile);
		File folder = new File(dir+"/"+type);
		String[] list = folder.list();
		nMin = 100;
		nMax = 900;
		for (String s : list) {
			File file = new File(dir+"/"+type+"/"+s);
//			if(!(s.contains("500_2")||s.contains("500_1")))
//			if(s.contains("300_3"))
//			if(s.contains("400_1"))
//			if(s.contains("200_2"))
//				if(s.contains("_1"))
//				if(!s.contains("_2"))
//				if(!s.contains("_3"))
//			if(s.contains("300_3")||s.contains("500_2")||s.contains("20f0_2"))
//			if(s.contains("2000_4"))
					if((!file.isHidden()) && (!s.contains("bounds.csv")) && (!s.contains("bug"))){
						instanceName = s;
						System.out.println(s);
						if(parse(file,nMin,nMax,dir,type,s)){
							if(optGiven){
								ub = optimum;
							}
							moreHK = false;
							solveDCMST(s);
						}
						System.gc();
					}
		}
	}

	public static boolean parse(File file, int nMin, int nMax, String dirOpt, String type, String s) {
		DCMST_Utils inst = new DCMST_Utils();
		if(inst.parse_T_DE_DR(file,nMin,nMax,dirOpt,type,s)){
			n = inst.n;
			lb = inst.lb;
			ub = inst.ub;
			optimum = inst.optimum;
			dist = inst.costs;
			dMax = inst.dMax;
			return true;
		}
		return false;
	}

	private static void solveDCMST(String instanceName) {
		solver = new Solver();
		totalCost = VariableFactory.bounded("obj",lb,ub,solver);
		final UndirectedGraphVar undi = new UndirectedGraphVar(solver, n, GraphType.ENVELOPE_SWAP_ARRAY, GraphType.LINKED_LIST,true);
		for(int i=0;i<n;i++){
			undi.getKernelGraph().activateNode(i);
			for(int j=i+1;j<n;j++){
				if(dist[i][j]!=-1 && !(dMax[i]==1 && dMax[j]==1)){
					undi.getEnvelopGraph().addEdge(i,j);
				}
			}
		}
		// constraints
		Constraint gc = GraphConstraintFactory.makeConstraint(solver);
		gc.addPropagators(new PropNodeDegree_AtLeast(undi, 1, gc, solver));
		gc.addPropagators(new PropNodeDegree_AtMost(undi, dMax, gc, solver));
		gc.addPropagators(new PropTreeNoSubtour(undi, gc, solver));
		gc.addPropagators(new PropKCC(undi, solver, gc, VariableFactory.bounded("1",1,1,solver)));
		gc.addPropagators(new PropTreeEvalObj(undi, totalCost, dist, gc, solver));

		gc.addPropagators(new PropOneNodes(undi, solver, gc));

		hk = PropLagr_DCMST.mstBasedRelaxation(undi, totalCost, dMax, dist, gc, solver);
		hk.waitFirstSolution(!optGiven);
		gc.addPropagators(hk);

		PropLagr_DCMST_withCuts hk2 = PropLagr_DCMST_withCuts.mstBasedRelaxation(undi, totalCost, dMax, dist, gc, solver);
		hk2.waitFirstSolution(!optGiven);
		gc.addPropagators(hk2);

		solver.post(gc);

		solver.getSearchLoop().plugSearchMonitor(new VoidSearchMonitor(){
			public void afterInitialPropagation() {
				int narc = 0;
				int nkarc = 0;
				int maxD = 0;
				for(int i=0;i<n;i++){
					narc += undi.getEnvelopGraph().getSuccessorsOf(i).getSize();
					if(maxD < undi.getEnvelopGraph().getSuccessorsOf(i).getSize()){
						maxD = undi.getEnvelopGraph().getSuccessorsOf(i).getSize();
					}
					nkarc+= undi.getKernelGraph().getSuccessorsOf(i).getSize();
				}
				narc /= 2;
				nkarc/= 2;
				System.out.println("%%%%%%%%%%%");
				System.out.println("M : "+narc+" / "+nkarc+"            "+(int)(solver.getMeasures().getInitialPropagationTimeCount()/1000)+"s");
				System.out.println("%%%%%%%%%%%");
				System.out.println(totalCost);
				System.out.println("%%%%%%%%%%%");
				System.out.println("max degree = "+maxD);
			}
		});

		// config
		AbstractStrategy firstSol = StrategyFactory.graphStrategy(undi,null,new FirstSol(undi), GraphStrategy.NodeArcPriority.ARCS);
		AbstractStrategy gs = StrategyFactory.graphStrategy(undi,null,new OneNodeOutMST(undi), GraphStrategy.NodeArcPriority.ARCS);
		AbstractStrategy strat = new Change(undi,firstSol,gs);
		switch (search){
			//ANDINST : first (if fail<100) then strat 0 truetrick
			//RANDOM :
			case 0: solver.set(gs);break;
			case 1: solver.set(new StaticStrategiesSequencer(new BottomUp_Minimization(totalCost),strat));break;
			case 2: solver.set(new StaticStrategiesSequencer(new Dichotomic_Minimization(totalCost,solver),strat));break;
			default: throw new UnsupportedOperationException();
		}
		IPropagationEngine propagationEngine = new PropagationEngine(solver.getEnvironment());
		solver.set(propagationEngine.set(new Sort(new PArc(propagationEngine, gc)).clearOut()));

		solver.getSearchLoop().getLimitsBox().setTimeLimit(TIMELIMIT);
		SearchMonitorFactory.log(solver, true, false);
		// resolution
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalCost);
		if(solver.getMeasures().getSolutionCount()==0 && solver.getMeasures().getTimeCount()<TIMELIMIT){
			throw new UnsupportedOperationException();
		}
		if(solver.getSearchLoop().getObjectivemanager().getBestValue()!=optimum && solver.getMeasures().getTimeCount()<TIMELIMIT){
			throw new UnsupportedOperationException();
		}
		if(solver.getMeasures().getSolutionCount()>1 && optGiven){
			throw new UnsupportedOperationException();
		}
		//output
		MinObjectiveManager man = (MinObjectiveManager)solver.getSearchLoop().getObjectivemanager();
		int bestLB = man.getBestKnownLowerBound();
		int bestUB = man.getBestKnownUpperBound();
		int bestCost = solver.getSearchLoop().getObjectivemanager().getBestValue();
		String txt = instanceName + ";" + solver.getMeasures().getSolutionCount() + ";" + solver.getMeasures().getFailCount() + ";"
				+ solver.getMeasures().getNodeCount() + ";"+ (int)(solver.getMeasures().getTimeCount()) + ";" + bestCost +";"+bestLB+";"+bestUB+";"+search+";\n";
		TextWriter.writeTextInto(txt, outFile);
	}

	private static class MST_MinDeg extends ArcStrategy<UndirectedGraphVar>{

		public MST_MinDeg (UndirectedGraphVar g){
			super(g);
		}

		@Override
		public boolean computeNextArc() {
			from = -1;
			to = -1;
			original();
			if(from==-1){
				return false;
			}
			return true;
		}

		private void minDelta(){
			ISet nei;
			int minDelta = 5*n;
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				int e = g.getEnvelopGraph().getNeighborsOf(i).getSize();
				if(e!=k && e>dMax[i])
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							if(dMax[i]-k<minDelta){
								minDelta = dMax[i]-k;
							}
						}
					}
			}
			int minDeg = 3*n;
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k && dMax[i]-k==minDelta)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
									+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
							if(d<minDeg){
								minDeg = d;
								from = i;
								to = j;
							}
						}
					}
			}
			if(to!=-1){
				return;
			}
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				int e = g.getEnvelopGraph().getNeighborsOf(i).getSize();
				if(e!=k)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							if(dMax[i]-k<minDelta){
								minDelta = dMax[i]-k;
							}
						}
					}
			}
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k && dMax[i]-k==minDelta)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
									+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
							if(d<minDeg){
								minDeg = d;
								from = i;
								to = j;
							}
						}
					}
			}
		}

		private void minDelta2(){
			ISet nei;
			int minDelta = 5*n;
			int maxDeg = 0;
			from = -1;
			for(int i=0;i<n;i++){
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				int e = g.getEnvelopGraph().getNeighborsOf(i).getSize();
				if(e!=k && e>dMax[i] && e>maxDeg){
					maxDeg = e;
					from = e;
				}
			}
			if(from==-1){
				for(int i=0;i<n;i++){
					int k = g.getKernelGraph().getNeighborsOf(i).getSize();
					int e = g.getEnvelopGraph().getNeighborsOf(i).getSize();
					if(e!=k && e>maxDeg){
						maxDeg = e;
						from = e;
					}
				}
			}
			if(from==-1){
				return;
			}
			int minDeg = 3*n;
			nei = g.getEnvelopGraph().getSuccessorsOf(from);
//			nei = hk.getSupport().getSuccessorsOf(i);
			int k = g.getKernelGraph().getNeighborsOf(from).getSize();
			for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
				if(!g.getKernelGraph().arcExists(from,j)){
					int d = g.getEnvelopGraph().getNeighborsOf(j).getSize();
					if(d<minDeg){
						minDeg = d;
						to = j;
					}
				}
			}
			if(to==-1){
				System.out.println("g");
				System.out.println(nei);
				System.out.println(g.getKernelGraph().getNeighborsOf(from));
				System.exit(0);
			}
		}

		private void original(){
			ISet nei;
			int minDelta = 5*n;
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				int e = g.getEnvelopGraph().getNeighborsOf(i).getSize();
				if(e!=k)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							if(dMax[i]-k<minDelta){
								minDelta = dMax[i]-k;
							}
						}
					}
			}
			int minDeg = 0;//3*n;
			for(int i=0;i<n;i++){
				nei = hk.getSupport().getSuccessorsOf(i);
				int k = g.getKernelGraph().getNeighborsOf(i).getSize();
				if(g.getEnvelopGraph().getNeighborsOf(i).getSize()!=k && dMax[i]-k==minDelta)
					for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
						if(!g.getKernelGraph().arcExists(i,j)){
							int d = g.getEnvelopGraph().getNeighborsOf(i).getSize()
									+ g.getEnvelopGraph().getNeighborsOf(j).getSize();
							if(d>minDeg){
								minDeg = d;
								from = i;
								to = j;
							}
						}
					}
			}
		}

	}

	private static class FirstSol extends ArcStrategy<UndirectedGraphVar>{

		public FirstSol (UndirectedGraphVar g){
			super(g);
		}

		@Override
		public boolean computeNextArc() {
			from = -1;
			to = -1;
			int minCost = 0;
			ISet env,ker;
			//new
			for(int i=0;i<n;i++){
				ker = g.getKernelGraph().getSuccessorsOf(i);
				env = g.getEnvelopGraph().getSuccessorsOf(i);
				if(ker.getSize()<dMax[i]-1)
					if(ker.getSize()==0){
						for(int j=env.getFirstElement();j>=0;j=env.getNextElement()){
							int cost = dist[i][j];
							if(g.getKernelGraph().getSuccessorsOf(j).getSize()<dMax[j]-1)
								if(to==-1 || cost<minCost){
									minCost = cost;
									from = i;
									to = j;
								}
						}
					}
			}
			if(to!=-1){
				return true;
			}
			for(int i=0;i<n;i++){
				ker = g.getKernelGraph().getSuccessorsOf(i);
				env = g.getEnvelopGraph().getSuccessorsOf(i);
				if(ker.getSize()==0){
					for(int j=env.getFirstElement();j>=0;j=env.getNextElement()){
						int cost = dist[i][j];
						if(to==-1 || cost<minCost){
							minCost = cost;
							from = i;
							to = j;
						}
					}
				}
			}
			if(to!=-1){
				return true;
			}
			for(int i=0;i<n;i++){
				ker = g.getKernelGraph().getSuccessorsOf(i);
				env = g.getEnvelopGraph().getSuccessorsOf(i);
				if(env.getSize()!=ker.getSize()){
					for(int j=env.getFirstElement();j>=0;j=env.getNextElement()){
						if(i<j && !ker.contain(j)){
							int cost = dist[i][j];
							if(to==-1 || cost<minCost){
								minCost = cost;
								from = i;
								to = j;
							}
						}
					}
				}
			}
			if(from==-1){
				return false;
			}
			return true;
		}
	}

	private static class Change extends AbstractStrategy<UndirectedGraphVar>{

		AbstractStrategy[] strats;
		public Change (UndirectedGraphVar g, AbstractStrategy... strats){
			super(new UndirectedGraphVar[]{g});
			this.strats = strats;
		}

		@Override
		public void init() {
			for(int i=0;i<strats.length;i++){
				strats[i].init();
			}
		}

		@Override
		public Decision getDecision() {
			if(solver.getMeasures().getSolutionCount()==0){
//				if(solver.getMeasures().getFailCount()<100){
				return strats[0].getDecision();
//				}
			}
			return strats[1].getDecision();
		}
	}

	private static class PropOneNodes extends Propagator<UndirectedGraphVar>{

		BitSet oneNode;
		int[] counter;

		protected PropOneNodes(UndirectedGraphVar vars, Solver solver, Constraint constraint) {
			super(new UndirectedGraphVar[]{vars}, solver, constraint, PropagatorPriority.LINEAR, true);
			oneNode = new BitSet(n);
			counter = new int[n];
		}

		@Override
		public int getPropagationConditions(int vIdx) {
			return EventType.REMOVEARC.mask;
		}

		@Override
		public void propagate(int evtmask) throws ContradictionException {
			preprocessOneNodes();
			UndirectedGraphVar g = vars[0];
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				if(oneNode.get(i))
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(oneNode.get(j)){
						if(!g.getKernelGraph().edgeExists(i,j)){
							g.removeArc(i, j, this);
						}
					}
				}
			}
		}

		@Override
		public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
			propagate(0);
		}

		@Override
		public ESat isEntailed() {
			return ESat.UNDEFINED;
		}

		private void preprocessOneNodes() {
			ISet nei;
			oneNode.clear();
			for(int i = 0;i<n;i++){
				counter[i] = 0;
			}
			UndirectedGraphVar g = vars[0];
			int[] maxDegree = dMax;
			LinkedList<Integer> list = new LinkedList<Integer>();
			for(int first = 0;first<n;first++){
				if(maxDegree[first]==1 && !oneNode.get(first)){
					int k=first;
					list.add(k);
					while(!list.isEmpty()){
						k = list.removeFirst();
						oneNode.set(k);
						nei = g.getKernelGraph().getSuccessorsOf(k);
						for(int s=nei.getFirstElement();s>=0;s=nei.getNextElement()){
							if(!oneNode.get(s)){
								counter[s]++;
								if(counter[s]>=maxDegree[s]){
									if(g.instantiated()){
										//ok
									}else{
										throw new UnsupportedOperationException();
									// contradiction mais devrait deja avoir ete capturee
									}
								}
								if(counter[s]==maxDegree[s]-1){
									oneNode.set(s);
									list.addLast(s);
								}
							}
						}
					}
				}
			}
		}
	}

	private static class OneNodeOutMST extends ArcStrategy<UndirectedGraphVar>{

		public OneNodeOutMST (UndirectedGraphVar g){
			super(g);
		}

		@Override
		public boolean computeNextArc() {
			return computeNext();
//			return computeNextInverse();
		}
		public boolean computeNext() {
			if(from!=-1 && g.getEnvelopGraph().getSuccessorsOf(from).getSize()!=g.getKernelGraph().getSuccessorsOf(from).getSize()){
				to = -1;
				int i = from;
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 || dMax[j]==1){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 || dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
			}
			from = -1;
			to = -1;
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 || dMax[j]==1){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 || dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
			}
			return false;
		}
		public boolean computeNextInverse() {
			if(from!=-1 && g.getEnvelopGraph().getSuccessorsOf(from).getSize()!=g.getKernelGraph().getSuccessorsOf(from).getSize()){
				to = -1;
				int i = from;
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[j]>1){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						from = i;
						to = j;
						return true;
					}
				}
			}
			from = -1;
			to = -1;
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]>1 && dMax[j]>1){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						from = i;
						to = j;
						return true;
					}
				}
			}
			return false;
		}
		public boolean computePatat() {
			if(from!=-1 && g.getEnvelopGraph().getSuccessorsOf(from).getSize()!=g.getKernelGraph().getSuccessorsOf(from).getSize()){
				to = -1;
				int i = from;
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 || dMax[j]==1){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 || dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
			}
			from = -1;
			to = -1;
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==1 || dMax[j]==1){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 && dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j))
							if(dMax[i]==2 || dMax[j]==2){
								from = i;
								to = j;
								return true;
							}
					}
				}
			}
			for(int i=0;i<n;i++){
				ISet nei = g.getEnvelopGraph().getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!g.getKernelGraph().edgeExists(i,j)){
						if(!hk.contains(i,j)){
							from = i;
							to = j;
							return true;
						}
					}
				}
			}
			return false;
		}
		public boolean computeMax() {
			from = -1;
			int minDelta = 0;
			for(int i=0;i<n;i++){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(g.getEnvelopGraph().getSuccessorsOf(i).getSize()!= k){
					if(dMax[i]-k>minDelta){
						minDelta = dMax[i]-k;
						from = i;
					}
				}
			}
			if(from==-1){
				return false;
			}
			to = -1;
			minDelta = 0;
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(from);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(!g.getKernelGraph().edgeExists(from,i)){
					if(!hk.contains(from,i))
					if(dMax[i]-k>minDelta){
						minDelta = dMax[i]-k;
						to = i;
					}
				}
			}
			if(to==-1)
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(!g.getKernelGraph().edgeExists(from,i)){
					if(dMax[i]-k>minDelta){
						minDelta = dMax[i]-k;
						to = i;
					}
				}
			}
			if(to==-1){
				throw new UnsupportedOperationException();
			}
			return true;
		}
		public boolean computeMin() {
			from = -1;
			int minDelta = n*2;
			for(int i=0;i<n;i++){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(g.getEnvelopGraph().getSuccessorsOf(i).getSize()!= k){
					if(dMax[i]-k<minDelta){
						minDelta = dMax[i]-k;
						from = i;
					}
				}
			}
			if(from==-1){
				return false;
			}
			to = -1;
			minDelta = 2*n;
			ISet nei = g.getEnvelopGraph().getSuccessorsOf(from);
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(!g.getKernelGraph().edgeExists(from,i)){
					if(!hk.contains(from,i))
					if(dMax[i]-k<minDelta){
						minDelta = dMax[i]-k;
						to = i;
					}
				}
			}
			if(to==-1)
			for(int i=nei.getFirstElement();i>=0;i=nei.getNextElement()){
				int k = g.getKernelGraph().getSuccessorsOf(i).getSize();
				if(!g.getKernelGraph().edgeExists(from,i)){
					if(dMax[i]-k<minDelta){
						minDelta = dMax[i]-k;
						to = i;
					}
				}
			}
			if(to==-1){
				throw new UnsupportedOperationException();
			}
			return true;
		}
	}
}