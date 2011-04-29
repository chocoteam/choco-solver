package choco.solver.search.enumerations.values;

public class Main {
	public static void main(String[] args) {
		// properties of the DSL 
		// - do not loose value
		// - do not duplicate value
		// - do not invent value
		// - computation always terminates
		// - computation complexity does not depend on the domain size

		// - three kinds of operations:
		//   - unsplitter
		//   - sorter
		//   - splitter
		//   - a fourth kind? sizer

		//id(9).enumerate();
		//id(9).reverse().enumerate();
		//id(9).unconcat().get(0).enumerate();
		//id(9).unconcat().get(1).enumerate();
//		id(9).unconcat().reverse().concat().enumerate();
//		id(9).unconcat().applyReverseAt(1).concat().enumerate();
		// rotate left
//		id(9).split().reverse().concat().enumerate();
		// queens
		id(9).unconcat().applyReverseAt(0).zip().enumerate();
		//id(9).unconcat(4).mapReverse().concat().enumerate();
//		id(9).unzip().concat().enumerate();
		
		// TO DO:
		// - test (debug and limitation)
		// - true higher order (Map, ApplyAt, Repeat)
		// - memory for Sorting
		// - split (filter on a predicate)
		// - SortBy f1 f2 f3 ...
		// - random
		// - take a bit set as a parameter
		// - take the set of the solver as a parameter
		// - program choice Var ; choice Val
	}
	static ValueIterator<Integer> id(int n) {
		return new Id(n);
	}
}