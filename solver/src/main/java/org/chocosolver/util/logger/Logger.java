/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.logger;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple logger framework, to deal with Model and Solver output.
 * <p>
 * This makes possible to add (or remove) {@link java.io.PrintStream} to print to,
 * in addition to {@code System.out}.
 * </p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/05/2021
 */
public class Logger {
    /**
     * List of {@link PrintStream} to print to.
     */
    protected final List<PrintStream> pstreams = new ArrayList<>();

    public Logger() {
        pstreams.add(System.out);
    }

    public Logger(Logger aLogger) {
        pstreams.addAll(aLogger.pstreams);
    }

    public final void add(PrintStream ps) {
        this.pstreams.add(ps);
    }

    public final void remove(PrintStream ps) {
        this.pstreams.remove(ps);
    }

    public Logger bold() {
        return this;
    }

    public Logger black() {
        return this;
    }

    public final void reset() {
        postfix();
    }

    public Logger white() {
        return this;
    }

    public Logger red() {
        return this;
    }

    public Logger green() {
        return this;
    }

    public Logger blue() {
        return this;
    }

    protected void prefix() {
    }

    protected void postfix() {
    }

    /**
     * Prints a string.
     *
     * @see PrintStream#print(String)
     */
    public final void print(String msg) {
        prefix();
        this.pstreams.forEach(p -> p.print(msg));
        postfix();
    }

    /**
     * Prints a string and then terminate the line.
     *
     * @see PrintStream#println(String)
     */
    public final void println(String msg) {
        prefix();
        this.pstreams.forEach(p -> p.println(msg));
        postfix();
    }

    /**
     * Prints a formatted string.
     *
     * @see PrintStream#printf(String, Object...)
     */
    public final void printf(String format, Object... args) {
        prefix();
        this.pstreams.forEach(p -> p.printf(format, args));
        postfix();
    }

    /**
     * Prints a formatted string.
     *
     * @see PrintStream#printf(Locale, String, Object...)
     */
    public final void printf(Locale l, String format, Object... args) {
        prefix();
        this.pstreams.forEach(p -> p.printf(l, format, args));
        postfix();
    }

}
