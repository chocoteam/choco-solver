/**
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

import solver.Configuration;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.IntConstraintFactory;
import solver.constraints.real.IntEqRealConstraint;
import solver.exception.SolverException;
import solver.variables.graph.DirectedGraphVar;
import solver.variables.graph.UndirectedGraphVar;
import solver.variables.impl.*;
import solver.variables.view.*;
import util.objects.setDataStructures.SetType;
import util.tools.StringUtils;

import java.util.Arrays;

/**
 * A factory to create variables (boolean, integer, set, graph, task and real) and views (most of them rely on integer variable).
 * <br/>
 * <p/>
 * Note that, for the sack of readability, the Java naming convention is not respected for methods arguments.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 18 nov. 2010
 */
public class VariableFactory {

    VariableFactory() {
    }

    private static final String CSTE_NAME = "cste -- ";

    /**
     * Provide a minimum value for integer variable lower bound.
     * Do not prevent from underflow, but may avoid it, somehow.
     */
    public static final int MIN_INT_BOUND = Integer.MIN_VALUE / 100;

    /**
     * Provide a minimum value for integer variable lower bound.
     * Do not prevent from overflow, but may avoid it, somehow.
     */
    public static final int MAX_INT_BOUND = Integer.MAX_VALUE / 100;

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
        return new BoolVarImpl(NAME, SOLVER);
    }

    /**
     * Build a boolean variable array, ie each variable's domain is [0,1]
     *
     * @param NAME   name of the variables
     * @param SIZE   number of variables
     * @param SOLVER solver involving the variable
     * @return an array of BoolVar
     */
    public static BoolVar[] boolArray(String NAME, int SIZE, Solver SOLVER) {
        BoolVar[] vars = new BoolVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = bool(NAME + "[" + i + "]", SOLVER);
        }
        return vars;
    }

    /**
     * Build a DIM1*DIM2-sized boolean variable matrix
     *
     * @param NAME   name of the variables
     * @param DIM1   number of rows
     * @param DIM2   number of columns
     * @param SOLVER solver involving the variable
     * @return a BoolVar matrix
     */
    public static BoolVar[][] boolMatrix(String NAME, int DIM1, int DIM2, Solver SOLVER) {
        BoolVar[][] vars = new BoolVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = boolArray(NAME + "[" + i + "]", DIM2, SOLVER);
        }
        return vars;
    }

    /**
     * Build an integer variable whose domain representation is abstracted by two integers:
     * a lower bound and an upper bound.
     * <p/> Its initial domain is [MIN,MAX]
     *
     * @param NAME   name of the variable
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return an integer variable with a bounded domain
     */
    public static IntVar bounded(String NAME, int MIN, int MAX, Solver SOLVER) {
        checkIntVar(NAME, MIN, MAX);
        if (MIN == MAX) {
            return fixed(NAME, MIN, SOLVER);
        } else if (MIN == 0 && MAX == 1) {
            return new BoolVarImpl(NAME, SOLVER);
        } else {
            return new IntervalIntVarImpl(NAME, MIN, MAX, SOLVER);
        }
    }

    /**
     * Build and array of bounded variables
     * (each variable domain is represented by two integers)
     *
     * @param NAME   name of the variables
     * @param SIZE   number of variables
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return an array of integer variables with bounded domains
     */
    public static IntVar[] boundedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = bounded(NAME + "[" + i + "]", MIN, MAX, SOLVER);
        }
        return vars;
    }

    /**
     * Build a DIM1*DIM2-sized matrix of bounded variables
     * (each variable domain is represented by two integers)
     *
     * @param NAME   name of the variables
     * @param DIM1   number of rows
     * @param DIM2   number of columns
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return an array of integer variables with bounded domains
     */
    public static IntVar[][] boundedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
        IntVar[][] vars = new IntVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = boundedArray(NAME + "[" + i + "]", DIM2, MIN, MAX, SOLVER);
        }
        return vars;
    }

    /**
     * Build an integer variable whose domain is explicitly represented with a BitSet.
     * Its initial domain is {MIN,MIN+1...MAX-1,MAX}
     *
     * @param NAME   name of the variable
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return an integer variable with an enumerated domain
     */
    public static IntVar enumerated(String NAME, int MIN, int MAX, Solver SOLVER) {
        checkIntVar(NAME, MIN, MAX);
        if (MIN == MAX) {
            return fixed(NAME, MIN, SOLVER);
        } else if (MIN == 0 && MAX == 1) {
            return new BoolVarImpl(NAME, SOLVER);
        } else {
            return new BitsetIntVarImpl(NAME, MIN, MAX, SOLVER);
        }
    }

    /**
     * Build an array of integer variables whose domains are explicitly represented
     *
     * @param NAME   name of the variables
     * @param SIZE   number of variables
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return an array of integer variables with enumerated domains
     */
    public static IntVar[] enumeratedArray(String NAME, int SIZE, int MIN, int MAX, Solver SOLVER) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = enumerated(NAME + "[" + i + "]", MIN, MAX, SOLVER);
        }
        return vars;
    }

    /**
     * Build a DIM1*DIM2-sized matrix of integer variables whose domains are explicitly represented
     *
     * @param NAME   name of the variables
     * @param DIM1   number of rows
     * @param DIM2   number of columns
     * @param MIN    initial lower bound
     * @param MAX    initial upper bound
     * @param SOLVER solver involving the variable
     * @return a matrix of integer variables with enumerated domains
     */
    public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int MIN, int MAX, Solver SOLVER) {
        IntVar[][] vars = new IntVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = enumeratedArray(NAME + "[" + i + "]", DIM2, MIN, MAX, SOLVER);
        }
        return vars;
    }

    /**
     * Build an integer variable whose domain is explicitly represented with a BitSet.
     * Its initial domain is VALUES
     *
     * @param NAME   name of the variable
     * @param VALUES initial domain
     * @param SOLVER solver involving the variable
     * @return an integer variable with an enumerated domain, initialized to VALUES
     */
    public static IntVar enumerated(String NAME, int[] VALUES, Solver SOLVER) {
        VALUES = sortIfNot(VALUES.clone());
        checkIntVar(NAME, VALUES[0], VALUES[VALUES.length - 1]);
        if (VALUES.length == 1) {
            return fixed(NAME, VALUES[0], SOLVER);
        } else {
			int gap = VALUES[VALUES.length - 1] - VALUES[0];
            if (gap > 30 && gap/VALUES.length > 5) {
                return new BitsetArrayIntVarImpl(NAME, VALUES, SOLVER);
            } else {
                return new BitsetIntVarImpl(NAME, VALUES, SOLVER);
            }
        }
    }

    /**
     * Build an integer variable array with enumerated domains.
     * Each domain is initialized to VALUE
     *
     * @param NAME   name of the variables
     * @param SIZE   number of variables
     * @param VALUES initial domain
     * @param SOLVER solver involving the variable
     * @return an integer variable array with enumerated domains initialized to VALUES
     */
    public static IntVar[] enumeratedArray(String NAME, int SIZE, int[] VALUES, Solver SOLVER) {
        IntVar[] vars = new IntVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = enumerated(NAME + "[" + i + "]", VALUES, SOLVER);
        }
        return vars;
    }

    /**
     * Build a DIM1*DIM2-sized matrix of integer variables with enumerated domains.
     * Each domain is initialized to VALUE
     *
     * @param NAME   name of the variables
     * @param DIM1   number of rows
     * @param DIM2   number of columns
     * @param VALUES initial domain
     * @param SOLVER solver involving the variable
     * @return an integer variable matrix with enumerated domains initialized to VALUES
     */
    public static IntVar[][] enumeratedMatrix(String NAME, int DIM1, int DIM2, int[] VALUES, Solver SOLVER) {
        IntVar[][] vars = new IntVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = enumeratedArray(NAME + "[" + i + "]", DIM2, VALUES, SOLVER);
        }
        return vars;
    }

    //*************************************************************************************
    // REAL VARIABLES
    //*************************************************************************************

    /**
     * Build a real variable with a bounded domain initialized to [MIN,MAX]
     *
     * @param NAME      name of the variable
     * @param MIN       initial lower bound
     * @param MAX       initial upper bound
     * @param PRECISION double precision (e.g., 0.00001d)
     * @param SOLVER    solver involving the variable
     * @return a real variable with a bounded domain initialized to [MIN,MAX]
     */
    public static RealVar real(String NAME, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        checkRealVar(NAME, MIN, MAX);
        return new RealVarImpl(NAME, MIN, MAX, PRECISION, SOLVER);
    }

    /**
     * Build a SIZE-sized array of real variables.
     * Each domain is initialized to [MIN,MAX]
     *
     * @param NAME      name of the variables
     * @param SIZE      number of variables
     * @param MIN       initial lower bound
     * @param MAX       initial upper bound
     * @param PRECISION double precision (e.g., 0.00001d)
     * @param SOLVER    solver involving the variable
     * @return a real variable array with bounded domains initialized to [MIN,MAX]
     */
    public static RealVar[] realArray(String NAME, int SIZE, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        RealVar[] vars = new RealVar[SIZE];
        for (int i = 0; i < SIZE; i++) {
            vars[i] = real(NAME + "[" + i + "]", MIN, MAX, PRECISION, SOLVER);
        }
        return vars;
    }

    /**
     * Build a DIM1*DIM2-sized matrix of real variables.
     * Each domain is initialized to [MIN,MAX]
     *
     * @param NAME      name of the variables
     * @param DIM1      number of rows
     * @param DIM2      number of columns
     * @param MIN       initial lower bound
     * @param MAX       initial upper bound
     * @param PRECISION double precision (e.g., 0.00001d)
     * @param SOLVER    solver involving the variable
     * @return a real variable matrix with bounded domains initialized to [MIN,MAX]
     */
    public static RealVar[][] realMatrix(String NAME, int DIM1, int DIM2, double MIN, double MAX, double PRECISION, Solver SOLVER) {
        RealVar[][] vars = new RealVar[DIM1][];
        for (int i = 0; i < DIM1; i++) {
            vars[i] = realArray(NAME + "[" + i + "]", DIM2, MIN, MAX, PRECISION, SOLVER);
        }
        return vars;
    }

    //*************************************************************************************
    // SET VARIABLES
    //*************************************************************************************

    /**
     * Builds a set variable with an initial domain given by ENVELOP and KERNEL.
     *
     * @param NAME     name of the variable
     * @param ENVELOPE elements potentially in the set
     * @param ENV_TYPE type of data structure for storing the envelope
     * @param KERNEL   elements that must belong to the final set
     * @param KER_TYPE type of data structure for storing the kernel
     * @param SOLVER   solver involving the variable
     * @return a set variable
     */
    public static SetVar set(String NAME, int[] ENVELOPE, SetType ENV_TYPE, int[] KERNEL, SetType KER_TYPE, Solver SOLVER) {
        return new SetVarImpl(NAME, ENVELOPE, ENV_TYPE, KERNEL, KER_TYPE, SOLVER);
    }

    /**
     * Builds a set variable with an initial domain given by ENVELOP and KERNEL.
     *
     * @param NAME     name of the variable
     * @param ENVELOPE elements potentially in the set
     * @param KERNEL   elements that must belong to the final set
     * @param SOLVER   solver involving the variable
     * @return a set variable
     */
    public static SetVar set(String NAME, int[] ENVELOPE, int[] KERNEL, Solver SOLVER) {
        return set(NAME, ENVELOPE, SetType.BITSET, KERNEL, SetType.BITSET, SOLVER);
    }

    /**
     * Builds a set variable with an initial domain given by ENVELOP and KERNEL.
     *
     * @param NAME     name of the variable
     * @param ENVELOPE elements potentially in the set
     * @param SOLVER   solver involving the variable
     * @return a set variable
     */
    public static SetVar set(String NAME, int[] ENVELOPE, Solver SOLVER) {
        return set(NAME, ENVELOPE, SetType.BITSET, new int[]{}, SetType.BITSET, SOLVER);
    }

    /**
     * Builds a set variable with an initial domain given by
     * ENVELOP = [MIN_ELEMENT,MAX_ELEMENT] and an empty KERNEL.
     * Uses a BitSet representation for both the envelope and the kernel
     *
     * @param NAME        name of the variable
     * @param MIN_ELEMENT
     * @param MAX_ELEMENT
     * @param SOLVER      solver involving the variable
     * @return a set variable
     */
    public static SetVar set(String NAME, int MIN_ELEMENT, int MAX_ELEMENT, Solver SOLVER) {
        return new SetVarImpl(NAME, MIN_ELEMENT, MAX_ELEMENT, SOLVER);
    }

    //*************************************************************************************
    // GRAPH VARIABLES
    //*************************************************************************************

    /**
     * Builds a non-directed graph variable with an empty domain
     * but allocates memory to deal with at most NB_NODES nodes.
     * <p/>
     * The domain of a graph variable is defined by two graphs:
     * <p/> The envelope graph denotes nodes and edges that may belong to a solution
     * <p/> The kernel graph denotes nodes and edges that must belong to any solution
     *
     * @param NAME     name of the variable
     * @param NB_NODES maximal number of nodes
     * @param SOLVER   solver involving the variable
     * @return a graph variable with an empty domain
     */
    public static UndirectedGraphVar undirectedGraph(String NAME, int NB_NODES, Solver SOLVER) {
        return new UndirectedGraphVar(NAME, SOLVER, NB_NODES, false);
    }

    /**
     * Builds a directed graph variable with an empty domain
     * but allocates memory to deal with at most NB_NODES nodes.
     * <p/>
     * The domain of a graph variable is defined by two graphs:
     * <p/> The envelope graph denotes nodes and arcs that may belong to a solution
     * <p/> The kernel graph denotes nodes and arcs that must belong to any solution
     *
     * @param NAME     name of the variable
     * @param NB_NODES maximal number of nodes
     * @param SOLVER   solver involving the variable
     * @return a graph variable with an empty domain
     */
    public static DirectedGraphVar directedGraph(String NAME, int NB_NODES, Solver SOLVER) {
        return new DirectedGraphVar(NAME, SOLVER, NB_NODES, false);
    }

    //*************************************************************************************
    // TASKS
    //*************************************************************************************

    /**
     * Build a Task software component (not a variable) which ensures that START + DURATION = END
     *
     * @param START    IntVar representing the start of the task
     * @param DURATION IntVar representing the duration of the task
     * @param END      IntVar representing the end of the task
     * @return A task Object ensuring that START + DURATION = END
     */
    public static Task task(IntVar START, IntVar DURATION, IntVar END) {
        return new Task(START, DURATION, END);
    }

    //*************************************************************************************
    // UTILITIES
    //*************************************************************************************

    /**
     * Casts VARIABLES into an integer variable array
     *
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
     *
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
     *
     * @param NAME name of the variable
     * @param MIN  lower bound of the domain
     * @param MAX  upper bound of the domain
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
     * Sorts the input array if it is not already sorted,
     * and removes multiple occurrences of the same value
     *
     * @param values
     * @return a sorted array containing each value of values exactly once
     */
    private static int[] sortIfNot(int[] values) {
        int n = values.length;
        boolean sorted = true;
        boolean noDouble = true;
        for (int i = 0; i < n - 1 && sorted; i++) {
            if (values[i] > values[i + 1]) {
                sorted = false;
                noDouble = false;// cannot be sure
            }
            if (values[i] == values[i + 1]) {
                noDouble = false;
            }
        }
        if (!sorted) {
            Arrays.sort(values);
        }
        if (!noDouble) {
            int nbVals = 1;
            for (int i = 0; i < n - 1; i++) {
                assert values[i] <= values[i + 1];
                if (values[i] < values[i + 1]) {
                    nbVals++;
                }
            }
            if (nbVals < n) {
                int[] correctValues = new int[nbVals];
                int idx = 0;
                for (int i = 0; i < n - 1; i++) {
                    if (values[i] < values[i + 1]) {
                        correctValues[idx++] = values[i];
                    }
                }
                correctValues[idx++] = values[n - 1];
                return correctValues;
            }
        }
        return values;
    }

    /**
     * Checks domain range.
     * Throws an exception if wrong range definition
     *
     * @param NAME name of the variable
     * @param MIN  lower bound of the domain
     * @param MAX  upper bound of the domain
     */
    private static void checkRealVar(String NAME, double MIN, double MAX) {
        if (MAX < MIN) {
            throw new SolverException(NAME + ": wrong domain definition, lower bound > upper bound");
        }
    }

    /**
     * Create a specific integer variable whom domain is reduced to the singleton {VALUE}.
     * <p/>
     * This API does not require any name, a default one will be assigned to the variable.
     * Furthermore, the object created will be cached, if not already, to avoid creating multiple occurrence of the
     * same "constant" variable.
     * <p/>
     * If one want to avoid the caching process, the following API should be used:
     * VariableFactory#fixed(String NAME, int VALUE, Solver SOLVER)
     *
     * @param VALUE  the value
     * @param SOLVER the solver to build the integer variable in.
     */
    public static IntVar fixed(int VALUE, Solver SOLVER) {
        return fixed(CSTE_NAME + VALUE, VALUE, SOLVER);
    }

    /**
     * Retrieve the specific zero/false boolvar.
     * <p/>
     *
     * @param SOLVER the solver to build the integer variable in.
     */
    public static BoolVar zero(Solver SOLVER) {
        return SOLVER.ZERO;
    }

    /**
     * Retrieve the specific one/true boolvar.
     * <p/>
     *
     * @param SOLVER the solver to build the integer variable in.
     */
    public static BoolVar one(Solver SOLVER) {
        return SOLVER.ONE;
    }

    //*************************************************************************************
    // CONSTANTS
    //*************************************************************************************

    /**
     * Create a specific integer variable, named NAME whom domain is reduced to the singleton {VALUE}.
     * <p/>
     * <b>Beware: if the name start with "cste -- ", the resulting variable will be cached.</b>
     *
     * @param NAME   name of the constant
     * @param VALUE  its value
     * @param SOLVER the solver to build the integer variable in.
     */
    public static IntVar fixed(String NAME, int VALUE, Solver SOLVER) {
        if (NAME.equals(CSTE_NAME + VALUE) && SOLVER.cachedConstants.containsKey(VALUE)) {
            return SOLVER.cachedConstants.get(VALUE);
        }
        FixedIntVarImpl cste = new FixedIntVarImpl(NAME, VALUE, SOLVER);
        if (NAME.equals(CSTE_NAME + VALUE)) {
            SOLVER.cachedConstants.put(VALUE, cste);
        }
        return cste;
    }

    /**
     * get a specific boolean variable, whom domain is reduced to the singleton {VALUE}.
     * This variable is unnamed as it is actually a solver constant
     *
     * @param VALUE  its value
     * @param SOLVER the solver to build the integer variable in.
     */
    public static IntVar fixed(boolean VALUE, Solver SOLVER) {
        if (VALUE) {
            return SOLVER.ONE;
        } else {
            return SOLVER.ZERO;
        }
    }

    /**
     * Create a specific set variable, named NAME whom domain is reduced to the singleton {VALUE}.
     *
     * @param NAME   name of the constant
     * @param VALUE  its value, a set of integers (duplicates will be removed)
     * @param SOLVER the solver to build the integer variable in.
     */
    public static SetVar fixed(String NAME, int[] VALUE, Solver SOLVER) {
        return new FixedSetVarImpl(NAME, VALUE, SOLVER);
    }

    //*************************************************************************************
    // VIEWS
    //*************************************************************************************

    /**
     * Create an offset view based on VAR, such that, the resulting view is defined on VAR + CSTE.
     * <p/>
     * The resulting IntVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
     * Any operations on this will transformed to operations on VAR following the offset rules.
     *
     * @param VAR  an integer variable
     * @param CSTE a constant
     */
    public static IntVar offset(IntVar VAR, int CSTE) {
        if (CSTE == 0) {
            return VAR;
        }
		if(Configuration.ENABLE_VIEWS) {
			return new OffsetView(VAR, CSTE, VAR.getSolver());
		}else{
			Solver s = VAR.getSolver();
			int lb = VAR.getLB()+CSTE;
			int ub = VAR.getUB()+CSTE;
			String name = "("+VAR.getName()+"+"+CSTE+")";
			IntVar ov;
			if(VAR.hasEnumeratedDomain()){
				ov = enumerated(name,lb,ub,s);
			}else{
				ov = bounded(name,lb,ub,s);
			}
			s.post(ICF.arithm(ov,"-",VAR,"=",CSTE));
			return ov;
		}
    }

	/**
	 * Create a kind of clone of VAR (an offset view with CSTE= 0), such that, the resulting view is defined on VAR.
	 * <p/>
	 * The resulting IntVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
	 * Any operations on this will transformed to operations on VAR following the offset rules.
	 *
	 * @param VAR an integer variable
	 */
	public static IntVar eq(IntVar VAR) {
		// this part should remain (at least for Propagator.checkVariable())
		if ((VAR.getTypeAndKind() & Variable.KIND) == Variable.BOOL) {
			return eqbool((BoolVar) VAR);
		} else {
			return eqint(VAR);
		}
	}

	/**
	 * Create a kind of clone of VAR such that, the resulting view is defined on VAR.
	 * <p/>
	 * The resulting BoolVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
	 * Any operations on this will transformed to operations on VAR.
	 *
	 * @param VAR an integer variable
	 */
	public static BoolVar eq(BoolVar VAR) {
		return eqbool(VAR);
	}

    private static IntVar eqint(IntVar ivar) {
		if(Configuration.ENABLE_VIEWS) {
			return new EqView(ivar, ivar.getSolver());
		}else{
			IntVar ov = ivar.duplicate();
			ivar.getSolver().post(ICF.arithm(ov,"=",ivar));
			return ov;
		}
    }

    private static BoolVar eqbool(BoolVar boolVar) {
		if(Configuration.ENABLE_VIEWS){
			return new BoolEqView(boolVar, boolVar.getSolver());

		}else{
			BoolVar ov = boolVar.duplicate();
			boolVar.getSolver().post(ICF.arithm(ov,"=",boolVar));
			return ov;
		}
    }

    /**
     * Create a view over BOOL holding the logical negation of BOOL (ie, &not;BOOL).
     * <p/>
     * The resulting BoolVar does not have explicit domain: it relies on the domain of BOOL for reading and writing operations.
     * Any operations on this will transformed to operations on VAR following the "not" rules.
     *
     * @param BOOL a boolean variable.
     */
    public static BoolVar not(BoolVar BOOL) {
		if(Configuration.ENABLE_VIEWS) {
			return new BoolNotView(BOOL, BOOL.getSolver());
		}else{
			Solver s = BOOL.getSolver();
			BoolVar ov = bool("not(" + BOOL.getName() + ")", s);
			s.post(ICF.arithm(ov, "!=", BOOL));
			return ov;
		}
    }

    /**
     * Create a view over VAR holding : &minus;VAR.
     * That is if VAR = [a,b], then this = [-b,-a].
     * <p/>
     * The resulting IntVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
     * Any operations on this will transformed to operations on VAR following the "minus" rules.
     *
     * @param VAR an integer variable
     */
    public static IntVar minus(IntVar VAR) {
		if(Configuration.ENABLE_VIEWS) {
			return new MinusView(VAR, VAR.getSolver());
		}else{
			Solver s = VAR.getSolver();
			int ub = -VAR.getLB();
			int lb = -VAR.getUB();
			String name = "-("+VAR.getName()+")";
			IntVar ov;
			if(VAR.hasEnumeratedDomain()){
				ov = enumerated(name,lb,ub,s);
			}else{
				ov = bounded(name,lb,ub,s);
			}
			s.post(ICF.arithm(ov,"-",VAR,"=",0));
			return ov;
		}
    }

    /**
     * Create a view over VAR such that: VAR&times;CSTE (CSTE&gt;-2).
     * <p/>
     * <br/>- if CSTE &lt; -1, throws an exception;
     * <br/>- if CSTE = -1, returns a minus view;
     * <br/>- if CSTE = 0, returns a fixed variable;
     * <br/>- if CSTE = 1, returns VAR;
     * <br/>- otherwise, returns a scale view;
     * <p/>
     * The resulting IntVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
     * Any operations on this will transformed to operations on VAR following the "scale" rules.
     *
     * @param VAR  an integer variable
     * @param CSTE a constant.
     */
    public static IntVar scale(IntVar VAR, int CSTE) {
        if (CSTE == -1) {
            return minus(VAR);
        }
        if (CSTE < 0) {
            throw new UnsupportedOperationException("scale required positive coefficient!");
        } else {
            IntVar var;
            if (CSTE == 0) {
                var = fixed(0, VAR.getSolver());
            } else if (CSTE == 1) {
                var = VAR;
            } else {
				if(Configuration.ENABLE_VIEWS) {
					var = new ScaleView(VAR, CSTE, VAR.getSolver());
				}else{
					Solver s = VAR.getSolver();
					int lb = VAR.getLB()*CSTE;
					int ub = VAR.getUB()*CSTE;
					String name = "("+VAR.getName()+"*"+CSTE+")";
					IntVar ov;
					if(VAR.hasEnumeratedDomain()){
						ov = enumerated(name,lb,ub,s);
					}else{
						ov = bounded(name,lb,ub,s);
					}
					s.post(ICF.times(VAR, fixed(CSTE, VAR.getSolver()), ov));
					return ov;
				}
            }
            return var;
        }
    }

    /**
     * Create a view over VAR such that: |VAR|.
     * <p/>
     * <br/>- if VAR is already instantiated, returns a fixed variable;
     * <br/>- if the lower bound of VAR is greater or equal to 0, returns VAR;
     * <br/>- if the upper bound of VAR is less or equal to 0, return a minus view;
     * <br/>- otherwise, returns an absolute view;
     * <p/>
     * The resulting IntVar does not have explicit domain: it relies on the domain of VAR for reading and writing operations.
     * Any operations on this will transformed to operations on VAR following the "absolute" rules.
     *
     * @param VAR an integer variable.
     */
    public static IntVar abs(IntVar VAR) {
        if (VAR.isInstantiated()) {
            return fixed(Math.abs(VAR.getValue()), VAR.getSolver());
        } else if (VAR.getLB() >= 0) {
            return VAR;
        } else if (VAR.getUB() <= 0) {
            return minus(VAR);
        } else {
            Solver s = VAR.getSolver();
			int ub = Math.max(-VAR.getLB(), VAR.getUB());
			String name = "|"+VAR.getName()+"|";
            IntVar abs;
            if (VAR.hasEnumeratedDomain()) {
                abs = enumerated(name, 0, ub, s);
            } else {
                abs = bounded(name, 0, ub, s);
            }
            s.post(IntConstraintFactory.absolute(abs, VAR));
            return abs;
        }
    }

    /**
     * Create a real variable based on an integer variable VAR.
     * The PRECISION is used to evaluate when the view is instantiated, that is, when
     * the size of the domain of the view is strictly less than  PRECISION.
     *
     * @param VAR       an integer variable
     * @param PRECISION used to evaluate the instantiation of the view.
     */
    public static RealVar real(IntVar VAR, double PRECISION) {
		if(Configuration.ENABLE_VIEWS) {
			return new RealView(VAR, PRECISION);
		}else{
			Solver s = VAR.getSolver();
			double lb = VAR.getLB();
			double ub = VAR.getUB();
			RealVar rv = real("(real)" + VAR.getName(), lb, ub, PRECISION, s);
			s.post(new IntEqRealConstraint(VAR,rv,PRECISION));
			return rv;
		}
    }

    /**
     * Create an array of real variables based on an array of integer variables VAR.
     * The PRECISION is used to evaluate when the view is instantiated, that is, when
     * the size of the domain of the view is strictly less than  PRECISION.
     *
     * @param VARS      array of integer variables
     * @param PRECISION used to evaluate the instantiation of each view.
     * @return
     */
    public static RealVar[] real(IntVar[] VARS, double PRECISION) {
		RealVar[] reals = new RealVar[VARS.length];
		if(Configuration.ENABLE_VIEWS) {
			for (int i = 0; i < VARS.length; i++) {
				reals[i] = real(VARS[i], PRECISION);
			}
		}else{
			Solver s = VARS[0].getSolver();
			for (int i = 0; i < VARS.length; i++) {
				double lb = VARS[i].getLB();
				double ub = VARS[i].getUB();
				reals[i] = real("(real)" + VARS[i].getName(), lb, ub, PRECISION, s);
			}
			s.post(new IntEqRealConstraint(VARS,reals,PRECISION));
		}
		return reals;
    }
}
