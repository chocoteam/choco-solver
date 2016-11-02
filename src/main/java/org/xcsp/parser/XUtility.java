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

import static org.xcsp.parser.XConstants.BIG_MAX_SAFE_LONG;
import static org.xcsp.parser.XConstants.BIG_MIN_SAFE_LONG;
import static org.xcsp.parser.XConstants.MINUS_INFINITY;
import static org.xcsp.parser.XConstants.PLUS_INFINITY;
import static org.xcsp.parser.XConstants.VAL_MINUS_INFINITY;
import static org.xcsp.parser.XConstants.VAL_PLUS_INFINITY;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xcsp.parser.XEnums.TypeChild;
import org.xcsp.parser.XVariables.XVar;

/** A class with some utility (static) methods. */
public class XUtility {

	// prevents the creation of an instance of this class.
	private XUtility() {
	}

	public static Object[] specificArrayFrom(List<Object> list) {
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray() : list.toArray((Object[]) java.lang.reflect.Array.newInstance(clazz, list.size()));
	}

	public static Object[][] specificArray2DFrom(List<Object[]> list) {
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray(new Object[0][]) : list.toArray((Object[][]) java.lang.reflect.Array.newInstance(clazz, list.size()));
	}

	/** Method that controls that the specified condition is verified. If it is not the case, a message is displayed and the program is stopped. */
	public static Object control(boolean condition, String message) {
		if (!condition) {
			System.out.println(message);
			System.exit(1);
		}
		return null;
	}

	/**
	 * Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised. The specified boolean allows us
	 * to indicate if some special values (such as +infinity) must be checked.
	 */
	public static Long safeLong(String s, boolean checkSpecialValues) {
		if (checkSpecialValues) {
			if (s.equals(PLUS_INFINITY))
				return VAL_PLUS_INFINITY;
			if (s.equals(MINUS_INFINITY))
				return VAL_MINUS_INFINITY;
		}
		if (s.length() > 18) { // 18 because MAX_LONG and MIN_LONG are composed of at most 19 characters
			BigInteger big = new BigInteger(s);
			control(big.compareTo(BIG_MIN_SAFE_LONG) >= 0 && big.compareTo(BIG_MAX_SAFE_LONG) <= 0, "Too small or big value for this parser : " + s);
			return big.longValue();
		} else
			return Long.parseLong(s);
	}

	/** Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised. */
	public static Long safeLong(String s) {
		return safeLong(s, false);
	}

