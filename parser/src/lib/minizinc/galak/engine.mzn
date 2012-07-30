annotation group(string:name);


annotation engine_def(ann: s);

annotation revision(
    array[int] of string: groups,
    ann : revt
);

annotation queue_engine(
    array[int] of ann: groups,    
    ann: itert, % iteration type
);


annotation list_engine(
    array[int] of ann: groups,
    ann: itert, % iteration type
    ann: compannotation,
);

%-----------------------------------------------------------------------------%
%
% Revision types.
%

annotation re_arc;
annotation re_var;
annotation re_con;

%-----------------------------------------------------------------------------%
%
% Iteration types.
%

annotation it_one;
annotation it_while_one;
annotation it_for;
annotation it_while;

%-----------------------------------------------------------------------------%
%
% Comparison types.
%
annotation inc_priority;
annotation min_dom;
annotation input;