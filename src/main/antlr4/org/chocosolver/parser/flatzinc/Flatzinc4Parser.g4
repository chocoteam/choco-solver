parser grammar Flatzinc4Parser;

options{
    language = Java;
    //output=AST;
    tokenVocab=Flatzinc4Lexer;
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

import org.chocosolver.parser.flatzinc.ast.*;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;

import java.util.ArrayList;
import java.util.List;
}

@members{

public Datas datas;

// the solver
public Solver mSolver;


public boolean allSolutions, freeSearch;
}


// PARSER RULES

flatzinc_model [Solver aSolver, Datas datas, boolean allSolutions, boolean freeSearch]
	:
	{
    this.mSolver = aSolver;
    this.datas = datas;
    this.allSolutions = allSolutions;
    this.freeSearch = freeSearch;
    }
       (pred_decl)* (param_decl | var_decl)* (constraint)* solve_goal
	;


par_type    returns [Declaration decl]
    :
    {
        List<Declaration> decls = new ArrayList();
    }
    ARRAY LS d=index_set{decls.add($d.decl);} (CM d=index_set{decls.add($d.decl);})* RS OF p=par_type_u
    {
    $decl = new DArray(decls,$p.decl);
    }
    |   p=par_type_u
    {
    $decl = $p.decl;
    }
    ;

par_type_u  returns [Declaration decl]
    :   BOOL
    {
    $decl=DBool.me;
    }
    |   FLOAT
    {
    $decl=DFloat.me;
    }
    |   SET OF INT
    {
    $decl=DSetOfInt.me;
    }
    |   INT
    {
    $decl=DInt.me;
    }
    ;

var_type    returns [Declaration decl]
    :
    {
    List<Declaration> decls = new ArrayList();
    }
        ARRAY LS d=index_set{decls.add($d.decl);} (CM d=index_set{decls.add($d.decl);})* RS OF VAR vt=var_type_u
    {
    $decl = new DArray(decls, $vt.decl);
    }
    |   VAR vt=var_type_u
    {
    $decl=$vt.decl;
    }

    ;

var_type_u  returns [Declaration decl]
    :   BOOL
    {
    $decl = DBool.me;
    }
    |   FLOAT
    {
    $decl = DFloat.me;
    }
    |   INT
    {
    $decl = DInt.me;
    }
    |   i1=INT_CONST DD i2=INT_CONST
     {
     $decl = new DInt2(EInt.make($i1.text), EInt.make($i2.text));
     }
//    |   FLOAT_ DD FLOAT_
    |
    {
    ArrayList<EInt> values = new ArrayList();
    }
        LB i=INT_CONST{values.add(EInt.make($i.text));} (CM i=INT_CONST{values.add(EInt.make($i.text));})* RB
    {
    $decl = new DManyInt(values);
    }
    |   SET OF i1=INT_CONST DD i2=INT_CONST
    {
    $decl = new DSet(new DInt2(EInt.make($i1.text), EInt.make($i2.text)));
    }
    |
    {
    ArrayList<EInt> values = new ArrayList();
    }
        SET OF LB i=INT_CONST{values.add(EInt.make($i.text));} (CM i=INT_CONST{values.add(EInt.make($i.text));})* RB
    {
    $decl = new DSet(new DManyInt(values));
    }
    ;



index_set returns [Declaration decl]
    :   i1=INT_CONST DD i2=INT_CONST
    {
    $decl = new DInt2(EInt.make($i1.text), EInt.make($i2.text));
    }
    |   INT
    {
    $decl = DInt.me;
    }
    ;

expr    returns[Expression exp]
    :   LB RB
    {
    $exp = new ESetList(new ArrayList());
    }
    |
    {
    ArrayList<EInt> values = new ArrayList();
    }
        LB i=INT_CONST{values.add(EInt.make($i.text));} (CM i=INT_CONST{values.add(EInt.make($i.text));})* RB
    {
    $exp = new ESetList(values);
    }
    |   b=bool_const
    {
    $exp=EBool.make($b.value);
    }
    |   i1=INT_CONST (DD i2=INT_CONST)?
    {
    if($i2==null){
        $exp=EInt.make($i1.text);
    }else{
        $exp = new ESetBounds(EInt.make($i1.text), EInt.make($i2.text));
    }
    }
    |
    {
    ArrayList<Expression> exps = new ArrayList();
    }
    LS (e=expr{exps.add($e.exp);} (CM e=expr{exps.add($e.exp);})*)? RS
    {
    if(exps.size()>0){
        $exp = new EArray(exps);
    }else{
        $exp = new EArray();
    }
    }
    |   ie=id_expr
    {
    $exp = $ie.exp;
    }
    |   STRING
    {
    $exp = new EString($STRING.text);
    }
//    |   FLOAT_
    ;

