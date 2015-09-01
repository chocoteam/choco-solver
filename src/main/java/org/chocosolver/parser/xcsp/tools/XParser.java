package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XConstraints.*;
import org.chocosolver.parser.xcsp.tools.XDomains.*;
import org.chocosolver.parser.xcsp.tools.XEnums.*;
import org.chocosolver.parser.xcsp.tools.XNodeExpr.XNodeLeaf;
import org.chocosolver.parser.xcsp.tools.XNodeExpr.XNodeParent;
import org.chocosolver.parser.xcsp.tools.XObjectives.Objective;
import org.chocosolver.parser.xcsp.tools.XObjectives.ObjectiveExpr;
import org.chocosolver.parser.xcsp.tools.XObjectives.ObjectiveSpecial;
import org.chocosolver.parser.xcsp.tools.XValues.*;
import org.chocosolver.parser.xcsp.tools.XVariables.Array;
import org.chocosolver.parser.xcsp.tools.XVariables.TypeVar;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.chocosolver.parser.xcsp.tools.XConstants.*;
import static org.chocosolver.parser.xcsp.tools.XUtility.*;

/**
 * This class corresponds to a Java parser that uses DOM (Document Object Model) to parse XCSP3 instances. <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author Christophe Lecoutre, CRIL-CNRS - lecoutre@cril.fr
 * @version 1
 */
public class XParser {

	/** The document to be parsed. */
	private Document document; //

	/** An XPath object that is useful for some tasks (queries). */
	private XPath xpath = XPathFactory.newInstance().newXPath();

	/** The map that stores pairs (id,variable). */
	private Map<String, Var> mapForVars = new HashMap<>();

	/** The map that stores pairs (id,array). */
	private Map<String, Array> mapForArrays = new HashMap<>();

	/** A map used as a cache for avoiding building several times the same domain objects; it stores pairs (textual-content,domain). */
	private Map<String, Dom> cacheForContentToDomain = new HashMap<>();

	/** The list of entries of the element <variables>. It contains variables and arrays. */
	public List<XVariables.Entry> entriesOfVariables = new ArrayList<>();

	/**
	 * The list of entries of the element <constraints>. It contains stand-alone constraints (extension, intension, allDifferent, ...), groups of constraints,
	 * and meta-constraints (sliding and logical constructions).
	 */
	public List<XConstraints.Entry> entriesOfConstraints = new ArrayList<>();

	/** The list of objectives of the element <objectives>. Typically, it contains 0 or 1 objective. */
	public List<Objective> objectives = new ArrayList<>();

	/**********************************************************************************************
	 * Parsing of Variables (and Domains)
	 *********************************************************************************************/

