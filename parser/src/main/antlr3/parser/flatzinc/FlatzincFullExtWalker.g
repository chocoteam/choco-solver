tree grammar FlatzincFullExtWalker;

options {
  tokenVocab=FlatzincFullExtParser;
  ASTLabelType=CommonTree;
}

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


import parser.flatzinc.ast.ext.*;

import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.Generator;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.generator.Sort;
import solver.propagation.generator.Queue;
import solver.propagation.generator.SortDyn;
import solver.propagation.generator.*;

import solver.propagation.ISchedulable;
import solver.recorders.fine.arc.FineArcEventRecorder;

import solver.Solver;
import solver.constraints.Constraint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayDeque;
import java.util.Collections;
}

@members{
// The flatzinc logger -- 'System.out/err' is fobidden!
protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

// maintains map between name and objects
public THashMap<String, Object> map;

public THashMap<String, ArrayList> groups;

// search for all solutions
public boolean all = false;
// free search strategy
public boolean free = false;

// the solver
public Solver mSolver;

// the layout dedicated to pretty print message wrt to fzn recommendations
public final FZNLayout mLayout = new FZNLayout();
}


flatzinc_model [Solver aSolver, THashMap<String, Object> map]
	:
	{
	this.mSolver = aSolver;
	this.map = map;
	this.groups = new THashMap();
    }
	   (pred_decl)* (param_decl)* (var_decl)* (constraint)* (engine)? solve_goal
	{
	if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
        mLayout.setSearchLoop(mSolver.getSearchLoop());
    }
	}
	;

engine
    :   ^(ENGINE
    {
    ArrayList<Pair> pairs= Pair.populate(mSolver);
    IPropagationEngine propagationEngine = new PropagationEngine(mSolver.getEnvironment());
    }
    (group_decl[pairs])+
    {
    ArrayList<PropagationStrategy> pss = new ArrayList();
    }
    (adt_decl[propagationEngine,null])+
    )
    {
    PropagationStrategy ps = null;
    if(pss.size() > 1){ // a master generator is required
        ps = new Sort(null,pss.toArray(new Generator[ps.size()]));
    }else{ // pss.size() == 1
        ps = pss.get(0);
    }
    mSolver.set(propagationEngine.set(ps));
    }
    ;

adt_decl    [IPropagationEngine pe, ArrayList in] returns [ArrayList<PropagationStrategy> ps]
    :
    {
    ArrayList elements = null;
    }
    ^(IDENTIFIER
    {
    elements = this.groups.get($IDENTIFIER.text);
    if(elements == null){
        throw new FZNException("Unknown ADT identifier:" + $IDENTIFIER.text);
    }
    }
    adt_type[pe,elements])
    |   ^(MANY {int bidx = input.mark() +1 ;} .)
    {
    ps = new ArrayList<PropagationStrategy>();
    for (int i = 0; i < in.size(); i++) {
        input.rewind(bidx);
        ps.addAll(adt_type(pe, (ArrayList) in.get(i)));
    }
    input.release(bidx);
    }
    ;
//adt_types  [IPropagationEngine pe, ArrayList in] returns [ArrayList<PropagationStrategy> ps]
//    :   ^(MANY {int bidx = input.mark() +1 ;} .)
//    {
//    ps = new ArrayList<PropagationStrategy>();
//    for (int i = 0; i < in.size(); i++) {
//        input.rewind(bidx);
//        ps.addAll(adt_type(pe, (ArrayList) in.get(i)));
//    }
//    input.release(bidx);
//    }
//    |   a=adt_type[pe,in]
//    {
//    $ps = a;
//    }
//    ;


