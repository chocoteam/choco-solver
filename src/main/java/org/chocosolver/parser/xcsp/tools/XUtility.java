package org.chocosolver.parser.xcsp.tools;

import org.chocosolver.parser.xcsp.tools.XEnums.TypeChild;
import org.chocosolver.parser.xcsp.tools.XVariables.Var;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.chocosolver.parser.xcsp.tools.XConstants.*;

/**
 * A class with some utility (static) methods.
 */
public class XUtility {

    /**
     * Method that controls that the specified condition is verified. If it is not the case, a message is displayed and the program is stopped.
     */
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
            if (s.equals(P_INFINITY))
                return VAL_P_INFINITY;
            if (s.equals(M_INFINITY))
                return VAL_M_INFINITY;
        }
        if (s.length() > 18) { // 18 because MAX_LONG and MIN_LONG are composed of at most 19 characters
            BigInteger big = new BigInteger(s);
            control(big.compareTo(BIG_MIN_SAFE_LONG) >= 0 && big.compareTo(BIG_MAX_SAFE_LONG) <= 0, "Too small or big value for this parser : " + s);
            return big.longValue();
        } else
            return Long.parseLong(s);
    }

    /**
     * Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised.
     */
    public static Long safeLong(String s) {
        return safeLong(s, false);
    }

    public static <T> T[] sort(T[] t) {
        Arrays.sort(t);
        return t;
    }

    /**
     * Method that joins the elements of the specified array, using the specified delimiter to separate them.
     */
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

    /**
     * Method that joins the elements of the specified array, using a white-space as delimiter.
     */
    public static String join(Object array) {
        return join(array, " ");
    }

    /**
     * Method that joins the elements of the specified map, using the specified separator and delimiter.
     */
    public static <K, V> String join(Map<K, V> m, String separator, String delimiter) {
        return m.entrySet().stream().map(e -> e.getKey() + separator + e.getValue()).reduce("", (n, p) -> n + (n.length() == 0 ? "" : delimiter) + p);
    }

    /**
     * Method that joins the elements of the specified two-dimensional array, using the specified separator and delimiter.
     */
    public static String join(Object[][] m, String separator, String delimiter) {
        return Arrays.stream(m).map(t -> join(t, delimiter)).reduce("", (n, p) -> n + (n.length() == 0 ? "" : separator) + p);
    }

    private static String simplify(String s) {
        return s.substring(1, s.length() - 1);
    }

    /**
     * Method for converting an array into a string.
     */
    public static String arrayToString(Object array) {
        assert array.getClass().isArray();
        if (array instanceof byte[])
            return Arrays.toString((byte[]) array);
        if (array instanceof short[])
            return Arrays.toString((short[]) array);
        if (array instanceof int[])
            return Arrays.toString((int[]) array);
        if (array instanceof long[])
            return Arrays.toString((long[]) array);
        if (array instanceof byte[][])
            return "(" + String.join(")(", Stream.of((byte[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
        if (array instanceof short[][])
            return "(" + String.join(")(", Stream.of((short[][]) array).map(t -> Arrays.toString(t)).toArray(String[]::new)) + ")";
        if (array instanceof int[][])
            return "(" + String.join(")(", Stream.of((int[][]) array).map(t -> Arrays.toString(t)).toArray(String[]::new)) + ")";
        if (array instanceof long[][])
            return "(" + String.join(")(", Stream.of((long[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
        if (array instanceof Long[][])
            return "(" + String.join(")(", Stream.of((Long[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
        if (array instanceof String[][])
            return "(" + String.join(")(", Stream.of((String[][]) array).map(t -> String.join(",", t)).toArray(String[]::new)) + ")";
        if (array instanceof String[])
            return String.join(" ", (String[]) array);
        if (array instanceof Var[][])
            return "(" + String.join(")(", Stream.of((Var[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
        if (array instanceof Var[])
            return String.join(" ", Stream.of((Var[]) array).map(t -> t.toString()).toArray(String[]::new));
        if (array instanceof Object[][])
            return "(" + String.join(")(", Stream.of((Object[][]) array).map(t -> arrayToString(t)).toArray(String[]::new)) + ")";
        // return "(" + String.join(")(", Stream.of((Object[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) + ")";
        if (array instanceof Object[])
            return String.join(" ",
                    Stream.of((Object[]) array).map(t -> t.getClass().isArray() ? "(" + arrayToString(t) + ")" : t.toString()).toArray(String[]::new));
        return null;
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

    /**
     * Collects the variables involved in the specified object, and add them to the specified set.
     */
    public static Set<Var> collectVarsIn(Object obj, Set<Var> set) {
        if (obj instanceof Object[])
            IntStream.range(0, Array.getLength(obj)).forEach(i -> collectVarsIn(Array.get(obj, i), set));
        else if (obj instanceof XNodeExpr) // possible if view
            ((XNodeExpr) obj).collectVars(set);
        else if (obj instanceof Var)
            set.add((Var) obj);
        return set;
    }

    /**
     * Method that loads an XMl document, suing the specified file name.
     */
    public static Document load(String fileName) {
        try {
            if (fileName.endsWith("xml.bz2") || fileName.endsWith("xml.lzma")) {
                Process p = Runtime.getRuntime().exec((fileName.endsWith("xml.bz2") ? "bunzip2 -c " : "lzma -c -d ") + fileName);
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.getInputStream());
                p.waitFor();
                return document;
            } else
                return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(fileName)));
        } catch (Exception e) {
            System.out.println(e + "Problem with " + fileName);
            (e instanceof SAXException && ((SAXException) e).getException() != null ? ((SAXException) e).getException() : e).printStackTrace();
            return null;
        }
    }

    /**
     * Method that returns an array with the child elements of the specified element.
     */
    public static Element[] childElementsOf(Element element) {
        NodeList childs = element.getChildNodes();
        return IntStream.range(0, childs.getLength()).mapToObj(i -> childs.item(i)).filter(e -> e.getNodeType() == Node.ELEMENT_NODE).toArray(Element[]::new);
    }

    /**
     * Method that returns an array with the child elements for the unique element of the specified document that has the specified tag name.
     */
    public static Element[] childElementsOf(Document document, String tagName) {
        assert document.getDocumentElement().getElementsByTagName(tagName).getLength() == 1;
        return childElementsOf((Element) document.getDocumentElement().getElementsByTagName(tagName).item(0));
    }

    public static int getIntValueOf(Element element, String attName, int defaultValue) {
        return element.getAttribute(attName).length() > 0 ? Integer.parseInt(element.getAttribute(attName)) : defaultValue;
    }

    /**
     * Determines whether the specified element has the specified type as tag.
     */
    public static boolean isTag(Element elt, TypeChild type) {
        return elt.getTagName().equals(type.name());
    }
}
