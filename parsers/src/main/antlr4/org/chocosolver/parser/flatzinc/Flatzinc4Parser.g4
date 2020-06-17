parser grammar Flatzinc4Parser;

options{
    language = Java;
    //output=AST;
    tokenVocab=Flatzinc4Lexer;
}

@header {
import org.chocosolver.parser.flatzinc.ast.*;
import org.chocosolver.parser.flatzinc.ast.declaration.*;
import org.chocosolver.parser.flatzinc.ast.expression.*;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Model;

import java.util.ArrayList;
import java.util.List;
}

@members{

public Datas datas;

// the model
public Model mModel;
}


// PARSER RULES

flatzinc_model [Model aModel, Datas datas]
	:
	{
    this.mModel = aModel;
    this.datas = datas;
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
	FVariable.make_variable(datas, $vt.decl, $IDENTIFIER.text, $anns.anns, $eq!=null?$e.exp:null, mModel);
    }
	;

constraint
	:
	{
    //  Model aModel, String id, List<Expression> exps, List<EAnnotation> annotations
    ArrayList<Expression> exps = new ArrayList();
    }
	    CONSTRAINT IDENTIFIER LP e=expr {exps.add($e.exp);} (CM e=expr{exps.add($e.exp);})* RP anns=annotations SC
    {
    String name = $IDENTIFIER.text;
    FConstraint.valueOf(name).build(mModel, datas, name, exps, $anns.anns);
    }
	;

solve_goal
	:   SOLVE anns=annotations res=resolution SC
	{
    FGoal.define_goal(mModel, $anns.anns,$res.rtype,$res.exp);
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
