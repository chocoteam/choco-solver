package org.xcsp.parser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xcsp.parser.XConstraints.CChild;
import org.xcsp.parser.XConstraints.CEntry;
import org.xcsp.parser.XConstraints.XBlock;
import org.xcsp.parser.XConstraints.XCtr;
import org.xcsp.parser.XConstraints.XGroup;
import org.xcsp.parser.XConstraints.XSlide;
import org.xcsp.parser.XDomains.XDom;
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XDomains.XDomSymbolic;
import org.xcsp.parser.XEnums.TypeArithmeticOperator;
import org.xcsp.parser.XEnums.TypeAtt;
import org.xcsp.parser.XEnums.TypeChild;
import org.xcsp.parser.XEnums.TypeCombination;
import org.xcsp.parser.XEnums.TypeConditionOperator;
import org.xcsp.parser.XEnums.TypeConditionOperatorRel;
import org.xcsp.parser.XEnums.TypeCtr;
import org.xcsp.parser.XEnums.TypeExpr;
import org.xcsp.parser.XEnums.TypeFlag;
import org.xcsp.parser.XEnums.TypeFramework;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XEnums.TypeOperator;
import org.xcsp.parser.XEnums.TypeRank;
import org.xcsp.parser.XNodeExpr.XNodeLeaf;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XObjectives.OEntry;
import org.xcsp.parser.XObjectives.OObjectiveExpr;
import org.xcsp.parser.XObjectives.OObjectiveSpecial;
import org.xcsp.parser.XParser.Condition;
import org.xcsp.parser.XParser.ConditionVal;
import org.xcsp.parser.XParser.ConditionVar;
import org.xcsp.parser.XValues.IntegerEntity;
import org.xcsp.parser.XValues.IntegerInterval;
import org.xcsp.parser.XValues.IntegerValue;
import org.xcsp.parser.XValues.SimpleValue;
import org.xcsp.parser.XVariables.VEntry;
import org.xcsp.parser.XVariables.XArray;
import org.xcsp.parser.XVariables.XVar;
import org.xcsp.parser.XVariables.XVarInteger;
import org.xcsp.parser.XVariables.XVarSymbolic;

public interface XCallbacks {

	/**********************************************************************************************
	 * Managing Parameters of XCallbacks
	 *********************************************************************************************/

	enum XCallbacksParameters {
		RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_COUNT_CASES,
		RECOGNIZE_SPECIAL_NVALUES_CASES,
		INTENSION_TO_EXTENSION_ARITY_LIMIT, // set it to 0 for deactivating "intension to extension" conversion
		INTENSION_TO_EXTENSION_SPACE_LIMIT,
		INTENSION_TO_EXTENSION_PRIORITY;
	}

	Map<XCallbacksParameters, Object> callbacksParameters = defaultParameters();