	/**
	 * Converts the specified long to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe according to the
	 * constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(long l, boolean useMargin) {
		control((useMargin ? XConstants.MIN_SAFE_INT : Integer.MIN_VALUE) <= l && l <= (useMargin ? XConstants.MAX_SAFE_INT : Integer.MAX_VALUE),
				"Too big integer value");
		return (int) l;
	}

	/**
	 * Converts the specified number to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe according to the
	 * constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(Number number, boolean useMargin) {
		return safeLong2Int(number.longValue(), useMargin);
	}

	/**
	 * Converts the specified long to int if it is safe to do it. Note that VAL_MINUS_INFINITY will be translated to VAL_MINUS_INFINITY_INT and that
	 * VAL_PLUS_INFINITY will be translated to VAL_PLUS_INFINITY_INT . When the specified boolean is set to true, we control that it is safe according to the
	 * constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2IntWhileHandlingInfinity(long l, boolean useMargin) {
		return l == XConstants.VAL_MINUS_INFINITY ? XConstants.VAL_MINUS_INFINITY_INT : l == XConstants.VAL_PLUS_INFINITY ? XConstants.VAL_PLUS_INFINITY_INT
				: safeLong2Int(l, true);
	}

	public static <T> T[] sort(T[] t) {
		Arrays.sort(t);
		return t;
	}

	/** Method that joins the elements of the specified array, using the specified delimiter to separate them. */
	public static String join(Object array, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, length = Array.getLength(array); i < length; i++) {
			Object item = Array.get(array, i);
			if (item != null && item.getClass().isArray())
				sb.append("[").append(join(item, delimiter)).append("]");
			else
				sb.append(item != null ? item.toString() : "null").append(i < length - 1 ? delimiter : "");
		}
		return sb.toString();
	}

	/** Method that joins the elements of the specified array, using a white-space as delimiter. */
	public static String join(Object array) {
		return join(array, " ");
	}

	/** Method that joins the elements of the specified map, using the specified separator and delimiter. */
	public static <K, V> String join(Map<K, V> m, String separator, String delimiter) {
		return m.entrySet().stream().map(e -> e.getKey() + separator + e.getValue()).reduce("", (n, p) -> n + (n.length() == 0 ? "" : delimiter) + p);
	}

	/** Method that joins the elements of the specified two-dimensional array, using the specified separator and delimiter. */
	public static String join(Object[][] m, String separator, String delimiter) {
		return Arrays.stream(m).map(t -> join(t, delimiter)).reduce("", (n, p) -> n + (n.length() == 0 ? "" : separator) + p);
	}

	/** Method for converting an array into a string. */
	public static String arrayToString(Object array, final char LEFT, final char RIGHT, final String SEP) {
		assert array.getClass().isArray();

		if (array instanceof boolean[])
			return Arrays.toString((boolean[]) array);
		if (array instanceof byte[])
			return Arrays.toString((byte[]) array);
		if (array instanceof short[])
			return Arrays.toString((short[]) array);
		if (array instanceof int[])
			return Arrays.toString((int[]) array);
		if (array instanceof long[])
			return Arrays.toString((long[]) array);
		if (array instanceof String[])
			return LEFT + String.join(SEP, (String[]) array) + RIGHT;
		if (array instanceof XVar[])
			return LEFT + String.join(SEP, Stream.of((XVar[]) array).map(x -> x.toString()).toArray(String[]::new)) + RIGHT;

		if (array instanceof boolean[][])
			return LEFT + String.join(SEP, Stream.of((boolean[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof byte[][])
			return LEFT + String.join(SEP, Stream.of((byte[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof short[][])
			return LEFT + String.join(SEP, Stream.of((short[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof int[][])
			return LEFT + String.join(SEP, Stream.of((int[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof long[][])
			return LEFT + String.join(SEP, Stream.of((long[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof String[][])
			return LEFT + String.join(SEP, Stream.of((String[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof boolean[][][])
			return LEFT + String.join(SEP, Stream.of((boolean[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof byte[][][])
			return LEFT + String.join(SEP, Stream.of((byte[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof short[][][])
			return LEFT + String.join(SEP, Stream.of((short[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof int[][][])
			return LEFT + String.join(SEP, Stream.of((int[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof long[][][])
			return LEFT + String.join(SEP, Stream.of((long[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof String[][][])
			return LEFT + String.join(SEP, Stream.of((String[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof Long[][])
			return LEFT + String.join(SEP, Stream.of((Long[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof XVar[][])
			return LEFT + String.join(SEP, Stream.of((XVar[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof Object[][])
			return LEFT + String.join(SEP, Stream.of((Object[][]) array).map(t -> arrayToString(t)).toArray(String[]::new)) + RIGHT;
		// return "(" + String.join(")(", Stream.of((Object[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
		if (array instanceof Object[])
			return String.join(SEP,
					Stream.of((Object[]) array).map(t -> t.getClass().isArray() ? LEFT + arrayToString(t) + RIGHT : t.toString()).toArray(String[]::new));
		return null;
	}

	/** Method for converting an array into a string. */
	public static String arrayToString(Object array) {
		return arrayToString(array, '[', ']', ", ");
	}

	/**
	 * Returns true if inside the specified object, there is an element that checks the predicate. If syntactic trees are encountered, we check the leaves only.
	 */
	public static boolean check(Object obj, Predicate<Object> p) {
		if (obj instanceof Object[])
			return IntStream.range(0, Array.getLength(obj)).anyMatch(i -> check(Array.get(obj, i), p));
		if (obj instanceof XNodeExpr)
			return ((XNodeExpr) obj).canFindLeafSuchThat(leaf -> p.test(leaf.value));
		return p.test(obj);
	}

	/** Collects the variables involved in the specified object, and add them to the specified set. */
	public static Set<XVar> collectVarsIn(Object obj, Set<XVar> set) {
		if (obj instanceof Object[])
			IntStream.range(0, Array.getLength(obj)).forEach(i -> collectVarsIn(Array.get(obj, i), set));
		else if (obj instanceof XNodeExpr) // possible if view
			((XNodeExpr) obj).collectVars(set);
		else if (obj instanceof XVar)
			set.add((XVar) obj);
		return set;
	}

	/** Method that loads an XML document, using the specified file name. */
	public static Document loadDocument(String fileName) throws Exception {
		if (fileName.endsWith("xml.bz2") || fileName.endsWith("xml.lzma")) {
			Process p = Runtime.getRuntime().exec((fileName.endsWith("xml.bz2") ? "bunzip2 -c " : "lzma -c -d ") + fileName);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.getInputStream());
			p.waitFor();
			return document;
		} else
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(fileName)));
	}

	/** Method that returns an array with the child elements of the specified element. */
	public static Element[] childElementsOf(Element element) {
		NodeList childs = element.getChildNodes();
		return IntStream.range(0, childs.getLength()).mapToObj(i -> childs.item(i)).filter(e -> e.getNodeType() == Node.ELEMENT_NODE).toArray(Element[]::new);
	}

	/** Method that returns an array with the child elements for the unique element of the specified document that has the specified tag name. */
	public static Element[] childElementsOf(Document document, String tagName) {
		assert document.getDocumentElement().getElementsByTagName(tagName).getLength() == 1;
		return childElementsOf((Element) document.getDocumentElement().getElementsByTagName(tagName).item(0));
	}

	public static int getIntValueOf(Element element, String attName, int defaultValue) {
		return element.getAttribute(attName).length() > 0 ? Integer.parseInt(element.getAttribute(attName)) : defaultValue;
	}

	/** Determines whether the specified element has the specified type as tag. */
	public static boolean isTag(Element elt, TypeChild type) {
		return elt.getTagName().equals(type.name());
	}
}
