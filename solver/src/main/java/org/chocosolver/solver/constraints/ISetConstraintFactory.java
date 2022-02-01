/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.set.*;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * Interface to make constraints over SetVar
 *
 * A kind of factory relying on interface default implementation to allow (multiple) inheritance
 *
 * @author Jean-Guillaume FAGES
 * @since 4.0.0
 */
public interface ISetConstraintFactory {

	//***********************************************************************************
	// BASICS : union/inter/subset/card
	//***********************************************************************************

	/**
	 * Creates a constraint ensuring that <i>union</i> is exactly the union of values taken by <i>ints</i>,
	 *
	 * @param ints  an array of integer variables
	 * @param union a set variable
	 * @return a constraint ensuring that <i>union</i> = {x | x in <i>ints</i>}
	 */
	default Constraint union(IntVar[] ints, SetVar union) {
		return new Constraint(ConstraintsName.SETINTVALUESUNION
				, new PropSetIntValuesUnion(ints, union)
				, new PropSetIntValuesUnion(ints, union)
		);
	}

	/**
	 * Creates a constraint which ensures that the union of <i>sets</i> is equal to <i>unionSet</i>
	 *
	 * @param sets an array of set variables
	 * @param unionSet set variable representing the union of <i>sets</i>
	 * @return A constraint ensuring that the union of <i>sets</i> is equal to <i>unionSet</i>
	 */
	default Constraint union(SetVar[] sets, SetVar unionSet) {
		return new Constraint(ConstraintsName.SETUNION, new PropUnion(sets, unionSet), new PropUnion(sets, unionSet));
	}

	/**
	 * Creates a constraint which ensures that the intersection of <i>sets</i> is equal to <i>intersectionSet</i>
	 *
	 * @param sets an array of set variables
	 * @param intersectionSet a set variable representing the intersection of <i>sets</i>
	 * @return A constraint ensuring that the intersection of <i>sets</i> is equal to <i>intersectionSet</i>
	 */
	default Constraint intersection(SetVar[] sets, SetVar intersectionSet) {
		return intersection(sets, intersectionSet, false);
	}

	/**
	 * Creates a constraint which ensures that the intersection of <i>sets</i> is equal to <i>intersectionSet</i>
	 *
	 * @param sets an array of set variables
	 * @param intersectionSet a set variable representing the intersection of <i>sets</i>
         * @param boundConsistent adds an additional propagator to guarantee BC
	 * @return A constraint ensuring that the intersection of <i>sets</i> is equal to <i>intersectionSet</i>
	 */
	default Constraint intersection(SetVar[] sets, SetVar intersectionSet, boolean boundConsistent) {
		if (sets.length == 0) {
			throw new IllegalArgumentException("The intersection of zero sets is undefined.");
		}
		if (boundConsistent) {
			return new Constraint(ConstraintsName.SETINTERSECTION,
				new PropIntersection(sets, intersectionSet),
				sets.length == 1
					? new PropAllEqual(new SetVar[]{sets[0], intersectionSet})
					: new PropIntersectionFilterSets(sets, intersectionSet));
		} else {
			return new Constraint(ConstraintsName.SETINTERSECTION, new PropIntersection(sets, intersectionSet));
		}
	}

	/**
	 * Creates a constraint establishing that <i>sets</i>[i] is a subset of <i>sets</i>[j] if i<j
	 *
	 * @param sets an array of set variables
	 * @return A constraint which ensures that <i>sets</i>[i] is a subset of <i>sets</i>[j] if i<j
	 */
	default Constraint subsetEq(SetVar... sets) {
		Propagator[] props = new Propagator[sets.length - 1];
		for (int i = 0; i < sets.length - 1; i++) {
			props[i] = new PropSubsetEq(sets[i], sets[i + 1]);
		}
		return new Constraint(ConstraintsName.SETSUBSETEQ, props);
	}

	/**
	 * Creates a constraint counting the number of empty sets <i>sets</i>
	 * |{s in <i>sets</i> where |s|=0}| = <i>nbEmpty</i>
	 *
	 * @param sets an array of set variables
	 * @param nbEmpty integer restricting the number of empty sets in <i>sets</i>
	 * @return A constraint ensuring that |{s in <i>sets</i> where |s|=0}| = <i>nbEmpty</i>
	 */
	default Constraint nbEmpty(SetVar[] sets, int nbEmpty) {
		return nbEmpty(sets, sets[0].getModel().intVar(nbEmpty));
	}

