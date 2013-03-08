tree grammar FlatzincExtWalker;

options {
  tokenVocab=FlatzincExtParser;
  ASTLabelType=CommonTree;
}

import FlatzincExtParser;

@header{
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.THashMap;
import gnu.trove.list.array.TIntArrayList;

import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import parser.flatzinc.FZNException;
import parser.flatzinc.FZNLayout;
import parser.flatzinc.ast.FConstraint;
import parser.flatzinc.ast.FGoal;
import parser.flatzinc.ast.FParameter;
import parser.flatzinc.ast.FVariable;

import solver.Solver;
import solver.constraints.Constraint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
}

@members{
// The flatzinc logger -- 'System.out/err' is fobidden!
protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

// maintains map between name and objects
public THashMap<String, Object> map;

// search for all solutions
public boolean all = false;
// free search strategy
public boolean free = false;

// the solver
public Solver mSolver;

// the layout dedicated to pretty print message wrt to fzn recommendations
public final FZNLayout mLayout = new FZNLayout();


public enum OPERATOR{EQ,NQ,LT,GT}

public enum ATTRIBUTE{VIDX,VCARD,CIDX,CARITY,PIDX,PPRIO,PARITY,PPRIOD}
}


flatzinc_model [Solver aSolver, THashMap<String, Object> map]
	:
	{
	this.mSolver = aSolver;
	this.map = map;
    }
	   (pred_decl)* (param_decl)* (var_decl)* (constraint)* (engine)? solve_goal
	{
	if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
        mLayout.setSearchLoop(mSolver.getSearchLoop());
    }
	}
	;

engine
    :   ENGINE adt_decl+ group_decl+
    ;

adt_decl
    :   IDENTIFIER adt_type
    |   MANY adt_type
    ;

adt_type
    :   QUEUE qiter (adt_decl)?
    |   HEAP qiter (adt_decl)?
    |   LIST liter (adt_decl)?
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
    :   grp_decl+
    ;

grp_decl
    :   IDENTIFIER grp_instr*
    ;

grp_instr
    :   FILTER predicates
    |   GROUPBY attribute
    |   ORDERBY attribute
    ;

predicates
    :   predicate
    |   AND predicates+
    |   OR predicates+
    |   NOT predicates
    ;

predicate   returns [Predicate pred]
    :
    IN ids=IDENTIFIER+
    {
    $pred = new ExtPredicate(ids);
    }
    |   IN a=attribute o=op i=INT_CONST
    {
    $pred = new IntPredicate(a,o,i);
    }
    ;

attribute   returns [ATTRIBUTES attr]
    :   VIDX    {$attr = ATTRIBUTE.VIDX;}
    |   VCARD   {$attr = ATTRIBUTE.VCARD;}
    |   CIDX    {$attr = ATTRIBUTE.CIDX;}
    |   CARITY  {$attr = ATTRIBUTE.CARITY;}
    |   PIDX    {$attr = ATTRIBUTE.PIDX;}
    |   PPRIO   {$attr = ATTRIBUTE.PPRIO;}
    |   PARITY  {$attr = ATTRIBUTE.PARITY;}
    |   PPRIOD  {$attr = ATTRIBUTE.PPRIOD;}
    ;

op  returns [OPERATOR value]
    :   OEQ {$value = OPERATOR.EQ;}
    |   ONQ {$value = OPERATOR.NQ;}
    |   OLT {$value = OPERATOR.LT;}
    |   OGT {$value = OPERATOR.GT;}
    ;


