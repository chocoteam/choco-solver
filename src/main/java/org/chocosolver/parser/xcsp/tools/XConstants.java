package org.chocosolver.parser.xcsp.tools;

import java.math.BigInteger;

public class XConstants {

	// For each primitive type, we can safely use all values except the extreme ones (as defined by SAFETY_MARGIN)
	// so as to be able to use special values (for example, for representing + and - infinity)
	private static final int SAFETY_MARGIN = 5;
	public static final long MIN_SAFE_BYTE = Byte.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_BYTE = Byte.MAX_VALUE - SAFETY_MARGIN;
	public static final long MIN_SAFE_SHORT = Short.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_SHORT = Short.MAX_VALUE - SAFETY_MARGIN;
	public static final long MIN_SAFE_INT = Integer.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_INT = Integer.MAX_VALUE - SAFETY_MARGIN;
	public static final long MIN_SAFE_LONG = Long.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_LONG = Long.MAX_VALUE - SAFETY_MARGIN;
	public static final BigInteger BIG_MIN_SAFE_LONG = BigInteger.valueOf(MIN_SAFE_LONG), BIG_MAX_SAFE_LONG = BigInteger.valueOf(MAX_SAFE_LONG);

	public static final String P_INFINITY = "+infinity";
	public static final String M_INFINITY = "-infinity";
	public static final long VAL_P_INFINITY = Long.MAX_VALUE;
	public static final long VAL_M_INFINITY = Long.MIN_VALUE;

	// We use the maximum value of each primitive type, minus 1, to denote STAR (for short tuples)
	public static final byte STAR_BYTE = Byte.MAX_VALUE - 1;
	public static final short STAR_SHORT = Short.MAX_VALUE - 1;
	public static final int STAR_INT = Integer.MAX_VALUE - 1;
	public static final long STAR = Long.MAX_VALUE - 1;

	/** We use the minimum long value, plus 1, to denote that a value is outside bounds (e.g., of a domain) */
	public static final long OUTSIDE_BOUNDS = Long.MIN_VALUE + 1;

	// Constants used for some first-level elements of the instances
	public static final String INSTANCE = "instance";
	public static final String VARIABLES = "variables";
	public static final String VAR = "var";
	public static final String ARRAY = "array";
	public static final String DOMAIN = "domain";
	public static final String REQUIRED = "required";
	public static final String POSSIBLE = "possible";
	public static final String CONSTRAINTS = "constraints";
	public static final String BLOC = "bloc";
	public static final String GROUP = "group";
	public static final String OBJECTIVES = "objectives";
	public static final String OBJECTIVE = "objective";
	public static final String MINIMIZE = "minimize";
	public static final String MAXIMIZE = "maximize";

	/** A regex for denoting delimiters used in lists (elements separated by commas and surrounded by parentheses) */
	public static final String DELIMITER_LISTS = "\\s*\\)\\s*\\(\\s*|\\s*\\(\\s*|\\s*\\)\\s*";

	/** A regex for denoting delimiters used in sets (elements separated by a comma and surrounded by brace brackets) */
	public static final String DELIMITER_SETS = "\\s*\\}\\s*\\{\\s*|\\s*\\{\\s*|\\s*\\}\\s*";

	/** A regex for denoting delimiters used in msets (elements separated by a comma and surrounded by double brace brackets) */
	public static final String DELIMITER_MSETS = "\\s*\\}\\}\\s*\\{\\{\\s*|\\s*\\{\\{\\s*|\\s*\\}\\}\\s*";
}