adt_type    [IPropagationEngine pe, ArrayList in] returns [ArrayList<PropagationStrategy> ps]
@init{
$ps = new ArrayList<PropagationStrategy>();
}

    :   ps_=adt_decl[pe,in]
    {
    ps=ps_;
    }
    |   ^(QUEUE it=qiter (ps_=adt_decl[pe,in])?)
    {
    Queue queue = null;
    if(ps_==null){
        // iterate over in to create arcs
        FineArcEventRecorder[] arcs = new FineArcEventRecorder[in.size()];
        for(int i = 0 ; i < in.size(); i++){
            Pair p = (Pair)in.get(i);
            FineArcEventRecorder er = PArc.make(pe,mSolver, p.var, p.prop,p.idxVinP);
            if(er == null)throw new FZNException("Cannot create the pair "+p);
            arcs[i] = er;
        }
        queue = new Queue(arcs);
    }else{
        queue = new Queue(ps_.toArray(new ISchedulable[ps_.size()]));
        //todo: deal with iterator!!
    }
    it.set(queue);
    $ps.add(queue);
    }
    |   ^(HEAP it=qiter (ps_=adt_decl[pe,in])?)
    {
    SortDyn sortdyn = null;
    if(ps_==null){
        // iterate over in to create arcs
        FineArcEventRecorder[] arcs = new FineArcEventRecorder[in.size()];
        for(int i = 0 ; i < in.size(); i++){
            Pair p = (Pair)in.get(i);
            FineArcEventRecorder er = PArc.make(pe,mSolver, p.var, p.prop,p.idxVinP);
            if(er == null)throw new FZNException("Cannot create the pair "+p);
            arcs[i] = er;
        }
        sortdyn = new SortDyn(null/*todo comparator!! */,arcs);
    }else{
        sortdyn = new SortDyn(null/*todo comparator!! */,ps_.toArray(new ISchedulable[ps_.size()]));
    }
    it.set(sortdyn);
    $ps.add(sortdyn);
    }

    |   ^(LIST it=liter (ps_=adt_decl[pe,in])?)
    {
    Sort sort = null;
    if(ps_==null){
        // iterate over in to create arcs
        FineArcEventRecorder[] arcs = new FineArcEventRecorder[in.size()];
        for(int i = 0 ; i < in.size(); i++){
            Pair p = (Pair)in.get(i);
            FineArcEventRecorder er = PArc.make(pe,mSolver, p.var, p.prop,p.idxVinP);
            if(er == null)throw new FZNException("Cannot create the pair "+p);
            arcs[i] = er;
        }
        sort = new Sort(null /*todo comparator !! */, arcs);
    }else{
        sort = new Sort(null /*todo comparator !! */, ps_.toArray(new ISchedulable[ps_.size()]));
    }
    it.set(sort);
    $ps.add(sort);
    }
    ;

qiter   returns [Iterator it]
    :   ONE {$it = Iterator.ONE;}
    |   WONE    {$it = Iterator.WONE;}
    ;

liter   returns [Iterator it]
    :   q=qiter {$it = q;}
    |   FOR {$it = Iterator.FOR;}
    |   WFOR{$it = Iterator.WFOR;}
    ;

group_decl  [ArrayList<Pair> pairs]
    :
    {
    ArrayList aGroup = new ArrayList();
    }
    ^(IDENTIFIER (i=grp_instrs[pairs]
    {
    aGroup.addAll(i);
    Pair.remove(pairs, i);
    })+)
    {
    groups.put($IDENTIFIER.text,aGroup);
    }
    ;

grp_instrs  [ArrayList before]  returns [ArrayList after]
    :   ^(GRP (a=grp_instr[before]{$before = a;})+)
    {
    $after = $before;
    }
    ;

grp_instr [ArrayList before]  returns [ArrayList after]
    :   ^(FILTER p=predicates)
    {
    $after = Filter.execute(p,before);
    }
    |   ^(GROUPBY a=attribute)
    {
    $after = GroupBy.execute(a,before);
    }
    |   ^(ORDERBY s=sort a=attribute)
    {
    $after = OrderBy.execute(a,before);
    if(!s){
        Collections.reverse($after);
    }
    }
    ;

sort returns [boolean incr]
    :   INC {incr = true;}
    |   DEC {incr = false;}
    ;


predicates  returns [Predicate pred]
    :   p=predicate
    {
    $pred = p;
    }
    |
    {
    ArrayList<Predicate> preds = new ArrayList();
    }
    ^(AND (p=predicates{preds.add(p);})+)
    {
    $pred = new BoolPredicate(preds, BoolPredicate.TYPE.AND);
    }
    |
    {
    ArrayList<Predicate> preds = new ArrayList();
    }
    ^(OR (p=predicates{preds.add(p);})+)
    {
    $pred = new BoolPredicate(preds, BoolPredicate.TYPE.OR);
    }
    |   ^(NOT p=predicates)
    {
    $pred = new NotPredicate(p);
    }
    ;

