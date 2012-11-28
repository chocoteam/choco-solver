/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package solver;

/**
 * Global settings
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/08/12
 */
public enum Configuration {
    ;
    // Set to true plugged explanation engine -- enable total deconnection from variable
    public static final boolean PLUG_EXPLANATION = false;

    // Set to true to print propagation information
    public static final boolean PRINT_PROPAGATION = false;

    // Set to true to print event occurring on variables
    public static final boolean PRINT_VAR_EVENT = false;

    // Set to true to print scheduling information
    public static final boolean PRINT_SCHEDULE = false;

    // Set to true to print contradiction information
    public static final boolean PRINT_CONTRADICTION = false;

    // Set to true to activate lazy update of deltas and generators
    public static final boolean LAZY_UPDATE = true;

    // Set to true to retain the variable last of a decision involved into a fail
    public static final boolean STORE_LAST_DECISION_VAR = true;
}
