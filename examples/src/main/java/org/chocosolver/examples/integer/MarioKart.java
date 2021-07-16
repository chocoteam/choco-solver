/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Random;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.lastConflict;
import static org.chocosolver.util.tools.ArrayUtils.flatten;

/**
 *
 * <h3>Path Constraint Problem</h3>
 *
 * <h2>The Problem</h2> The path problem a simple problem where we are trying to find the best path in a directed graph.
 * A path is a succession of node starting from the source node to the destination node. Each node has to be connected
 * to the previous and the next node by a a directed edge in the graph. The path can't admit any cycle </br>
 *
 * <h2>The Model</h2> This model will specify all the constraints to make a coherent path in the graph. The graph is both
 * represented by a BoolVar matrix and an array of IntVar (successor representation). For instance, if the boolean (i, j)
 * of the matrix is equals to true, that means that the path contains this edge starting from the node of id i to the node
 * of id j. If the edge doesn't exists in the graph the the boolean value is equals to the Choco constants FALSE.
 * Else the edge can be used or not, so its representation is a choco boolean variable which can be instantiated to
 * true : edge is in the path or to false : the edge isn't in the path.
 *
 * <h2>The Example</h2> We are presenting this problem through an little example of Mario's day. </br> Mario is an
 * Italian Plumber and is work is mainly to find gold in the plumbing of all the houses of the neighborhood. Mario is
 * moving in the city using his kart that has a specified amount of fuel. Mario starts his day of work from his personal
 * house and always end to his friend Luigi's house to have the supper. The problem here is to plan the best path for
 * Mario in order to earn the more money with the amount of fuel of his kart ! </p> (Version 1.3) We are making the
 * analogy of this problem to the knapsack problem. In fact we want to found a set of edges that form a path where Mario
 * can find the more gold and respects the fuel limit constraint. The analogy is the following :
 * <ul>
 * <li>The weight is the consumption to go through the edge</li>
 * <li>The energy is the gold that we can earn on the house at the end of the edge</li>
 * </ul>
 *
 * @author Amaury Ollagnier, Jean-Guillaume Fages
 * @since 21/05/2013
 */
@SuppressWarnings("unchecked")
public class MarioKart extends AbstractProblem {

	// CONSTANTS

	/** The seed of the generation of the problem */
	private static int SEED = 1789;
	/** The number of house in the neighborhood of Mario : Size of the graph : Number of nodes */
	private static int HOUSE_NUMBER = 15;
	/** The distance of the city in meters : Max length of the edges in the graph */
	private static int CITY_SIZE = 5000;
	/** The maximum amount of gold that Mario has ever founded in a house plumbing */
	private static int MAX_GOLD = 100;
	/** The Mario's house id. Random generation if equals to Integer.MAX_VALUE */
	private static int MARIO_HOUSE_ID = 0;//Integer.MAX_VALUE;
	/** The Luigi's house id. Random generation if equals to Integer.MAX_VALUE */
	private static int LUIGI_HOUSE_ID = 1;//Integer.MAX_VALUE;
	/** The amount of fuel of the kart in mini-litres */
	private static int FUEL = 2000;
	/** The kart of mario */
	private static KART MARIOS_KART = KART.ECOLO;

	// INSTANCES VARIABLES

	/** The dimension of the graph i.e. the number of nodes in the graph */
	private int n;
	/** The source node id */
	private int s;
	/** The destination node id */
	private int t;
	/** The matrix of consumptions the keeps the number of mini-litres needed to go to one house to another */
	private int[][] consumptions;
	/** The amount of gold that Mario can find in each house */
	private int[] gold;

	/**
	 * The boolean matrix represents all the edges in the graph, and if the boolean (i, j) of the matrix is equals to
	 * true, that means that the path contains this edge starting from the node of id i to the node of id j. If the edge
	 * doesn't exists in the graph the the boolean value is equals to the Choco constants FALSE. Else the edge can be
	 * used or not, so it representation is a choco boolean variable which can be instantiated to true : edge is in the
	 * path or to false : the edge isn't in the path.
	 */
	private BoolVar[][] edges;

	/**
	 * The next value table. The next variable of a node is the id of the next node in the path + an offset. If the node
	 * isn't used, the next value is equals to the current node id + the offset
	 */
	private IntVar[] next;

	/** Integer Variable which represents the overall size of the path founded */
	private IntVar size;