	/**
	 * Creates a constraint counting the number of empty sets <i>sets</i>
	 * |{s in <i>sets</i> where |s|=0}| = <i>nbEmpty</i>
	 *
	 * @param sets an array of set variables
	 * @param nbEmpty integer variable restricting the number of empty sets in <i>sets</i>
	 * @return A constraint ensuring that |{s in <i>sets</i> where |s|=0}| = <i>nbEmpty</i>
	 */
	default Constraint nbEmpty(SetVar[] sets, IntVar nbEmpty) {
		return new Constraint(ConstraintsName.SETNBEMPTY, new PropNbEmpty(sets, nbEmpty));
	}

	/**
	 * Creates a constraint linking <i>set1</i> and <i>set2</i> with an index <i>offset</i> :
	 * x in <i>set1</i> <=> x+offset in <i>set2</i>
	 *
	 * @param set1 a set variable
	 * @param set2 a set variable
	 * @param offset offset index
	 * @return a constraint ensuring that x in <i>set1</i> <=> x+<i>offset</i> in <i>set2</i>
	 */
	default Constraint offSet(SetVar set1, SetVar set2, int offset) {
		return new Constraint(ConstraintsName.SETOFFSET, new PropOffSet(set1, set2, offset));
	}

	/**
	 * Creates a constraint preventing <i>set</i> to be empty
	 *
	 * @param set a SetVar which should not be empty
	 * @return a constraint ensuring that <i>set</i> is not empty
	 */
	default Constraint notEmpty(SetVar set) {
		return new Constraint(ConstraintsName.SETNOTEMPTY, new PropNotEmpty(set));
	}

	//***********************************************************************************
	// SUM
	//***********************************************************************************

	/**
	 * Creates a constraint summing elements of <i>set</i>
	 * sum{i | i in <i>set</i>} = <i>sum</i>
	 *
	 * @param set       a set variable
	 * @param sum       an integer variable representing sum{i | i in <i>set</i>}
	 * @return a constraint ensuring that sum{i | i in <i>set</i>} = <i>sum</i>
	 */
	default Constraint sum(SetVar set, IntVar sum) {
		return sumElements(set,null,0,sum);
	}

	/**
	 * Creates a constraint summing weights given by a set of indices:
	 * sum{<i>weights</i>[i] | i in <i>indices</i>} = <i>sum</i>
	 *
	 * Also ensures that elements in <i>indices</i> belong to [0, weights.length-1]
	 *
	 * @param indices   a set variable
	 * @param weights   integers representing the weight of each element in <i>indices</i>
	 * @param sum       an integer variable representing sum{<i>weights</i>[i] | i in <i>indices</i>}
	 * @return a constraint ensuring that sum{<i>weights</i>[i] | i in <i>indices</i>} = <i>sum</i>
	 */
	default Constraint sumElements(SetVar indices, int[] weights, IntVar sum) {
		return sumElements(indices,weights,0,sum);
	}

	/**
	 * Creates a constraint summing weights given by a set of indices:
	 * sum{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>sum</i>
	 *
	 * Also ensures that elements in <i>indices</i> belong to [offset, offset+weights.length-1]
	 *
	 * @param indices   a set variable
	 * @param weights   integers representing the weight of each element in <i>indices</i>
	 * @param offset    offset index : should be 0 by default
	 *                  but generally 1 with MiniZinc API
	 *                  which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param sum       an integer variable representing sum{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>}
	 * @return a constraint ensuring that sum{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>sum</i>
	 */
	default Constraint sumElements(SetVar indices, int[] weights, int offset, IntVar sum) {
		return new Constraint(ConstraintsName.SETSUM, new PropSumOfElements(indices, weights, offset, sum));
	}

	//***********************************************************************************
	// MAX - MIN
	//***********************************************************************************

