grammar SimpleCalc;

// parser rules

tokens {
    QUEUE='queue';
    LIST='list';
    HEAP='heap';
    ARC='arc';
    VAR='var';
    PROP='prop';
    SWITCH='switch';
    ONE='one';
    WONE='wone';
    FOR='for';
    WFOR='wfor';
    INC='inc';
    DEC='dec';
    ASSERT='assert';
    MAX='max';
    MIN='min';
    PRIORITY='priority';
    DEGREE='degree';
    OPEQ='==';
    OPNQ='!=';
    OPGT='>';
    OPLT='<';
    LP='(';
    RP=')';
    CO=',';
}

@header {
	package generator;
	import solver.propagation.generator.Queue;
	import solver.propagation.generator.Sort;
	import solver.propagation.generator.SortDyn;
	import solver.propagation.generator.PArc;
	import solver.propagation.generator.PVar;
	import solver.propagation.generator.PCons;
	import solver.propagation.generator.PCons;
	import solver.propagation.generator.Generator;
}

@lexer::header {
	package generator;
}

@member{

}


eval
    :   PRIORITY 
    |   DEGREE
    ;

func
    :   MIN
	|   MAX
	;

funcExp
    :   func LP eval  RP
    ;

comp
    :   INC
    |   DEC
    ;

compExp
    :   comp LP funcExp RP
    ;

opers
    :   OPEQ
    |   OPNQ
    |   OPGT
    |   OPLT
    ;

predExp
    :  ASSERT LP eval opers (DOUBLE | INT) RP
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

recs	:	rec (CO rec)*;

group
    :   QUEUE LP recs CO qiter RP
    |   LIST LP recs CO liter (CO compExp)? RP
    |   HEAP LP recs CO qiter CO funcExp RP
    ;

groups
    :   group ( CO  group)*
    ;

switcher
    :   SWITCH LP groups CO funcExp RP
    ;

parse
  :  group
  ;

targets
	: NAME ( CO  NAME)*
	;

rec returns [Generator element]
    :   ARC LP (targets | predExp) RP   {element = null;}
    |   VAR LP (targets | predExp) RP   {element = null;}
    |   PROP LP (targets | predExp) RP  {element = null;}
    |   group                           {element = null;}
    |   switcher                        {element = null;}
    ;

// lexer rules

INT
    : ('0'..'9')+
    ;

DOUBLE
    : INT+'.'INT+
    ;

NAME
    : ('a'..'z'|'A'..'Z'|'0'..'9'|'.')+
    ;

/* We're going to ignore all white space characters */
WS
    :   (' ' | '\t' | '\r'| '\n') {$channel=HIDDEN;}
;