	/** All the gold that Mario has found on the path : the objective variable of the problem */
	private IntVar goldFound;

	/** The consumption of the Mario's Kart in the path */
	private IntVar fuelConsumed;

	// METHODS


	@Override
 public void buildModel() {
		model = new Model();
		data();
		variables();
		constraints();
		strengthenFiltering();
	}

	@Override
	public void configureSearch() {
		/* Listeners */
		Solver r = model.getSolver();
		/* Heuristic choices */
		r.setSearch(lastConflict(inputOrderLBSearch(next)));
	}

	@Override
	public void solve() {
		model.setObjective(true, goldFound);
		while (model.getSolver().solve()) {
			prettyOut();
		}
		printInputData();
	}

	private void prettyOut() {
		/* log out the solution of the problem founded */
		System.out.println((int) ((size.getValue() + 0d) / (HOUSE_NUMBER + 0d) * 100) + " % of houses visited");
		System.out.println((int) ((fuelConsumed.getValue() + 0d) / (FUEL + 0d) * 100) + " % of fuel burned");
		System.out.println("! " + goldFound.getValue() + " gold coins earned !");
	}

	private void printInputData(){
		System.out.println("nbHouses = "+HOUSE_NUMBER+";");
		System.out.println("MarioHouse = "+MARIO_HOUSE_ID+";");
		System.out.println("LuigiHouse = "+LUIGI_HOUSE_ID+";");
		System.out.println("fuelMax = "+FUEL+";");
		System.out.println("goldTotalAmount = "+MAX_GOLD*HOUSE_NUMBER+";");
		String conso = "conso = [";
		for(int i=0;i<HOUSE_NUMBER;i++){
			String s = "|";
			for(int j=0;j<HOUSE_NUMBER-1;j++){
				s+=this.consumptions[i][j]+",";
			}
			conso += s+this.consumptions[i][HOUSE_NUMBER-1];
		}
		conso+="|];";
		System.out.println(conso);
		String goldInHouse = "goldInHouse = [";
		for(int i=0;i<HOUSE_NUMBER-1;i++){
			goldInHouse+=this.gold[i]+",";
		}
		goldInHouse += this.gold[HOUSE_NUMBER-1]+"];";
		System.out.println(goldInHouse);
	}

	/** Creation of the problem instance */
	private void data() {
		/* Data of the town */
		int[][] distances = generateRandomCosts(HOUSE_NUMBER, SEED, CITY_SIZE);
		consumptions = computeConsumptions(distances);
		gold = generateGolds(HOUSE_NUMBER, SEED, MAX_GOLD);

		/* Mario and Luigi houses */
		Random rd = new Random(SEED);
		if (MARIO_HOUSE_ID == Integer.MAX_VALUE)
			MARIO_HOUSE_ID = rd.nextInt(HOUSE_NUMBER - 1);
		if (LUIGI_HOUSE_ID == Integer.MAX_VALUE) {
			LUIGI_HOUSE_ID = rd.nextInt(HOUSE_NUMBER - 1);
			while (LUIGI_HOUSE_ID == MARIO_HOUSE_ID)
				LUIGI_HOUSE_ID = rd.nextInt(HOUSE_NUMBER - 1);
		}

		/* Force some values */
		consumptions[LUIGI_HOUSE_ID][MARIO_HOUSE_ID] = 0;
		gold[MARIO_HOUSE_ID] = 0; // no money at your house
		gold[LUIGI_HOUSE_ID] = 0; // don't steel luigi !

		/* The basics variables of the graph */
		this.n = HOUSE_NUMBER;
		this.s = MARIO_HOUSE_ID;
		this.t = LUIGI_HOUSE_ID;
	}

	/** Creation of CP variables */
	private void variables() {
		/* Choco variables */
		fuelConsumed = model.intVar("Fuel Consumption", 0, FUEL, true);
		goldFound = model.intVar("Gold Found", 0, CITY_SIZE * MAX_GOLD, true);
		/* Initialisation of the boolean matrix */
		edges = model.boolVarMatrix("edges", n, n);
		/* Initialisation of all the next value for each house */
		next = model.intVarArray("next", n, 0, n - 1, false);
		/* Initialisation of the size variable */
		size = model.intVar("size", 2, n, true);
	}

