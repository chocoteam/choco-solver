/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.handlers;

import org.chocosolver.solver.search.strategy.SearchParams;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Messages;
import org.kohsuke.args4j.spi.OneArgumentOptionHandler;
import org.kohsuke.args4j.spi.Setter;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/2020
 */
public class VarSelHandler extends OneArgumentOptionHandler<SearchParams.VarSelConf> {

    public VarSelHandler(CmdLineParser parser, OptionDef option, Setter<? super SearchParams.VarSelConf> setter) {
        super(parser, option, setter);
    }

    /**
     * Returns {@code "STRING[]"}.
     *
     * @return return "STRING[]";
     */
    @Override
    public String getDefaultMetaVariable() {
        return "[String,int]";
    }


    @Override
    protected SearchParams.VarSelConf parse(String argument) throws NumberFormatException, CmdLineException {
        if (argument.startsWith("[")) argument = argument.substring(1);
        if (argument.endsWith("]")) argument = argument.substring(0, argument.length() - 1);
        String[] pars = argument.split(",");
        if (pars.length == 2) {
            return new SearchParams.VarSelConf(
                    SearchParams.VariableSelection.valueOf(pars[0].toUpperCase()),
                    Integer.parseInt(pars[1])
            );
        }
        throw new CmdLineException(owner,
                Messages.ILLEGAL_UUID, argument);
    }
}