	/** Parses a basic domain, i.e., a domain for an integer, symbolic, float or stochastic variable (or array). */
	private DomBasic parseDomBasic(Element elt, TypeVar type) {
		String content = elt.getTextContent().trim();
		DomBasic dom = (DomBasic) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = DomBasic.parse(content, type));
		return dom;
	}

	/** Parse a complex domain for a set variable (or array). */
	private DomSet parseDomSet(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt);
		String req = childs[0].getTextContent().trim(), pos = childs[1].getTextContent().trim(), content = req + " | " + pos;
		DomSet dom = (DomSet) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = DomSet.parse(req, pos, type));
		return dom;
	}

	/** Parse a complex domain for a graph variable (or array). */
	private DomGraph parseDomGraph(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt), req = childElementsOf(childs[0]), pos = childElementsOf(childs[1]);
		String reqV = req[0].getTextContent().trim(), reqE = req[1].getTextContent().trim();
		String posV = pos[0].getTextContent().trim(), posE = pos[1].getTextContent().trim();
		String content = reqV + " | " + reqE + " | " + posV + " | " + posE;
		DomGraph dom = (DomGraph) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = DomGraph.parse(reqV, reqE, posV, posE, type));
		return dom;
	}

	/** Parse a domain for any type of variable (or array). */
	private Dom parseDomain(Element elt, TypeVar type) {
		return type.isBasic() ? parseDomBasic(elt, type) : type.isSet() ? parseDomSet(elt, type) : parseDomGraph(elt, type);
	}

	/** Gives the type of the variable (or array). Recall that integer is the default value. */
	private TypeVar giveTypeVar(Element elt) {
		String s = elt.getAttribute(TypeAtt.type.name());
		return s.length() == 0 ? TypeVar.integer : TypeVar.valueOf(s.replaceFirst("\\s+", "_"));
	}

	/** Gives the 'size' (an array of integers as defined in XCSP3) of the array of variables. */
	private int[] giveArraySize(Element elt) {
		StringTokenizer st = new StringTokenizer(elt.getAttribute(TypeAtt.size.name()), "[]");
		return IntStream.range(0, st.countTokens()).map(i -> Integer.parseInt(st.nextToken())).toArray();
	}

	/** Allows us to manage aliases, i.e., indirection due to the use of the 'as' attribute. */
	private Element getActualElementToAnalyse(Element elt) {
		try {
			String id = elt.getAttribute(TypeAtt.as.name());
			return id.length() == 0 ? elt : (Element) xpath.evaluate("//*[@id='" + id + "']", document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return (Element) XUtility.control(false, "Bad use of 'as'" + elt.getTagName());
		}
	}

	/** Parses all elements inside the element <variables>. */
	public void parseVariables() {
		Map<String, Dom> cacheForId2Domain = new HashMap<>(); // a map for managing pairs (id,domain); remember that aliases can be encountered
		for (Element elt : childElementsOf(document, VARIABLES)) {
			String id = elt.getAttribute(TypeAtt.id.name());
			TypeVar type = giveTypeVar(elt);
			Element actualForElt = getActualElementToAnalyse(elt); // managing aliases, i.e., 'as' indirection
			Dom dom = cacheForId2Domain.get(actualForElt.getAttribute(TypeAtt.id.name())); // necessary not null when 'as' indirection
			if (elt.getTagName().equals(VAR)) {
				if (dom == null && !type.isQualitative())
					cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
				entriesOfVariables.add(new Var(id, type, dom));
			} else {
				int[] size = giveArraySize(elt);
				if (dom == null && !type.isQualitative()) {
					Element[] childs = childElementsOf(actualForElt);
					if (childs.length > 0 && childs[0].getTagName().equals(DOMAIN)) { // we have to deal with mixed domains
						Array array = new Array(id, type, size);
						Stream.of(childs).forEach(child -> {
							Element actualForChild = getActualElementToAnalyse(child);
							Dom domChild = cacheForId2Domain.get(actualForChild.getAttribute(TypeAtt.id.name()));
							if (domChild == null) {
								domChild = parseDomain(actualForChild, type);
								String idChild = child.getAttribute(TypeAtt.id.name());
								if (idChild.length() > 0)
									cacheForId2Domain.put(idChild, domChild);
							}
							array.setDom(child.getAttribute("for"), domChild);
						});
						entriesOfVariables.add(array);
					} else {
						cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
						entriesOfVariables.add(new Array(id, type, size, dom));
					}
				} else
					entriesOfVariables.add(new Array(id, type, size, dom));
			}
		}
		for (XVariables.Entry entry : entriesOfVariables)
			if (entry instanceof Var)
				mapForVars.put(entry.id, (Var) entry);
			else {
				Stream.of(((Array) entry).vars).forEach(var -> mapForVars.put(var.id, var));
				mapForArrays.put(entry.id, (Array) entry);
			}
		// entriesOfVariables.stream().forEach(e -> System.out.println(e));
	}

	/**********************************************************************************************
	 * General Parsing Methods (for basic entities, conditions, simple and double sequences)
	 *********************************************************************************************/

	/**
	 * Parse the specified token, as a variable, an interval, a rational, a decimal, a long, a set (literal), a parameter, or a functional expression. If
	 * nothing above matches, the token is returned (and considered as a symbolic value).
	 */
	private Object parseData(String tok) {
		if (mapForVars.get(tok) != null)
			return mapForVars.get(tok);
		if (Character.isDigit(tok.charAt(0)) || tok.charAt(0) == '+' || tok.charAt(0) == '-') {
			String[] t = tok.split("\\.\\.");
			if (t.length == 2)
				return new IntegerInterval(safeLong(t[0]), safeLong(t[1]));
			t = tok.split("/");
			if (t.length == 2)
				return new Rational(safeLong(t[0]), safeLong(t[1]));
			t = tok.split("\\.");
			if (t.length == 2)
				return new Decimal(safeLong(t[0]), safeLong(t[1]));
			return safeLong(tok);
		}
		if (tok.charAt(0) == '{') { // set value
			String sub = tok.substring(1, tok.length() - 1); // empty set if sub.length() = 0
			return sub.length() == 0 ? new Object[] {} : Stream.of(sub.split("\\s*,\\s*")).mapToLong(s -> safeLong(s)).toArray();
		}
		if (tok.charAt(0) == '%')
			return new Parameter(tok.equals("%...") ? -1 : Integer.parseInt(tok.substring(1)));
		if (tok.indexOf("(") != -1)
			return parseExpression(tok);
		return tok; // tok must be a symbolic value
	}

	private Object parseData(Element elt) {
		return parseData(elt.getTextContent().trim());
	}

	/** Parses a pair of the form (operator, operand) */
	private Object[] parseCondition(String tok) {
		int pos = tok.indexOf(',');
		String left = tok.substring(tok.charAt(0) != '(' ? 0 : 1, pos), right = tok.substring(pos + 1, tok.length()
				- (tok.charAt(tok.length() - 1) == ')' ? 1 : 0));
		return new Object[] { TypeConditionOperator.valueOf(left), parseData(right) };
	}

	/** Parses a pair of the form (operator, operand) */
	private Object[] parseCondition(Element elt) {
		return parseCondition(elt.getTextContent().trim());
	}

	/** Parses a sequence of pairs of the form (operator, operand) */
	private Object[][] parseConditions(Element elt) {
		return Stream.of(elt.getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(tok -> parseCondition(tok)).toArray(Object[][]::new);
	}

	/**
	 * Parse a sequence of tokens (separated by the specified delimiter). Each token can represent a compact list of array variables, or a basic entity.
	 */
	private Object[] parseSequence(String seq, String delimiter) {
		List<Object> list = new ArrayList<>();
		for (String tok : seq.split(delimiter)) {
			int pos = tok.indexOf("[");
			Array array = pos == -1 ? null : mapForArrays.get(tok.substring(0, pos));
			if (array != null)
				list.addAll(array.getVarsFor(tok));
			else
				list.add(parseData(tok));
		}
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray() : list.toArray((Object[]) java.lang.reflect.Array.newInstance(clazz, list.size()));
	}

	private Object[] parseSequence(Element elt) {
		return parseSequence(elt.getTextContent().trim(), "\\s+");
	}

	/**
	 * Parse a double sequence, i.e. a sequence of tokens separated by the specified delimiter, and composed of entities separated by ,
	 */
	private Object[][] parseDoubleSequence(Element elt, String delimiter) {
		String content = elt.getTextContent().trim();
		List<Object[]> list = Stream.of(content.split(delimiter)).skip(1).map(tok -> parseSequence(tok, "\\s*,\\s*")).collect(Collectors.toList());
		Class<?> clazz = list.size() > 0 && list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray(new Object[0][]) : list.toArray((Object[][]) java.lang.reflect.Array.newInstance(clazz, list.size()));
	}

	/**
	 * Parse a double sequence of variables. Either the double sequence only contains simple variables, or is represented by a compact form.
	 */
	private Var[][] parseDoubleSequenceOfVars(Element elt) {
		String content = elt.getTextContent().trim();
		if (content.charAt(0) == '(')
			return Stream.of(content.split(DELIMITER_LISTS)).skip(1).map(s -> (Var[]) parseSequence(s, "\\s*,\\s*")).toArray(Var[][]::new);
		Array array = mapForArrays.get(content.substring(0, content.indexOf("[")));
		IntegerEntity[] indexRanges = array.buildIndexRanges(content);
		int first = -1, second = -1;
		for (int i = 0; first == -1 && i < indexRanges.length; i++)
			if (!indexRanges[i].isSingleton())
				first = i;
		for (int i = indexRanges.length - 1; second == -1 && i >= 0; i--)
			if (!indexRanges[i].isSingleton())
				second = i;
		Var[][] m = new Var[(int) indexRanges[first].width()][(int) indexRanges[second].width()];
		int[] indexes = Stream.of(indexRanges).mapToInt(it -> (int) it.smallest()).toArray(); // first index
		for (int i = 0; i < m.length; i++) {
			indexes[first] = i + (int) indexRanges[first].smallest();
			for (int j = 0; j < m[i].length; j++) {
				indexes[second] = j + (int) indexRanges[second].smallest();
				m[i][j] = array.varAt(indexes);
			}
		}
		return m;
	}

	/**********************************************************************************************
	 * Generic Constraints : Extension and Intension
	 *********************************************************************************************/

	class ModifiableBoolean {
		public boolean value;
	}

	/**
	 * Parse the tuples contained in the specified element. A 2-dimensional array of String, byte, short, int or long is returned, depending of the specified
	 * primitive (primitive set to null stands for String). The specified array of domains, if not null, can be used to filter out some tuples.
	 */
	private Object parseTuples(Element elt, TypePrimitive primitive, DomBasic[] doms, ModifiableBoolean mb) {
		String s = elt.getTextContent().trim();
		if (s.length() == 0)
			return null;
		if (s.charAt(0) != '(') { // necessarily a unary constraint if '(' not present as first character
			if (primitive == null) // case SYMBOLIC, so we return an array of string
				return Stream.of(s.split("\\s+")).filter(tok -> doms == null || ((DomSymbolic) doms[0]).contains(tok)).toArray(String[]::new);
			else
				return primitive.parseSeq(s, doms == null ? null : (DomInteger) doms[0]);
		}
		if (primitive == null) // in that case, we keep String (although integers can also be present at some places)
			return Stream.of(s.split(DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*"))
					.filter(t -> doms == null || IntStream.range(0, t.length).noneMatch(i -> !((DomSymbolic) doms[i]).contains(t[i]))).toArray(String[][]::new);
		List<Object> list = new ArrayList<>();
		int leftParenthesis = 0, rightParenthesis = leftParenthesis + 1;
		while (s.charAt(rightParenthesis) != ')')
			rightParenthesis++;
		String tok = s.substring(leftParenthesis + 1, rightParenthesis).trim();
		long[] tmp = new long[tok.split("\\s*,\\s*").length];
		while (tok != null) {
			if (primitive.parseTuple(tok, tmp, doms, mb)) // if not filtered-out parsed tuple
				if (primitive == TypePrimitive.BYTE) {
					byte[] t = new byte[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (byte) tmp[i];
					list.add(t);
				} else if (primitive == TypePrimitive.SHORT) {
					short[] t = new short[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (short) tmp[i];
					list.add(t);
				} else if (primitive == TypePrimitive.INT) {
					int[] t = new int[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (int) tmp[i];
					list.add(t);
				} else
					list.add(tmp.clone());
			for (leftParenthesis = rightParenthesis + 1; leftParenthesis < s.length() && s.charAt(leftParenthesis) != '('; leftParenthesis++)
				;
			if (leftParenthesis == s.length())
				tok = null;
			else {
				for (rightParenthesis = leftParenthesis + 1; s.charAt(rightParenthesis) != ')'; rightParenthesis++)
					;
				tok = s.substring(leftParenthesis + 1, rightParenthesis).trim();
			}
		}
		// returns a 2-dimensional array of byte, short, int or long
		return list.size() == 0 ? new long[0][] : list.toArray((Object[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size()));
	}

	/** Parses a constraint <extension>. */
	private void parseExtension(Element elt, Element[] sons, Object[][] args) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		Var[] vars = leafs.get(0).value instanceof Var[] ? (Var[]) leafs.get(0).value : null; // may be null if a constraint template
		TypePrimitive primitive = args != null ? TypePrimitive.whichPrimitiveFor((Var[][]) args) : vars != null ? TypePrimitive.whichPrimitiveFor(vars) : null;
		DomBasic[] doms = args != null ? DomBasic.domainsFor((Var[][]) args) : vars != null ? DomBasic.domainsFor(vars) : null;
		ModifiableBoolean mb = new ModifiableBoolean();
		// We use doms to possibly filter out some tuples, and primitive to build an array of values of this primitive (short, byte, int or long)
		leafs.add(new Child(isTag(sons[1], TypeChild.supports) ? TypeChild.supports : TypeChild.conflicts, parseTuples(sons[1], primitive, doms, mb)));
		if (doms == null || leafs.get(1).value instanceof IntegerEntity[])
			leafs.get(1).attributes.put(TypeAtt.unfiltered, Boolean.TRUE + ""); // if true, we inform solvers that some tuples can be invalid (wrt the domains of variables)
		if (mb.value)
			leafs.get(1).attributes.put(TypeAtt.starred, Boolean.TRUE + ""); // if true, we inform solvers that the table (list of tuples) contains the special value *
	}

	/** Parses a functional expression, as used for example in elements <intension>. */
	private XNodeExpr parseExpression(String s) {
		int leftParenthesisPosition = s.indexOf('(');
		if (leftParenthesisPosition == -1) { // i.e., if leaf
			Var var = mapForVars.get(s);
			if (var != null)
				return new XNodeLeaf(TypeExpr.VAR, var);
			if (s.charAt(0) == '%')
				return new XNodeLeaf(TypeExpr.PAR, safeLong(s.substring(1)));
			String[] t = s.split("\\.");
			if (t.length == 2)
				return new XNodeLeaf(TypeExpr.DECIMAL, new Decimal(safeLong(t[0]), safeLong(t[1])));
			return new XNodeLeaf(TypeExpr.LONG, safeLong(s));
		} else {
			int rightParenthesisPosition = s.lastIndexOf(")");
			TypeExpr operator = TypeExpr.valueOf(s.substring(0, leftParenthesisPosition).toUpperCase());
			if (leftParenthesisPosition == rightParenthesisPosition - 1) { // actually, this is also a leaf which is set(), the empty set
				control(operator == TypeExpr.SET, " Erreur");
				return new XNodeLeaf(TypeExpr.SET, null);
			}
			String content = s.substring(leftParenthesisPosition + 1, rightParenthesisPosition);
			List<XNodeExpr> nodes = new ArrayList<>();
			for (int right = 0; right < content.length(); right++) {
				int left = right;
				for (int nbOpens = 0; right < content.length(); right++) {
					if (content.charAt(right) == '(')
						nbOpens++;
					else if (content.charAt(right) == ')')
						nbOpens--;
					else if (content.charAt(right) == ',' && nbOpens == 0)
						break;
				}
				nodes.add(parseExpression(content.substring(left, right).trim()));
			}
			return new XNodeParent(operator, nodes.toArray(new XNodeExpr[0]));
		}
	}

	/** Parses a constraint <intension>. */
	private void parseIntension(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.function, parseExpression((sons.length == 0 ? elt : sons[0]).getTextContent().trim())));
	}

	/**********************************************************************************************
	 * Language-based Constraints
	 *********************************************************************************************/

	private void parseRegular(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		String[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> t.split("\\s*,\\s*")).toArray(String[][]::new);
		leafs.add(new Child(TypeChild.transition, trans));
		leafs.add(new Child(TypeChild.start, sons[2].getTextContent().trim()));
		leafs.add(new Child(TypeChild.FINAL, sons[3].getTextContent().trim().split("\\s+")));
	}

	private void parseGrammar(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.terminal, sons[1].getTextContent().trim().split("\\s+")));
		String[][][] rules = Stream.of(sons[2].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] sp = t.split("\\s*,\\s*");
			String[] leftWord = sp[0].split("\\s+"), rightWord = sp.length == 1 ? new String[] { "" } : sp[1].split("\\s+");
			return new String[][] { leftWord, rightWord };
		}).toArray(String[][][]::new);
		leafs.add(new Child(TypeChild.rules, rules));
		leafs.add(new Child(TypeChild.start, sons[3].getTextContent().trim()));
	}

	private void parseMDD(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		String[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> t.split("\\s*,\\s*")).toArray(String[][]::new);
		leafs.add(new Child(TypeChild.transition, trans));
	}

	/**********************************************************************************************
	 * Comparison-based Constraints
	 *********************************************************************************************/

	private void parseAllDifferent(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new Child(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			if (type == TypeChild.matrix)
				leafs.add(new Child(type, parseDoubleSequenceOfVars(sons[0])));
			else {
				Element except = isTag(sons[lastSon], TypeChild.except) ? sons[lastSon] : null;
				for (int i = 0, limit = lastSon - (except != null ? 1 : 0); i <= limit; i++)
					leafs.add(new Child(type, parseSequence(sons[i])));
				if (except != null) {
					if (lastSon == 1)
						leafs.add(new Child(TypeChild.except, leafs.get(0).setVariableInvolved() ? parseDoubleSequence(except, DELIMITER_SETS)
								: parseSequence(except)));
					else
						leafs.add(new Child(TypeChild.except, parseDoubleSequence(except, type == TypeChild.list ? DELIMITER_LISTS
								: type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
				}
			}
		}
	}

	private void parseAllEqual(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new Child(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			for (int i = 0; i <= lastSon; i++)
				leafs.add(new Child(type, parseSequence(sons[i])));
		}
	}

	private void parseAllDistant(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i < lastSon; i++)
			leafs.add(new Child(type, parseSequence(sons[i])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseOrdered(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		if (type == TypeChild.matrix)
			leafs.add(new Child(type, parseDoubleSequenceOfVars(sons[0])));
		else
			for (int i = 0; i < lastSon; i++)
				leafs.add(new Child(type, parseSequence(sons[i])));
		leafs.add(new Child(TypeChild.operator, TypeOperator.valueOf(sons[lastSon].getTextContent().trim())));
	}

	private void parseLex(Element elt, Element[] sons, int lastSon) {
		parseOrdered(elt, sons, lastSon);
	}

	private void parseAllIncomparable(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i <= lastSon; i++)
			leafs.add(new Child(type, parseSequence(sons[i])));
	}

	/**********************************************************************************************
	 * Counting and Summing Constraints
	 *********************************************************************************************/

	private void parseSum(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.list))
			leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		else
			leafs.add(new Child(TypeChild.index, parseData(sons[0])));
		if (isTag(sons[1], TypeChild.coeffs)) // if (lastSon == 2)
			leafs.add(new Child(TypeChild.coeffs, parseSequence(sons[1])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCount(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseNValues(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		Element except = isTag(sons[lastSon - 1], TypeChild.except) ? sons[lastSon - 1] : null;
		for (int i = 0, limit = lastSon - (except != null ? 2 : 1); i <= limit; i++)
			leafs.add(new Child(type, parseSequence(sons[i])));
		if (except != null)
			leafs.add(new Child(TypeChild.except, lastSon == 2 ? parseSequence(except) : parseDoubleSequence(except, type == TypeChild.list ? DELIMITER_LISTS
					: type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCardinality(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new Child(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new Child(TypeChild.rowOccurs, parseDoubleSequenceOfVars(sons[2])));
			leafs.add(new Child(TypeChild.colOccurs, parseDoubleSequenceOfVars(sons[3])));
		} else {
			leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new Child(TypeChild.occurs, parseSequence(sons[2])));
		}
	}

	private void parseBalance(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.values))
			leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseSpread(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.total))
			leafs.add(new Child(TypeChild.total, parseData(sons[1])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseDeviation(Element elt, Element[] sons, int lastSon) {
		parseSpread(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Connection Constraints
	 *********************************************************************************************/

	private void parseMaximum(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.index))
			leafs.add(new Child(TypeChild.index, parseData(sons[1])));
		if (isTag(sons[lastSon], TypeChild.condition))
			leafs.add(new Child(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseMinimum(Element elt, Element[] sons, int lastSon) {
		parseMaximum(elt, sons, lastSon);
	}

	private void parseElement(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new Child(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new Child(TypeChild.index, parseSequence(sons[1])));
		} else {
			leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new Child(TypeChild.index, parseData(sons[1])));
		}
		leafs.add(new Child(TypeChild.value, parseData(sons[lastSon])));
	}

	private void parseChannel(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new Child(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
			if (lastSon == 1)
				leafs.add(new Child(isTag(sons[1], TypeChild.list) ? TypeChild.list : TypeChild.value, parseSequence(sons[1])));
		}
	}

	private void parsePermutation(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.list, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new Child(TypeChild.mapping, parseSequence(sons[2])));
	}

	private void parsePrecedence(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new Child(TypeChild.operator, TypeOperator.valueOf(sons[lastSon].getTextContent().trim())));
	}

	/**********************************************************************************************
	 * Packing and Scheduling Constraints
	 *********************************************************************************************/

	private void parseStretch(Element elt, Element[] sons, int lastSon) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new Child(TypeChild.widths, parseSequence(sons[2])));
		if (lastSon == 3)
			leafs.add(new Child(TypeChild.patterns, parseDoubleSequence(sons[3], DELIMITER_LISTS)));
	}

	private void parseNoOverlap(Element elt, Element[] sons) {
		boolean multiDimensional = sons[0].getTextContent().trim().charAt(0) == '('; // no possibility currently of using compact forms if multi-dimensional
		leafs.add(new Child(TypeChild.origins, multiDimensional ? parseDoubleSequence(sons[0], DELIMITER_LISTS) : parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.lengths, multiDimensional ? parseDoubleSequence(sons[1], DELIMITER_LISTS) : parseSequence(sons[1])));
	}

	private void parseCumulative(Element elt, Element[] sons) {
		int cnt = 0;
		leafs.add(new Child(TypeChild.origins, parseSequence(sons[cnt++])));
		leafs.add(new Child(TypeChild.lengths, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.ends))
			leafs.add(new Child(TypeChild.ends, parseSequence(sons[cnt++])));
		leafs.add(new Child(TypeChild.heights, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.machines)) {
			leafs.add(new Child(TypeChild.machines, parseSequence(sons[cnt++])));
			leafs.add(new Child(TypeChild.conditions, parseConditions(sons[cnt++])));
		} else
			leafs.add(new Child(TypeChild.condition, parseCondition(sons[cnt++])));
	}

	private void parseBinPacking(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.sizes, parseSequence(sons[1])));
		if (isTag(sons[2], TypeChild.condition))
			leafs.add(new Child(TypeChild.condition, parseCondition(sons[2])));
		else
			leafs.add(new Child(TypeChild.conditions, parseConditions(sons[2])));
	}

	private void parseKnapsack(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.weights, parseSequence(sons[1])));
		leafs.add(new Child(TypeChild.profits, parseSequence(sons[2])));
		leafs.add(new Child(TypeChild.limit, parseData(sons[3])));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[4])));
	}

	/**********************************************************************************************
	 * Graph Constraints
	 *********************************************************************************************/

	private Child listOrGraph(Element elt) {
		return isTag(elt, TypeChild.list) ? new Child(TypeChild.list, parseSequence(elt)) : new Child(TypeChild.graph, parseData(elt));
	}

	private void parseCircuit(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new Child(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(listOrGraph(sons[0]));
			if (lastSon == 1)
				leafs.add(new Child(TypeChild.size, parseData(sons[1])));
		}
	}

	private void parseNCircuits(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new Child(TypeChild.condition, parseCondition(sons[1])));
	}

	private void parsePath(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new Child(TypeChild.start, parseData(sons[1])));
		leafs.add(new Child(TypeChild.FINAL, parseData(sons[2])));
		if (lastSon == 3)
			leafs.add(new Child(TypeChild.size, parseData(sons[3])));
	}

	private void parseNPaths(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseTree(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new Child(TypeChild.root, parseData(sons[1])));
		if (lastSon == 2)
			leafs.add(new Child(TypeChild.size, parseData(sons[2])));
	}

	private void parseArbo(Element elt, Element[] sons, int lastSon) {
		parseTree(elt, sons, lastSon);
	}

	private void parseNTrees(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseNArbos(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseNCliques(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseClause(Element elt, Element[] sons, int lastSon) {
		String[] toks = (sons.length == 0 ? elt : sons[0]).getTextContent().trim().split("\\s+");
		leafs.add(new Child(TypeChild.list, Stream.of(toks)
				.map(tok -> new Object[] { tok.charAt(0) != '-', mapForVars.get(tok.charAt(0) != '-' ? tok : tok.substring(1)) }).toArray(Object[][]::new)));
	}

	private void parseCube(Element elt, Element[] sons, int lastSon) {
		parseClause(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Set Constraints
	 *********************************************************************************************/

	private void parseAllIntersecting(Element elt, Element[] sons) {
		if (sons.length == 0)
			leafs.add(new Child(TypeChild.list, parseSequence(elt))); // necessary, case disjoint or overlapping
		else {
			leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new Child(TypeChild.condition, parseCondition(sons[1])));
		}
	}

	private void parseRange(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.index, parseData(sons[1])));
		leafs.add(new Child(TypeChild.image, parseData(sons[2])));
	}

	private void parseRoots(Element elt, Element[] sons) {
		parseRange(elt, sons);
	}

	private void parsePartition(Element elt, Element[] sons) {
		leafs.add(new Child(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new Child(TypeChild.value, parseData(sons[1])));
	}

	/**********************************************************************************************
	 * Main methods for constraints
	 *********************************************************************************************/

	private List<Child> leafs; // is you want to avoid this field, just pass it through as argument of every method called in the long sequence of 'if' below

	/**
	 * Parses an entry of <constraints>, except that soft and reification features are managed apart (in the calling method).
	 * 
	 * @param elt
	 *            The element to parse (must be a group, a meta-constraint or a constraint)
	 * @param args
	 *            Only useful for extension constraints, so as to possibly filter tuples, when analyzing the possible args (scopes)
	 * @param sons
	 *            The set of child elements of elt
	 * @param lastSon
	 *            The position of the last son to handle when parsing here (since <cost>, if present, is managed apart)
	 * @return the parsed entry
	 */
	private Entry parseConstraintEntry(Element elt, Object[][] args, Element[] sons, int lastSon) {
		if (elt.getTagName().equals(GROUP)) {
			List<Object[]> l = IntStream.range(1, lastSon + 1).mapToObj(i -> parseSequence(sons[i])).collect(Collectors.toList());
			Object[][] groupArgs = l.stream().noneMatch(o -> !(o instanceof Var[])) ? l.toArray(new Var[0][]) : l.toArray(new Object[0][]);
			return new Group(parseConstraintEntryOuter(sons[0], groupArgs), groupArgs);
		}

		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.slide) {
			Child[] lists = IntStream.range(0, lastSon).mapToObj(i -> new Child(TypeChild.list, parseSequence(sons[i]))).toArray(Child[]::new);
			int[] offset = Stream.of(sons).limit(lists.length).mapToInt(s -> XUtility.getIntValueOf(s, TypeAtt.offset.name(), 1)).toArray();
			int[] collect = Stream.of(sons).limit(lists.length).mapToInt(s -> XUtility.getIntValueOf(s, TypeAtt.collect.name(), 1)).toArray();
			if (lists.length == 1) { // we need to compute the value of collect[0], which corresponds to the arity of the constraint template
				Ctr ctr = (Ctr) parseConstraintEntryOuter(sons[lastSon], null);
				XUtility.control(ctr.abstraction instanceof AbstractionBasic, "Other cases must be implemented");
				if (ctr.type == TypeCtr.intension)
					collect[0] = ((XNodeExpr) (ctr.childs[0].value)).maxParameterNumber() + 1;
				else {
					Parameter[] pars = (Parameter[]) ctr.childs[((AbstractionBasic) ctr.abstraction).abstractChildPosition].value;
					XUtility.control(Stream.of(pars).noneMatch(p -> p.number == -1), "One parameter is %..., which is forbidden in slide");
					collect[0] = Stream.of(pars).mapToInt(p -> p.number + 1).max().orElseThrow(() -> new RuntimeException());
				}
			}
			Var[][] scopes = Slide.buildScopes(Stream.of(lists).map(ls -> (Var[]) ls.value).toArray(Var[][]::new), offset, collect,
					elt.getAttribute(TypeAtt.circular.name()).equals(Boolean.TRUE.toString()));
			return new Slide(lists, offset, collect, (Ctr) parseConstraintEntryOuter(sons[lastSon], scopes), scopes);
		}

		if (type == TypeCtr.seqbin) {
			Child list = new Child(TypeChild.list, parseSequence(sons[0]));
			Var[] t = (Var[]) list.value;
			Var[][] scopes = IntStream.range(0, t.length - 1).mapToObj(i -> new Var[] { t[i], t[i + 1] }).toArray(Var[][]::new);
			Child number = new Child(TypeChild.number, parseData(sons[3]));
			return new Seqbin(list, (Ctr) parseConstraintEntryOuter(sons[1], scopes), (Ctr) parseConstraintEntryOuter(sons[2], scopes), number, scopes);
		}

		if (type.isLogical())
			return new Logic(type, IntStream.range(0, lastSon + 1).mapToObj(i -> parseConstraintEntryOuter(sons[i], args)).toArray(Entry[]::new));

		leafs = new ArrayList<>();
		if (type == TypeCtr.extension)
			parseExtension(elt, sons, args);
		else if (type == TypeCtr.intension)
			parseIntension(elt, sons);
		else if (type == TypeCtr.regular)
			parseRegular(elt, sons);
		else if (type == TypeCtr.grammar)
			parseGrammar(elt, sons);
		else if (type == TypeCtr.mdd)
			parseMDD(elt, sons, lastSon);
		else if (type == TypeCtr.allDifferent)
			parseAllDifferent(elt, sons, lastSon);
		else if (type == TypeCtr.allEqual)
			parseAllEqual(elt, sons, lastSon);
		else if (type == TypeCtr.allDistant)
			parseAllDistant(elt, sons, lastSon);
		else if (type == TypeCtr.ordered)
			parseOrdered(elt, sons, lastSon);
		else if (type == TypeCtr.lex)
			parseLex(elt, sons, lastSon);
		else if (type == TypeCtr.allIncomprable)
			parseAllIncomparable(elt, sons, lastSon);
		else if (type == TypeCtr.sum)
			parseSum(elt, sons, lastSon);
		else if (type == TypeCtr.count)
			parseCount(elt, sons, lastSon);
		else if (type == TypeCtr.nValues)
			parseNValues(elt, sons, lastSon);
		else if (type == TypeCtr.cardinality)
			parseCardinality(elt, sons, lastSon);
		else if (type == TypeCtr.balance)
			parseBalance(elt, sons, lastSon);
		else if (type == TypeCtr.spread)
			parseSpread(elt, sons, lastSon);
		else if (type == TypeCtr.deviation)
			parseDeviation(elt, sons, lastSon);
		else if (type == TypeCtr.maximum)
			parseMaximum(elt, sons, lastSon);
		else if (type == TypeCtr.minimum)
			parseMinimum(elt, sons, lastSon);
		else if (type == TypeCtr.element)
			parseElement(elt, sons, lastSon);
		else if (type == TypeCtr.channel)
			parseChannel(elt, sons, lastSon);
		else if (type == TypeCtr.permutation)
			parsePermutation(elt, sons, lastSon);
		else if (type == TypeCtr.precedence)
			parsePrecedence(elt, sons, lastSon);
		else if (type == TypeCtr.stretch)
			parseStretch(elt, sons, lastSon);
		else if (type == TypeCtr.noOverlap)
			parseNoOverlap(elt, sons);
		else if (type == TypeCtr.cumulative)
			parseCumulative(elt, sons);
		else if (type == TypeCtr.binPacking)
			parseBinPacking(elt, sons);
		else if (type == TypeCtr.knapsack)
			parseKnapsack(elt, sons);
		else if (type == TypeCtr.circuit)
			parseCircuit(elt, sons, lastSon);
		else if (type == TypeCtr.nCircuits)
			parseNCircuits(elt, sons, lastSon);
		else if (type == TypeCtr.path)
			parsePath(elt, sons, lastSon);
		else if (type == TypeCtr.nPaths)
			parseNPaths(elt, sons, lastSon);
		else if (type == TypeCtr.tree)
			parseTree(elt, sons, lastSon);
		else if (type == TypeCtr.nTrees)
			parseNTrees(elt, sons, lastSon);
		else if (type == TypeCtr.arbo)
			parseArbo(elt, sons, lastSon);
		else if (type == TypeCtr.nArbos)
			parseNArbos(elt, sons, lastSon);
		else if (type == TypeCtr.nCliques)
			parseNCliques(elt, sons, lastSon);
		else if (type == TypeCtr.clause)
			parseClause(elt, sons, lastSon);
		else if (type == TypeCtr.cube)
			parseCube(elt, sons, lastSon);
		else if (type == TypeCtr.allIntersecting)
			parseAllIntersecting(elt, sons);
		else if (type == TypeCtr.range)
			parseRange(elt, sons);
		else if (type == TypeCtr.roots)
			parseRoots(elt, sons);
		else if (type == TypeCtr.partition)
			parsePartition(elt, sons);
		return new Ctr(type, leafs.toArray(new Child[leafs.size()]));
	}

	/**
	 * Called to parse any constraint entry in <constraints> , that can be a group, a constraint, or a meta-constraint. This method calls parseConstraintEntry.
	 */
	private Entry parseConstraintEntryOuter(Element elt, Object[][] args) {
		Element[] sons = childElementsOf(elt);
		int lastSon = sons.length - 1 - (elt.getAttribute(TypeAtt.type.name()).equals("soft") ? 1 : 0); // last son position, excluding <cost> that is managed
																										// apart
		Entry entry = parseConstraintEntry(elt, args, sons, lastSon);
		entry.copyAttributesOf(elt); // we copy the attributes for the constraint
		if (entry instanceof Ctr)
			for (int i = 0; i <= lastSon; i++)
				((Ctr) entry).childs[i].copyAttributesOf(sons[i]); // we copy the attributes for each parameter of the constraint
		else if (entry instanceof Slide)
			for (int i = 0; i < lastSon; i++)
				((Slide) entry).lists[i].copyAttributesOf(sons[i]); // we copy the attributes for the list(s) involved in slide
		// Note that for seqbin and logic entries, no need to copy any attributes at this place
		if (lastSon == sons.length - 2) { // we handle the possible presence of <cost>
			entry.cost = new Child(TypeChild.cost, parseCondition(sons[sons.length - 1]));
			entry.cost.copyAttributesOf(sons[sons.length - 1]);
		}
		String name = entry instanceof Group ? null : (String) entry.getNameOfReificationVar();
		if (name != null) { // we handle possible reification
			entry.reificationVar = mapForVars.get(name);
			XUtility.control(entry.reificationVar != null, "Pb with reification variable " + name);
		}
		return entry;
	}

	/** Computes the degree of each variable. Important to be aware of the useful variables */
	private void computeVarDegrees() {
		for (XConstraints.Entry entry : entriesOfConstraints)
			if (entry instanceof Group) {
				Group group = (Group) entry;
				for (int i = 0; i < group.argss.length; i++)
					for (Var var : group.getScope(i))
						var.degree++;
			} else
				for (Var var : entry.getVars())
					var.degree++;
	}

	/** Parses the element <constraints> of the document. */
	private void parseConstraints() {
		for (Element elt : childElementsOf(document, CONSTRAINTS))
			if (elt.getTagName().equals(BLOC))
				for (Element child : childElementsOf(elt))
					entriesOfConstraints.add(parseConstraintEntryOuter(child, null));
			else
				entriesOfConstraints.add(parseConstraintEntryOuter(elt, null));
		computeVarDegrees();
	}

	/** Gives the type of the objective. Recall that expression is the default value. */
	private TypeObjective getTypeObj(Element elt) {
		String val = elt.getAttribute(TypeAtt.type.name());
		return val.length() == 0 ? TypeObjective.expression : TypeObjective.valueOf(val);
	}

	/** Parses the element <objectives> (if it exists) of the document. */
	private void parseObjectives() {
		if (document.getDocumentElement().getElementsByTagName(OBJECTIVES).getLength() == 1) {
			int cnt = 0;
			for (Element elt : childElementsOf(document, OBJECTIVES)) {
				String id = elt.getAttribute(TypeAtt.id.name()).length() > 0 ? elt.getAttribute(TypeAtt.id.name()) : "Obj" + (cnt++);
				boolean minimize = elt.getTagName().equals(MINIMIZE);
				TypeObjective type = getTypeObj(elt);
				if (type == TypeObjective.expression)
					objectives.add(new ObjectiveExpr(id, minimize, type, parseExpression(elt.getTextContent().trim())));
				else {
					Element[] sons = childElementsOf(elt);
					Var[] vars = (Var[]) parseSequence(sons.length == 0 ? elt : sons[0]);
					SimpleValue[] coeffs = sons.length != 2 ? null : SimpleValue.parseSeq(sons[1].getTextContent().trim());
					objectives.add(new ObjectiveSpecial(id, minimize, type, vars, coeffs));
				}
			}
		}
	}

	/** Loads and parses the XCSP3 file corresponding to the specified file name. */
	public XParser(String fileName) throws Exception {
		document = load(fileName);
		System.out.println("Parsing variables...");
		parseVariables();
		System.out.println("Parsing constraints...");
		parseConstraints();
		System.out.println("Parsing objectives...");
		parseObjectives();

		entriesOfVariables.stream().forEach(e -> System.out.println(e));
		entriesOfConstraints.stream().forEach(e -> System.out.println(e.toString()));
		objectives.stream().forEach(e -> System.out.println(e.toString()));
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 1)
			System.out.println("XParser 1.0 (August, 31, 2015)\nUsage : java XParser <instanceName>");
		else
			new XParser(args[0]);
	}
}
