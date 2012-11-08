parser grammar FlatzincFullExtParser;

options{
    language = Java;
    output=AST;
    tokenVocab=FlatzincFullExtLexer;
    backtrack = true;
}

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
	:   (pred_decl)* (param_decl)* (var_decl)* (constraint)* (group_decl)* (structure)? solve_goal
	;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////  OUR DSL /////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//engine
//    :   (group_decl)* //structure
//    ->  group_decl+ //structure
//    ;

// DECLARATION OF A GROUP
group_decl
    :   IDENTIFIER CL predicates SC
    ->  ^(IDENTIFIER predicates)
    ;


// COMBINATION OF PREDICATES
predicates
	:	predicate
	|	LP predicates (AND predicates)+ RP
	->  ^(AND predicates+)
	|	LP predicates (OR predicates)+ RP
	->  ^(OR predicates+)
	;

// AVAILABLE PREDICATES
predicate
	:	TRUE
	|	attribute op INT_CONST
	|	IN LP IDENTIFIER (CM IDENTIFIER)* RP
	->  ^(IN IDENTIFIER+)
	|	NOT predicate
	;

// ATTRIBUTE ACCESSIBLE THROUGH THE SOLVER
attribute
	: 	VNAME
    |   VCARD
    |   CNAME
    |   CARITY
    |   PPRIO
    |   PARITY
    |   PPRIOD
	;


// AVAILABLE OPERATORS
op
    :   OEQ
    |   ONQ
    |   OLT
    |   OGT
    |   OLQ
    |   OGQ
	;

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////


//// DECLARATION OF THE STRUCTURE
structure
	:   struct SC!
	|   struct_reg SC!
	;

struct
    :	coll OF LB elt (CM elt)* RB (KEY comb_attr)?
    ->  ^(STRUC elt+ comb_attr? coll)
	;

struct_reg
	:	IDENTIFIER AS coll OF LB many RB (KEY comb_attr)?
	->  ^(STREG IDENTIFIER many comb_attr? coll)
	;

//TODO: remove backtrack options
elt
    :	struct_reg
    |   struct
    |   IDENTIFIER (KEY attribute)?
	;

many
    :	EACH attribute AS coll (OF LB m=many RB)? (KEY comb_attr)?
    ->  {m==null}?  ^(attribute comb_attr? coll)
    ->              ^(EACH attribute comb_attr? many coll)
	;

/////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////
//
coll
    :	QUEUE LP! qiter RP!
    |	(REV)? LIST LP! liter RP!
    |	(MIN|MAX) HEAP  LP! qiter RP!
	;

qiter
    :	ONE
    |   WONE
    ;

liter
    :	qiter
    |   FOR
    |   WFOR
    ;


comb_attr
	:	attr_op (DO attr_op)*  (DO attribute)?
	->  ^(DO attr_op* attribute?)
	|   (attr_op DO)* attribute
	->  ^(DO attr_op* attribute?)
	;

attr_op
    :	ANY
    |   MIN
    |   MAX
    |   SUM
    |   SIZE
	;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////  BACK TO FLATZINC  ///////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


pred_decl
	:   PREDICATE IDENTIFIER LP pred_param (CM pred_param)* RP SC
	-> ^(PREDICATE IDENTIFIER pred_param+)
	;

pred_param
    :   pred_param_type CL IDENTIFIER
    ->  ^(CL pred_param_type IDENTIFIER)
    ;

pred_param_type
    :   par_pred_param_type
    |   var_pred_param_type
    ;

par_type
    :   ARRAY LS index_set (CM index_set)* RS OF par_type_u
    ->  ^(ARRPAR index_set+ par_type_u)
    |   par_type_u
    ->  ^(APAR par_type_u)
    ;

par_type_u
    :   BOOL
    |   FLOAT
    |   SET OF INT
    |   INT
    ;

var_type
    :   ARRAY LS index_set (CM index_set)* RS OF VAR var_type_u
    ->  ^(ARRVAR index_set+ var_type_u)
    |   VAR var_type_u
    ->  ^(AVAR var_type_u)
    ;

