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

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.*;
import parser.flatzinc.FZNException;
import parser.flatzinc.FZNLayout;
import parser.flatzinc.ast.FConstraint;
import parser.flatzinc.ast.FParameter;
import parser.flatzinc.ast.FVariable;
import parser.flatzinc.ast.FGoal;
import parser.flatzinc.ast.Datas;


import parser.flatzinc.ast.ext.*;

import solver.propagation.DSLEngine;
import solver.propagation.generator.Generator;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.generator.Sort;
import solver.propagation.generator.Queue;
import solver.propagation.generator.SortDyn;
import solver.propagation.generator.*;

import solver.propagation.ISchedulable;
import solver.propagation.generator.Arc;

import solver.Solver;
import solver.constraints.Constraint;
import solver.ResolutionPolicy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Arrays;
}

@members{
// The flatzinc logger -- 'System.out/err' is fobidden!
protected static final Logger LOGGER = LoggerFactory.getLogger("fzn");

// maintains map between name and objects
public Datas datas;

public THashMap<String, ArrayList> groups;

// the solver
public Solver mSolver;

}


flatzinc_model [Solver aSolver, Datas datas]
	:
	{
	this.mSolver = aSolver;
	this.datas= datas;
	this.groups = new THashMap();
    }
	   (pred_decl)* (param_decl)* (var_decl)* (constraint)* engine? solve_goal
	{
	if (LoggerFactory.getLogger("fzn").isInfoEnabled()) {
        datas.plugLayout(mSolver);
    }
	}
	;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////  OUR DSL /////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
engine
@init{
	ArrayList<Arc> arcs= Arc.populate(mSolver);
	DSLEngine propagationEngine = new DSLEngine(mSolver);
	}
@after{
    if(!arcs.isEmpty()){
        LOGGER.warn("\% Remaining arcs after group declarations");
        throw new FZNException("Remaining arcs after group declarations");
    }
    if (arcs.isEmpty() && ps == null) {
        LOGGER.warn("\% no engine defined");
        throw new FZNException("no engine defined");
    }
    mSolver.set(propagationEngine.set(ps));
}
    :   (group_decl[arcs])+ ps = structure[propagationEngine]
;

group_decl  [ArrayList<Arc> arcs]
    :
    ^(IDENTIFIER p=predicates)
    {
    ArrayList<Arc> aGroup = Filter.execute(p,arcs);
    if(aGroup.isEmpty()){
        LOGGER.error("\% Empty predicate declaration :"+ $IDENTIFIER.line+":"+$IDENTIFIER.pos);
        throw new FZNException("Empty predicate declaration");
    }
    Arc.remove(arcs, aGroup);
    groups.put($IDENTIFIER.text,aGroup);
    }
    ;


// COMBINATION OF PREDICATES
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
    ;

// AVAILABLE PREDICATES
predicate   returns [Predicate pred]
	:	TRUE
	{
	$pred = TruePredicate.singleton;
	}
	|	a=attribute o=op i=INT_CONST
	{
    $pred = new IntPredicate(a,o,Integer.valueOf($i.text));
    }
	|
	{
	ArrayList<String> ids = new ArrayList();
	}
	    ^(IN (i=IDENTIFIER{ids.add($IDENTIFIER.text);})+)
	{
	$pred = new ExtPredicate(ids, datas);
	}
	|	NOT p=predicate
	{
    $pred = new NotPredicate(p);
    }
	;


// ATTRIBUTE ACCESSIBLE THROUGH THE SOLVER
attribute   returns [Attribute attr]
    :   VAR     {$attr = Attribute.VAR;}
    |   CSTR    {$attr = Attribute.CSTR;}
    |   PROP    {$attr = Attribute.PROP;}
    |   VNAME    {$attr = Attribute.VNAME;}
    |   VCARD   {$attr = Attribute.VCARD;}
    |   CNAME    {$attr = Attribute.CNAME;}
    |   CARITY  {$attr = Attribute.CARITY;}
    |   PPRIO   {$attr = Attribute.PPRIO;}
    |   PARITY  {$attr = Attribute.PARITY;}
    |   PPRIOD  {$attr = Attribute.PPRIOD;}
    ;

