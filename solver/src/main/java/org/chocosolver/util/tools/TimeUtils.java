/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    /**
     * To convert milliseconds in nanoseconds
     */
    public static final long MILLISECONDS_IN_NANOSECONDS = 1000 * 1000;

    /**
     * Pattern of a duration: days, hours, minutes, milliseconds and seconds, in that order, each one optional.
     * Only seconds may be decimal.
     */
    private static final Pattern DURATION = Pattern.compile(
            "(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m(?!s))?(?:(\\d+)ms)?(?:(\\d+(?:\\.\\d+)?)s)?");

    /**
     * Pattern of a plain number of milliseconds
     */
    private static final Pattern PLAIN = Pattern.compile("\\d+");

    private TimeUtils() {
    }

    /**
     * Convert a string which represents a duration.
     * <p>
     * The accepted format is a sequence of the following components, each one optional, in this order
     * (blanks are ignored):
     * <ul>
     * <li>{@code <n>d}: days, {@code n} is a non-negative integer,</li>
     * <li>{@code <n>h}: hours, {@code n} is a non-negative integer,</li>
     * <li>{@code <n>m}: minutes, {@code n} is a non-negative integer,</li>
     * <li>{@code <n>ms}: milliseconds, {@code n} is a non-negative integer,</li>
     * <li>{@code <x>s}: seconds, {@code x} is a non-negative number, possibly decimal.</li>
     * </ul>
     * A string made of a single non-negative integer, with no unit, is a number of milliseconds.
     * Any other string is rejected, including decimal days, hours or minutes (e.g. "1.5m"),
     * other unit names (e.g. "2min") and components in a different order (e.g. "30s2m").
     * <p>
     * Examples:
     * <p>
     * - "1d2h3m4.5s": one day, two hours, three minutes, four seconds and 500 milliseconds<p/>
     * - "2h30m": two hours and 30 minutes<p/>
     * - "30.5s": 30 seconds and 500 ms<p/>
     * - "180s": three minutes<p/>
     * - "50ms" or "50": 50 milliseconds
     *
     * @param duration a String which describes the duration
     * @return the duration in milliseconds
     * @throws IllegalArgumentException if the string does not respect the format above
     */
    public static long convertInMilliseconds(String duration) {
        String d = duration.replaceAll("\\s+", "");
        if (PLAIN.matcher(d).matches()) {
            return Long.parseLong(d);
        }
        Matcher m = DURATION.matcher(d);
        if (d.isEmpty() || !m.matches()) {
            throw new IllegalArgumentException("Invalid duration \"" + duration + "\". Expected a sequence of "
                    + "<n>d<n>h<n>m<n>ms<x>s (in this order, each one optional, only seconds may be decimal), "
                    + "or a number of milliseconds, e.g. \"2h30m\", \"30.5s\", \"50ms\".");
        }
        long ms = 0;
        if (m.group(1) != null) ms += TimeUnit.DAYS.toMillis(Long.parseLong(m.group(1)));
        if (m.group(2) != null) ms += TimeUnit.HOURS.toMillis(Long.parseLong(m.group(2)));
        if (m.group(3) != null) ms += TimeUnit.MINUTES.toMillis(Long.parseLong(m.group(3)));
        if (m.group(4) != null) ms += Long.parseLong(m.group(4));
        if (m.group(5) != null) ms += Math.round(Double.parseDouble(m.group(5)) * 1000);
        return ms;
    }

    /**
     * Convert a string which represents a duration, see {@link #convertInMilliseconds(String)} for the format.
     *
     * @param duration a String which describes the duration
     * @return the duration in seconds (floored, so 30.5s returns 30)
     * @throws IllegalArgumentException if the string does not respect the format
     */
    public static long convertInSeconds(String duration) {
        return convertInMilliseconds(duration) / 1000;
    }
}
