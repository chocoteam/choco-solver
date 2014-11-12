/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package solver.search.bind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.search.bind.nop.NOPISearchBinder;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.ClassLoader.getSystemResources;
import static java.util.Arrays.asList;
import static solver.Configuration.SEARCH_BINDER_PATH;
import static solver.search.bind.impl.StaticBinder.REQUESTED_API_VERSION;
import static solver.search.bind.impl.StaticBinder.getSingleton;

/**
 * A binder factory that provides default strategies for Solver.
 * Deeply inspired from SLF4J implementation.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco
 * @since 23/10/14
 */
public class SearchBinderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchBinderFactory.class);

    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;

    static String CLASSPATH = SEARCH_BINDER_PATH;

    static int INITIALIZATION_STATE = UNINITIALIZED;

    static NOPISearchBinder NOP_FALLBACK_FACTORY = new NOPISearchBinder();

    /**
     * It is BinderFactory's responsibility to track version changes and manage
     * the compatibility list.
     * <p/>
     */
    static private final String[] API_COMPATIBILITY_LIST = new String[]{"3.2.2"};


    private static void performInitialization() {
        bind();
        if (INITIALIZATION_STATE == SUCCESSFUL_INITIALIZATION) {
            versionSanityCheck();
        }
    }

    private static void bind() {
        try {
            Set<URL> staticLoggerBinderPathSet = findPossibleStaticLoggerBinderPathSet();
            reportMultipleBindingAmbiguity(staticLoggerBinderPathSet);
            // the next line does the binding
            getSingleton();
            INITIALIZATION_STATE = SUCCESSFUL_INITIALIZATION;
            reportActualBinding(staticLoggerBinderPathSet);
        } catch (ExceptionInInitializerError ncde) {
            INITIALIZATION_STATE = NOP_FALLBACK_INITIALIZATION;
            LOGGER.warn("Failed to load class \"solver.search.bind.impl.StaticBinder\".");
            LOGGER.warn("Defaulting to no-operation (NOP) binder implementation");
        } catch (Exception e) {
            failedBinding(e);
            throw new IllegalStateException("Unexpected initialization failure", e);
        }
    }

    static void failedBinding(Throwable t) {
        INITIALIZATION_STATE = FAILED_INITIALIZATION;
        LOGGER.error("Failed to instantiate choco-solver search binder", t);
    }


    private static void versionSanityCheck() {
        try {
            String requested = REQUESTED_API_VERSION;

            boolean match = false;
            for (int i = 0; i < API_COMPATIBILITY_LIST.length; i++) {
                if (requested.startsWith(API_COMPATIBILITY_LIST[i])) {
                    match = true;
                }
            }
            if (!match) {
                LOGGER.warn("The requested version " + requested
                        + " by your choco-solver binding is not compatible with "
                        + asList(API_COMPATIBILITY_LIST).toString());
            }
        } catch (NoSuchFieldError ignored) {
        } catch (Throwable e) {
            LOGGER.error("Unexpected problem occurred during version sanity check", e);
        }
    }

    private static Set<URL> findPossibleStaticLoggerBinderPathSet() {
        // LinkedHashSet appropriate here because it preserves insertion order during iteration
        Set<URL> staticLoggerBinderPathSet = new LinkedHashSet<>();
        try {
            ClassLoader loggerFactoryClassLoader = SearchBinderFactory.class
                    .getClassLoader();
            Enumeration<URL> paths;
            if (loggerFactoryClassLoader == null) {
                paths = getSystemResources(CLASSPATH);
            } else {
                paths = loggerFactoryClassLoader
                        .getResources(CLASSPATH);
            }
            while (paths.hasMoreElements()) {
                URL path = paths.nextElement();
                staticLoggerBinderPathSet.add(path);
            }
        } catch (IOException ioe) {
            LOGGER.warn("Error getting resources from path", ioe);
        }
        return staticLoggerBinderPathSet;
    }

    private static boolean isAmbiguousStaticLoggerBinderPathSet(Set<URL> staticLoggerBinderPathSet) {
        return staticLoggerBinderPathSet.size() > 1;
    }

    /**
     * Prints a warning message on the console if multiple bindings were found on the class path.
     * No reporting is done otherwise.
     */
    private static void reportMultipleBindingAmbiguity(Set<URL> staticLoggerBinderPathSet) {
        if (isAmbiguousStaticLoggerBinderPathSet(staticLoggerBinderPathSet)) {
            LOGGER.warn("Class path contains multiple bindings.");
            for (URL path : staticLoggerBinderPathSet) {
                LOGGER.warn("Found binding in [" + path + "]");
            }
        }
    }

    private static void reportActualBinding(Set<URL> staticLoggerBinderPathSet) {
        if (isAmbiguousStaticLoggerBinderPathSet(staticLoggerBinderPathSet)) {
            LOGGER.warn("Actual binding is of type ["
                    + getSingleton().getBinder().getClass().getName() + "]");
        }
    }

    /**
     * Return the {@link solver.search.bind.ISearchBinder} instance in use.
     * <p/>
     * <p/>
     * ILoggerFactory instance is bound with this class at compile time.
     *
     * @return the ILoggerFactory instance in use
     */
    public static ISearchBinder getSearchBinder() {
        if (INITIALIZATION_STATE == UNINITIALIZED) {
            INITIALIZATION_STATE = ONGOING_INITIALIZATION;
            performInitialization();
        }
        switch (INITIALIZATION_STATE) {
            case SUCCESSFUL_INITIALIZATION:
                return getSingleton().getBinder();
            case NOP_FALLBACK_INITIALIZATION:
                return NOP_FALLBACK_FACTORY;
            case FAILED_INITIALIZATION:
            case ONGOING_INITIALIZATION:
                throw new IllegalStateException("UNSUCCESSFUL_INIT_MSG");
        }
        throw new IllegalStateException("Unreachable code");
    }

    /**
     * Return the {@link solver.search.bind.ISearchBinder} instance in use.
     * <p/>
     * <p/>
     * ILoggerFactory instance is bound with this class at compile time.
     * Use the default classpath, set in {@link solver.Configuration#SEARCH_BINDER_PATH}.
     */
    public static void setSearchBinderClasspath(String CLASSPATH) {
        SearchBinderFactory.INITIALIZATION_STATE = UNINITIALIZED;
        SearchBinderFactory.CLASSPATH = CLASSPATH;
    }

    /**
     * Reset the default search binder to default one
     */
    public static void reset() {
        SearchBinderFactory.INITIALIZATION_STATE = UNINITIALIZED;
        SearchBinderFactory.CLASSPATH = SEARCH_BINDER_PATH;
    }
}