	/**
	 * Creates a constraint over the maximum element in a set:
	 * max{i | i in <i>set</i>} = <i>maxElementValue</i>
	 *
	 * @param set             a set variable
	 * @param maxElementValue an integer variable representing the maximum element in <i>set</i>
	 * @param notEmpty         true : the set variable cannot be empty
	 *                         false : the set may be empty (if <i>set</i> is empty, this constraint is not applied)
	 * @return a constraint ensuring that max{i | i in <i>set</i>} = <i>maxElementValue</i>
	 */
	default Constraint max(SetVar set, IntVar maxElementValue, boolean notEmpty) {
		return max(set, null, 0, maxElementValue, notEmpty);
	}

	/**
	 * Creates a constraint over the maximum element induces by a set:
	 * max{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>maxElementValue</i>
	 *
	 * @param indices           a set variable containing elements in range [offset,weights.length-1+offset]
	 * @param weights           integers representing the weight of each element in <i>indices</i>
	 * @param offset            offset index : should be 0 by default
	 *                          but generally 1 with MiniZinc API
	 *                          which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param maxElementValue an integer variable representing the maximum weight induced by <i>indices</i>
	 * @param notEmpty         true : the set variable cannot be empty
	 *                         false : the set may be empty (if <i>indices</i> is empty, this constraint is not applied)
	 * @return a constraint ensuring that max{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>maxElementValue</i>
	 */
	default Constraint max(SetVar indices, int[] weights, int offset, IntVar maxElementValue, boolean notEmpty) {
		if (notEmpty) {
			return new Constraint(ConstraintsName.SETMAX, new PropNotEmpty(indices), new PropMaxElement(indices, weights, offset, maxElementValue, true));
		} else {
			return new Constraint(ConstraintsName.SETMAX, new PropMaxElement(indices, weights, offset, maxElementValue, false));
		}
	}

	/**
	 * Creates a constraint over the minimum element in a set:
	 * min{i | i in <i>set</i>} = <i>minElementValue</i>
	 *
	 * @param set             a set variable
	 * @param minElementValue an integer variable representing the minimum element in <i>set</i>
	 * @param notEmpty         true : the set variable cannot be empty
	 *                         false : the set may be empty (if <i>set</i> is empty, this constraint is not applied)
	 * @return a constraint ensuring that min{i | i in <i>set</i>} = <i>minElementValue</i>
	 */
	default Constraint min(SetVar set, IntVar minElementValue, boolean notEmpty) {
		return min(set, null, 0, minElementValue, notEmpty);
	}

	/**
	 * Creates a constraint over the minimum element induces by a set:
	 * min{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>minElementValue</i>
	 *
	 * @param indices           a set variable containing elements in range [offset,weights.length-1+offset]
	 * @param weights           integers representing the weight of each element in <i>indices</i>
	 * @param offset            offset index : should be 0 by default
	 *                          but generally 1 with MiniZinc API
	 *                          which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param minElementValue an integer variable representing the minimum weight induced by <i>indices</i>
	 * @param notEmpty         true : the set variable cannot be empty
	 *                         false : the set may be empty (if <i>indices</i> is empty, this constraint is not applied)
	 * @return a constraint ensuring that min{<i>weights</i>[i-<i>offset</i>] | i in <i>indices</i>} = <i>minElementValue</i>
	 */
	default Constraint min(SetVar indices, int[] weights, int offset, IntVar minElementValue, boolean notEmpty) {
		if (notEmpty) {
			return new Constraint(ConstraintsName.SETMIN, new PropNotEmpty(indices), new PropMinElement(indices, weights, offset, minElementValue, true));
		} else {
			return new Constraint(ConstraintsName.SETMIN, new PropMinElement(indices, weights, offset, minElementValue, false));
		}
	}

	//***********************************************************************************
	// CHANNELING CONSTRAINTS : bool/int/graph
	//***********************************************************************************

	/**
	 * Creates a constraint channeling a set variable with boolean variables :
	 * i in <i>set</i> <=> <i>bools</i>[i] = TRUE
	 *
	 * @param bools	   an array of boolean variables
	 * @param set      a set variable
	 * @return a constraint ensuring that i in <i>set</i> <=> <i>bools</i>[i] = TRUE
	 */
	default Constraint setBoolsChanneling(BoolVar[] bools, SetVar set) {
		return setBoolsChanneling(bools,set,0);
	}