// AVAILABLE OPERATOR
op  returns [Operator value]
    :   OEQ {$value = Operator.EQ;}
    |   ONQ {$value = Operator.NQ;}
    |   OLT {$value = Operator.LT;}
    |   OGT {$value = Operator.GT;}
    |   OLQ {$value = Operator.LQ;}
    |   OGQ {$value = Operator.GQ;}
    ;

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////

structure   [DSLEngine pe] returns [PropagationStrategy ps]
	:	s=struct[pe]
	{
	$ps = s;
	}
	|   sr=struct_reg[pe]
	{
	$ps = sr;
	}
	;

struct  [DSLEngine pe] returns[PropagationStrategy item]
@init{
     ArrayList<ISchedulable> elements = new ArrayList<ISchedulable>();
}
@after{
     $item = c;
}
    :	^(STRUC1 (element = elt[pe]{elements.addAll(Arrays.asList(element));})+ c=coll[elements, ca])
	|   ^(STRUC2 (element = elt[pe]{elements.addAll(Arrays.asList(element));})+ ca=comb_attr c=coll[elements, ca])
	;

struct_reg  [DSLEngine pe] returns[PropagationStrategy item]
@init{
    int m_idx = -1,c_idx = -1;
}
@after{
//    String id = $IDENTIFIER.text;
    ArrayList<Arc> arcs = groups.get($id.text);
    if(arcs == null){
        LOGGER.error("\% Unknown group_decl :"+id);
        throw new FZNException("Unknown group_decl :"+id);
    }
    for(int k = 0; k < arcs.size(); k++){
        pe.declareArc(arcs.get(k));
    }
    input.seek(m_idx);
    ArrayList<PropagationStrategy> pss = many(arcs).pss;
    input.release(m_idx);
    input.seek(c_idx);
    $item = coll(pss, ca);
    input.release(c_idx);
    //BEWARE: kind of ugly patch...
    match(input, Token.UP, null);
}
	:	^(STREG id=IDENTIFIER {m_idx = input.mark();} . {c_idx = input.mark();} . )
    |   ^(STREG id=IDENTIFIER ca=comb_attr {m_idx = input.mark();} . {c_idx = input.mark();} . )
	;

elt	[DSLEngine pe] returns [ISchedulable[\] items]
    :	s=struct[pe]
    {
    $items = new ISchedulable[]{s};
    }
	|	sr=struct_reg[pe]
	{
	$items = new ISchedulable[]{sr};
	}
	|	IDENTIFIER (KEY a=attribute)?
	{
	String id = $IDENTIFIER.text;
	ArrayList<Arc> scope = groups.get(id);
	// iterate over in to create arcs
    Arc[] arcs = scope.toArray(new Arc[scope.size()]);
    for(int i = 0 ; i < scope.size(); i++){
        Arc arc = scope.get(i);
        pe.declareArc(arc);
        arc.attachEvaluator(a);
    }
    $items = arcs;
	}
	;

