/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.xcsp.parser;

import java.math.BigInteger;

public interface XConstants {

	// For each primitive type, we can safely use all values except the extreme ones (as defined by SAFETY_MARGIN)
	// so as to be able to use special values (for example, for representing +infinity and -infinity)
	int SAFETY_MARGIN = 10;
	long MIN_SAFE_BYTE = Byte.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_BYTE = Byte.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_SHORT = Short.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_SHORT = Short.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_INT = Integer.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_INT = Integer.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_LONG = Long.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_LONG = Long.MAX_VALUE - SAFETY_MARGIN;
	BigInteger BIG_MIN_SAFE_LONG = BigInteger.valueOf(MIN_SAFE_LONG), BIG_MAX_SAFE_LONG = BigInteger.valueOf(MAX_SAFE_LONG);

	String MINUS_INFINITY = "-infinity";
	String PLUS_INFINITY = "+infinity";
	long VAL_MINUS_INFINITY = Long.MIN_VALUE;
	long VAL_PLUS_INFINITY = Long.MAX_VALUE;
	int VAL_MINUS_INFINITY_INT = Integer.MIN_VALUE;
	int VAL_PLUS_INFINITY_INT = Integer.MAX_VALUE;

	// We use the maximum value of each primitive type, minus 1, to denote STAR (related to the concept of short tuples)
	byte STAR_BYTE = Byte.MAX_VALUE - 1;
	short STAR_SHORT = Short.MAX_VALUE - 1;
	int STAR_INT = Integer.MAX_VALUE - 1;
	long STAR = Long.MAX_VALUE - 1;

	/** We use the minimum long value, plus 1, to denote that a value is outside bounds (e.g., of a domain) */
	long OUTSIDE_BOUNDS = Long.MIN_VALUE + 1;

	// Constants used for some first-level elements of the instances
	String INSTANCE = "instance";
	String VARIABLES = "variables";
	String VAR = "var";
	String ARRAY = "array";
	String DOMAIN = "domain";
	String REQUIRED = "required";
	String POSSIBLE = "possible";
	String CONSTRAINTS = "constraints";
	String BLOCK = "block";
	String GROUP = "group";
	String OBJECTIVES = "objectives";
	String OBJECTIVE = "objective";
	String MINIMIZE = "minimize";
	String MAXIMIZE = "maximize";

	/** A regex for denoting delimiters used in lists (elements separated by commas and surrounded by parentheses) */
	String DELIMITER_LISTS = "\\s*\\)\\s*\\(\\s*|\\s*\\(\\s*|\\s*\\)\\s*";

	/** A regex for denoting delimiters used in sets (elements separated by a comma and surrounded by brace brackets) */
	String DELIMITER_SETS = "\\s*\\}\\s*\\{\\s*|\\s*\\{\\s*|\\s*\\}\\s*";

	/** A regex for denoting delimiters used in msets (elements separated by a comma and surrounded by double brace brackets) */
	String DELIMITER_MSETS = "\\s*\\}\\}\\s*\\{\\{\\s*|\\s*\\{\\{\\s*|\\s*\\}\\}\\s*";
}