predicate   returns [Predicate pred]
    :
    {
    ArrayList<String> ids = new ArrayList();
    }
    ^(IN (i=IDENTIFIER{ids.add($IDENTIFIER.text);})+)
    {
    $pred = new ExtPredicate(ids,map);
    }
    |   ^(IN a=attribute o=op i=INT_CONST)
    {
    $pred = new IntPredicate(a,o,Integer.valueOf($i.text));
    }
    ;

attribute   returns [Attribute attr]
    :   VIDX    {$attr = Attribute.VIDX;}
    |   VCARD   {$attr = Attribute.VCARD;}
    |   CIDX    {$attr = Attribute.CIDX;}
    |   CARITY  {$attr = Attribute.CARITY;}
    |   PIDX    {$attr = Attribute.PIDX;}
    |   PPRIO   {$attr = Attribute.PPRIO;}
    |   PARITY  {$attr = Attribute.PARITY;}
    |   PPRIOD  {$attr = Attribute.PPRIOD;}
    ;

op  returns [Operator value]
    :   EQ {$value = Operator.EQ;}
    |   NQ {$value = Operator.NQ;}
    |   LT {$value = Operator.LT;}
    |   GT {$value = Operator.GT;}
    ;

par_type    returns [Declaration decl]
    :
    {
        List<Declaration> decls = new ArrayList();
    }
        ^(ARRPAR (d=index_set{decls.add(d);})+ p=par_type_u)
    {
    $decl = new DArray(decls,p);
    }
    |   ^(APAR p=par_type_u)
    {
    $decl = p;
    }
    ;

