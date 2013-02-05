/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables;

import choco.kernel.memory.setDataStructures.ISet;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.exception.SolverException;
import solver.variables.fast.BitsetIntVarImpl;
import solver.variables.fast.BooleanBoolVarImpl;
import solver.variables.fast.IntervalIntVarImpl;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.UndirectedGraphVar;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18 nov. 2010
 */
public enum VariableFactory {
	;

	//TODO : build domain in Variable

	//*************************************************************************************
	// INTEGER VARIABLES
	//*************************************************************************************

	/**
	 * Build a boolean variable, ie domain is [0,1]
	 *
	 * @param NAME   name of the variable
	 * @param SOLVER solver involving the variable
	 * @return a BoolVar
	 */
	public static BoolVar bool(String NAME, Solver SOLVER) {
		BooleanBoolVarImpl var = new BooleanBoolVarImpl(NAME, SOLVER);
		//var.setHeuristicVal(HeuristicValFactory.presetI(var));
		return var;
	}

	/**
	 * Build a boolean variable array, ie each variable's domain is [0,1]
	 * @param NAME	name of the variables
	 * @param SIZE	number of variables
	 * @param SOLVER solver involving the variable
	 * @return an array of BoolVar
	 */
	public static BoolVar[] boolArray(String NAME, int SIZE, Solver SOLVER) {
		BoolVar[] vars = new BoolVar[SIZE];
		for (int i = 0; i < SIZE; i++) {
			vars[i] = bool(NAME + "_" + i, SOLVER);
		}
		return vars;
	}

	/**
	 * Build a DIM1*DIM2-sized boolean variable matrix
	 * @param NAME	name of the variables
	 * @param DIM1	number of rows
	 * @param DIM2	number of columns
	 * @param SOLVER solver involving the variable
	 * @return a BoolVar matrix
	 */
	public static BoolVar[][] boolMatrix(String NAME, int DIM1, int DIM2, Solver SOLVER) {
		BoolVar[][] vars = new BoolVar[DIM1][];
		for (int i = 0; i < DIM1; i++) {
			vars[i] = boolArray(NAME + "_" + i, DIM2, SOLVER);
		}
		return vars;
	}

	/**
	 * Build an integer variable whose domain representation is abstracted by two integers:
	 * a lower bound and an upper bound.
	 * <p/> Its initial domain is [MIN,MAX]
	 * @param NAME	name of the variable
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return an integer variable with a bounded domain
	 */
	public static IntVar bounded(String NAME, int MIN, int MAX, Solver SOLVER) {
		checkIntVar(NAME, MIN, MAX);
		if (MIN == MAX) {
			return Views.fixed(NAME, MIN, SOLVER);
		} else if (MIN == 0 && MAX == 1) {
			return new BooleanBoolVarImpl(NAME, SOLVER);
		} else {
			return new IntervalIntVarImpl(NAME, MIN, MAX, SOLVER);
		}
	}