many    [ArrayList<Arc> in]   returns[ArrayList<PropagationStrategy> pss, int depth]
@init{
    $pss = new ArrayList<PropagationStrategy>();
    int c_idx = -1, m_idx = -1;

}
@after{
    if(m_idx == -1){ // we are in a "last many" case
        // Build as many list as different values of "attribute" in "in"
        // if "attribute" is dynamic, the overall range of values must be created
        if(a.isDynamic()){
             int max = 0;
             for(int i = 0; i< in.size(); i++){
                 Arc arc = in.get(i);
                 int ev = a.eval(arc);
                 if(ev > max)max = ev;
             }

             input.seek(c_idx);
             PropagationStrategy _ps = coll(in, ca);
             input.release(c_idx);

            Switcher sw = new Switcher(a, 0,max, _ps, in.toArray(new Arc[in.size()]));
            $pss.addAll(Arrays.asList(sw.getPS()));
        }else{
            // otherwise, create as many "coll" as value of attribute
            TIntObjectHashMap<ArrayList<Arc>> sublists = new TIntObjectHashMap<ArrayList<Arc>>();
            for(int i = 0; i< in.size(); i++){
                Arc arc = in.get(i);
                int ev = a.eval(arc);
                if(!sublists.contains(ev)){
                    ArrayList<Arc> evlist = new ArrayList<Arc>();
                    sublists.put(ev, evlist);
                }
                sublists.get(ev).add(arc);
            }

            int[] evs = sublists.keys();
            for (int k = 0; k < evs.length; k++) {
                int ev = evs[k];
                input.seek(c_idx);
                $pss.add(coll(sublists.get(ev), ca));
            }
            input.release(c_idx);
            $depth = 0;
        }
    }else{ // we are in a recursive many
        if(a.isDynamic()){
            // Build as many list as different values of "attribute" in "in"
            int max = 0;
            for(int i = 0; i< in.size(); i++){
                Arc arc = in.get(i);
                int ev = a.eval(arc);
                if (max < ev) {
                    max = ev;
                }
            }
            int _d = 0;
            input.seek(m_idx);
            FlatzincFullExtWalker.many_return manyret = many(in);
            // 1. get depth
            _d = manyret.depth;
            // 2. build correct attribute (including depth
            ArrayList<AttributeOperator> aos = new ArrayList<AttributeOperator>();
            aos.add(AttributeOperator.ANY);
            for(int i = 1 ; i < _d; i++){
                aos.add(AttributeOperator.ANY);
            }
            CombinedAttribute _ca = new CombinedAttribute(aos, a);
            // 3. build on coll
            input.seek(c_idx);
            PropagationStrategy _ps = coll(manyret.pss, ca);
            input.release(c_idx);

            Switcher sw = new Switcher(_ca, 0,max, _ps, manyret.pss.toArray(new PropagationStrategy[manyret.pss.size()]));
            $pss.addAll(Arrays.asList(sw.getPS()));
            $depth = _d +1;
        }else{
            // Build as many list as different values of "attribute" in "in"
            TIntObjectHashMap<ArrayList<Arc>> sublists = new TIntObjectHashMap<ArrayList<Arc>>();
            for(int i = 0; i< in.size(); i++){
                Arc arc = in.get(i);
                int ev = a.eval(arc);
                if(!sublists.contains(ev)){
                    ArrayList<Arc> evlist = new ArrayList<Arc>();
                    sublists.put(ev, evlist);
                }
                sublists.get(ev).add(arc);
            }
            int[] evs = sublists.keys();
            ArrayList<ArrayList<PropagationStrategy>> _pss = new ArrayList<ArrayList<PropagationStrategy>>(evs.length);
            int _d = 0;
            for (int k = 0; k < evs.length; k++) {
                int ev = evs[k];
                input.seek(m_idx);
                FlatzincFullExtWalker.many_return manyret = many(sublists.get(ev));
                _pss.add(manyret.pss);
                assert (k == 0 || _d == manyret.depth);
                _d = manyret.depth;
            }
            $depth = _d +1;
            input.release(m_idx);
            for (int p = 0; p < _pss.size(); p++) {
                ArrayList<PropagationStrategy> _ps = _pss.get(p);
                input.seek(c_idx);
                $pss.add(coll(_ps, ca));
            }
            input.release(c_idx);
        }
    }
}
    :   ^(MANY1 a=attribute {c_idx = input.mark();} . )
    |   ^(MANY2 a=attribute ca=comb_attr {c_idx = input.mark();} . )
    |   ^(MANY3 a=attribute {m_idx = input.mark();}.  {c_idx = input.mark();} . )
    |   ^(MANY4 a=attribute ca=comb_attr {m_idx = input.mark();} . {c_idx = input.mark();} . )
	;

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////


