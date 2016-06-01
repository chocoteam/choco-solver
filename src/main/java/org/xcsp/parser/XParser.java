package org.xcsp.parser;

import static org.xcsp.parser.XConstants.BLOCK;
import static org.xcsp.parser.XConstants.CONSTRAINTS;
import static org.xcsp.parser.XConstants.DELIMITER_LISTS;
import static org.xcsp.parser.XConstants.DELIMITER_MSETS;
import static org.xcsp.parser.XConstants.DELIMITER_SETS;
import static org.xcsp.parser.XConstants.DOMAIN;
import static org.xcsp.parser.XConstants.GROUP;
import static org.xcsp.parser.XConstants.MINIMIZE;
import static org.xcsp.parser.XConstants.OBJECTIVES;
import static org.xcsp.parser.XConstants.VAL_MINUS_INFINITY;
import static org.xcsp.parser.XConstants.VAL_MINUS_INFINITY_INT;
import static org.xcsp.parser.XConstants.VAL_PLUS_INFINITY;
import static org.xcsp.parser.XConstants.VAL_PLUS_INFINITY_INT;
import static org.xcsp.parser.XConstants.VAR;
import static org.xcsp.parser.XConstants.VARIABLES;
import static org.xcsp.parser.XUtility.childElementsOf;
import static org.xcsp.parser.XUtility.control;
import static org.xcsp.parser.XUtility.isTag;
import static org.xcsp.parser.XUtility.safeLong;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xcsp.parser.XConstraints.CChild;
import org.xcsp.parser.XConstraints.CEntry;
import org.xcsp.parser.XConstraints.CEntryReifiable;
import org.xcsp.parser.XConstraints.XBlock;
import org.xcsp.parser.XConstraints.XCtr;
import org.xcsp.parser.XConstraints.XGroup;
import org.xcsp.parser.XConstraints.XLogic;
import org.xcsp.parser.XConstraints.XParameter;
import org.xcsp.parser.XConstraints.XReification;
import org.xcsp.parser.XConstraints.XSeqbin;
import org.xcsp.parser.XConstraints.XSlide;
import org.xcsp.parser.XConstraints.XSoftening;
import org.xcsp.parser.XDomains.XDom;
import org.xcsp.parser.XDomains.XDomBasic;
import org.xcsp.parser.XDomains.XDomGraph;
import org.xcsp.parser.XDomains.XDomInteger;
import org.xcsp.parser.XDomains.XDomSet;
import org.xcsp.parser.XDomains.XDomSymbolic;
import org.xcsp.parser.XEnums.TypeAtt;
import org.xcsp.parser.XEnums.TypeChild;
import org.xcsp.parser.XEnums.TypeClass;
import org.xcsp.parser.XEnums.TypeCombination;
import org.xcsp.parser.XEnums.TypeConditionOperator;
import org.xcsp.parser.XEnums.TypeCtr;
import org.xcsp.parser.XEnums.TypeExpr;
import org.xcsp.parser.XEnums.TypeFlag;
import org.xcsp.parser.XEnums.TypeFramework;
import org.xcsp.parser.XEnums.TypeMeasure;
import org.xcsp.parser.XEnums.TypeObjective;
import org.xcsp.parser.XEnums.TypeOperator;
import org.xcsp.parser.XEnums.TypeReification;
import org.xcsp.parser.XNodeExpr.XNodeLeaf;
import org.xcsp.parser.XNodeExpr.XNodeParent;
import org.xcsp.parser.XObjectives.OEntry;
import org.xcsp.parser.XObjectives.OObjectiveExpr;
import org.xcsp.parser.XObjectives.OObjectiveSpecial;
import org.xcsp.parser.XValues.Decimal;
import org.xcsp.parser.XValues.IntegerEntity;
import org.xcsp.parser.XValues.IntegerInterval;
import org.xcsp.parser.XValues.Rational;
import org.xcsp.parser.XValues.SimpleValue;
import org.xcsp.parser.XValues.TypePrimitive;
import org.xcsp.parser.XVariables.TypeVar;
import org.xcsp.parser.XVariables.XArray;
import org.xcsp.parser.XVariables.XVar;

/**
 * This class corresponds to a Java parser that uses DOM (Document Object Model) to parse XCSP3 instances. <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author Christophe Lecoutre, CRIL-CNRS - lecoutre@cril.fr
 * @version 1.3
 */
public class XParser {

	/** The document to be parsed. */
	private Document document; //

	/** An XPath object that is useful for some tasks (queries). */
	private XPath xpath = XPathFactory.newInstance().newXPath();

	/** The map that stores pairs (id,variable). */
	private Map<String, XVar> mapForVars = new HashMap<>();

	/** The map that stores pairs (id,array). */
	private Map<String, XArray> mapForArrays = new HashMap<>();

	/** A map used as a cache for avoiding building several times the same domain objects; it stores pairs (textualContent,domain). */
	private Map<String, XDom> cacheForContentToDomain = new HashMap<>();

	/** The list of entries of the element <variables>. It contains variables and arrays. */
	public List<XVariables.VEntry> vEntries = new ArrayList<>();

	/**
	 * The list of entries of the element <constraints>. It contains stand-alone constraints (extension, intension, allDifferent, ...), groups of constraints,
	 * and meta-constraints (sliding and logical constructions).
	 */
	public List<XConstraints.CEntry> cEntries = new ArrayList<>();

