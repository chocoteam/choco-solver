/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.solver.search.strategy.Search;
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
public class RestartHandler extends OneArgumentOptionHandler<ParserParameters.ResConf> {

    public RestartHandler(CmdLineParser parser, OptionDef option, Setter<? super ParserParameters.ResConf> setter) {
        super(parser, option, setter);
    }

    /**
     * Returns {@code "STRING[]"}.
     *
     * @return return "STRING[]";
     */
    @Override
    public String getDefaultMetaVariable() {
        return "[String,int,double?,int]";
    }


    @Override
    protected ParserParameters.ResConf parse(String argument) throws NumberFormatException, CmdLineException {
        if (argument.startsWith("[")) argument = argument.substring(1);
        if (argument.endsWith("]")) argument = argument.substring(0, argument.length() - 1);
        String[] pars = argument.split(",");
        switch (pars.length) {
            case 3:
                return new ParserParameters.ResConf(
                        Search.Restarts.valueOf(pars[0].toUpperCase()),
                        Integer.parseInt(pars[1]),
                        Integer.parseInt(pars[2])
                );
            case 4:
                return new ParserParameters.ResConf(
                        Search.Restarts.valueOf(pars[0].toUpperCase()),
                        Integer.parseInt(pars[1]),
                        Double.parseDouble(pars[2]),
                        Integer.parseInt(pars[3])
                );
            default:
                throw new CmdLineException(owner,
                        Messages.ILLEGAL_UUID, argument);
        }
    }
}
