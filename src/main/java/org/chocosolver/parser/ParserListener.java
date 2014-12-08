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
package org.chocosolver.parser;

/**
 * A parser listener to ease user interaction.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version choco-parsers
 * @since 21/10/2014
 */
public interface ParserListener {

    /**
     * Actions to do before starting the parsing the parameters
     */
    void beforeParsingParameters();

    /**
     * Actions to do after ending the parsing  the parameters
     */
    void afterParsingParameters();

    /**
     * Actions to do before creating the solver
     */
    void beforeSolverCreation();

    /**
     * Actions to do after the solver is created
     */
    void afterSolverCreation();

    /**
     * Actions to do before starting the parsing the input file
     */
    void beforeParsingFile();

    /**
     * Actions to do after ending the parsing  the input file
     */
    void afterParsingFile();

    /**
     * Actions to do before configuring the search
     */
    void beforeConfiguringSearch();

    /**
     * Actions to do after ending the search configuration
     */
    void afterConfiguringSearch();

    /**
     * Actions to do before starting the resolution
     */
    void beforeSolving();

    /**
     * Actions to do after ending the resolution
     */
    void afterSolving();
}
