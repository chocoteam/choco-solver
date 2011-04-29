/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.common.util.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * see {@link solver.Configuration}.
 * @author Arnaud Malapert</br>
 * @since 29 avr. 2010 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public final class PropertyUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

	public static final String TOOLS_PREFIX = "tools.";

	private PropertyUtils() {
		super();
	}

	public static void logOnAbsence(String key) {
		LOGGER.debug("properties...[read-property-from-key:{0}][FAIL]", key);
	}

	public static void logOnFailure(String resource) {
		System.err.println("properties...[load-properties:"+resource+"][FAIL]");
	}

	public static void logOnSuccess(String resource) {
		LOGGER.debug("properties...[load-properties:{0}]", resource);
	}


	public static void loadProperties(Properties properties, File... files) {
		for (File file : files) {
			try {
				properties.load(new FileReader(file));
				logOnSuccess(file.getName());
			} catch (FileNotFoundException e) {
				logOnFailure(file.getName());
			} catch (IOException e) {
				logOnFailure(file.getName());
			}
		}
	}

	public static void loadProperties(Properties properties, String... resources) {
		for (String resource : resources) {
			try {
				properties.load(new InputStreamReader(properties.getClass().getResourceAsStream(resource), "ISO-8859-1"));
				logOnSuccess(resource);
			} catch (IOException e) {
				logOnFailure(resource);
			}
		}
	}

	public static boolean readBoolean(Properties properties, final String key, boolean defaultValue) {
		final String b = properties.getProperty(key);
		if( b == null ) {
			logOnAbsence(key);
			return defaultValue;
		} else return Boolean.parseBoolean(b);
	}

	public static int readInteger(Properties properties, final String key, int defaultValue) {
		final String b = properties.getProperty(key);
		if( b == null ) {
			logOnAbsence(key);
			return defaultValue;
		} else return Integer.parseInt(b);
	}

	public static double readDouble(Properties properties, final String key, double defaultValue) {
		final String b = properties.getProperty(key);
		if( b == null ) {
			logOnAbsence(key);
			return defaultValue;
		} else return Double.parseDouble(b);
	}

	public static String readString(Properties properties, final String key, String defaultValue) {
		final String b = properties.getProperty(key);
		if( b == null ) {
			logOnAbsence(key);
			return defaultValue;
		} else return b;
	}

	@SuppressWarnings("unchecked")
	public static   <T extends Enum<T>> T readEnum(Properties properties, final String key, T defaultValue) {
		final String b = properties.getProperty(key);
		if( b == null ) {
			logOnAbsence(key);
			return defaultValue;
		} else return (T) Enum.valueOf(defaultValue.getClass(), b);
	}
}