	/** The list of objectives of the element <objectives>. Typically, it contains 0 or 1 objective. */
	public List<XObjectives.OEntry> oEntries = new ArrayList<>();

	/** The type of the framework used for the loaded instance. */
	public TypeFramework typeFramework;

	/** In case of multi-objective optimization, indicates the type that must be considered. */
	public TypeCombination typeCombination;

	/** The classes that must be discarded. Used just before posting variables, constraints and objectives. **/
	public TypeClass[] discardedClasses;

	/**********************************************************************************************
	 * Basic class for entries in variables, constraints and objectives
	 *********************************************************************************************/

	/** The class root of any entry in variables, constraints and objectives. The basic attributes id, class and note are managed here. */
	public static abstract class AnyEntry {
		/** The id (unique identifier) of the entry. */
		public String id;

		/** The classes associated with the entry. */
		public TypeClass[] classes;

		/** The note (short comment) associated with the entry. */
		public String note;

		/**
		 * The attributes that are associated with the element. Useful for storing all attributes by a simple copy. It is mainly used when dealing with special
		 * parameters of constraints (startIndex, circular, ...).
		 */
		public final Map<TypeAtt, String> attributes = new HashMap<>();

		/** The flags associated with the entry. Currently, used only for table constraints. */
		public final Set<TypeFlag> flags = new HashSet<>();

		/** Returns the Boolean value of the specified attribute, if it exists, the specified default value otherwise. */
		public final boolean getAttributeValue(TypeAtt att, boolean defaultValue) {
			return attributes.get(att) == null ? defaultValue : attributes.get(att).toLowerCase().equals("true");
		}

		/** Returns the int value of the specified attribute, if it exists, the specified default value otherwise. */
		public final int getAttributeValue(TypeAtt att, int defaultValue) {
			return attributes.get(att) == null ? defaultValue : XUtility.safeLong2Int(safeLong(attributes.get(att)), true);
		}

		/** Returns the value of the specified attribute, if it exists, the specified default value otherwise. */
		public final <T extends Enum<T>> T getAttributeValue(TypeAtt att, Class<T> clazz, T defaultValue) {
			return attributes.get(att) == null ? defaultValue : XEnums.valueOf(clazz, attributes.get(att));
		}

		/** Collect the XMl attributes of the specified element into a map (using an enum type for keys, and String for values). */
		public void copyAttributesOf(Element elt) {
			NamedNodeMap al = elt.getAttributes();
			IntStream.range(0, al.getLength()).forEach(i -> attributes.put(TypeAtt.valOf(al.item(i).getNodeName()), al.item(i).getNodeValue()));
			if (id == null && attributes.containsKey(TypeAtt.id))
				id = attributes.get(TypeAtt.id);
			if (attributes.containsKey(TypeAtt.CLASS))
				classes = TypeClass.classesFor(attributes.get(TypeAtt.CLASS).split("\\s+"));
			if (attributes.containsKey(TypeAtt.note))
				note = attributes.get(TypeAtt.note);
		}

		protected AnyEntry() {
		}

		protected AnyEntry(String id) {
			this.id = id;
		}
	}

	/** Returns the value of the specified attribute for the specified element, if it exists, the specified default value otherwise. */
	private <T extends Enum<T>> T giveAttributeValue(Element elt, String attName, Class<T> clazz, T defaultValue) {
		String s = elt.getAttribute(attName);
		return s.length() == 0 ? defaultValue : XEnums.valueOf(clazz, s.replaceFirst("\\s+", "_"));
	}

	/**********************************************************************************************
	 * Parsing of Variables (and Domains)
	 *********************************************************************************************/