	/** Post all the constraints of the problem */
	private void constraints() {
		/* The scalar constraint to compute global consumption of the kart to perform the path */
		model.scalar(flatten(edges), flatten(consumptions), "=", fuelConsumed).post();

		/* The scalar constraint to compute the amount of gold founded by Mario in the path. With our model if a
		 * node isn't used then his next value is equals to his id. Then the boolean edges[i][i] is equals to true */
		BoolVar[] used = new BoolVar[n];
		for (int i = 0; i < used.length; i++)
			used[i] = edges[i][i].not();
		model.scalar(used, gold, "=", goldFound).post();

		/* The subCircuit constraint. This forces all the next value to form a circuit which the overall size is equals
		 * to the size variable. This constraint check if the path contains any sub circles. */
		model.subCircuit(next, 0, size).post();

		/* The path has to end on the t node. This constraint doesn't create a path, but a circle or a circuit. So we
		 * force the edge (t,s) then all the other node of the circuit will form a starting from s and ending at t */
		model.arithm(next[t], "=", s).post();

		/* The boolean channeling constraint. Enforce the relation between the next values and the edges values in the
		 * graph boolean variable matrix */
		for (int i = 0; i < n; i++) {
			model.boolsIntChanneling(edges[i], next[i], 0).post();
		}
	}

	/** Adds more constraints to get a stronger filtering */
	private void strengthenFiltering() {
		/* FUEL RELATED FILTERING:
		 * identifies the min/max fuel consumption involved by visiting each house */
		IntVar[] fuelHouse = new IntVar[HOUSE_NUMBER];
		for (int i = 0; i < HOUSE_NUMBER; i++) {
			fuelHouse[i] = model.intVar("fuelHouse", 0, FUEL, false);
			model.element(fuelHouse[i], consumptions[i], next[i], 0).post();
		}
		model.sum(fuelHouse, "=", fuelConsumed).post();

		/* GOLD RELATED FILTERING
		* This problem can be seen has a knapsack problem where are trying to found the set of edges that contains the
		* more golds and respects the fuel limit constraint. The analogy is the following : the weight is the
		* consumption to go through the edge and the energy is the gold that we can earn */
		int[][] goldMatrix = new int[n][n];
		for (int i = 0; i < goldMatrix.length; i++)
			for (int j = 0; j < goldMatrix.length; j++)
				goldMatrix[i][j] = (i == j) ? 0 : gold[i];
		model.knapsack(flatten(edges), model.intVar(FUEL),
				goldFound, flatten(consumptions), flatten(goldMatrix)).post();
	}

	// SOME USEFUL METHODS

	/**
	 * Compute the matrix of consumption from the matrix of distance regarding the kart of mario
	 *
	 * @param distances matrix of int
	 * @return a matrix of consumption
	 */
	private static int[][] computeConsumptions(int[][] distances) {
		int[][] conso = new int[distances.length][distances.length];
		for (int i = 0; i < conso.length; i++)
			for (int j = 0; j < conso.length; j++)
				conso[i][j] = (int) (distances[i][j] * MARIOS_KART.getConsoMiniLitreByMeter());
		return conso;
	}

	/**
	 * Put a random amount of gold in each house
	 * @param n int
	 * @param s int
	 * @param max int
	 * @return generate the amount of gold in the plumbing of all the houses
	 */
	private int[] generateGolds(int n, int s, int max) {
		int[] golds = new int[n];
		Random rd = new Random(s);
		for (int i = 0; i < golds.length; i++)
			golds[i] = rd.nextInt(max);
		return golds;
	}

	// LAUNCHER

	/**
	 * The main to execute
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		new MarioKart().execute(args);
	}

	// TOOLS FOR THE RESOLUTION

	/** The possibles type of kart */
	private enum KART {
		TRUNK(10),
		NORMAL(5),
		ECOLO(2);

		/** The consumption of the kart in litre / 100km */
		private double conso;

		/**
		 * @param conso the consumption of the kart in litre / 100km
		 * Return Build a kart with his specify conso
		 */
		KART(double conso) {
			this.conso = conso;
		}

		/**
		 * @return the consumption in mini litre by meter
		 */
		public double getConsoMiniLitreByMeter() {
			return conso / 10d;
		}
	}

	private static int[][] generateRandomCosts(int n, int s, int max) {
	        Random rd = new Random(s);
	        int[][] costs = new int[n][n];
	        for (int i = 0; i < n; i++) {
	            for (int j = i + 1; j < n; j++) {
	                costs[j][i] = costs[i][j] = rd.nextInt(max);
	            }
	        }
	        return costs;
	    }
}
