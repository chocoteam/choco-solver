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
package parser.flatzinc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/13
 */
public class ParserConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ParserConfiguration.class);

    private static final String UDPATH = "/myparser.properties";

    private static final String PATH = "/parser.properties";

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(ParserConfiguration.class.getResourceAsStream(PATH));
        } catch (Exception e) {
            logger.error("Unable to load " + PATH + " file from classpath.", e);
            System.exit(1);
        }
        // then override values, if any
        try {
            properties.load(ParserConfiguration.class.getResourceAsStream(UDPATH));
        } catch (NullPointerException e) {
            //            logger.warn("No user defined properties. Skip loading " + UDPATH + " file.");
        } catch (Exception e) {
            logger.error("Unable to load " + UDPATH + " file from classpath.", e);
        }
    }

    // Set to true to print constraint creation during parsing
    public static final boolean PRINT_CONSTRAINT = Boolean.parseBoolean(properties.getProperty("PRINT_CONSTRAINT"));

    // Set to true to print scheduling information
    public static final boolean ENABLE_CLAUSE = Boolean.parseBoolean(properties.getProperty("ENABLE_CLAUSE"));

    // Set to true to log the resolution trace
    public static final boolean PRINT_SEARCH = Boolean.parseBoolean(properties.getProperty("PRINT_SEARCH"));
}