id_expr returns [Expression exp]
//options {backtrack=true;}
    :
    {
    ArrayList<Expression> exps = new ArrayList();
    }
        IDENTIFIER LP e=expr{exps.add($e.exp);} (CM e=expr{exps.add($e.exp);})* RP
    {
    $exp = new EAnnotation(new EIdentifier(datas, $IDENTIFIER.text), exps);
    }

    |   IDENTIFIER LS i=INT_CONST RS
    {
    $exp = new EIdArray(datas, $IDENTIFIER.text, Integer.parseInt($i.text));
    }
    |   IDENTIFIER
    {
    $exp = new EIdentifier(datas, $IDENTIFIER.text);
    }
    ;


param_decl
	:   pt=par_type CL IDENTIFIER EQ e=expr SC
	{
    // Parameter(Datas datas, Declaration type, String identifier, Expression expression)
    FParameter.make_parameter(datas, $pt.decl, $IDENTIFIER.text, $e.exp);
    }
	;


var_decl
	:
	vt=var_type CL IDENTIFIER anns=annotations (eq=EQ e=expr)? SC
	{
	FVariable.make_variable(datas, $vt.decl, $IDENTIFIER.text, $anns.anns, $eq!=null?$e.exp:null, mSolver);
    }
	;

constraint
	:
	{
    //  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
    ArrayList<Expression> exps = new ArrayList();
    }
	    CONSTRAINT IDENTIFIER LP e=expr {exps.add($e.exp);} (CM e=expr{exps.add($e.exp);})* RP anns=annotations SC
    {
    FConstraint.make_constraint(mSolver, datas, $IDENTIFIER.text, exps, $anns.anns);
    }
	;

solve_goal
	:   SOLVE anns=annotations res=resolution SC
	{
    FGoal.define_goal(mSolver, $anns.anns,$res.rtype,$res.exp);
    }
	;

resolution returns[ResolutionPolicy rtype, Expression exp]
    :   MINIMIZE e=expr
    {
    $rtype=ResolutionPolicy.MINIMIZE;
    $exp=$e.exp;
    }
    |   MAXIMIZE e=expr
    {
    $rtype=ResolutionPolicy.MAXIMIZE;
    $exp=$e.exp;
    }
    |   SATISFY
    {
    $rtype=ResolutionPolicy.SATISFACTION;
    $exp=null;
    }
    ;

annotations  returns [List<EAnnotation> anns]
    :
    {
    $anns = new ArrayList();
    }
        (DC e=annotation{$anns.add($e.ann);})*
    ;

annotation  returns [EAnnotation ann]
    :
    {
    ArrayList<Expression> exps = new ArrayList();
    }
    IDENTIFIER (LP e=expr{exps.add($e.exp);} (CM e=expr{exps.add($e.exp);})* RP)?
    {
    $ann = new EAnnotation(new EIdentifier(datas,$IDENTIFIER.text), exps);
    }
    ;


bool_const  returns [boolean value]
    :   TRUE {$value = true;}
    |   FALSE{$value = false;}
    ;

//TODO : not use yet
pred_decl
	:   PREDICATE IDENTIFIER LP pred_param (CM pred_param)* RP SC
	{
//        LOGGER.info("\% skip predicate : "+ $IDENTIFIER.text);
	}
	;

//TODO : not use yet
pred_param
    :   pred_param_type CL IDENTIFIER
    ;

//TODO : not use yet
pred_param_type
    :   par_pred_param_type
    |   var_pred_param_type
    ;

//TODO : not use yet
par_pred_param_type
    :   par_type
//    |   FLOAT_ DD FLOAT_
//    ->  ^(DD FLOAT_ FLOAT_)
    |   INT_CONST DD INT_CONST
    //->  ^(DD INT_CONST INT_CONST)
    |   LB INT_CONST (CM INT_CONST)* RB
    //->  ^(CM INT_CONST+)
    |   SET OF INT_CONST DD INT_CONST
    //->  ^(SET ^(DD INT_CONST INT_CONST))
    |   SET OF LB INT_CONST (CM INT_CONST)* RB
    //->  ^(SET ^(CM INT_CONST+))
//    |   ARRAY LS index_set (CM index_set)* RS OF FLOAT_ DD FLOAT_
//    ->  ^(ARRAY index_set+ ^(DD FLOAT_ FLOAT_))
    |   ARRAY LS index_set (CM index_set)* RS OF INT_CONST DD INT_CONST
    //->  ^(ARRAY index_set+ ^(DD INT_CONST INT_CONST))
    |   ARRAY LS index_set (CM index_set)* RS OF LB INT_CONST (CM INT_CONST)* RB
    //->  ^(ARRAY index_set+ ^(CM INT_CONST+))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF INT_CONST DD INT_CONST
    //->  ^(ARRAY index_set+ ^(SET ^(DD INT_CONST INT_CONST)))
    |   ARRAY LS index_set (CM index_set)* RS OF SET OF LB INT_CONST (CM INT_CONST)* RB
    //->  ^(ARRAY index_set+ ^(SET ^(CM INT_CONST+)))
    ;


//TODO : not use yet
var_pred_param_type
    :   var_type
    //->  ^(VAR var_type)
    |   VAR SET OF INT
    //->  ^(VAR SET)
    |   ARRAY LS index_set (CM index_set)* RS OF VAR SET OF INT
    //->  ^(ARRAY index_set+ ^(VAR SET))
    ;
