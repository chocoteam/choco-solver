/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables;

import org.chocosolver.solver.ISelf;
import org.chocosolver.solver.Model;

import java.util.Arrays;

/**
 * Interface to create variables resulting from functions (encoded as constraints).
 * <p>
 *
 * @author Jean-Guillaume FAGES
 */
public interface IResultVariableFactory extends ISelf<Model> {

	//*************************************************************************************
	// SUM
	//*************************************************************************************

	/**
	 * Creates an integer variable equal to the sum of input variables toSum
	 * @param name name of the resulting variable
	 * @param toSum integer variables to be summed
	 * @return an integer variable equal to the sum of input variables toSum
	 */
	default IntVar sum(String name, IntVar... toSum){
		int lb = Arrays.stream(toSum).mapToInt(IntVar::getLB).sum();
		int ub = Arrays.stream(toSum).mapToInt(IntVar::getUB).sum();
		IntVar result = ref().intVar(name, lb, ub, true);
		if (!result.isInstantiated()) {
			ref().sum(toSum, "=", result).post();
		}
		return result;
	}

	//*************************************************************************************
	// COUNT
	//*************************************************************************************

	/**
	 * Creates a variable counting how many variables in vars are equal to value
	 * @param name name of the result variable
	 * @param value integer value whose occurrence in vars to be counted
	 * @param vars integer variables to be counted
	 * @return a variable counting how many variables in vars are equal to value
	 */
	default IntVar count(String name, int value, IntVar... vars) {
		int lb = (int) Arrays.stream(vars).filter(v -> v.isInstantiatedTo(value)).count();
		int ub = (int) Arrays.stream(vars).filter(v -> v.contains(value)).count();
		IntVar result = ref().intVar(name, lb, ub, true);
		if (!result.isInstantiated()) {
			ref().count(value, vars, result).post();
		}
		return result;
	}

	/**
	 * Creates a variable counting how many variables in vars are equal to value
	 * @param name name of the result variable
	 * @param value integer variable whose occurrence in vars is to be counted
	 * @param vars integer variables to be counted
	 * @return a variable counting how many variables in vars are equal to value
	 */
	default IntVar count(String name, IntVar value, IntVar... vars) {
		if(value.isInstantiated()) {
			return count(name, value.getValue(), vars);
		}
		IntVar result = ref().intVar(name, 0, vars.length, true);
		if (!result.isInstantiated()) {
			ref().count(value, vars, result).post();
		}
		return result;
	}

	//*************************************************************************************
	// ELEMENT
	//*************************************************************************************

	/**
	 * Creates a variable equal to table[index-offser]
	 * @param name name of the result variable
	 * @param table array of integer values
	 * @param index variable indicating which cell to select
	 * @param offset parameter applying to index
	 * @return a variable equal to table[index-offser]
	 */
	default IntVar element(String name, int[] table, IntVar index, int offset){
		IntVar result = ref().intVar(name, index.stream()
				.filter(v-> v>=offset && v<table.length+offset)
				.map(v->table[v-offset]).toArray());
		if (!result.isInstantiated()) {
			ref().element(result, table, index, offset).post();
		} else if(index.getLB()<offset || index.getUB()>offset+table.length-1){
			ref().member(index, offset, offset+table.length-1).post();
		}
		return result;
	}

