parser grammar FlatzincExtParser;

options{
    language = Java;
    output=AST;
    tokenVocab=FlatzincExtLexer;
}

import FlatzincParser;

@header {
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

package parser.flatzinc;

}

flatzinc_ext_model
	:   (pred_decl)* (param_decl)* (var_decl)* (constraint)* (engine)? solve_goal
	;

engine
    :   ENGINE LP adt_decl (CM adt_decl)* SC group_decl (CM group_decl)* RP
    ->  ENGINE adt_decl+ group_decl+
    ;

adt_decl
    :   IDENTIFIER CL! adt_type
    |   MANY LP! adt_type RP!
    ;

adt_type
    :   QUEUE LP qiter (CM adt_decl)? RP
    ->  QUEUE qiter (adt_decl)?
    |   HEAP LP qiter (CM adt_decl)? RP
    ->  HEAP qiter (adt_decl)?
    |   LIST LP liter (CM adt_decl)? RP
    ->  LIST liter (adt_decl)?
    ;

qiter
    :   ONE
    |   WONE
    ;

liter
    :   qiter
    |   FOR
    |   WFOR
    ;

group_decl
    :   grp_decl (PL grp_decl)?
    ->  grp_decl+
    ;

grp_decl
    :   IDENTIFIER CL! grp_instr*
    ;

grp_instr
    :   FILTER LP! predicates RP!
    |   GROUPBY LP! attribute RP!
    |   ORDERBY LP! attribute RP!
    ;

predicates
    :   predicate
    |   AND LP predicates (CM predicates)* RP
    ->  AND predicates+
    |   OR LP predicates (CM predicates)* RP
    -> OR predicates+
    |   NOT LP! predicates RP!
    ;

predicate
    :   IN LP IDENTIFIER (CM IDENTIFIER)* RP
    ->  IN IDENTIFIER+
    |   IN RP! attribute op INT_CONST LP!
    ;

attribute
    :   VIDX
    |   VCARD
    |   CIDX
    |   CARITY
    |   PIDX
    |   PPRIO
    |   PARITY
    |   PPRIOD
    ;

op
    :   OEQ
    |   ONQ
    |   OLT
    |   OGT
    ;







