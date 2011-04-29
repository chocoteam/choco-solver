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

package solver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;
import java.util.Properties;

import static choco.kernel.common.util.tools.PropertyUtils.*;

/**
 * An extension of <code>Properties</code> to define configuration for a <code>Solver</code>.
 * @author  Xavier Lorca
 * @author  Charles Prud'homme
 * @author  Arnaud Malapert
 * @version 0.01, june 2010
 * @since   0.01
 * @see solver.Solver
 */

public class Configuration extends Properties {


	private static final long serialVersionUID = 1L;

	protected static final String VALUE_TRUE = "true";
    protected static final String VALUE_FALSE = "false";
    protected static final String VALUE_OFF = "OFF";

     //////////////////////////////////////// ANNOTATION ////////////////////////////////////////////////////////////////

	/**
     * Annotation to define a default value for a field.
     */
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface Default {

    	String value();
    }

    //////////////////////////////////////// _DEFAULT KEYS //////////////////////////////////////////////////////////////
//    /**
//     * <br/><b>Goal</b>: do restart from root node after each solution.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String RESTART_AFTER_SOLUTION = "cp.restart.after_solution";
//
//    /**
//     * <br/><b>Goal</b>: To enable luby restart.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String RESTART_LUBY = "cp.restart.luby";
//
//    /**
//     * <br/><b>Goal</b>: To enable geometrical restart.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String RESTART_GEOMETRICAL = "cp.restart.geometrical";
//
//    /**
//     * <br/><b>Goal</b>: initial number of fails limiting the first search.
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 512
//     */
//    @Default(value = "512")
//    public static final String RESTART_BASE = "cp.restart.base";
//
//    /**
//     * <br/><b>Goal</b>: geometrical factor for restart strategy
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 2
//     */
//    @Default(value = "2")
//    public static final String RESTART_LUBY_GROW = "cp.restart.luby.grow";
//
//    /**
//     * <br/><b>Goal</b>: geometrical factor for restart strategy
//     * <br/><b>Type</b>: double
//     * <br/><b>Default value</b>: 1.2
//     */
//    @Default(value = "1.2")
//    public static final String RESTART_GEOM_GROW = "cp.restart.geometrical.grow";
//
//    /**
//     * <br/><b>Goal</b>: Restart Policy limit type. If the limit bound defined by the policy (Luby, Geom) is reached, the search is restarted.
//     * <br/><b>Type</b>: Limit
//     * <br/><b>Default value</b>: BACKTRACK
//     */
//    @Default(value = "BACKTRACK")
//    public static final String RESTART_POLICY_LIMIT = "cp.restart.policy.limit.type";
//
//    /**
//     * <br/><b>Goal</b>: Enable nogood recording from restart.
//     * <br/><b>Type</b>: boolean
//     * <br/> @see Nogood Recording from Restarts. Lecoutre, C.; Sais, L.; Tabary, S. & Vidal, <br>
//	 * IJCAI 2007 Proceedings of the 20th International Joint Conference on Artificial Intelligence, Hyderabad, India, January 6-12, 2007, 2007, 131-136
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String NOGOOD_RECORDING_FROM_RESTART = "cp.restart.nogood_recording";
//
//    /**
//     * <br/><b>Goal</b>: Tells the strategy wether or not use recomputation.
//     * The value of the parameter indicates the maximum recomputation gap, i.e. the maximum number of decisions between two storages.
//     * If the parameter is lower than or equal to 1, the trailing storage mechanism is used (default).
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 1
//     */
//    @Default(value = "1")
//    public static final String RECOMPUTATION_GAP = "cp.recomputation.gap";
//
//    /**
//     * <br/><b>Goal</b>: Enable card reasonning: decide if redundant constraints are automatically added
//     * to the model to reason on cardinalities on sets as well as kernel and enveloppe.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: true
//     */
//    @Default(value = VALUE_TRUE)
//    public static final String CARD_REASONNING = "cp.propagation.cardinality_reasonning";
//
    /**
     * <br/><b>Goal</b>: Initial seed to generate streams of pseudorandom numbers
     * <br/><b>Type</b>: int
     * <br/><b>Default value</b>: 0
     */
    @Default(value = "0")
    public static final String RANDOM_SEED = "cp.random.seed";
//
//    /**
//     * <br/><b>Goal</b>: Search limit type. If the search has not ended in the define limit bound, it is automatically stopped.
//     * <br/><b>Type</b>: Limit
//     * <br/><b>Default value</b>: UNDEF
//     */
//    @Default(value = "UNDEF")
//    public static final String SEARCH_LIMIT = "cp.search.limit.type";
//
//    /**
//     * <br/><b>Goal</b>: Search limit bound. If the search has not ended in the define search limit bound,
//     * it is automatically stopped.
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 2147483647 ({@link Integer.MAX_VALUE})
//     */
//    @Default(value = "2147483647")
//    public static final String SEARCH_LIMIT_BOUND = "cp.search.limit.value";
//
//    /**
//     * <br/><b>Goal</b>: Restart limit type. If the limit bound is reached, the search stops restarting.
//     * <br/><b>Type</b>: Limit
//     * <br/><b>Default value</b>: UNDEF
//     */
//    @Default(value = "UNDEF")
//    public static final String RESTART_LIMIT = "cp.restart.limit.type";
//
//    /**
//     * <br/><b>Goal</b>: Restart limit bound. If the limit bound is reached, the search stops restarting.
//     * it is automatically stopped.
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 2147483647 ({@link Integer.MAX_VALUE})
//     */
//    @Default(value = "2147483647")
//    public static final String RESTART_LIMIT_BOUND = "cp.restart.limit.value";
//
//    /**
//     * <br/><b>Goal</b>: Enforce the use of shaving before starting the search.
//     * <br/><i>The shaving mechanism is related to singloton consistency</i>.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String INIT_SHAVING = "cp.init.propagation.shaving";
//
//    /**
//     * <br/><b>Goal</b>: Compute a destructive lower bound before starting the search (optimization).
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String INIT_DESTRUCTIVE_LOWER_BOUND = "cp.init.propagation.dLB";
//
//    /**
//     * <br/><b>Goal</b>: Apply shaving while computing the destructive lower bound.
//     * <br/><i> For each hypothetical upper bound, the consistency test applies shaving</i>.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String INIT_DLB_SHAVING = "cp.init.propagation.shaving.dLB";
//
//
//    /**
//     * <br/><b>Goal</b>: perform shaving only on decision vars.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: true
//     */
//    @Default(value = VALUE_TRUE)
//    public static final String INIT_SHAVE_ONLY_DECISIONS = "cp.init.propagation.shaving.only_decision_vars";
//
//    /**
//     * <br/><b>Goal</b>: Apply a bottom-up search algorithm (optimization).
//     * <br/><i> The top-down strategy (default) starts with a upper bound and tries to improve it.</i>
//     * <br/><i> The bottom-up starts with a lower bound as target upper bound which is incremented by one unit until the
//     * problem becomes feasible.</i>.
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: false
//     */
//    @Default(value = VALUE_FALSE)
//    public static final String BOTTOM_UP = "cp.search.bottom_up";
//
//    /**
//     * <br/><b>Goal</b>:
//     * <br/><b>Type</b>: int
//     * <br/><b>Default value</b>: 21474836 ({@link choco.Choco.MAX_UPPER_BOUND})
//     */
//    @Default(value = "21474836")
//    public static final String HORIZON_UPPER_BOUND = "cp.scheduling.horizon";
//
    /**
     * <br/><b>Goal</b>: Solution pool capacity, number of solutions to store within the solutions' pool.
     * <br/><b>Type</b>: int
     * <br/><b>Default value</b>: 1
     */
    @Default(value = "0")
    public static final String SOLUTION_POOL_CAPACITY = "cp.solution.pool_capacity";

//    /**
//     * <br/><b>Goal</b>:
//     * <br/><b>Type</b>: boolean
//     * <br/><b>Default value</b>: true
//     */
//    @Default(value = VALUE_TRUE)
//    public static final String RESTORE_BEST_SOLUTION = "cp.solution.restore";
//
//    /**
//     * <br/><b>Goal</b>: Precision of the search for problem involving real variables
//     * <br/><b>Type</b>: double
//     * <br/><b>Default value</b>: 1.0e-6
//     */
//    @Default(value = "1.0e-6")
//    public final static String REAL_PRECISION = "cp.real.precision";
//
//    /**
//     * <br/><b>Goal</b>: Minimal width reduction between two propagations, for problem involving real variables
//     * <br/><b>Type</b>: double
//     * <br/><b>Default value</b>: 0.99
//     */
//    @Default(value = "0.99")
//    public final static String REAL_REDUCTION = "cp.real.reduction";
//
//    /**
//     * <br/><b>Goal</b>: The ratio of holes within domains to which decision are performed to switch from BC to AC
//     * <br/><b>Type</b>: double
//     * <br/><b>Default value</b>: 0.7f
//     */
//    @Default(value = "0.7f")
//    public static final String RATION_HOLE = "cp.domain.rationHole";