	/**
	 * Creates a constraint channeling a set variable with boolean variables :
	 * i in <i>set</i> <=> <i>bools</i>[i-<i>offset</i>] = TRUE
	 *
	 * @param bools	   an array of boolean variables
	 * @param set      a set variable
	 * @param offset   offset index : should be 0 by default
	 *                 but generally 1 with MiniZinc API
	 *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @return a constraint ensuring that i in <i>set</i> <=> <i>bools</i>[i-<i>offset</i>] = TRUE
	 */
	default Constraint setBoolsChanneling(BoolVar[] bools, SetVar set, int offset) {
		return new Constraint(ConstraintsName.SETBOOLCHANNELING, new PropBoolChannel(set, bools, offset));
	}

	/**
	 * Creates a constraint channeling set variables and integer variables :
	 * x in <i>sets</i>[y] <=> <i>ints</i>[x] = y
	 *
	 * @param sets     an array of set variables
	 * @param ints     an array of integer variables
	 * @return a constraint ensuring that x in <i>sets</i>[y] <=> <i>ints</i>[x] = y
	 */
	default Constraint setsIntsChanneling(SetVar[] sets, IntVar[] ints) {
		return setsIntsChanneling(sets,ints,0,0);
	}

	/**
	 * Creates a constraint channeling set variables and integer variables :
	 * x in <i>sets</i>[y-<i>offset1</i>] <=> <i>ints</i>[x-<i>offset2</i>] = y
	 *
	 * @param sets     an array of set variables
	 * @param ints     an array of integer variables
	 * @param offset1  offset index : should be 0 by default
	 *                 but generally 1 with MiniZinc API
	 *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param offset2 offset index : should be 0 by default
	 *                 but generally 1 with MiniZinc API
	 *                 which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @return a constraint ensuring that x in <i>sets</i>[y-<i>offset1</i>] <=> <i>ints</i>[x-<i>offset2</i>] = y
	 */
	default Constraint setsIntsChanneling(SetVar[] sets, IntVar[] ints, int offset1, int offset2) {
		return new Constraint(ConstraintsName.SETINTCHANNELING,
				new PropIntChannel(sets, ints, offset1, offset2),
				new PropIntChannel(sets, ints, offset1, offset2), new PropAllDisjoint(sets)
		);
	}

	//***********************************************************************************
	// MINIZINC API
	//***********************************************************************************

	/**
	 * Creates a constraint stating that the intersection of <i>set1</i> and <i>set2</i> should be empty
	 * Note that they can be both empty
	 *
	 * @param set1 a set variable
	 * @param set2 a set variable
	 * @return a constraint ensuring that <i>set1</i> and <i>set2</i> are disjoint (empty intersection)
	 */
	default Constraint disjoint(SetVar set1, SetVar set2) {
		return allDisjoint(set1, set2);
	}

	/**
	 * Creates a constraint stating that the intersection of <i>sets</i> should be empty
	 * Note that there can be multiple empty sets
	 *
	 * @param sets an array of disjoint set variables
	 * @return a constraint ensuring that <i>sets</i> are all disjoint (empty intersection)
	 */
	default Constraint allDisjoint(SetVar... sets) {
		return new Constraint(ConstraintsName.SETALLDISJOINT, new PropAllDisjoint(sets));
	}

	/**
	 * Creates a constraint stating that <i>sets</i> should all be different (not necessarily disjoint)
	 * Note that there cannot be more than one empty set
	 *
	 * @param sets an array of different set variables
	 * @return a constraint ensuring that <i>sets</i> are all different
	 */
	default Constraint allDifferent(SetVar... sets) {
		return new Constraint(ConstraintsName.SETALLDIFFERENT, new PropAllDiff(sets),
				new PropAllDiff(sets), new PropAtMost1Empty(sets)
		);
	}

	/**
	 * Creates a constraint stating that <i>sets</i>  should be all equal
	 *
	 * @param sets an array of set variables to be equal
	 * @return a constraint ensuring that all sets in <i>sets</i> are equal
	 */
	default Constraint allEqual(SetVar... sets) {
		return new Constraint(ConstraintsName.SETALLEQUAL, new PropAllEqual(sets));
	}