	/** Parses a basic domain, i.e., a domain for an integer, symbolic, float or stochastic variable (or array). */
	private XDomBasic parseDomBasic(Element elt, TypeVar type) {
		String content = elt.getTextContent().trim();
		XDomBasic dom = (XDomBasic) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = XDomBasic.parse(content, type));
		return dom;
	}

	/** Parse a complex domain for a set variable (or array). */
	private XDomSet parseDomSet(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt);
		String req = childs[0].getTextContent().trim(), pos = childs[1].getTextContent().trim(), content = req + " | " + pos;
		XDomSet dom = (XDomSet) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = XDomSet.parse(req, pos, type));
		return dom;
	}

	/** Parse a complex domain for a graph variable (or array). */
	private XDomGraph parseDomGraph(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt), req = childElementsOf(childs[0]), pos = childElementsOf(childs[1]);
		String reqV = req[0].getTextContent().trim(), reqE = req[1].getTextContent().trim();
		String posV = pos[0].getTextContent().trim(), posE = pos[1].getTextContent().trim();
		String content = reqV + " | " + reqE + " | " + posV + " | " + posE;
		XDomGraph dom = (XDomGraph) cacheForContentToDomain.get(content);
		if (dom == null)
			cacheForContentToDomain.put(content, dom = XDomGraph.parse(reqV, reqE, posV, posE, type));
		return dom;
	}

	/** Parse a domain for any type of variable (or array). */
	private XDom parseDomain(Element elt, TypeVar type) {
		return type.isBasic() ? parseDomBasic(elt, type) : type.isSet() ? parseDomSet(elt, type) : parseDomGraph(elt, type);
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
		Map<String, XDom> cacheForId2Domain = new HashMap<>(); // a map for managing pairs (id,domain); remember that aliases can be encountered
		for (Element elt : childElementsOf(document, VARIABLES)) {
			XVariables.VEntry entry = null;
			String id = elt.getAttribute(TypeAtt.id.name());
			TypeVar type = elt.getAttribute(TypeAtt.type.name()).length() == 0 ? TypeVar.integer : TypeVar.valueOf(elt.getAttribute(TypeAtt.type.name()));
			Element actualForElt = getActualElementToAnalyse(elt); // managing aliases, i.e., 'as' indirection
			XDom dom = cacheForId2Domain.get(actualForElt.getAttribute(TypeAtt.id.name())); // necessary not null when 'as' indirection
			if (elt.getTagName().equals(VAR)) {
				if (dom == null && !type.isQualitative())
					cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
				entry = XVar.build(id, type, dom);
			} else {
				int[] size = giveArraySize(elt);
				if (dom == null && !type.isQualitative()) {
					Element[] childs = childElementsOf(actualForElt);
					if (childs.length > 0 && childs[0].getTagName().equals(DOMAIN)) { // we have to deal with mixed domains
						XArray array = new XArray(id, type, size);
						Stream.of(childs).forEach(child -> {
							Element actualForChild = getActualElementToAnalyse(child);
							XDom domChild = cacheForId2Domain.get(actualForChild.getAttribute(TypeAtt.id.name()));
							if (domChild == null) {
								domChild = parseDomain(actualForChild, type);
								String idChild = child.getAttribute(TypeAtt.id.name());
								if (idChild.length() > 0)
									cacheForId2Domain.put(idChild, domChild);
							}
							array.setDom(child.getAttribute("for"), domChild);
						});
						entry = array;
					} else {
						cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
						entry = new XArray(id, type, size, dom);
					}
				} else
					entry = new XArray(id, type, size, dom);
			}
			entry.copyAttributesOf(elt); // we copy the attributes for the variable or array
			if (TypeClass.disjoint(entry.classes, discardedClasses))
				vEntries.add(entry);
		}
		for (XVariables.VEntry entry : vEntries)
			if (entry instanceof XVar)
				mapForVars.put(entry.id, (XVar) entry);
			else {
				Stream.of(((XArray) entry).vars).forEach(var -> {
					if (var != null)
						mapForVars.put(var.id, var);
				});
				mapForArrays.put(entry.id, (XArray) entry);
			}
		// entriesOfVariables.stream().forEach(e -> System.out.println(e));
	}

	/**********************************************************************************************
	 * General Parsing Methods (for basic entities, conditions, simple and double sequences)
	 *********************************************************************************************/

	/** The root class for denoting a condition, i.e., a pair (operator,operand) used in many constraints. */
	public static abstract class Condition {
		/** The operator of the condition */
		public TypeConditionOperator operator;

		Condition(TypeConditionOperator operator) {
			this.operator = operator;
			XUtility.control(operator.isSet() == (this instanceof ConditionIntvl), "Bad operator");
		}
	}

	/** The class denoting a condition where the operand is a variable. */
	public static class ConditionVar extends Condition {
		public XVar x;

		ConditionVar(TypeConditionOperator operator, XVar x) {
			super(operator);
			this.x = x;
		}
	}

	/** The class denoting a condition where the operand is a value. */
	public static class ConditionVal extends Condition {
		public int k;

		ConditionVal(TypeConditionOperator operator, int k) {
			super(operator);
			this.k = k;
		}
	}

	/** The class denoting a condition where the operand is an interval. */
	public static class ConditionIntvl extends Condition {
		public int min, max;

		ConditionIntvl(TypeConditionOperator operator, int min, int max) {
			super(operator);
			this.min = min;
			this.max = max;
		}
	}

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
			return new XParameter(tok.equals("%...") ? -1 : Integer.parseInt(tok.substring(1)));
		if (tok.indexOf("(") != -1)
			return parseExpression(tok);
		return tok; // tok must be a symbolic value
	}

	private Object parseData(Element elt) {
		return parseData(elt.getTextContent().trim());
	}

	/** Parses a pair of the form (operator, operand) */
	private Condition parseCondition(String tok) {
		int pos = tok.indexOf(',');
		String left = tok.substring(tok.charAt(0) != '(' ? 0 : 1, pos), right = tok.substring(pos + 1, tok.length()
				- (tok.charAt(tok.length() - 1) == ')' ? 1 : 0));
		TypeConditionOperator op = TypeConditionOperator.valueOf(left.trim().toUpperCase());
		Object o = parseData(right);
		Condition c = null;
		if (o instanceof XVar)
			c = new ConditionVar(op, (XVar) o);
		else if (o instanceof Long)
			c = new ConditionVal(op, XUtility.safeLong2Int((Long) o, true));
		else {
			int min = ((IntegerInterval) o).inf == VAL_MINUS_INFINITY ? VAL_MINUS_INFINITY_INT : XUtility.safeLong2Int(((IntegerInterval) o).inf, true);
			int max = ((IntegerInterval) o).sup == VAL_PLUS_INFINITY ? VAL_PLUS_INFINITY_INT : XUtility.safeLong2Int(((IntegerInterval) o).sup, true);
			c = new ConditionIntvl(op, min, max);
		}
		return c;
	}

	/** Parses a pair of the form (operator, operand) */
	private Condition parseCondition(Element elt) {
		return parseCondition(elt.getTextContent().trim());
	}

	/** Parses a sequence of pairs of the form (operator, operand) */
	private Condition[] parseConditions(Element elt) {
		return Stream.of(elt.getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(tok -> parseCondition(tok)).toArray(Condition[]::new);
	}

	/**
	 * Parse a sequence of tokens (separated by the specified delimiter). Each token can represent a compact list of array variables, or a basic entity.
	 */
	private Object[] parseSequence(String seq, String delimiter) {
		List<Object> list = new ArrayList<>();
		for (String tok : seq.split(delimiter)) {
			int pos = tok.indexOf("[");
			XArray array = pos == -1 ? null : mapForArrays.get(tok.substring(0, pos));
			if (array != null)
				list.addAll(array.getVarsFor(tok));
			else
				list.add(parseData(tok));
		}
		return XUtility.specificArrayFrom(list);
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
		return XUtility.specificArray2DFrom(list);
	}

	/**
	 * Parse a double sequence of variables. Either the double sequence only contains simple variables, or is represented by a compact form.
	 */
	private Object[][] parseDoubleSequenceOfVars(Element elt) {
		String content = elt.getTextContent().trim();
		if (content.charAt(0) == '(') {
			List<Object[]> list = Stream.of(content.split(DELIMITER_LISTS)).skip(1).map(tok -> parseSequence(tok, "\\s*,\\s*")).collect(Collectors.toList());
			return XUtility.specificArray2DFrom(list);
		}
		XArray array = mapForArrays.get(content.substring(0, content.indexOf("[")));
		IntegerEntity[] indexRanges = array.buildIndexRanges(content);
		int first = -1, second = -1;
		for (int i = 0; first == -1 && i < indexRanges.length; i++)
			if (!indexRanges[i].isSingleton())
				first = i;
		for (int i = indexRanges.length - 1; second == -1 && i >= 0; i--)
			if (!indexRanges[i].isSingleton())
				second = i;
		int length1 = (int) indexRanges[first].width(), length2 = (int) indexRanges[second].width();
		List<Object[]> list2D = new ArrayList<>();
		int[] indexes = Stream.of(indexRanges).mapToInt(it -> (int) it.smallest()).toArray(); // first index
		for (int i = 0; i < length1; i++) {
			List<Object> list = new ArrayList<>();
			indexes[first] = i + (int) indexRanges[first].smallest();
			for (int j = 0; j < length2; j++) {
				indexes[second] = j + (int) indexRanges[second].smallest();
				list.add(array.varAt(indexes));
			}
			list2D.add(XUtility.specificArrayFrom(list));
		}
		return XUtility.specificArray2DFrom(list2D);
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
	private Object parseTuples(Element elt, TypePrimitive primitive, XDomBasic[] doms, ModifiableBoolean mb) {
		String s = elt.getTextContent().trim();
		if (s.length() == 0)
			return null;
		if (s.charAt(0) != '(') { // necessarily a unary constraint if '(' not present as first character
			if (primitive == null) // case SYMBOLIC, so we return an array of string
				return Stream.of(s.split("\\s+")).filter(tok -> doms == null || ((XDomSymbolic) doms[0]).contains(tok)).toArray(String[]::new);
			else
				return primitive.parseSeq(s, doms == null ? null : (XDomInteger) doms[0]);
		}
		if (primitive == null) // in that case, we keep String (although integers can also be present at some places)
			return Stream
					.of(s.split(DELIMITER_LISTS))
					.skip(1)
					.map(tok -> tok.split("\\s*,\\s*"))
					.filter(t -> doms == null
							|| IntStream.range(0, t.length).noneMatch(
									i -> !(doms[i] instanceof XDomSymbolic ? (((XDomSymbolic) doms[i]).contains(t[i])) : ((XDomInteger) doms[i])
											.contains(Integer.parseInt(t[i]))))).toArray(String[][]::new);
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
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		XVar[] vars = leafs.get(0).value instanceof XVar[] ? (XVar[]) leafs.get(0).value : null; // may be null if a constraint template
		TypePrimitive primitive = args != null ? TypePrimitive.whichPrimitiveFor((XVar[][]) args) : vars != null ? TypePrimitive.whichPrimitiveFor(vars) : null;
		XDomBasic[] doms = args != null ? XDomBasic.domainsFor((XVar[][]) args) : vars != null ? XDomBasic.domainsFor(vars) : null;
		ModifiableBoolean mb = new ModifiableBoolean();
		// We use doms to possibly filter out some tuples, and primitive to build an array of values of this primitive (short, byte, int or long)
		leafs.add(new CChild(isTag(sons[1], TypeChild.supports) ? TypeChild.supports : TypeChild.conflicts, parseTuples(sons[1], primitive, doms, mb)));
		if (doms == null || leafs.get(1).value instanceof IntegerEntity[])
			leafs.get(1).flags.add(TypeFlag.UNCLEAN_TUPLES); // we inform solvers that some tuples can be invalid (wrt the domains of variables)
		if (mb.value)
			leafs.get(1).flags.add(TypeFlag.STARRED_TUPLES); // we inform solvers that the table (list of tuples) contains the special value *
	}

	/** Parses a functional expression, as used for example in elements <intension>. */
	private XNodeExpr parseExpression(String s) {
		int leftParenthesisPosition = s.indexOf('(');
		if (leftParenthesisPosition == -1) { // i.e., if leaf
			XVar var = mapForVars.get(s);
			if (var != null)
				return new XNodeLeaf(TypeExpr.VAR, var);
			if (s.charAt(0) == '%')
				return new XNodeLeaf(TypeExpr.PAR, safeLong(s.substring(1)));
			String[] t = s.split("\\.");
			if (t.length == 2)
				return new XNodeLeaf(TypeExpr.DECIMAL, new Decimal(safeLong(t[0]), safeLong(t[1])));
			if (Character.isDigit(s.charAt(0)) || s.charAt(0) == '+' || s.charAt(0) == '-')
				return new XNodeLeaf(TypeExpr.LONG, safeLong(s));
			return new XNodeLeaf(TypeExpr.SYMBOL, s);
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
		leafs.add(new CChild(TypeChild.function, parseExpression((sons.length == 0 ? elt : sons[0]).getTextContent().trim())));
	}

	/** Parses a constraint <smart>. Will be included in specifications later. */
	private void parseSmart(Element elt, Element[] sons) {
		for (Element son : sons)
			leafs.add(new CChild(TypeChild.list, parseSequence(son)));
	}

	/**********************************************************************************************
	 * Language-based Constraints
	 *********************************************************************************************/

	private void parseRegular(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		Object[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ? safeLong(tr[1]) : tr[1];
			return new Object[] { tr[0], value, tr[2] };
		}).toArray(Object[][]::new);
		leafs.add(new CChild(TypeChild.transition, trans));
		leafs.add(new CChild(TypeChild.start, sons[2].getTextContent().trim()));
		leafs.add(new CChild(TypeChild.FINAL, sons[3].getTextContent().trim().split("\\s+")));
	}

	private void parseGrammar(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.terminal, sons[1].getTextContent().trim().split("\\s+")));
		String[][][] rules = Stream.of(sons[2].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] sp = t.split("\\s*,\\s*");
			String[] leftWord = sp[0].split("\\s+"), rightWord = sp.length == 1 ? new String[] { "" } : sp[1].split("\\s+");
			return new String[][] { leftWord, rightWord };
		}).toArray(String[][][]::new);
		leafs.add(new CChild(TypeChild.rules, rules));
		leafs.add(new CChild(TypeChild.start, sons[3].getTextContent().trim()));
	}

	private void parseMDD(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		Object[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ? safeLong(tr[1]) : tr[1];
			return new Object[] { tr[0], value, tr[2] };
		}).toArray(Object[][]::new);
		// String[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> t.split("\\s*,\\s*")).toArray(String[][]::new);
		leafs.add(new CChild(TypeChild.transition, trans));
	}

	/**********************************************************************************************
	 * Comparison-based Constraints
	 *********************************************************************************************/

	private void parseAllDifferent(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			if (type == TypeChild.matrix)
				leafs.add(new CChild(type, parseDoubleSequenceOfVars(sons[0])));
			else {
				Element except = isTag(sons[lastSon], TypeChild.except) ? sons[lastSon] : null;
				for (int i = 0, limit = lastSon - (except != null ? 1 : 0); i <= limit; i++)
					leafs.add(new CChild(type, parseSequence(sons[i])));
				if (except != null) {
					if (lastSon == 1)
						leafs.add(new CChild(TypeChild.except, leafs.get(0).setVariableInvolved() ? parseDoubleSequence(except, DELIMITER_SETS)
								: parseSequence(except)));
					else
						leafs.add(new CChild(TypeChild.except, parseDoubleSequence(except, type == TypeChild.list ? DELIMITER_LISTS
								: type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
				}
			}
		}
	}

	private void parseAllEqual(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			for (int i = 0; i <= lastSon; i++)
				leafs.add(new CChild(type, parseSequence(sons[i])));
		}
	}

	private void parseAllDistant(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i < lastSon; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseOrdered(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		if (type == TypeChild.matrix)
			leafs.add(new CChild(type, parseDoubleSequenceOfVars(sons[0])));
		else
			for (int i = 0; i < lastSon; i++)
				leafs.add(new CChild(type, parseSequence(sons[i])));
		leafs.add(new CChild(TypeChild.operator, TypeOperator.valOf(sons[lastSon].getTextContent())));
	}

	private void parseLex(Element elt, Element[] sons, int lastSon) {
		parseOrdered(elt, sons, lastSon);
	}

	private void parseAllIncomparable(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i <= lastSon; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
	}

	/**********************************************************************************************
	 * Counting and Summing Constraints
	 *********************************************************************************************/

	private void parseSum(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.list))
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		else
			leafs.add(new CChild(TypeChild.index, parseData(sons[0])));
		if (isTag(sons[1], TypeChild.coeffs)) // if (lastSon == 2)
			leafs.add(new CChild(TypeChild.coeffs, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCount(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseNValues(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		Element except = isTag(sons[lastSon - 1], TypeChild.except) ? sons[lastSon - 1] : null;
		for (int i = 0, limit = lastSon - (except != null ? 2 : 1); i <= limit; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
		if (except != null)
			leafs.add(new CChild(TypeChild.except, lastSon == 2 ? parseSequence(except) : parseDoubleSequence(except, type == TypeChild.list ? DELIMITER_LISTS
					: type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCardinality(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new CChild(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new CChild(TypeChild.rowOccurs, parseDoubleSequenceOfVars(sons[2])));
			leafs.add(new CChild(TypeChild.colOccurs, parseDoubleSequenceOfVars(sons[3])));
		} else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new CChild(TypeChild.occurs, parseSequence(sons[2])));
		}
	}

	private void parseBalance(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.values))
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseSpread(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.total))
			leafs.add(new CChild(TypeChild.total, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseDeviation(Element elt, Element[] sons, int lastSon) {
		parseSpread(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Connection Constraints
	 *********************************************************************************************/

	private void parseMaximum(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.index))
			leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		if (isTag(sons[lastSon], TypeChild.condition))
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseMinimum(Element elt, Element[] sons, int lastSon) {
		parseMaximum(elt, sons, lastSon);
	}

	private void parseElement(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new CChild(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new CChild(TypeChild.index, parseSequence(sons[1])));
		} else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		}
		leafs.add(new CChild(TypeChild.value, parseData(sons[lastSon])));
	}

	private void parseChannel(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			if (lastSon == 1)
				leafs.add(new CChild(isTag(sons[1], TypeChild.list) ? TypeChild.list : TypeChild.value, parseSequence(sons[1])));
		}
	}

	private void parsePermutation(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.mapping, parseSequence(sons[2])));
	}

	private void parsePrecedence(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.operator, TypeOperator.valOf(sons[lastSon].getTextContent())));
	}

	/**********************************************************************************************
	 * Packing and Scheduling Constraints
	 *********************************************************************************************/

	private void parseStretch(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.widths, parseSequence(sons[2])));
		if (lastSon == 3)
			leafs.add(new CChild(TypeChild.patterns, parseDoubleSequence(sons[3], DELIMITER_LISTS)));
	}

	private void parseNoOverlap(Element elt, Element[] sons) {
		boolean multiDimensional = sons[0].getTextContent().trim().charAt(0) == '('; // no possibility currently of using compact forms if multi-dimensional
		leafs.add(new CChild(TypeChild.origins, multiDimensional ? parseDoubleSequence(sons[0], DELIMITER_LISTS) : parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.lengths, multiDimensional ? parseDoubleSequence(sons[1], DELIMITER_LISTS) : parseSequence(sons[1])));
	}

	private void parseCumulative(Element elt, Element[] sons) {
		int cnt = 0;
		leafs.add(new CChild(TypeChild.origins, parseSequence(sons[cnt++])));
		leafs.add(new CChild(TypeChild.lengths, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.ends))
			leafs.add(new CChild(TypeChild.ends, parseSequence(sons[cnt++])));
		leafs.add(new CChild(TypeChild.heights, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.machines)) {
			leafs.add(new CChild(TypeChild.machines, parseSequence(sons[cnt++])));
			leafs.add(new CChild(TypeChild.conditions, parseConditions(sons[cnt++])));
		} else
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[cnt++])));
	}

	private void parseBinPacking(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.sizes, parseSequence(sons[1])));
		if (isTag(sons[2], TypeChild.condition))
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[2])));
		else
			leafs.add(new CChild(TypeChild.conditions, parseConditions(sons[2])));
	}

	private void parseKnapsack(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.weights, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.profits, parseSequence(sons[2])));
		leafs.add(new CChild(TypeChild.limit, parseData(sons[3])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[4])));
	}

	/**********************************************************************************************
	 * Graph Constraints
	 *********************************************************************************************/

	private CChild listOrGraph(Element elt) {
		return isTag(elt, TypeChild.list) ? new CChild(TypeChild.list, parseSequence(elt)) : new CChild(TypeChild.graph, parseData(elt));
	}

	private void parseCircuit(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(listOrGraph(sons[0]));
			if (lastSon == 1)
				leafs.add(new CChild(TypeChild.size, parseData(sons[1])));
		}
	}

	private void parseNCircuits(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[1])));
	}

	private void parsePath(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.start, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.FINAL, parseData(sons[2])));
		if (lastSon == 3)
			leafs.add(new CChild(TypeChild.size, parseData(sons[3])));
	}

	private void parseNPaths(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseTree(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.root, parseData(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.size, parseData(sons[2])));
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

	/**********************************************************************************************
	 * Elementary Constraints
	 *********************************************************************************************/

	private void parseClause(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence((sons.length == 0 ? elt : sons[0]))));
	}

	private void parseInstantiation(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
	}

	/**********************************************************************************************
	 * Set Constraints
	 *********************************************************************************************/

	private void parseAllIntersecting(Element elt, Element[] sons) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt))); // necessary, case disjoint or overlapping
		else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[1])));
		}
	}

	private void parseRange(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.image, parseData(sons[2])));
	}

	private void parseRoots(Element elt, Element[] sons) {
		parseRange(elt, sons);
	}

	private void parsePartition(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.value, parseData(sons[1])));
	}

	/**********************************************************************************************
	 * Main methods for constraints
	 *********************************************************************************************/

	private List<CChild> leafs; // is you want to avoid this field, just pass it through as argument of every method called in the long sequence of 'if' below

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
	private CEntry parseCEntry(Element elt, Object[][] args, Element[] sons, int lastSon) {
		if (elt.getTagName().equals(GROUP)) {
			List<Object[]> l = IntStream.range(1, lastSon + 1).mapToObj(i -> parseSequence(sons[i])).collect(Collectors.toList());
			Object[][] groupArgs = l.stream().noneMatch(o -> !(o instanceof XVar[])) ? l.toArray(new XVar[0][]) : l.toArray(new Object[0][]);
			return new XGroup((CEntryReifiable) parseCEntryOuter(sons[0], groupArgs), groupArgs);
		}
		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.slide) {
			CChild[] lists = IntStream.range(0, lastSon).mapToObj(i -> new CChild(TypeChild.list, parseSequence(sons[i]))).toArray(CChild[]::new);
			int[] offset = Stream.of(sons).limit(lists.length).mapToInt(s -> XUtility.getIntValueOf(s, TypeAtt.offset.name(), 1)).toArray();
			int[] collect = Stream.of(sons).limit(lists.length).mapToInt(s -> XUtility.getIntValueOf(s, TypeAtt.collect.name(), 1)).toArray();
			if (lists.length == 1) { // we need to compute the value of collect[0], which corresponds to the arity of the constraint template
				XCtr ctr = (XCtr) parseCEntryOuter(sons[lastSon], null);
				XUtility.control(ctr.abstraction.abstractChilds.length == 1, "Other cases must be implemented");
				if (ctr.getType() == TypeCtr.intension)
					collect[0] = ((XNodeExpr) (ctr.childs[0].value)).maxParameterNumber() + 1;
				else {
					XParameter[] pars = (XParameter[]) ctr.abstraction.abstractChilds[0].value;
					XUtility.control(Stream.of(pars).noneMatch(p -> p.number == -1), "One parameter is %..., which is forbidden in slide");
					collect[0] = Stream.of(pars).mapToInt(p -> p.number + 1).max().orElseThrow(() -> new RuntimeException());
				}
			}
			XVar[][] scopes = XSlide.buildScopes(Stream.of(lists).map(ls -> (XVar[]) ls.value).toArray(XVar[][]::new), offset, collect,
					elt.getAttribute(TypeAtt.circular.name()).equals(Boolean.TRUE.toString()));
			return new XSlide(lists, offset, collect, (XCtr) parseCEntryOuter(sons[lastSon], scopes), scopes);
		}

		if (type == TypeCtr.seqbin) {
			CChild list = new CChild(TypeChild.list, parseSequence(sons[0]));
			XVar[] t = (XVar[]) list.value;
			XVar[][] scopes = IntStream.range(0, t.length - 1).mapToObj(i -> new XVar[] { t[i], t[i + 1] }).toArray(XVar[][]::new);
			CChild number = new CChild(TypeChild.number, parseData(sons[3]));
			return new XSeqbin(list, (XCtr) parseCEntryOuter(sons[1], scopes), (XCtr) parseCEntryOuter(sons[2], scopes), number, scopes);
		}

		if (type.isLogical())
			return new XLogic(type, IntStream.range(0, lastSon + 1).mapToObj(i -> parseCEntryOuter(sons[i], args)).toArray(CEntryReifiable[]::new));

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
		else if (type == TypeCtr.allIncomparable)
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
		else if (type == TypeCtr.instantiation)
			parseInstantiation(elt, sons, lastSon);
		else if (type == TypeCtr.allIntersecting)
			parseAllIntersecting(elt, sons);
		else if (type == TypeCtr.range)
			parseRange(elt, sons);
		else if (type == TypeCtr.roots)
			parseRoots(elt, sons);
		else if (type == TypeCtr.partition)
			parsePartition(elt, sons);
		else if (type == TypeCtr.smart)
			parseSmart(elt, sons);
		return new XCtr(type, leafs.toArray(new CChild[leafs.size()]));
	}

	/**
	 * Called to parse any constraint entry in <constraints> , that can be a group, a constraint, or a meta-constraint. This method calls parseCEntry.
	 */
	private CEntry parseCEntryOuter(Element elt, Object[][] args) {
		Element[] sons = childElementsOf(elt);
		int lastSon = sons.length - 1 - (elt.getAttribute(TypeAtt.type.name()).equals("soft") ? 1 : 0); // last son position, excluding <cost> that is managed
																										// apart
		CEntry entry = parseCEntry(elt, args, sons, lastSon);
		entry.copyAttributesOf(elt); // we copy the attributes
		if (entry instanceof XCtr)
			for (int i = 0; i <= lastSon; i++)
				((XCtr) entry).childs[i].copyAttributesOf(sons[i]); // we copy the attributes for each parameter of the constraint
		else if (entry instanceof XSlide)
			for (int i = 0; i < lastSon; i++)
				((XSlide) entry).lists[i].copyAttributesOf(sons[i]); // we copy the attributes for the list(s) involved in slide
		// Note that for seqbin and logic entries, no need to copy any attributes at this place

		if (entry instanceof CEntryReifiable) {
			CEntryReifiable entryReifiable = (CEntryReifiable) entry;
			Map<TypeAtt, String> attributes = entryReifiable.attributes;
			// dealing with softening
			if (lastSon == sons.length - 2) {
				Integer defaultCost = attributes.containsKey(TypeAtt.defaultCost) ? Integer.parseInt(attributes.get(TypeAtt.defaultCost)) : null;
				NamedNodeMap al = sons[sons.length - 1].getAttributes();
				TypeMeasure type = al.getNamedItem(TypeAtt.measure.name()) == null ? null : XEnums.valueOf(TypeMeasure.class,
						al.getNamedItem(TypeAtt.measure.name()).getNodeValue());
				String parameters = al.getNamedItem(TypeAtt.parameters.name()) == null ? null : al.getNamedItem(TypeAtt.parameters.name()).getNodeValue();
				Condition condition = parseCondition(sons[sons.length - 1]);
				entryReifiable.softening = new XSoftening(type, parameters, condition, defaultCost);
			}
			// dealing with reification
			if (attributes.containsKey(TypeAtt.reifiedBy))
				entryReifiable.reification = new XReification(TypeReification.FULL, mapForVars.get(attributes.get(TypeAtt.reifiedBy)));
			else if (attributes.containsKey(TypeAtt.hreifiedFrom))
				entryReifiable.reification = new XReification(TypeReification.HALF_FROM, mapForVars.get(attributes.get(TypeAtt.hreifiedFrom)));
			else if (attributes.containsKey(TypeAtt.hreifiedTo))
				entryReifiable.reification = new XReification(TypeReification.HALF_TO, mapForVars.get(attributes.get(TypeAtt.hreifiedTo)));
		}
		return entry;
	}

	/** Recursive parsing, traversing possibly multiple blocks */
	private void recursiveParsingOfConstraints(Element elt, List<CEntry> list) {
		if (elt.getTagName().equals(BLOCK)) {
			List<CEntry> blockEntries = new ArrayList<>();
			Stream.of(childElementsOf(elt)).forEach(child -> recursiveParsingOfConstraints(child, blockEntries));
			XBlock ctrBlock = new XBlock(blockEntries);
			ctrBlock.copyAttributesOf(elt);
			if (TypeClass.disjoint(ctrBlock.classes, discardedClasses))
				list.add(ctrBlock);
		} else {
			CEntry entry = parseCEntryOuter(elt, null);
			if (TypeClass.disjoint(entry.classes, discardedClasses))
				list.add(entry);
		}
	}

	/** Parses the element <constraints> of the document. */
	private void parseConstraints() {
		Stream.of(childElementsOf(document, CONSTRAINTS)).forEach(elt -> recursiveParsingOfConstraints(elt, cEntries));
		// updateVarDegreesWith(cEntries);
	}

	/** Parses the element <objectives> (if it exists) of the document. */
	private void parseObjectives() {
		NodeList nl = document.getDocumentElement().getElementsByTagName(OBJECTIVES);
		if (nl.getLength() == 1) {
			Element objectives = (Element) nl.item(0);
			typeCombination = giveAttributeValue(objectives, TypeAtt.combination.name(), TypeCombination.class, TypeCombination.PARETO);
			for (Element elt : childElementsOf(objectives)) {
				OEntry entry = null;
				boolean minimize = elt.getTagName().equals(MINIMIZE);
				TypeObjective type = giveAttributeValue(elt, TypeAtt.type.name(), TypeObjective.class, TypeObjective.EXPRESSION);
				if (type == TypeObjective.EXPRESSION) {
					entry = new OObjectiveExpr(minimize, type, parseExpression(elt.getTextContent().trim()));
				} else {
					Element[] sons = childElementsOf(elt);
					XVar[] vars = (XVar[]) parseSequence(sons.length == 0 ? elt : sons[0]);
					SimpleValue[] coeffs = sons.length != 2 ? null : SimpleValue.parseSeq(sons[1].getTextContent().trim());
					entry = new OObjectiveSpecial(minimize, type, vars, coeffs);
				}
				entry.copyAttributesOf(elt);
				if (TypeClass.disjoint(entry.classes, discardedClasses))
					oEntries.add(entry);
			}
		}

	}

	/** Updates the degree of each variable occurring somewhere in the specified list. */
	private void updateVarDegreesWith(List<CEntry> list) {
		for (XConstraints.CEntry entry : list)
			if (entry instanceof XBlock)
				updateVarDegreesWith(((XBlock) entry).subentries);
			else if (entry instanceof XGroup) {
				XGroup group = (XGroup) entry;
				for (int i = 0; i < group.argss.length; i++)
					for (XVar var : group.getScope(i))
						var.degree++;
			} else
				for (XVar var : entry.vars())
					var.degree++;
	}

	/** Computes the degree of each variable. Important for being aware of the useless variables (variables of degree 0). */
	private void computeVarDegrees() {
		updateVarDegreesWith(cEntries);
		for (OEntry entry : oEntries) {
			if (entry instanceof OObjectiveExpr)
				for (XVar x : ((OObjectiveExpr) entry).rootNode.collectVars(new HashSet<>()))
					x.degree++;
			else
				for (XVar x : ((OObjectiveSpecial) entry).vars)
					x.degree++;
		}
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of TypeClass denotes the classes that must
	 * be discarded (e.g., symmetryBreaking).
	 */
	public XParser(Document document, TypeClass[] discardedClasses) throws Exception {
		this.document = document;
		this.discardedClasses = discardedClasses;
		typeFramework = giveAttributeValue(document.getDocumentElement(), TypeAtt.type.name(), TypeFramework.class, TypeFramework.CSP);

		parseVariables();
		parseConstraints();
		parseObjectives();
		computeVarDegrees();

		// vEntries.stream().forEach(e -> System.out.println(e.toString()));
		// cEntries.stream().forEach(e -> System.out.println(e.toString()));
		// oEntries.stream().forEach(e -> System.out.println(e.toString()));
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of strings denotes the classes that must be
	 * discarded (e.g., symmetryBreaking).
	 */
	public XParser(Document document, String... discardedClasses) throws Exception {
		this(document, TypeClass.classesFor(discardedClasses));
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of TypeClass denotes the classes that
	 * must be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inpuStream, TypeClass[] discardedClasses) throws Exception {
		this(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inpuStream), discardedClasses);
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of strings denotes the classes that must
	 * be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inputStream, String... discardedClasses) throws Exception {
		this(inputStream, TypeClass.classesFor(discardedClasses));
	}

}