    /**
     * Creates an empty property list with loaded default values.
     */
    public Configuration() {
    	super(new Properties());
    	load(defaults);
    }


    /**
     * Creates an empty property list with the specified defaults.
     *
     * @param defaults the defaults.
     */
    public Configuration(final Properties defaults) {
    	super(defaults);
    }

    /**
     * Set default configuration.
     * First clear then load default values for defined keys.
     */
    public void setDefault() {
        this.clear();
        load(this);
    }


    /**
     * Load the default value of keys defined in @Default annotation
     *
     * @throws IllegalAccessException if the specified object is not an
     *                                instance of the class or interface declaring the underlying
     */
    private void load(Properties properties) {
    	Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
            Default annotation = f.getAnnotation(Default.class);
            try {
                properties.put(f.get(this), annotation.value());
            } catch (IllegalAccessException e) {
                logOnFailure(f.getName());
            }
        }
        logOnSuccess("default");
    }

    /**
     * Load the default value of keys defined in @Default annotation
     *
     * @throws IllegalAccessException if the specified object is not an
     *                                instance of the class or interface declaring the underlying
     */
    public String loadDefault(String key){
        Field[] fields = Configuration.class.getFields();
        for (Field f : fields) {
            try {
                if(f.get(this).equals(key)){
                    Default ann = f.getAnnotation(Default.class);
                    return ann.value();
                }
            } catch (IllegalAccessException e) {
                logOnFailure(key);
            }
        }
        throw new NullPointerException("cant find ");
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a boolean.
     */
    public boolean readBoolean(String key) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            value = loadDefault(key);
        }
        return Boolean.valueOf(value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public boolean readBoolean(final String key, boolean defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else{
            return Boolean.parseBoolean(b);
        }
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public int readInt(String key) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return Integer.valueOf(value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public int readInt(final String key, int defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else{
            return Integer.parseInt(b);
        }
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     */
    public File readFile(String key) {
        final String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return new File(value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     *
     */
    public File readFile(final String key, File defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else{
            return new File(b);
        }
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a long.
     */
    public long readLong(String key) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return Long.parseLong(value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a long.
     */
    public long readLong(final String key, long defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else{
            return Long.parseLong(b);
        }
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a double.
     */
    public double readDouble(String key) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return Double.valueOf(value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public double readDouble(final String key, double defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else{
            return Double.parseDouble(b);
        }
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public String readString(final String key) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return value;
    }


    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as an integer.
     */
    public String readString(final String key, String defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else return b;
    }

    /**
     * Returns the value to which the specified key is mapped.
     *
     * @param key   the key whose associated value is to be returned
     * @param clazz the class of the enum expected
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a boolean.
     */
    @SuppressWarnings({"unchecked"})
    public <T extends Enum<T>> T readEnum(String key, Class clazz) {
        String value = this.getProperty(key);
        if (value == null) {
            logOnAbsence(key);
            throw new NullPointerException("cant find ");
        }
        return (T) Enum.valueOf(clazz, value);
    }

    /**
     * Returns the value to which the specified key is mapped, if exists.
     * Otherwise, return default value.
     *
     * @param key          the key whose associated value is to be returned
     * @param defaultValue value to return the key does not exist in the configuration
     * @return the value to which the specified key is mapped
     * @throws NullPointerException  if the specified key is null
     * @throws NumberFormatException if the value cannot be parsed
     *                               as a boolean.
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T readEnum(final String key, T defaultValue) {
        final String b = this.getProperty(key);
        if (b == null) {
            logOnAbsence(key);
            return defaultValue;
        } else return (T) Enum.valueOf(defaultValue.getClass(), b);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putInt(String key, int value) {
        this.put(key, Integer.toString(value));
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putLong(String key, long value) {
        this.put(key, Long.toString(value));
    }
    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putDouble(String key, double value) {
        this.put(key, Double.toString(value));
    }

    public void putTrue(String key) {
    	putBoolean(key, true);
    }

    public void putFalse(String key) {
    	putBoolean(key, false);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putBoolean(String key, boolean value) {
        this.put(key, Boolean.toString(value));
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putEnum(String key, Enum value) {
        this.put(key, value.name());
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>. <p>
     * *
     *
     * @param key   the hashtable key
     * @param value the value
     * @throws NullPointerException if the key or value is
     *                              <code>null</code>
     */
    public void putFile(String key, File value) {
        this.put(key, value.getAbsolutePath());
    }


	public void storeDefault(File file, String comments) throws IOException {
		if(defaults != null) defaults.store(new FileWriter(file), comments);
	}

}