	/**
	 * Creates a constraint stating that partitions <i>universe</i> into <i>sets</i>:
	 * union(<i>sets</i>) = <i>universe</i>
	 * intersection(<i>sets</i>) = {}
	 *
	 * @param sets     an array of set variables whose values are subsets of <i>universe</i>
	 * @param universe a set variable representing the union of <i>sets</i>
	 * @return a constraint which ensures that <i>sets</i> forms a partition of <i>universe</i>
	 */
	default Constraint partition(SetVar[] sets, SetVar universe) {
		Constraint allDisjoint = allDisjoint(sets);
		allDisjoint.ignore();
		return new Constraint(ConstraintsName.SETPARTITION, ArrayUtils.append(
				allDisjoint.getPropagators(),
				new Propagator[]{new PropUnion(sets, universe), new PropUnion(sets, universe)}
		));
	}

	/**
	 * Creates a constraint stating that :
	 * x in <i>sets</i>[y-<i>offset1</i>] <=> y in <i>invSets</i>[x-<i>offset2</i>]
	 *
	 * @param sets         an array of set variables
	 * @param invSets an array of set variables
	 * @param offset1     offset index : should be 0 by default
	 *                     but generally 1 with MiniZinc API
	 *                     which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param offset2     offset index : should be 0 by default
	 *                     but generally 1 with MiniZinc API
	 *                     which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @return a constraint ensuring that x in <i>sets</i>[y-<i>offset1</i>] <=> y in <i>invSets</i>[x-<i>offset2</i>]
	 */
	default Constraint inverseSet(SetVar[] sets, SetVar[] invSets, int offset1, int offset2) {
		return new Constraint(ConstraintsName.SETINVERSE, new PropInverse(sets, invSets, offset1, offset2));
	}

	/**
	 * Creates a constraint stating that <i>sets</i> are symmetric sets:
	 * x in <i>sets</i>[y] <=> y in <i>sets</i>[x]
	 *
	 * @param sets   an array of set variables
	 * @return a constraint ensuring that x in <i>sets</i>[y] <=> y in <i>sets</i>[x]
	 */
	default Constraint symmetric(SetVar... sets) {
		return symmetric(sets,0);
	}

	/**
	 * Creates a constraint stating that <i>sets</i> are symmetric sets:
	 * x in <i>sets</i>[y-<i>offset</i>] <=> y in <i>sets</i>[x-<i>offset</i>]
	 *
	 * @param sets   an array of set variables
	 * @param offset offset index : should be 0 by default
	 *               but generally 1 with MiniZinc API
	 *               which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @return a constraint ensuring that x in <i>sets</i>[y-<i>offset</i>] <=> y in <i>sets</i>[x-<i>offset</i>]
	 */
	default Constraint symmetric(SetVar[] sets, int offset) {
		return new Constraint(ConstraintsName.SETSYMMETRIC, new PropSymmetric(sets, offset));
	}

	/**
	 * Creates a constraint enabling to retrieve an element <i>set</i> in <i>sets</i>:
	 * <i>sets</i>[<i>index</i>] = <i>set</i>
	 *
	 * @param index  an integer variable pointing to <i>set</i>'s index into array <i>sets</i>
	 * @param sets   an array of set variables representing possible values for <i>set</i>
	 * @param set    a set variable equal to <i>sets</i>[<i>index</i>]
	 * @return a constraint ensuring that <i>sets</i>[<i>index</i>] = <i>set</i>
	 */
	default Constraint element(IntVar index, SetVar[] sets, SetVar set) {
		return element(index,sets,0,set);
	}