par_type_u returns [Declaration decl]
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
    ^(ARRVAR (d=index_set{decls.add(d);})+ d=var_type_u)
    {
    $decl = new DArray(decls, d);
    }
    |   ^(AVAR d=var_type_u)
    {
    $decl=d;
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
    |   ^(DD i1=INT_CONST i2=INT_CONST)
    {
    $decl = new DInt2(EInt.make($i1.text), EInt.make($i2.text));
    }
//    |   ^(DD FLOAT_ FLOAT_)
//    {
////    TODO: throw exception
//    $decl = null;
//    }
    |
    {
    ArrayList<EInt> values = new ArrayList();
    }
        ^(CM (i=INT_CONST{values.add(EInt.make($i.text));})+)
    {
    $decl = new DManyInt(values);
    }
    |   ^(SET ^(DD i1=INT_CONST i2=INT_CONST))
    {
    $decl = new DSet(new DInt2(EInt.make($i1.text), EInt.make($i2.text)));
    }
    |
    {
    ArrayList<EInt> values = new ArrayList();
    }
       ^(SET ^(CM (i=INT_CONST{values.add(EInt.make($i.text));})+))
    {
    $decl = new DSet(new DManyInt(values));
    }
    ;

index_set returns [Declaration decl]
    :   ^(INDEX ^(DD i1=INT_CONST i2=INT_CONST))
    {
    $decl = new DInt2(EInt.make($i1.text), EInt.make($i2.text));
    }
    |   ^(INDEX INT)
    {
    $decl = DInt.me;
    }
    ;

expr    returns[Expression exp]
    :
    {
    ArrayList<EInt> values = new ArrayList();
    }
        LB (i=INT_CONST{values.add(EInt.make($i.text));})+ RB
    {
    $exp = new ESetList(values);
    }
    |   b=bool_const
    {
    $exp=EBool.make(b);
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
       ^(EXPR LS (e=expr{exps.add(e);})* RS)
    {
    if(exps.size()>0){
        $exp = new EArray(exps);
    }else{
        $exp = new EArray();
    }
    }
    |   e=id_expr
    {
    $exp = e;
    }
    |   STRING
    {
    $exp = new EString($STRING.text);
    }
//    |   FLOAT_
    ;


//id_expr returns [Expression e]
//    :
//    IDENTIFIER ((LP exps=(expr+) RP)| (LS i=INT_CONST RS))?
//    {
//    if(exps == null || $INT_CONST == null){
//        $exp = nex EIdentifier(map, $IDENTIFIER.text);
//    }else if(exps != null) {
//        $exps = new EArray(exps);
//    }else if($INT_CONST != null){
//        $exps = new EIdArray(map, $IDENTIFIER.text, i);
//    }else{
//    //todo throw exception
//    }
//    }
//    ;

id_expr returns [Expression exp]
    :
    {
    ArrayList<Expression> exps = new ArrayList();
    }
       IDENTIFIER ((LP e=expr{exps.add(e);} (CM e=expr{exps.add(e);})* RP)|(LS i=INT_CONST RS))?
    {
    if(exps.size()>0){
        $exp = new EAnnotation(new EIdentifier(map, $IDENTIFIER.text), exps);
    }else if($i!=null) {
        $exp = new EIdArray(map, $IDENTIFIER.text, Integer.parseInt($i.text));
    }else{
        $exp = new EIdentifier(map, $IDENTIFIER.text);
    }
    }
    ;


param_decl
	:   ^(PAR IDENTIFIER pt=par_type e=expr)
	{
	// Parameter(THashMap<String, Object> map, Declaration type, String identifier, Expression expression)
    FParameter.make_parameter(map, pt, $IDENTIFIER.text, e);
    }
	;


var_decl
	:   ^(VAR IDENTIFIER vt=var_type anns=annotations e=expr?)
	{
	FVariable.make_variable(map, vt, $IDENTIFIER.text, anns, e, mSolver, mLayout);
	}
	;

constraint
	:
	{
	//  Solver aSolver, String id, List<Expression> exps, List<EAnnotation> annotations
	ArrayList<Expression> exps = new ArrayList();
	}
	    ^(CONSTRAINT IDENTIFIER (e=expr{exps.add(e);})+ anns=annotations)
	{
	String id = $IDENTIFIER.text;
	FConstraint.make_constraint(mSolver, map, id, exps, anns);
	}
	;

solve_goal
	:
	{
	FGoal.Resolution type = FGoal.Resolution.SATISFY;
	Expression expr = null;
	}
	^(SOLVE anns=annotations res=resolution[type,expr])
	{
    FGoal.define_goal(free, all, mSolver,anns,type,expr);
	}
	;

resolution  [FGoal.Resolution type, Expression expr]
    :   SATISFY
    |   ^(MINIMIZE e=expr)
    {
    $type=FGoal.Resolution.MINIMIZE;
    $expr=e;
    }
    |   ^(MAXIMIZE e=expr)
    {
    $type=FGoal.Resolution.MAXIMIZE;
    $expr=e;
    }
    ;

annotations returns [List<EAnnotation> anns]
    :
    {
    anns = new ArrayList();
    }
        ^(ANNOTATIONS (e=annotation {anns.add(e);})*)
    ;

annotation  returns [EAnnotation ann]
    :
    {
    ArrayList<Expression> exps = new ArrayList();
    }
    IDENTIFIER (LP (e=expr{exps.add(e);})+ RP)?
    {
    $ann = new EAnnotation(new EIdentifier(map,$IDENTIFIER.text), exps);
    }
    ;

bool_const  returns [boolean value]
    :   TRUE {$value = true;}
    |   FALSE{$value = false;}
    ;

//TODO : not use yet
pred_decl
	:   ^(PREDICATE IDENTIFIER pred_param+)
	{
//        LOGGER.info("\% skip predicate : "+ $IDENTIFIER.text);
	}
	;

//TODO : not use yet
pred_param
    :   ^(CL pred_param_type IDENTIFIER)
    ;

//TODO : not use yet
pred_param_type
    :   par_pred_param_type
    |   var_pred_param_type
    ;

//TODO : not use yet
par_pred_param_type
    :   par_type
//    |   ^(DD FLOAT_ FLOAT_)
    |   ^(DD INT_CONST INT_CONST)
    |   ^(CM INT_CONST+)
    |   ^(SET ^(DD INT_CONST INT_CONST))
    |   ^(SET ^(CM INT_CONST+))
//    |   ^(ARRAY index_set+ ^(DD FLOAT_ FLOAT_))
    |   ^(ARRAY index_set+ ^(DD INT_CONST INT_CONST))
    |   ^(ARRAY index_set+ ^(CM INT_CONST+))
    |   ^(ARRAY index_set+ ^(SET ^(DD INT_CONST INT_CONST)))
    |   ^(ARRAY index_set+ ^(SET ^(CM INT_CONST+)))
    ;

//TODO : not use yet
var_pred_param_type
    :   ^(VAR var_type)
    |   ^(VAR SET)
    |   ^(ARRAY index_set+ ^(VAR SET))
    ;