	/**
	 * Build and array of bounded variables
	 * (each variable domain is represented by two integers)
	 * @param NAME	name of the variables
	 * @param SIZE	number of variables
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return an array of integer variables with bounded domains
	 */
	public static IntVar[] boundedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
		IntVar[] vars = new IntVar[SIZE];
		for (int i = 0; i < SIZE; i++) {
			vars[i] = bounded(NAME + "_" + i, MIN, MAX, SOLVER);
		}
		return vars;
	}

	/**
	 * Build a DIM1*DIM2-sized matrix of bounded variables
	 * (each variable domain is represented by two integers)
	 * @param NAME	name of the variables
	 * @param DIM1	number of rows
	 * @param DIM2	number of columns
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return an array of integer variables with bounded domains
	 */
	public static IntVar[][] boundedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
		IntVar[][] vars = new IntVar[DIM1][DIM2];
		for (int i = 0; i < DIM1; i++) {
			vars[i] = boundedArray(NAME + "_" + i, DIM2, MIN, MAX, SOLVER);
		}
		return vars;
	}

	/**
	 * Build an integer variable whose domain is explicitly represented with a BitSet.
	 * Its initial domain is {MIN,MIN+1...MAX-1,MAX}
	 * @param NAME	name of the variable
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return an integer variable with an enumerated domain
	 */
	public static IntVar enumerated(String NAME, int MIN, int MAX, Solver SOLVER) {
		checkIntVar(NAME, MIN, MAX);
		if (MIN == MAX) {
			return Views.fixed(NAME, MIN, SOLVER);
		} else if (MIN == 0 && MAX == 1) {
			return new BooleanBoolVarImpl(NAME, SOLVER);
		} else {
			return new BitsetIntVarImpl(NAME, MIN, MAX, SOLVER);
		}
	}

	/**
	 * Build an array of integer variables whose domains are explicitly represented
	 * @param NAME	name of the variables
	 * @param SIZE	number of variables
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return an array of integer variables with enumerated domains
	 */
	public static IntVar[] enumeratedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
		IntVar[] vars = new IntVar[SIZE];
		for (int i = 0; i < SIZE; i++) {
			vars[i] = enumerated(NAME + "_" + i, MIN, MAX, SOLVER);
		}
		return vars;
	}

	/**
	 * Build a DIM1*DIM2-sized matrix of integer variables whose domains are explicitly represented
	 * @param NAME	name of the variables
	 * @param DIM1	number of rows
	 * @param DIM2	number of columns
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param SOLVER solver involving the variable
	 * @return a matrix of integer variables with enumerated domains
	 */
	public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
		IntVar[][] vars = new IntVar[DIM1][DIM2];
		for (int i = 0; i < DIM1; i++) {
			for (int j = 0; j < DIM2; j++) {
				vars[i][j] = enumerated(NAME + "_" + i + "_" + j, MIN, MAX, SOLVER);
			}
		}
		return vars;
	}

	/**
	 * Build an integer variable whose domain is explicitly represented with a BitSet.
	 * Its initial domain is VALUES
	 * @param NAME		name of the variable
	 * @param VALUES	initial domain
	 * @param SOLVER	solver involving the variable
	 * @return an integer variable with an enumerated domain, initialized to VALUES
	 */
	public static IntVar enumerated(String NAME, int[] VALUES, Solver SOLVER) {
		checkIntVar(NAME, VALUES[0], VALUES[VALUES.length - 1]);
		if (VALUES.length == 1) {
			return Views.fixed(NAME, VALUES[0], SOLVER);
		} else {
			BitsetIntVarImpl var = new BitsetIntVarImpl(NAME, VALUES, SOLVER);
			//var.setHeuristicVal(HeuristicValFactory.presetI(var));
			return var;
		}
	}

	/**
	 * Build an integer variable array with enumerated domains.
	 * Each domain is initialized to VALUE
	 * @param NAME		name of the variables
	 * @param SIZE		number of variables
	 * @param VALUES	initial domain
	 * @param SOLVER	solver involving the variable
	 * @return an integer variable array with enumerated domains initialized to VALUES
	 */
	public static IntVar[] enumeratedArray(String NAME, int SIZE, int[] VALUES, Solver SOLVER) {
		IntVar[] vars = new IntVar[SIZE];
		for (int i = 0; i < SIZE; i++) {
			vars[i] = enumerated(NAME + "_" + i, VALUES, SOLVER);
		}
		return vars;
	}

	/**
	 * Build a DIM1*DIM2-sized matrix of integer variables with enumerated domains.
	 * Each domain is initialized to VALUE
	 * @param NAME		name of the variables
	 * @param DIM1		number of rows
	 * @param DIM2		number of columns
	 * @param VALUES	initial domain
	 * @param SOLVER	solver involving the variable
	 * @return an integer variable matrix with enumerated domains initialized to VALUES
	 */
	public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int[] VALUES, Solver SOLVER) {
		IntVar[][] vars = new IntVar[DIM1][];
		for (int i = 0; i < DIM1; i++) {
			vars[i] = enumeratedArray(NAME + "_" + i, DIM2, VALUES, SOLVER);
		}
		return vars;
	}

	//*************************************************************************************
	// REAL VARIABLES
	//*************************************************************************************


	/**
	 * Build a real variable with a bounded domain initialized to [MIN,MAX]
	 * @param NAME	name of the variable
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param PRECISION	double precision (e.g., 0.00001d)
	 * @param SOLVER solver involving the variable
	 * @return a real variable with a bounded domain initialized to [MIN,MAX]
	 */
	public static RealVar real(String NAME, double MIN, double MAX, double PRECISION, Solver SOLVER) {
		checkRealVar(NAME, MIN, MAX);
		RealVar var = new RealVarImpl(NAME, MIN, MAX, PRECISION, SOLVER);
		//var.setHeuristicVal(HeuristicValFactory.presetI(var));
		return var;
	}

	/**
	 * Build a SIZE-sized array of real variables.
	 * Each domain is initialized to [MIN,MAX]
	 * @param NAME	name of the variables
	 * @param SIZE	number of variables
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param PRECISION	double precision (e.g., 0.00001d)
	 * @param SOLVER solver involving the variable
	 * @return a real variable array with bounded domains initialized to [MIN,MAX]
	 */
	public static RealVar[] realArray(String NAME, int SIZE, double MIN, double MAX, double PRECISION, Solver SOLVER) {
		RealVar[] vars = new RealVar[SIZE];
		for (int i = 0; i < SIZE; i++) {
			vars[i] = real(NAME + "_" + i, MIN, MAX, PRECISION, SOLVER);
		}
		return vars;
	}

	/**
	 * Build a DIM1*DIM2-sized matrix of real variables.
	 * Each domain is initialized to [MIN,MAX]
	 * @param NAME	name of the variables
	 * @param DIM1	number of rows
	 * @param DIM2	number of columns
	 * @param MIN	initial lower bound
	 * @param MAX	initial upper bound
	 * @param PRECISION	double precision (e.g., 0.00001d)
	 * @param SOLVER solver involving the variable
	 * @return a real variable matrix with bounded domains initialized to [MIN,MAX]
	 */
	public static RealVar[][] realMatrix(String NAME, int DIM1, int DIM2, double MIN, double MAX, double PRECISION, Solver SOLVER) {
		RealVar[][] vars = new RealVar[DIM1][DIM2];
		for (int i = 0; i < DIM1; i++) {
			vars[i] = realArray(NAME + "_" + i, DIM2, MIN, MAX, PRECISION, SOLVER);
		}
		return vars;
	}

	//*************************************************************************************
	// SET VARIABLES
	//*************************************************************************************


	/**
	 * Builds a set variable with an empty domain.
	 * The set variable represents a set of positive integers
	 * @param NAME name of the variable
	 * @param SOLVER solver involving the variable
	 * @return a set variable with an empty domain
	 */
	public static SetVar set(String NAME, Solver SOLVER){
		return new SetVarImpl(NAME,SOLVER) ;
	}

	/**
	 * Builds a set variable with an initial domain given by ENVELOP and KERNEL.
	 * BEWARE elements must be positive or null
	 * @param NAME name of the variable
	 * @param ENVELOP elements potentially in the set
	 * @param KERNEL elements that must belong to the final set
	 * @param SOLVER solver involving the variable
	 * @return a set variable
	 */
	public static SetVar set(String NAME, ISet ENVELOP, ISet KERNEL, Solver SOLVER) {
		SetVar s = set(NAME, SOLVER);
		for (int i = ENVELOP.getFirstElement(); i >= 0; i = ENVELOP.getNextElement()) {
			s.getEnvelope().add(i);
		}
		if (KERNEL != null)
			for (int i = KERNEL.getFirstElement(); i >= 0; i = KERNEL.getNextElement()) {
				s.getKernel().add(i);
			}
		return s;
	}

	//*************************************************************************************
	// GRAPH VARIABLES
	//*************************************************************************************

	/**
	 * Builds a non-directed graph variable with an empty domain
	 * but allocates memory to deal with at most NB_NODES nodes.
	 *
	 * The domain of a graph variable is defined by two graphs:
	 * <p/> The envelope graph denotes nodes and edges that may belong to a solution
	 * <p/> The kernel graph denotes nodes and edges that must belong to any solution
	 * @param NAME		name of the variable
	 * @param NB_NODES  maximal number of nodes
	 * @param SOLVER 	solver involving the variable
	 * @return a graph variable with an empty domain
	 */
	public static UndirectedGraphVar undirectedGraph(String NAME, int NB_NODES, Solver SOLVER){
		return new UndirectedGraphVar(NAME,SOLVER,NB_NODES,false) ;
	}

	/**
	 * Builds a directed graph variable with an empty domain
	 * but allocates memory to deal with at most NB_NODES nodes.
	 *
	 * The domain of a graph variable is defined by two graphs:
	 * <p/> The envelope graph denotes nodes and arcs that may belong to a solution
	 * <p/> The kernel graph denotes nodes and arcs that must belong to any solution
	 * @param NAME		name of the variable
	 * @param NB_NODES  maximal number of nodes
	 * @param SOLVER 	solver involving the variable
	 * @return a graph variable with an empty domain
	 */
	public static DirectedGraphVar directedGraph(String NAME, int NB_NODES, Solver SOLVER){
		return new DirectedGraphVar(NAME,SOLVER,NB_NODES,false) ;
	}

	//*************************************************************************************
	// TASKS
	//*************************************************************************************

	/**
	 * Build a Task software component (not a variable) which ensures that START + DURATION = END
	 * Rises a ContradictionException if START + DURATION cannot be equal to END
	 * @param START IntVar representing the start of the task
	 * @param DURATION IntVar representing the duration of the task
	 * @param END IntVar representing the end of the task
	 * @return A task Object ensuring that START + DURATION = END
	 */
	public static Task task(IntVar START, IntVar DURATION, IntVar END) throws ContradictionException {
		return new Task(START,DURATION,END);
	}

	//*************************************************************************************
	// UTILITIES
	//*************************************************************************************

	/**
	 * Casts VARIABLES into an integer variable array
	 * @param VARIABLES variables
	 * @return an IntVar array containing variables in VARIABLES
	 */
	public static IntVar[] castToIntVar(Variable... VARIABLES) {
		IntVar[] ivars = new IntVar[VARIABLES.length];
		for (int i = 0; i < VARIABLES.length; i++) {
			ivars[i] = (IntVar) VARIABLES[i];
		}
		return ivars;
	}

	/**
	 * Inserts VARS into a new DIM1*DIM2-size matrix:
	 * put variable of VARS[i*DIM2+j] into mat[i][j]
	 * @param VARS variables
	 * @param DIM1 number of rows
	 * @param DIM2 number of columns
	 *             Note that DIM1*DIM2 is supposed to be equal to VARS.length
	 * @return a matrix representation of VARS
	 */
	public static IntVar[][] toMatrix(IntVar[] VARS, int DIM1, int DIM2) {
		IntVar[][] mat = new IntVar[DIM1][DIM2];
		for (int i = 0; i < DIM1; i++) {
			for (int j = 0; j < DIM2; j++) {
				mat[i][j] = VARS[i + j * DIM1];
			}
		}
		return mat;
	}

	/**
	 * Checks domain range.
	 * Throws an exception if wrong range definition
	 * @param NAME	name of the variable
	 * @param MIN	lower bound of the domain
	 * @param MAX	upper bound of the domain
	 */
	private static void checkIntVar(String NAME, int MIN, int MAX) {
		if (MIN - Integer.MIN_VALUE == 0 || MAX - Integer.MAX_VALUE == 0) {
			throw new SolverException(NAME + ": consider reducing the bounds to avoid unexpected results");
		}
		if (MAX < MIN) {
			throw new SolverException(NAME + ": wrong domain definition, lower bound > upper bound");
		}
	}

	/**
	 * Checks domain range.
	 * Throws an exception if wrong range definition
	 * @param NAME	name of the variable
	 * @param MIN	lower bound of the domain
	 * @param MAX	upper bound of the domain
	 */
	private static void checkRealVar(String NAME, double MIN, double MAX) {
		if (MAX < MIN) {
			throw new SolverException(NAME + ": wrong domain definition, lower bound > upper bound");
		}
	}
}
