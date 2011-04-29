package samples.graph;

import java.util.BitSet;
import benchmarks.graph.DataWriter;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.gary.NTree;
import solver.constraints.propagators.PropagatorPriority;
import solver.search.limits.ILimit;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.graph.GraphType;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class Tree extends AbstractProblem{

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private static final long TIMELIMIT = 1000;
	private static String file = "results_tree.csv";
	private BitSet[] data;
	private DirectedGraphVar g;
	private IntVar nTree;
	private int n;
	private int d;
	private Boolean sat;
	static int seed = 0;
	private static GraphType gtype;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public Tree(BitSet[] input, int nbSuccsPerNodes) {
		solver = new Solver();
		data = input;
		n = data.length;
		d = nbSuccsPerNodes;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		g = VariableFactory.digraph("G",data, gtype, GraphType.SPARSE,solver);
		nTree = VariableFactory.enumerated("NTREE ", 1,n, solver);
		Constraint[] cstrs = new Constraint[]{new NTree(g,nTree, solver, PropagatorPriority.LINEAR)};
		solver.post(cstrs);
	}

	@Override
	public void configureSolver() {
		AbstractStrategy strategy = StrategyFactory.randomArcs(g, solver.getEnvironment());
		solver.set(strategy);
	}

	@Override
	public void solve() {
		solver.getSearchLoop().getLimitsFactory().add(new ILimit() {
			private long iniTime;
			@Override
			public void update() {}
			@Override
			public boolean isReached() {return System.currentTimeMillis()-iniTime>TIMELIMIT;}
			@Override
			public void init() {iniTime = System.currentTimeMillis();}
		});
		sat = solver.findSolution();
		DataWriter.writeTextInto(n+";"+d+";"+solver.getMeasures().getNodeCount()+";"+
				solver.getMeasures().getBackTrackCount()+";"+solver.getMeasures().getTimeCount()+";"+NTree.filteringCounter+";"+sat+";\n", file);
		if(solver.getMeasures().getBackTrackCount()>0){
			throw new UnsupportedOperationException("error bksCount>0");
		}
	}

	@Override
	public void prettyOut() {}

	public static boolean performOneTest(int n, int d){
		if(n<d)throw new UnsupportedOperationException("n must be greater or equal to d");
		BitSet[] data = DataGenerator.makeTreeData(n, d);
		Tree tsample = new Tree(data,d);
		tsample.execute();
		return tsample.sat!=null && tsample.solver.getMeasures().getTimeCount()<=TIMELIMIT;
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public static void main(String[] args) {
		gtype = GraphType.SPARSE;
		testN();
		gtype = GraphType.DENSE;
		testN();
	}
	private static void testN(){
		file = "tree_"+(TIMELIMIT/1000)+"sec_d5_"+gtype+".csv";
		DataWriter.clearFile(file);
		DataWriter.writeTextInto("n;d;nodes;bks;time;counter;solved;\n", file);
		int i = 0;
		int[] ds = new int[]{5};
//		int[] ds = new int[]{5,20,50,30000};
		int dMax = 50000;
		int nMax = 50000;
		for(int n=20;n<=100;n+=10){
			if (n<nMax){
				for(int d:ds){
					if(d<dMax){
						if(d>n){
							d=n;
						}
						boolean success = false;
						for(i=0;i<1;i++){
							seed = i;
							DirectedGraphVar.seed = i;
							DataGenerator.seed = i;
							System.out.println(n+" : "+d);
							if(performOneTest(n, d)){
								success = true;
							}
							for(int k=0;k<1;k++){
								System.gc();
							}
						}
						if(!success){
							dMax = d;
							if (dMax == 5){
								nMax = n;
							}
						}
						if(d==n){
							break;
						}
					}
				}
			
			}else{
				break;
			}
		}
	}
	private static void testD(){
		file = "results_JGF_Tree_BENCH_D_bigValues.csv";
		DataWriter.clearFile(file);
		DataWriter.writeTextInto("n;d;nodes;bks;time;counter;solved;\n", file);
		int[] ns = new int[]{50,100,300,500};
		int[] ds = new int[]{2,4,6,8,10,20,40,60,80,100,200,400,600,800,1000};
		for(int n:ns){
			for(int d:ds){
				if(d>n){
					d=n;
				}
				System.out.println(n+" : "+d);
				performOneTest(n, d);
				if(d==n){
					break;
				}
			}
		}
	}
}
