/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    /** To convert milliseconds in nanoseconds */
    public static final long MILLISECONDS_IN_NANOSECONDS = 1000 * 1000;

    /** Pattern for days */
    private static final Pattern Dp = Pattern.compile("(\\d+)d");

    /** Pattern for hours */
    private static final Pattern Hp = Pattern.compile("(\\d+)h");

    /** Pattern for minutes*/
    private static final Pattern Mp = Pattern.compile("(\\d+)m");

    /** Pattern for seconds */
    private static final Pattern Sp = Pattern.compile("(\\d+(\\.\\d+)?)s");

    private TimeUtils() {}

    /**
     * Convert a string which represents a duration. It can be composed of days, hours, minutes and seconds.
     * Examples:
     * <p>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes
     *
     * @param duration a String which describes the duration
     * @return the duration in milliseconds
     */
    public static long convertInMilliseconds(String duration) {
        long milliseconds = 0;
        String duration0 = duration.replaceAll("\\s+", "");
        Matcher matcher = Dp.matcher(duration0);
        if (matcher.find() && matcher.groupCount() == 1) {
            int days = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        }
        matcher = Hp.matcher(duration0);
        if (matcher.find() && matcher.groupCount() == 1) {
            int hours = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(hours, TimeUnit.HOURS);
        }
        matcher = Mp.matcher(duration0);
        if (matcher.find() && matcher.groupCount() == 1) {
            int minutes = Integer.parseInt(matcher.group(1));
            milliseconds += TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
        }
        matcher = Sp.matcher(duration0);
        if (matcher.find() && matcher.groupCount() == 2) {
            double seconds = Double.parseDouble(matcher.group(1));
            milliseconds += (int) (seconds * 1000);
        }
        if (milliseconds == 0) {
            milliseconds = Long.parseLong(duration0);
        }
        return milliseconds;
    }

    /**
     * Convert a string which represents a duration. It can be composed of days, hours, minutes and seconds.
     * Examples:
     * <p>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes
     *
     * @param duration a String which describes the duration
     * @return the duration in seconds (floored, so 30.5s returns 30)
     */
    public static long convertInSeconds(String duration) {
        return convertInMilliseconds(duration)/1000;
    }
}