coll    [ArrayList<? extends ISchedulable> elements, CombinedAttribute ca] returns [PropagationStrategy ps]
@init{
    if(elements.isEmpty()){
        LOGGER.error("\% Create a empty collection");
        throw new FZNException("Create a empty collection");
    }else if(elements.size() == 1){
        LOGGER.warn("\% Create a collection with a single element");
    }
}

    :	^(QUEUE it=qiter)
    {
    $ps = new Queue(elements.toArray(new ISchedulable[elements.size()]));
    $ps = it.set($ps);
    $ps.attachEvaluator(ca);
    }
    |	^(LIST r=REV? it=liter)
    {
    ISchedulable[] elts = elements.toArray(new ISchedulable[elements.size()]);
    // check if an order is required
    boolean order = false;
    for (int i = 0; i < elts.length; i++) {
        try {
            elts[i].evaluate();
            order = true;
        } catch (NullPointerException npe) {
            if (order) {
                LOGGER.error("\% Cannot sort the collection, keys are missing");
                throw new FZNException("Cannot sort the collection, keys are missing");
            }
        }
    }
    $ps = new Sort(order, r != null, elts);
    $ps = it.set($ps);
    $ps.attachEvaluator(ca);
    }
    |	^(HEAP m=MAX? it=qiter)
	{
	ISchedulable[] elts = elements.toArray(new ISchedulable[elements.size()]);
    for (int i = 0; i < elts.length; i++) {
        try {
            elts[i].evaluate();
        } catch (NullPointerException npe) {
                LOGGER.error("\% Cannot sort the collection, keys are missing");
                throw new FZNException("Cannot sort the collection, keys are missing");
        }
    }
    $ps = new SortDyn(m != null, elts);
    $ps = it.set($ps);
    $ps.attachEvaluator(ca);
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



comb_attr   returns[CombinedAttribute ca]
@init{
    ArrayList<AttributeOperator> aos = new ArrayList<AttributeOperator>();
}
@after{
    $ca = new CombinedAttribute(aos, ea);
}
    :	^(CA1 (ao = attr_op{aos.add(ao);})* ea=attribute?)
    |   ^(CA2 (ao = attr_op{aos.add(ao);})+ ea=attribute)
	;

attr_op returns[AttributeOperator ao]
    :	ANY {$ao = AttributeOperator.ANY;}
    |   MIN {$ao = AttributeOperator.MIN;}
    |   MAX {$ao = AttributeOperator.MAX;}
    |   SUM {$ao = AttributeOperator.SUM;}
    |   SIZE{$ao = AttributeOperator.SIZE;}
	;


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////  BACK TO FLATZINC  ///////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
    :   LB RB
    {
    $exp = new ESetList(new ArrayList());
    }
    |
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
//        $exp = nex EIdentifier(datas, $IDENTIFIER.text);
//    }else if(exps != null) {
//        $exps = new EArray(exps);
//    }else if($INT_CONST != null){
//        $exps = new EIdArray(datas, $IDENTIFIER.text, i);
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
        $exp = new EAnnotation(new EIdentifier(datas, $IDENTIFIER.text), exps);
    }else if($i!=null) {
        $exp = new EIdArray(datas, $IDENTIFIER.text, Integer.parseInt($i.text));
    }else{
        $exp = new EIdentifier(datas, $IDENTIFIER.text);
    }
    }
    ;


param_decl
	:   ^(PAR IDENTIFIER pt=par_type e=expr)
	{
	// Parameter(THashMap<String, Object> datas, Declaration type, String identifier, Expression expression)
    FParameter.make_parameter(datas, pt, $IDENTIFIER.text, e);
    }
	;


var_decl
	:   ^(VAR IDENTIFIER vt=var_type anns=annotations e=expr?)
	{
	FVariable.make_variable(datas, vt, $IDENTIFIER.text, anns, e, mSolver);
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
	FConstraint.make_constraint(mSolver, datas, id, exps, anns);
	}
	;

solve_goal
	:
	^(SOLVE anns=annotations res=resolution)
	{
    FGoal.define_goal(datas, mSolver,anns,res.type,res.expr);
	}
	;

resolution  returns[ResolutionPolicy type, Expression expr]
    :   SATISFY
    {
    $type=ResolutionPolicy.SATISFACTION;
    $expr=null;
    }
    |   ^(MINIMIZE e=expr)
    {
    $type=ResolutionPolicy.MINIMIZE;
    $expr=e;
    }
    |   ^(MAXIMIZE e=expr)
    {
    $type=ResolutionPolicy.MAXIMIZE;
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
    $ann = new EAnnotation(new EIdentifier(datas,$IDENTIFIER.text), exps);
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