var_type_u
    :   BOOL
    |   FLOAT
    |   INT
    |   INT_CONST DD INT_CONST
    ->  ^(DD INT_CONST INT_CONST)
//    |   FLOAT_ DD FLOAT_
//    ->  ^(DD FLOAT_ FLOAT_)
    |   LB INT_CONST (CM INT_CONST)* RB
    ->  ^(CM INT_CONST+)
    |   SET OF INT_CONST DD INT_CONST
    ->  ^(SET ^(DD INT_CONST INT_CONST))
    |   SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(SET ^(CM INT_CONST INT_CONST))
    ;

par_pred_param_type
    :   par_type
//    |   FLOAT_ DD FLOAT_
//    ->  ^(DD FLOAT_ FLOAT_)
    |   INT_CONST DD INT_CONST
    ->  ^(DD INT_CONST INT_CONST)
    |   LB INT_CONST (CM INT_CONST)* RB
    ->  ^(CM INT_CONST+)
    |   SET OF INT_CONST DD INT_CONST
    ->  ^(SET ^(DD INT_CONST INT_CONST))
    |   SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(SET ^(CM INT_CONST+))
//    |   ARRAY LS index_set (CM index_set)* RS OF FLOAT_ DD FLOAT_
//    ->  ^(ARRAY index_set+ ^(DD FLOAT_ FLOAT_))
    |   ARRAY LS index_set (CM index_set)* RS OF INT_CONST DD INT_CONST
    ->  ^(ARRAY index_set+ ^(DD INT_CONST INT_CONST))
    |   ARRAY LS index_set (CM index_set)* RS OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(ARRAY index_set+ ^(CM INT_CONST+))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF INT_CONST DD INT_CONST
    ->  ^(ARRAY index_set+ ^(SET ^(DD INT_CONST INT_CONST)))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF LB INT_CONST (CM INT_CONST)* RB
    ->  ^(ARRAY index_set+ ^(SET ^(CM INT_CONST+)))
    ;


var_pred_param_type
    :   var_type
    ->  ^(VAR var_type)
    |   VAR SET OF INT
    ->  ^(VAR SET)
    |   ARRAY LS index_set (CM index_set)* RS OF VAR SET OF INT
    ->  ^(ARRAY index_set+ ^(VAR SET))
    ;

index_set
    :   INT_CONST DD INT_CONST
    ->  ^(INDEX ^(DD INT_CONST INT_CONST))
    |   INT
    ->  ^(INDEX INT)
    ;

expr
    :   LB INT_CONST (CM INT_CONST)* RB
    ->  LB INT_CONST+ RB
    |   bool_const
    |   INT_CONST (DD INT_CONST)?
    |   LS (expr (CM expr)*)? RS
    ->  ^(EXPR LS expr* RS)
    |   id_expr
    |   STRING
//    |   FLOAT_
    ;

id_expr
//options {backtrack=true;}
    :   IDENTIFIER ((LP expr (CM expr)* RP)|(LS INT_CONST RS))?
    ;


param_decl
	:   par_type CL IDENTIFIER EQ expr SC
	->  ^(PAR IDENTIFIER par_type expr)
	;


var_decl
	:   var_type CL IDENTIFIER annotations (EQ expr)? SC
	->  ^(VAR IDENTIFIER var_type annotations expr?)
	;

constraint
	:   CONSTRAINT IDENTIFIER LP expr (CM expr)* RP annotations SC
	->  ^(CONSTRAINT IDENTIFIER expr+ annotations)
	;

solve_goal
	:   SOLVE^ annotations resolution SC!
	;

resolution
    :   MINIMIZE^ expr
    |   MAXIMIZE^ expr
    |   SATISFY^
    ;

annotations
    :   (DC annotation)*
    ->  ^(ANNOTATIONS annotation*)
    ;

annotation
    :   IDENTIFIER (LP expr (CM expr)* RP)?
    ->  IDENTIFIER (LP expr+ RP)?
    ;


bool_const
    :   TRUE^
    |   FALSE^
    ;