	static Map<XCallbacksParameters, Object> defaultParameters() {
		Object dummy = new Object();
		Map<XCallbacksParameters, Object> map = new HashMap<>();
		map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES, dummy);
		map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES, dummy);
		map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES, dummy);
		map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_COUNT_CASES, dummy);
		map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_NVALUES_CASES, dummy);
		map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_ARITY_LIMIT, 0); // included
		map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_SPACE_LIMIT, 1000000);
		map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY, Boolean.TRUE);
		return map;
	}

	/**********************************************************************************************
	 * Auxiliary Methods used for transforming some piece of data
	 *********************************************************************************************/

	/** Constant to control the maximum number of values in a domain */
	static final int NB_MAX_VALUES = 10000000;

	/** Method called when a piece of code has not been implemented. */
	static Object unimplementedCase(Object... objects) {
		throw new RuntimeException("Unimplemented case " + Stream.of(objects).map(o -> o.toString()).collect(Collectors.joining("\n")));
	}

	static Object trDom(XDom xd) {
		if (xd instanceof XDomInteger) {
			IntegerEntity[] pieces = (IntegerEntity[]) ((XDomInteger) xd).values;
			if (pieces.length == 1 && pieces[0] instanceof IntegerInterval)
				return pieces[0];
			int[] values = IntegerEntity.toIntArray(pieces, NB_MAX_VALUES);
			XUtility.control(values != null, "Too many values. You have to extend the parser.");
			return values;
		}
		if (xd instanceof XDomSymbolic)
			return ((XDomSymbolic) xd).values;
		return unimplementedCase(xd);
	}

	static int trInteger(Long l) {
		return XUtility.safeLong2Int(l, true);
	}

	static int[] trIntegers(Object value) {
		if (value instanceof int[])
			return (int[]) value;
		if (value instanceof IntegerEntity[]) {
			int[] values = IntegerEntity.toIntArray((IntegerEntity[]) value, NB_MAX_VALUES);
			XUtility.control(values != null, "Too many values. You have to extend the parser.");
			return values;
		}
		// Note that STAR is not allowed in simple lists (because this is irrelevant), which allows us to write:
		return IntStream.range(0, Array.getLength(value)).map(i -> trInteger((long) Array.get(value, i))).toArray();
	}

	static int[][] trIntegers2D(Object value) {
		if (value instanceof int[][])
			return (int[][]) value;
		if (value instanceof byte[][]) {
			byte[][] m = (byte[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == XConstants.STAR_BYTE ? XConstants.STAR_INT : m[i][j];
			return tuples;
		}
		if (value instanceof short[][]) {
			short[][] m = (short[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == XConstants.STAR_SHORT ? XConstants.STAR_INT : m[i][j];
			return tuples;
		}
		if (value instanceof long[][]) {
			long[][] m = (long[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == XConstants.STAR ? XConstants.STAR_INT : trInteger(m[i][j]);
			return tuples;
		}
		if (value instanceof Long[][]) {
			Long[][] m = (Long[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == XConstants.STAR ? XConstants.STAR_INT : trInteger(m[i][j]);
			return tuples;
		}
		return (int[][]) unimplementedCase(value);
	}

	/**********************************************************************************************
	 * Main Methods for loading variables, constraints and objectives
	 *********************************************************************************************/

	/**
	 * Method to override if you need other tools to load the file.
	 * 
	 * @param fileName
	 *            the name of a XCSP3 file
	 * @return the document corresponding to the XCSP3 file whose name is given
	 */
	default Document loadDocument(String fileName) throws Exception {
		return XUtility.loadDocument(fileName);
	}

	default void loadInstance(String fileName, String... discardedClasses) throws Exception {
		Document document = loadDocument(fileName);
		System.out.println("Document = " + document);
		XParser parser = new XParser(document, discardedClasses);
		beginInstance(parser.typeFramework);
		beginVariables(parser.vEntries);
		loadVariables(parser);
		endVariables();
		beginConstraints(parser.cEntries);
		loadConstraints(parser);
		endConstraints();
		beginObjectives(parser.oEntries, parser.typeCombination);
		loadObjectives(parser.oEntries);
		endObjectives();
		// annotations
		endInstance();
	}

	default void loadVariables(XParser parser) {
		Map<XDom, Object> cache4DomObject = new HashMap<>();
		for (VEntry entry : parser.vEntries) {
			if (entry instanceof XVar)
				loadVar((XVar) entry, cache4DomObject);
			else {
				beginArray((XArray) entry);
				loadArray((XArray) entry, cache4DomObject);
				endArray((XArray) entry);
			}
		}
	}

	default void loadVar(XVar v, Map<XDom, Object> cache4DomObject) {
		if (v.degree == 0)
			return;
		Object domObject = cache4DomObject.get(v.dom);
		if (domObject == null)
			cache4DomObject.put(v.dom, domObject = trDom(v.dom));
		if (domObject instanceof IntegerInterval) {
			IntegerInterval ii = (IntegerInterval) domObject;
			int min = XUtility.safeLong2IntWhileHandlingInfinity(ii.inf, true);
			int max = XUtility.safeLong2IntWhileHandlingInfinity(ii.sup, true);
			buildVarInteger((XVarInteger) v, min, max);
		} else if (domObject instanceof int[])
			buildVarInteger((XVarInteger) v, (int[]) domObject);
		else if (domObject instanceof String[])
			buildVarSymbolic((XVarSymbolic) v, (String[]) domObject);
		else
			unimplementedCase(v);
	}

	default void loadArray(XArray va, Map<XDom, Object> cache4DomObject) {
		Stream.of(va.vars).filter(v -> v != null).forEach(v -> loadVar(v, cache4DomObject));
	}

	default void loadConstraints(XParser parser) {
		loadConstraints(parser.cEntries); // recursive loading process (through potential blocks)
	}

	default void loadConstraints(List<CEntry> list) {
		for (CEntry entry : list) {
			if (entry instanceof XBlock)
				loadBlock((XBlock) entry);
			else if (entry instanceof XGroup)
				loadGroup((XGroup) entry);
			else if (entry instanceof XSlide)
				loadSlide((XSlide) entry);
			else if (entry instanceof XCtr)
				loadCtr((XCtr) entry);
			else
				unimplementedCase(entry);
		}
	}

	default void loadBlock(XBlock b) {
		beginBlock(b);
		loadConstraints(b.subentries); // recursive call
		endBlock(b);
	}

	default void loadGroup(XGroup g) {
		beginGroup(g);
		if (g.template instanceof XCtr)
			loadCtrs((XCtr) g.template, g.argss, g);
		else
			unimplementedCase(g);
		endGroup(g);
	}

	default void loadSlide(XSlide s) {
		beginSlide(s);
		loadCtrs((XCtr) s.template, s.scopes, s);
		endSlide(s);
	}

	default void loadCtrs(XCtr template, Object[][] argss, CEntry entry) {
		Stream.of(argss).forEach(args -> {
			template.abstraction.concretize(args);
			loadCtr(template);
		});
	}

	class CtrLoaderInteger {
		private XCallbacks xc;

		private CtrLoaderInteger(XCallbacks xc) {
			this.xc = xc;
		}

		private void load(XCtr c) {
			switch (c.getType()) {
			case intension:
				intension(c);
				break;
			case extension:
				extension(c);
				break;
			case regular:
				regular(c);
				break;
			case mdd:
				mdd(c);
				break;
			case allDifferent:
				allDifferent(c);
				break;
			case allEqual:
				allEqual(c);
				break;
			case ordered:
				ordered(c);
				break;
			case lex:
				lex(c);
				break;
			case sum:
				sum(c);
				break;
			case count:
				count(c);
				break;
			case nValues:
				nValues(c);
				break;
			case cardinality:
				cardinality(c);
				break;
			case maximum:
				maximum(c);
				break;
			case minimum:
				minimum(c);
				break;
			case element:
				element(c);
				break;
			case channel:
				channel(c);
				break;
			case stretch:
				stretch(c);
				break;
			case noOverlap:
				noOverlap(c);
				break;
			case cumulative:
				cumulative(c);
				break;
			case clause: // not in XCSP3-core
				clause(c);
				break;
			case instantiation:
				instantiation(c);
				break;
			default:
				unimplementedCase(c);
			}
		}

		private void unaryPrimitive(String id, XNodeExpr sonLeft, XNodeExpr sonRight, TypeConditionOperatorRel op) {
			XVarInteger x = (XVarInteger) ((XNodeLeaf) sonLeft).value;
			int k = XUtility.safeLong2Int((Long) ((XNodeLeaf) sonRight).value, true);
			xc.buildCtrPrimitive(id, x, op, k);
		}

		private void binaryPrimitive(String id, XNodeExpr sonLeft, XNodeExpr sonRight, TypeArithmeticOperator opa, TypeConditionOperatorRel op) {
			XVarInteger x = (XVarInteger) ((XNodeLeaf) ((XNodeParent) sonLeft).sons[0]).value;
			XVarInteger y = (XVarInteger) ((XNodeLeaf) ((XNodeParent) sonLeft).sons[1]).value;
			int k = XUtility.safeLong2Int((Long) ((XNodeLeaf) sonRight).value, true);
			xc.buildCtrPrimitive(id, x, opa, y, op, k);
		}

		private void ternaryPrimitive(String id, XNodeExpr sonLeft, XNodeExpr sonRight, TypeArithmeticOperator opa, TypeConditionOperatorRel op) {
			XVarInteger x = (XVarInteger) ((XNodeLeaf) ((XNodeParent) sonLeft).sons[0]).value;
			XVarInteger y = (XVarInteger) ((XNodeLeaf) ((XNodeParent) sonLeft).sons[1]).value;
			XVarInteger z = (XVarInteger) ((XNodeLeaf) sonRight).value;
			xc.buildCtrPrimitive(id, x, opa, y, op, z);
		}

		private boolean intensionToExtension(XCtr c, XVarInteger[] scope, boolean firstCall) {
			if (firstCall && callbacksParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY) == Boolean.FALSE)
				return false;
			if (!firstCall && callbacksParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY) == Boolean.TRUE)
				return false;
			if (scope.length > ((Number) callbacksParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_ARITY_LIMIT)).intValue())
				return false;
			long[] domSizes = Stream.of(scope).mapToLong(x -> IntegerEntity.getNbValues((IntegerEntity[]) ((XDomInteger) x.dom).values)).toArray();
			if (LongStream.of(domSizes).anyMatch(l -> l == -1L || l > 1000000))
				return false;
			int spaceLimit = ((Number) callbacksParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_SPACE_LIMIT)).intValue();
			long product = 1;
			for (long l : domSizes)
				if ((product *= l) > spaceLimit)
					return false;
			int[][] domValues = Stream.of(scope).map(x -> IntegerEntity.toIntArray((IntegerEntity[]) ((XDomInteger) x.dom).values, 1000000))
					.toArray(int[][]::new);

			XNodeParent root = (XNodeParent) c.childs[0].value;
			EvaluationManager man = new EvaluationManager(root.canonicalForm(new ArrayList<>(), scope).toArray(new String[0]));
			List<int[]> list = new ArrayList<>();
			int[] tupleIdx = new int[scope.length], tupleVal = new int[scope.length];
			boolean hasNext = true;
			while (hasNext) {
				for (int i = 0; i < scope.length; i++)
					tupleVal[i] = domValues[i][tupleIdx[i]];
				if (man.evaluate(tupleVal) == 1)
					list.add(tupleVal.clone());
				hasNext = false;
				for (int i = 0; !hasNext && i < tupleIdx.length; i++)
					if (tupleIdx[i] + 1 < domValues[i].length) {
						tupleIdx[i]++;
						hasNext = true;
					} else
						tupleIdx[i] = 0;
			}

			if (list.size() == 0) { // special case because 0 tuple
				xc.buildCtrFalse(c.id, c.vars());
			} else {
				if (scope.length == 1) // unary constraint
					xc.buildCtrExtension(c.id, scope[0], list.stream().mapToInt(t -> t[0]).toArray(), true, new HashSet<>());
				else
					xc.buildCtrExtension(c.id, scope, list.toArray(new int[0][]), true, new HashSet<>());
			}
			return true;
		}

		private void intension(XCtr c) {
			XVarInteger[] scope = Stream.of(c.vars()).map(x -> (XVarInteger) x).toArray(XVarInteger[]::new);
			if (intensionToExtension(c, scope, true))
				return;
			XNodeParent root = (XNodeParent) c.childs[0].value;
			if (root.sons.length == 2) {
				XNodeExpr son0 = root.sons[0], son1 = root.sons[1];
				if (scope.length == 1 && callbacksParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES)) {
					TypeConditionOperatorRel op = XEnums.valueOf(TypeConditionOperatorRel.class, root.type.name());
					if (op != null) {
						if (son0.type == TypeExpr.VAR && son1.type == TypeExpr.LONG) {
							unaryPrimitive(c.id, son0, son1, op);
							return;
						} else if (son1.type == TypeExpr.VAR && son0.type == TypeExpr.LONG) {
							unaryPrimitive(c.id, son1, son0, op.reverseForSwap());
							return;
						}
					}
				}
				if (scope.length == 2 && callbacksParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES)) {
					TypeConditionOperatorRel op = XEnums.valueOf(TypeConditionOperatorRel.class, root.type.name());
					if (op != null) {
						if (son0.hasBasicForm() && son1.hasBasicForm()) {
							XVarInteger x = (XVarInteger) son0.getValueOfFirstLeafOfType(TypeExpr.VAR);
							XVarInteger y = (XVarInteger) son1.getValueOfFirstLeafOfType(TypeExpr.VAR);
							Long l1 = (Long) son0.getValueOfFirstLeafOfType(TypeExpr.LONG);
							Long l2 = (Long) son1.getValueOfFirstLeafOfType(TypeExpr.LONG);
							int k = (l2 == null ? 0 : XUtility.safeLong2Int(l2, true) * (son1.type == TypeExpr.SUB ? -1 : 1))
									- (l1 == null ? 0 : XUtility.safeLong2Int(l1, true) * (son0.type == TypeExpr.SUB ? -1 : 1));
							xc.buildCtrPrimitive(c.id, x, TypeArithmeticOperator.SUB, y, op, k);
							return;
						} else {
							if (son0.arithmeticOperatorOnTwoVariables() != null && son1.type == TypeExpr.LONG) {
								binaryPrimitive(c.id, son0, son1, son0.arithmeticOperatorOnTwoVariables(), op);
								return;
							} else if (son1.arithmeticOperatorOnTwoVariables() != null && son0.type == TypeExpr.LONG) {
								binaryPrimitive(c.id, son1, son0, son1.arithmeticOperatorOnTwoVariables(), op.reverseForSwap());
								return;
							}
						}
					}
				}
				if (scope.length == 3 && callbacksParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES)) {
					TypeConditionOperatorRel op = XEnums.valueOf(TypeConditionOperatorRel.class, root.type.name());
					if (op != null) {
						if (son0.arithmeticOperatorOnTwoVariables() != null && son1.type == TypeExpr.VAR) {
							ternaryPrimitive(c.id, son0, son1, son0.arithmeticOperatorOnTwoVariables(), op);
							return;
						} else if (son1.arithmeticOperatorOnTwoVariables() != null && son0.type == TypeExpr.VAR) {
							ternaryPrimitive(c.id, son1, son0, son1.arithmeticOperatorOnTwoVariables(), op.reverseForSwap());
							return;
						}
					}
				}
			}
			if (intensionToExtension(c, scope, false))
				return;
			xc.buildCtrIntension(c.id, scope, root);
		}

		static Map<Object, int[][]> cache4Tuples = new HashMap<>();

		private void extension(XCtr c) {
			CChild c1 = c.childs[1];
			boolean positive = c1.type == TypeChild.supports;
			if (c1.value == null || Array.getLength(c1.value) == 0) { // special case because 0 tuple
				if (positive)
					xc.buildCtrFalse(c.id, c.vars());
				else
					xc.buildCtrTrue(c.id, c.vars());
			} else {
				XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
				if (list.length == 1) // unary constraint
					xc.buildCtrExtension(c.id, list[0], trIntegers(c1.value), positive, c1.flags);
				else {
					int[][] tuples = cache4Tuples.get(c1.value);
					if (tuples == null)
						cache4Tuples.put(c1.value, tuples = trIntegers2D(c1.value));
					xc.buildCtrExtension(c.id, list, tuples, positive, c1.flags);
				}
			}
		}

		private void regular(XCtr c) {
			xc.buildCtrRegular(c.id, (XVarInteger[]) c.childs[0].value, (Object[][]) c.childs[1].value, (String) c.childs[2].value,
					(String[]) c.childs[3].value);
		}

		private void mdd(XCtr c) {
			xc.buildCtrMDD(c.id, (XVarInteger[]) c.childs[0].value, (Object[][]) c.childs[1].value);
		}

		private void allDifferent(XCtr c) {
			CChild[] childs = c.childs;
			if (childs[0].type == TypeChild.matrix) {
				XUtility.control(childs.length == 1, "Other forms of allDifferent-matrix not implemented");
				xc.buildCtrAllDifferentMatrix(c.id, (XVarInteger[][]) (childs[0].value));
			} else if (childs[0].type == TypeChild.list) {
				if (childs.length == 1)
					xc.buildCtrAllDifferent(c.id, (XVarInteger[]) childs[0].value);
				else if (childs[1].type == TypeChild.except)
					xc.buildCtrAllDifferentExcept(c.id, (XVarInteger[]) childs[0].value, trIntegers(childs[1].value));
				else if (childs[childs.length - 1].type == TypeChild.list)
					xc.buildCtrAllDifferentList(c.id, Stream.of(childs).map(p -> p.value).toArray(XVarInteger[][]::new));
				else
					unimplementedCase(c);
			} else
				unimplementedCase(c);
		}

		private void allEqual(XCtr c) {
			if (c.childs[0].type == TypeChild.list)
				if (c.childs.length == 1)
					xc.buildCtrAllEqual(c.id, (XVarInteger[]) c.childs[0].value);
				else
					unimplementedCase(c);
			else
				unimplementedCase(c);
		}

		private void ordered(XCtr c) {
			if (c.childs[0].type == TypeChild.list)
				if (c.childs.length == 2)
					xc.buildCtrOrdered(c.id, (XVarInteger[]) c.childs[0].value, (TypeOperator) c.childs[1].value);
				else
					unimplementedCase(c);
			else
				unimplementedCase(c);
		}

		private void lex(XCtr c) {
			TypeOperator op = (TypeOperator) c.childs[c.childs.length - 1].value;
			if (c.childs[0].type == TypeChild.matrix)
				xc.buildCtrLexMatrix(c.id, (XVarInteger[][]) c.childs[0].value, op);
			else {
				XUtility.control(!op.isSet(), "Lex on sets and msets currently not implemented");
				xc.buildCtrLex(c.id, Stream.of(c.childs).map(p -> p.value).toArray(XVarInteger[][]::new), op);
			}
		}

		private void sum(XCtr c) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			Condition condition = (Condition) c.childs[c.childs.length - 1].value;
			if (c.childs.length == 2)
				xc.buildCtrSum(c.id, list, condition);
			else
				xc.buildCtrSum(c.id, list, trIntegers(c.childs[1].value), condition);
		}

		private void count(XCtr c) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			Condition condition = (Condition) c.childs[2].value;
			if (c.childs[1].value instanceof Long[]) {
				Long[] values = (Long[]) c.childs[1].value;
				if (callbacksParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_COUNT_CASES)) {
					if (values.length == 1) {
						if (condition instanceof ConditionVal) {
							if (condition.operator == TypeConditionOperator.LT) {
								xc.buildCtrAtMost(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k - 1);
								return;
							}
							if (condition.operator == TypeConditionOperator.LE) {
								xc.buildCtrAtMost(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
								return;
							}
							if (condition.operator == TypeConditionOperator.GE) {
								xc.buildCtrAtLeast(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
								return;
							}
							if (condition.operator == TypeConditionOperator.GT) {
								xc.buildCtrAtLeast(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k + 1);
								return;
							}
							if (condition.operator == TypeConditionOperator.EQ) {
								xc.buildCtrExactly(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
								return;
							}
						} else if (condition instanceof ConditionVar) {
							if (condition.operator == TypeConditionOperator.EQ) {
								xc.buildCtrExactly(c.id, list, trInteger(values[0]), (XVarInteger) ((ConditionVar) condition).x);
								return;
							}
						}
					} else {
						if (condition.operator == TypeConditionOperator.EQ) {
							if (condition instanceof ConditionVal) {
								xc.buildCtrAmong(c.id, list, trIntegers(values), ((ConditionVal) condition).k);
								return;
							} else if (condition instanceof ConditionVar) {
								xc.buildCtrAmong(c.id, list, trIntegers(values), (XVarInteger) ((ConditionVar) condition).x);
								return;
							}
						}
					}
				}
				xc.buildCtrCount(c.id, list, trIntegers(c.childs[1].value), condition);
			} else
				xc.buildCtrCount(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
		}

		private void nValues(XCtr c) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			Condition condition = (Condition) c.childs[c.childs.length - 1].value;
			if (callbacksParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_NVALUES_CASES) && c.childs.length == 2
					&& condition instanceof ConditionVal) {
				if (condition.operator == TypeConditionOperator.EQ && ((ConditionVal) condition).k == list.length) {
					xc.buildCtrAllDifferent(c.id, list);
					return;
				} else if (condition.operator == TypeConditionOperator.EQ && ((ConditionVal) condition).k == 1) {
					xc.buildCtrAllEqual(c.id, list);
					return;
				} else if ((condition.operator == TypeConditionOperator.GE && ((ConditionVal) condition).k == 2)
						|| (condition.operator == TypeConditionOperator.GT && ((ConditionVal) condition).k == 1)) {
					xc.buildCtrNotAllEqual(c.id, list);
					return;
				}
			}
			if (c.childs.length == 2)
				xc.buildCtrNValues(c.id, list, condition);
			else
				xc.buildCtrNValuesExcept(c.id, list, trIntegers(c.childs[1].value), condition);
		}

		private void cardinality(XCtr c) {
			CChild[] childs = c.childs;
			XUtility.control(childs[1].value instanceof Long[], "unimplemented case");
			boolean closed = childs[0].getAttributeValue(TypeAtt.closed, false);
			if (childs[1].value instanceof Long[]) {
				if (childs[2].value instanceof Long[])
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), trIntegers(childs[2].value));
				else if (childs[2].value instanceof XVar[])
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), (XVarInteger[]) childs[2].value);
				else {
					XUtility.control(childs[2].value instanceof IntegerInterval[], "Pb");
					int[] occursMin = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.inf, true)).toArray();
					int[] occursMax = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.sup, true)).toArray();
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), occursMin, occursMax);
				}
			} else {
				if (childs[2].value instanceof Long[])
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, trIntegers(childs[2].value));
				else if (childs[2].value instanceof XVar[])
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, (XVarInteger[]) childs[2].value);
				else {
					XUtility.control(childs[2].value instanceof IntegerInterval[], "Pb");
					int[] occursMin = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.inf, true)).toArray();
					int[] occursMax = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.sup, true)).toArray();
					xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, occursMin, occursMax);
				}
			}
		}

		private void minimumMaximum(XCtr c) {
			CChild[] childs = c.childs;
			XVarInteger[] list = (XVarInteger[]) childs[0].value;
			Condition condition = childs[childs.length - 1].type == TypeChild.condition ? (Condition) childs[childs.length - 1].value : null;
			if (childs[1].type == TypeChild.condition)
				if (c.getType() == TypeCtr.maximum)
					xc.buildCtrMaximum(c.id, list, condition);
				else
					xc.buildCtrMinimum(c.id, list, condition);
			else {
				int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
				TypeRank rank = childs[1].getAttributeValue(TypeAtt.rank, TypeRank.class, TypeRank.ANY);
				if (c.getType() == TypeCtr.maximum)
					xc.buildCtrMaximum(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, condition);
				else
					xc.buildCtrMinimum(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, condition);
			}
		}

		private void maximum(XCtr c) {
			minimumMaximum(c);
		}

		private void minimum(XCtr c) {
			minimumMaximum(c);
		}

		private void element(XCtr c) {
			CChild[] childs = c.childs;
			XVarInteger[] list = (XVarInteger[]) childs[0].value;
			if (childs[1].type == TypeChild.value) {
				if (childs[1].value instanceof XVar)
					xc.buildCtrElement(c.id, list, (XVarInteger) childs[1].value);
				else
					xc.buildCtrElement(c.id, list, XUtility.safeLong2Int((Long) childs[1].value, true));
			} else {
				int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
				TypeRank rank = childs[1].getAttributeValue(TypeAtt.rank, TypeRank.class, TypeRank.ANY);
				if (childs[2].value instanceof XVar)
					xc.buildCtrElement(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, (XVarInteger) childs[2].value);
				else
					xc.buildCtrElement(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, XUtility.safeLong2Int((Long) childs[2].value, true));
			}
		}

		private void channel(XCtr c) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			int startIndex0 = c.childs[0].getAttributeValue(TypeAtt.startIndex, 0);
			if (c.childs.length == 1)
				xc.buildCtrChannel(c.id, list, startIndex0);
			else if (c.childs[1].type == TypeChild.list) {
				int startIndex1 = c.childs[1].getAttributeValue(TypeAtt.startIndex, 0);
				xc.buildCtrChannel(c.id, list, startIndex0, (XVarInteger[]) c.childs[1].value, startIndex1);
			} else
				xc.buildCtrChannel(c.id, list, startIndex0, (XVarInteger) c.childs[1].value);
		}

		private void stretch(XCtr c) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			int[] values = trIntegers(c.childs[1].value);
			int[] widthsMin = Stream.of((IntegerInterval[]) c.childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.inf, true)).toArray();
			int[] widthsMax = Stream.of((IntegerInterval[]) c.childs[2].value).mapToInt(ii -> XUtility.safeLong2Int(ii.sup, true)).toArray();
			if (c.childs.length == 3)
				xc.buildCtrStretch(c.id, list, values, widthsMin, widthsMax);
			else
				xc.buildCtrStretch(c.id, list, values, widthsMin, widthsMax, trIntegers2D(c.childs[3].value));
		}

		private void noOverlap(XCtr c) {
			boolean zeroIgnored = c.childs[0].getAttributeValue(TypeAtt.zeroIgnored, true);
			if (c.childs[0].value instanceof XVarInteger[][]) {
				if (c.childs[1].value instanceof XVarInteger[][])
					xc.buildCtrNoOverlap(c.id, (XVarInteger[][]) c.childs[0].value, (XVarInteger[][]) c.childs[1].value, zeroIgnored);
				else
					xc.buildCtrNoOverlap(c.id, (XVarInteger[][]) c.childs[0].value, trIntegers2D(c.childs[1].value), zeroIgnored);
			} else {
				if (c.childs[1].value instanceof XVarInteger[])
					xc.buildCtrNoOverlap(c.id, (XVarInteger[]) c.childs[0].value, (XVarInteger[]) c.childs[1].value, zeroIgnored);
				else
					xc.buildCtrNoOverlap(c.id, (XVarInteger[]) c.childs[0].value, trIntegers(c.childs[1].value), zeroIgnored);
			}
		}

		private void cumulative(XCtr c) {
			CChild[] childs = c.childs;
			XVarInteger[] origins = (XVarInteger[]) childs[0].value;
			Condition condition = (Condition) childs[childs.length - 1].value;
			if (childs.length == 4) {
				if (childs[1].value instanceof Long[] && childs[2].value instanceof Long[])
					xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), trIntegers(childs[2].value), condition);
				else if (childs[1].value instanceof Long[] && !(childs[2].value instanceof Long[]))
					xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), (XVarInteger[]) childs[2].value, condition);
				else if (!(childs[1].value instanceof Long[]) && childs[2].value instanceof Long[])
					xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, trIntegers(childs[2].value), condition);
				else
					xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, (XVarInteger[]) childs[2].value, condition);
			} else {
				XVarInteger[] ends = (XVarInteger[]) childs[2].value;
				if (childs[1].value instanceof Long[] && childs[3].value instanceof Long[])
					xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), ends, trIntegers(childs[3].value), condition);
				else if (childs[1].value instanceof Long[] && !(childs[3].value instanceof Long[]))
					xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), ends, (XVarInteger[]) childs[3].value, condition);
				else if (!(childs[1].value instanceof Long[]) && childs[3].value instanceof Long[])
					xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, ends, trIntegers(childs[3].value), condition);
				else
					xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, ends, (XVarInteger[]) childs[3].value, condition);
			}
		}

		private void instantiation(XCtr c) {
			xc.buildCtrInstantiation(c.id, (XVarInteger[]) c.childs[0].value, trIntegers(c.childs[1].value));
		}

		private void clause(XCtr c) {
			Object[] t = (Object[]) c.childs[0].value;
			XVarInteger[] pos = Stream.of(t).filter(o -> o instanceof XVar).map(o -> (XVar) o).toArray(XVarInteger[]::new);
			XVarInteger[] neg = Stream.of(t).filter(o -> !(o instanceof XVar)).map(o -> (XVar) ((XNodeLeaf) ((XNodeParent) o).sons[0]).value)
					.toArray(XVarInteger[]::new);
			xc.buildCtrClause(c.id, pos, neg);
		}
	}

	class CtrLoaderSymbolic {
		private XCallbacks xc;

		private CtrLoaderSymbolic(XCallbacks xc) {
			this.xc = xc;
		}

		private void load(XCtr c) {
			switch (c.getType()) {
			case intension:
				intension(c);
				break;
			case extension:
				extension(c);
				break;
			case allDifferent:
				allDifferent(c);
				break;
			default:
				unimplementedCase(c);
			}
		}

		private void intension(XCtr c) {
			xc.buildCtrIntension(c.id, Stream.of(c.vars()).toArray(XVarSymbolic[]::new), (XNodeParent) c.childs[0].value);
		}

		private void extension(XCtr c) {
			CChild c1 = c.childs[1];
			boolean positive = c1.type == TypeChild.supports;
			if (c1.value == null || Array.getLength(c1.value) == 0) { // 0 tuple
				if (positive)
					xc.buildCtrFalse(c.id, c.vars());
				else
					xc.buildCtrTrue(c.id, c.vars());
			} else {
				XVarSymbolic[] list = (XVarSymbolic[]) c.childs[0].value;
				if (list.length == 1) // unary constraint
					xc.buildCtrExtension(c.id, list[0], (String[]) c1.value, positive, c1.flags);
				else
					xc.buildCtrExtension(c.id, list, (String[][]) c1.value, positive, c1.flags);
			}
		}

		private void allDifferent(XCtr c) {
			if (c.childs.length == 1 && c.childs[0].type == TypeChild.list)
				xc.buildCtrAllDifferent(c.id, (XVarSymbolic[]) (c.childs[0].value));
			else
				unimplementedCase(c);
		}

	}

	default void loadCtr(XCtr c) {
		CChild[] childs = c.childs;
		XUtility.control(Stream.of(TypeChild.cost, TypeChild.set, TypeChild.mset).noneMatch(t -> t == childs[childs.length - 1].type),
				"soft, set and mset currently not implemented");
		if (Stream.of(c.vars()).allMatch(x -> x instanceof XVarInteger))
			new CtrLoaderInteger(this).load(c);
		else if (Stream.of(c.vars()).allMatch(x -> x instanceof XVarSymbolic))
			new CtrLoaderSymbolic(this).load(c);
		else
			unimplementedCase(c);
	}

	default void loadObjective(OEntry entry) {
		if (entry.type == TypeObjective.EXPRESSION) {
			XNodeExpr node = ((OObjectiveExpr) entry).rootNode;
			if (node.getType() == TypeExpr.VAR) {
				if (entry.minimize)
					buildObjToMinimize(entry.id, (XVarInteger) ((XNodeLeaf) node).value);
				else
					buildObjToMaximize(entry.id, (XVarInteger) ((XNodeLeaf) node).value);
			} else {
				if (entry.minimize)
					buildObjToMinimize(entry.id, (XNodeParent) node);
				else
					buildObjToMaximize(entry.id, (XNodeParent) node);
			}
		} else {
			XVarInteger[] vars = (XVarInteger[]) ((OObjectiveSpecial) entry).vars;
			SimpleValue[] vals = ((OObjectiveSpecial) entry).coeffs;
			int[] coeffs = vals == null ? null : Stream.of(vals).mapToInt(val -> trInteger(((IntegerValue) val).v)).toArray();
			if (coeffs == null) {
				if (entry.minimize)
					buildObjToMinimize(entry.id, entry.type, vars);
				else
					buildObjToMaximize(entry.id, entry.type, vars);
			} else {
				if (entry.minimize)
					buildObjToMinimize(entry.id, entry.type, vars, coeffs);
				else
					buildObjToMaximize(entry.id, entry.type, vars, coeffs);
			}
		}
	}

	default void loadObjectives(List<OEntry> list) {
		list.stream().forEach(entry -> loadObjective(entry));
	}

	default void buildCtrTrue(String id, XVar[] list) {
	}

	default void buildCtrFalse(String id, XVar[] list) {
		throw new RuntimeException("Constraint with only conflicts");
	}

	/**********************************************************************************************
	 * Methods called at Specific Moments
	 *********************************************************************************************/

	void beginInstance(TypeFramework type);

	void endInstance();

	void beginVariables(List<VEntry> vEntries);

	void endVariables();

	void beginArray(XArray a);

	void endArray(XArray a);

	void beginConstraints(List<CEntry> cEntries);

	void endConstraints();

	void beginBlock(XBlock b);

	void endBlock(XBlock b);

	void beginGroup(XGroup g);

	void endGroup(XGroup g);

	void beginSlide(XSlide s);

	void endSlide(XSlide s);

	void beginObjectives(List<OEntry> oEntries, TypeCombination type);

	void endObjectives();

	// void beginAnnotations(List<AEntry> aEntries) ;
	// void endAnnotations() ;

	/**********************************************************************************************
	 * Methods to be implemented on integer variables/constraints
	 *********************************************************************************************/

	void buildVarInteger(XVarInteger x, int minValue, int maxValue);

	void buildVarInteger(XVarInteger x, int[] values);

	void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent syntaxTreeRoot);

	/** Primitive constraint of the form x <op> k, with x a variable, k a constant (int) and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k);

	/** Primitive constraint of the form x <opa> y <op> k, with x and y variables, k a constant (int), <opa> in {+,-,*,/,%,dist} and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k);

	/** Primitive constraint of the form x <opa> y <op> z, with x y and z variables, k a constant (int), <opa> in {+,-,*,/,%,dist} and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, XVarInteger z);

	void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags);

	void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags);

	void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates);

	void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions);

	void buildCtrAllDifferent(String id, XVarInteger[] list);

	void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except);

	void buildCtrAllDifferentList(String id, XVarInteger[][] lists);

	void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix);

	void buildCtrAllEqual(String id, XVarInteger[] list);

	void buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator);

	void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator);

	void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator);

	void buildCtrSum(String id, XVarInteger[] list, Condition condition);

	void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition);

	void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition);

	void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition);

	void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k);

	void buildCtrAtMost(String id, XVarInteger[] list, int value, int k);

	void buildCtrExactly(String id, XVarInteger[] list, int value, int k);

	void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k);

	void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k);

	void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k);

	void buildCtrNValues(String id, XVarInteger[] list, Condition condition);

	void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition);

	void buildCtrNotAllEqual(String id, XVarInteger[] list);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax);

	void buildCtrMaximum(String id, XVarInteger[] list, Condition condition);

	void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	void buildCtrMinimum(String id, XVarInteger[] list, Condition condition);

	void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	void buildCtrElement(String id, XVarInteger[] list, XVarInteger value);

	void buildCtrElement(String id, XVarInteger[] list, int value);

	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value);

	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value);

	void buildCtrChannel(String id, XVarInteger[] list, int startIndex);

	void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2);

	void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value);

	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax);

	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns);

	void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg);

	void buildCtrInstantiation(String id, XVarInteger[] list, int[] values);

	/**********************************************************************************************
	 * Methods to be implemented for managing objectives
	 *********************************************************************************************/

	void buildObjToMinimize(String id, XVarInteger x);

	void buildObjToMaximize(String id, XVarInteger x);

	void buildObjToMinimize(String id, XNodeParent syntaxTreeRoot);

	void buildObjToMaximize(String id, XNodeParent syntaxTreeRoot);

	void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list);

	void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list);

	void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs);

	void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs);

	/**********************************************************************************************
	 * Methods to be implemented on symbolic variables/constraints
	 *********************************************************************************************/

	void buildVarSymbolic(XVarSymbolic x, String[] values);

	void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent syntaxTreeRoot);

	void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags);

	void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags);

	void buildCtrAllDifferent(String id, XVarSymbolic[] list);
}
