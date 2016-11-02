/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.xcsp.parser;

import org.xcsp.parser.XConstraints.CEntry;
import org.xcsp.parser.XConstraints.XBlock;
import org.xcsp.parser.XConstraints.XGroup;
import org.xcsp.parser.XConstraints.XSlide;
import org.xcsp.parser.XEnums.*;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XObjectives.OEntry;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XVariables.VEntry;
import org.xcsp.parser.XVariables.XArray;
import org.xcsp.parser.XVariables.XVarInteger;
import org.xcsp.parser.XVariables.XVarSymbolic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface XCallbacks2 extends XCallbacks {

	/**********************************************************************************************
	 * Methods called at Specific Moments
	 *********************************************************************************************/

	default void beginInstance(TypeFramework type) {
	}

	default void endInstance() {
	}

	default void beginVariables(List<VEntry> vEntries) {
	}

	default void endVariables() {
	}

	default void beginArray(XArray a) {
	}

	default void endArray(XArray a) {
	}

	default void beginConstraints(List<CEntry> cEntries) {
	}

	default void endConstraints() {
	}

	default void beginBlock(XBlock b) {
	}

	default void endBlock(XBlock b) {
	}

	default void beginGroup(XGroup g) {
	}

	default void endGroup(XGroup g) {
	}

	default void beginSlide(XSlide s) {
	}

	default void endSlide(XSlide s) {
	}

	default void beginObjectives(List<OEntry> oEntries, TypeCombination type) {
	}

	default void endObjectives() {
	}

	/**********************************************************************************************
	 * Methods to be implemented on integer variables/constraints
	 *********************************************************************************************/

	default Object unimplementedCase(Object... objects) {
		System.out.println("\n\n**********************");
		System.out.println("Missing Implementation");
		StackTraceElement[] t = Thread.currentThread().getStackTrace();
		System.out.println("  Method " + t[2].getMethodName());
		System.out.println("  Class " + t[2].getClassName());
		System.out.println("  Line " + t[2].getLineNumber());
		System.out.println("**********************");
		System.out.println(Stream.of(objects).filter(o -> o != null).map(o -> o.toString()).collect(Collectors.joining("\n")));
		// throw new RuntimeException();
		System.exit(1);
		return null;
	}

	default void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
		unimplementedCase(x.id);
	}

	default void buildVarInteger(XVarInteger x, int[] values) {
		unimplementedCase(x.id);
	}

	default void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
	}

	default void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k) {
		unimplementedCase(id);
	}

	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k) {
		unimplementedCase(id);
	}

	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, XVarInteger z) {
		unimplementedCase(id);
	}

	default void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	default void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	default void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		unimplementedCase(id);
	}

	default void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		unimplementedCase(id);
	}

	default void buildCtrAllDifferent(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	default void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		unimplementedCase(id);
	}

	default void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		unimplementedCase(id);
	}

	default void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		unimplementedCase(id);
	}

	default void buildCtrAllEqual(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	default void buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator) {
		unimplementedCase(id);
	}

	default void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator) {
		unimplementedCase(id);
	}

	default void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator) {
		unimplementedCase(id);
	}

	default void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	default void buildCtrAtMost(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	default void buildCtrExactly(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	default void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k) {
		unimplementedCase(id);
	}

	default void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k) {
		unimplementedCase(id);
	}

	default void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k) {
		unimplementedCase(id);
	}

	default void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrNotAllEqual(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		unimplementedCase(id);
	}

	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		unimplementedCase(id);
	}

	default void buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		unimplementedCase(id);
	}

	default void buildCtrElement(String id, XVarInteger[] list, int value) {
		unimplementedCase(id);
	}

	default void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		unimplementedCase(id);
	}

	default void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		unimplementedCase(id);
	}

	default void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		unimplementedCase(id);
	}

	default void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		unimplementedCase(id);
	}

	default void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		unimplementedCase(id);
	}

	default void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		unimplementedCase(id);
	}

	default void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		unimplementedCase(id);
	}

	default void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	default void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	default void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	default void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	default void buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		unimplementedCase(id);
	}

	default void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		unimplementedCase(id);
	}

	/**********************************************************************************************
	 * Methods to be implemented for managing objectives
	 *********************************************************************************************/

	default void buildObjToMinimize(String id, XVarInteger x) {
		unimplementedCase(id);
	}

	default void buildObjToMaximize(String id, XVarInteger x) {
		unimplementedCase(id);
	}

	default void buildObjToMinimize(String id, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
	}

	default void buildObjToMaximize(String id, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
	}

	default void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCase(id);
	}

	default void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCase(id);
	}

	default void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCase(id);
	}

	default void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCase(id);
	}

	/**********************************************************************************************
	 * Methods to be implemented on symbolic variables/constraints
	 *********************************************************************************************/

	default void buildVarSymbolic(XVarSymbolic x, String[] values) {
		unimplementedCase(x.id);
	}

	default void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent syntaxTreeRoot) {
		unimplementedCase(id);
	}

	default void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	default void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	default void buildCtrAllDifferent(String id, XVarSymbolic[] list) {
		unimplementedCase(id);
	}

}
