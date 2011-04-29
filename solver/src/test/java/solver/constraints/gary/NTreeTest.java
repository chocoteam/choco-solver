package solver.constraints.gary;

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.PropagatorPriority;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;
import choco.kernel.memory.IEnvironment;

public class NTreeTest {

	@Test
	public static void model(int n, int tmin, int tmax) {
		Solver s = new Solver();
		IEnvironment env = s.getEnvironment();

		DirectedGraphVar g = VariableFactory.digraph("G",n, GraphType.DENSE,s,"clique");
		IntVar nTree = VariableFactory.enumerated("NTREE ", tmin,tmax, s);
		
		Constraint[] cstrs = new Constraint[]{new NTree(g,nTree, s, PropagatorPriority.LINEAR)};

		AbstractStrategy strategy = StrategyFactory.randomArcs(g, env);

		
		s.post(cstrs);
		s.set(strategy);
		s.findSolution();
		
		if(s.getMeasures().getBackTrackCount()>0){
			System.out.println("nice : "+DirectedGraphVar.seed);System.exit(0);
		}
	}
	
	@Test
	public static void debug() {
//		for(int s=0;s<3;s++){
//			for(int n=100;n<500;n*=2){
//				for(int t1=1;t1<n;t1*=2){
//					for(int t2=t1;t2<n;t2*=2){
//						DirectedGraphVar.seed = s;
//						model(n,t1,t2);
//					}
//				}
//			}
//		}
		DirectedGraphVar.seed = 0;
		model(10,11,12);
	}
}
