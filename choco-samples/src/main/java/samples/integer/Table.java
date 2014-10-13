package samples.integer;

import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.extension.Tuples;
import solver.search.strategy.ISF;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Random;

/**
 * Small illustration of a table constraint
 * @author Guillaume Perez, Jean-Guillaume Fages
 */
public class Table extends AbstractProblem {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	IntVar[] vars;
	int nbTuples = 5;
	int n = 4;
	int upB = 10;
	int lowB = -10;

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public void buildModel() {
		vars = new IntVar[n];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = VariableFactory.enumerated("Q_" + i, lowB, upB, solver);
		}
		Random rand = new Random(12);
		Tuples tuples = new Tuples(true);
		System.out.println("Allowed tuples");
		for(int i = 0; i < nbTuples ; i++){
			int[] tuple = new int[n];
			for(int j = 0; j < n; j++){
				tuple[j] = rand.nextInt(upB - lowB) + lowB;
				System.out.print(tuple[j] + " ");
			}
			tuples.add(tuple);
			System.out.println();
		}
		solver.post(ICF.table(vars,tuples,"STR2+"));
	}

	@Override
	public void createSolver(){
		solver = new Solver("Table sample");
	}

	@Override
	public void configureSearch() {
		solver.set(ISF.minDom_LB(vars));
	}

	@Override
	public void prettyOut() {}

	@Override
	public void solve() {
		solver.findAllSolutions();
	}

	//***********************************************************************************
	// MAIN
	//***********************************************************************************

	public static void main(String[] args){
	    new Table().execute(args);
	}
}