	/**
	 * Creates a variable equal to table[index-offser]
	 * @param name name of the result variable
	 * @param table array of integer variables
	 * @param index variable indicating which cell to select
	 * @param offset parameter applying to index
	 * @return a variable equal to table[index-offser]
	 */
	default IntVar element(String name, IntVar[] table, IntVar index, int offset) {
		int lb = index.stream()
				.filter(v-> v>=offset && v<table.length+offset)
				.map(v->table[v-offset].getLB()).min().getAsInt();
		int ub = index.stream()
				.filter(v-> v>=offset && v<table.length+offset)
				.map(v->table[v-offset].getUB()).max().getAsInt();
		IntVar result = ref().intVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().element(result, table, index, offset).post();
		} else if(index.getLB()<offset || index.getUB()>offset+table.length-1){
			ref().member(index, offset, offset+table.length-1).post();
		}
		return result;
	}

	/**
	 * Creates a variable equal to min(vars)
	 * @param name name of the result variable
	 * @param vars a set of integer variables
	 * @return a variable equal to min(vars)
	 */
	default IntVar min(String name, IntVar... vars) {
		int lb = Arrays.stream(vars).mapToInt(IntVar::getLB).min().getAsInt();
		int ub = Arrays.stream(vars).mapToInt(IntVar::getUB).max().getAsInt();
		IntVar result = ref().intVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().min(result, vars).post();
		}
		return result;
	}

	/**
	 * Creates a variable equal to max(vars)
	 * @param name name of the result variable
	 * @param vars a set of integer variables
	 * @return a variable equal to max(vars)
	 */
	default IntVar max(String name, IntVar[] vars) {
		int lb = Arrays.stream(vars).mapToInt(IntVar::getLB).min().getAsInt();
		int ub = Arrays.stream(vars).mapToInt(IntVar::getUB).max().getAsInt();
		IntVar result = ref().intVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().max(result, vars).post();
		}
		return result;
	}

	/**
	 * Creates a variable equal to argmin(vars)
	 * @param name name of the result variable
	 * @param vars a set of integer variables
	 * @return a variable equal to argmin(vars)
	 */
	default IntVar argmin(String name, IntVar[] vars) {
		IntVar result = ref().intVar(name, 0, vars.length-1, false);
		if (!result.isInstantiated()) {
			ref().argmin(result, 0, vars).post();
		}
		return result;
	}

	/**
	 * Creates a variable equal to argmax(vars)
	 * @param name name of the result variable
	 * @param vars a set of integer variables
	 * @return a variable equal to argmax(vars)
	 */
	default IntVar argmax(String name, IntVar[] vars) {
		IntVar result = ref().intVar(name, 0, vars.length-1, false);
		if (!result.isInstantiated()) {
			ref().argmax(result, 0, vars).post();
		}
		return result;
	}


	//*************************************************************************************
	// SET CONSTRAINTS
	//*************************************************************************************

	/**
	 * Creates a set variable equal to the union of vars
	 * @param name name of the result variable
	 * @param vars integer variables
	 * @return a set variable equal to the union of vars
	 */
	default SetVar union(String name, IntVar... vars) {
		int[] lb = new int[0];
		int[] ub = Arrays.stream(vars).flatMapToInt(IntVar::stream).distinct().toArray();
		SetVar result = ref().setVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().union(vars, result).post();
		}
		return result;
	}

	/**
	 * Creates a set variable equal to the union of sets
	 * @param name name of the result variable
	 * @param sets set variables
	 * @return a set variable equal to the union of sets
	 */
	default SetVar union(String name, SetVar... sets) {
		int[] lb = new int[0];
		int[] ub = Arrays.stream(sets).flatMapToInt(s-> Arrays.stream(s.getUB().toArray())).distinct().toArray();
		SetVar result = ref().setVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().union(sets, result).post();
		}
		return result;
	}

	/**
	 * Creates a set variable equal to the intersection of sets
	 * @param name name of the result variable
	 * @param sets set variables
	 * @return a set variable equal to the intersection of sets
	 */
	default SetVar intersection(String name, SetVar... sets) {
		int[] lb = new int[0];
		int[] ub = Arrays.stream(sets).flatMapToInt(s-> Arrays.stream(s.getUB().toArray())).distinct().toArray();
		SetVar result = ref().setVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().intersection(sets, result).post();
		}
		return result;
	}

	/**
	 * Creates a variable counting how many set variables are empty
	 * @param name name of the result variable
	 * @param vars set variables
	 * @return an integer variable counting how many set variables are empty
	 */
	default IntVar nbEmpty(String name, SetVar... vars){
		int lb = (int) Arrays.stream(vars).filter(s->s.getUB().isEmpty()).count();
		int ub = (int) Arrays.stream(vars).filter(s->s.getLB().isEmpty()).count();
		IntVar result = ref().intVar(name, lb, ub, true);
		if (!result.isInstantiated()) {
			ref().nbEmpty(vars, result).post();
		}
		return result;
	}

	/**
	 * Creates an integer variable equal to SUM_i_in_indices_(values[i-offset])
	 * @param name name of the result variable
	 * @param values
	 * @param offset
	 * @param indices
	 * @return an integer variable equal to SUM_i_in_indices_(values[i-offset])
	 */
	default IntVar sum(String name, int[] values, int offset, SetVar indices){
		int lb = Arrays.stream(indices.getLB().toArray()).map(i->values[i-offset]).sum();
		int ub = Arrays.stream(indices.getUB().toArray()).map(i->values[i-offset]).sum();
		IntVar result = ref().intVar(name, lb, ub, true);
		if (!result.isInstantiated()) {
			ref().sumElements(indices, values, offset, result).post();
		}
		return result;
	}

	/**
	 * Creates a set variable <i>set</i> = <i>sets</i>[<i>index</i>-<i>offset</i>]
	 *
	 * @param name   name of the result variable
	 * @param index  an integer variable pointing to <i>set</i>'s index into array <i>sets</i>
	 * @param sets   an array of set variables representing possible values for <i>set</i>
	 * @param offset offset index : should be 0 by default
	 *               but generally 1 with MiniZinc API
	 *               which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @return a set variable equal to <i>sets</i>[<i>index</i>-<i>offset</i>]
	 */
	default SetVar element(String name, IntVar index, SetVar[] sets, int offset) {
		int[] lb = new int[0];
		int[] ub = index.stream()
				.filter(i -> i>= offset && i<sets.length+offset)
				.flatMap(i-> Arrays.stream(sets[i].getUB().toArray())).distinct().toArray();
		SetVar result = ref().setVar(name, lb, ub);
		if (!result.isInstantiated()) {
			ref().element(index, sets, offset, result).post();
		} else if(index.getLB()<offset || index.getUB()>offset+sets.length-1){
			ref().member(index, offset, offset+sets.length-1).post();
		}
		return result;
	}
}