	/**
	 * Creates a constraint enabling to retrieve an element <i>set</i> in <i>sets</i>:
	 * <i>sets</i>[<i>index</i>-<i>offset</i>] = <i>set</i>
	 *
	 * @param index  an integer variable pointing to <i>set</i>'s index into array <i>sets</i>
	 * @param sets   an array of set variables representing possible values for <i>set</i>
	 * @param offset offset index : should be 0 by default
	 *               but generally 1 with MiniZinc API
	 *               which counts from 1 to n instead of counting from 0 to n-1 (Java standard)
	 * @param set    a set variable equal to <i>sets</i>[<i>index</i>-<i>offset</i>]
	 * @return a constraint ensuring that <i>sets</i>[<i>index</i>-<i>offset</i>] = <i>set</i>
	 */
	default Constraint element(IntVar index, SetVar[] sets, int offset, SetVar set) {
		return new Constraint(ConstraintsName.SETELEMENT, new PropElement(index, sets, offset, set), new PropElement(index, sets, offset, set));
	}

	/**
	 * Creates a member constraint stating that <i>set</i> belongs to <i>sets</i>
	 *
	 * @param sets set variables representing possible values for <i>set</i>
	 * @param set  a set variable which takes its value in <i>sets</i>
	 * @return a constraint ensuring that <i>set</i> belongs to <i>sets</i>
	 */
	default Constraint member(SetVar[] sets, SetVar set) {
		IntVar index = set.getModel().intVar("idx_tmp", 0, sets.length - 1, false);
		return element(index, sets, 0, set);
	}

	/**
	 * Creates a member constraint stating that the value of <i>intVar</i> is in <i>set</i>
	 *
	 * @param intVar an integer variables which takes its value in <i>set</i>
	 * @param set    a set variables containing possible values for <i>intVar</i>
	 * @return a constraint ensuring that the value of <i>intVar</i> is in <i>set</i>
	 */
	default Constraint member(final IntVar intVar, final SetVar set) {
		if(intVar.isInstantiated()){
			return member(intVar.getValue(),set);
		}else {
			return new Constraint(ConstraintsName.SETMEMBER,
					intVar.hasEnumeratedDomain() ?
							new PropIntEnumMemberSet(set, intVar) :
							new PropIntBoundedMemberSet(set, intVar)) {
				@Override
				public Constraint makeOpposite() {
					return notMember(intVar, set);
				}
			};
		}
	}

	/**
	 * Creates a member constraint stating that the constant <i>cst</i> is in <i>set</i>
	 *
	 * @param cst an integer
	 * @param set a set variable
	 * @return a constraint ensuring that <i>cst</i> is in <i>set</i>
	 */
	default Constraint member(final int cst, final SetVar set) {
		return new Constraint(ConstraintsName.SETMEMBER,new PropIntCstMemberSet(set, cst)) {
			@Override
			public Constraint makeOpposite() {
				return notMember(cst, set);
			}
		};
	}

	/**
	 * Creates a member constraint stating that the value of <i>intVar</i> is not in <i>set</i>
	 *
	 * @param intVar an integer variables which does not take its values in <i>set</i>
	 * @param set     a set variables representing impossible values for <i>intVar</i>
	 * @return a constraint ensuring that the value of <i>intVar</i> is not in <i>set</i>
	 */
	default Constraint notMember(final IntVar intVar, final SetVar set) {
		if(intVar.isInstantiated()){
			return notMember(intVar.getValue(),set);
		}else {
			IntVar integer = intVar;
			if (!intVar.hasEnumeratedDomain()) {
				Model s = intVar.getModel();
				integer = s.intVar("enumViewOf(" + intVar.getName() + ")", intVar.getLB(), intVar.getUB(), false);
				s.arithm(integer, "=", intVar).post();
			}
			return new Constraint(ConstraintsName.SETNOTMEMBER,
					new PropNotMemberIntSet(integer, set),
					new PropNotMemberSetInt(integer, set)) {
				@Override
				public Constraint makeOpposite() {
					return member(intVar, set);
				}
			};
		}
	}

	/**
	 * Creates a member constraint stating that the constant <i>cst</i> is not in <i>set</i>
	 *
	 * @param cst an integer
	 * @param set a set variable
	 * @return a constraint ensuring that <i>cst</i> is not in <i>set</i>
	 */
	default Constraint notMember(final int cst, final SetVar set) {
		return new Constraint(ConstraintsName.SETNOTMEMBER,new PropIntCstNotMemberSet(set, cst)) {
			@Override
			public Constraint makeOpposite() {
				return member(cst, set);
			}
		};
	}
}
