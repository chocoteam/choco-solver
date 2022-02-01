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

import org.chocosolver.util.tools.StringUtils;

/**
 * A logger that supports colors (ANSI code).
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/05/2021
 */
public class ANSILogger extends Logger {

    private final StringBuilder pf = new StringBuilder();

    public ANSILogger() {
        super();
    }

    public ANSILogger(Logger aLogger) {
        super(aLogger);
    }

    @Override
    public Logger bold() {
        pf.append(StringUtils.ANSI_BOLD);
        return this;
    }

    @Override
    public Logger black() {
        pf.append(StringUtils.ANSI_BLACK);
        return this;
    }

    @Override
    public Logger white() {
        pf.append(StringUtils.ANSI_WHITE);
        return this;
    }

    @Override
    public Logger red() {
        pf.append(StringUtils.ANSI_RED);
        return this;
    }

    @Override
    public Logger green() {
        pf.append(StringUtils.ANSI_GREEN);
        return this;
    }

    @Override
    public Logger blue() {
        pf.append(StringUtils.ANSI_BLUE);
        return this;
    }

    @Override
    protected void prefix() {
        pstreams.forEach(p -> p.print(pf));
    }

    @Override
    protected void postfix() {
        pstreams.forEach(p -> p.print(StringUtils.ANSI_RESET));
        pf.setLength(0);
    }
}
