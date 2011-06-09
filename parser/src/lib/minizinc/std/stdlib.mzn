%-----------------------------------------------------------------------------%
% MiniZinc standard library.
%-----------------------------------------------------------------------------%
% This file contains built-in operations that can be expressed in MiniZinc,
% and so are not implemented as true built-ins within the compiler.

%-----------------------------------------------------------------------------%
%
% Search annotations
%

annotation seq_search(array[int] of ann: s);

annotation int_search(
    array[int] of var int: vars,
    ann: select,
    ann: choice,
    ann: explore,
);

annotation int_search_all(
    ann: select,
    ann: choice,
    ann: explore
);

annotation int_search_bd(
    ann: a,
    ann: b,
    ann: c,
    ann: d
);

annotation bool_search(
    array[int] of var bool: vars,
    ann: select,
    ann: choice,
    ann: explore
);

annotation float_search(
    array[int] of var float: vars,
    float: prec,
    ann: select,
    ann: choice,
    ann: explore
);

annotation set_search(
    array[int] of var set of int: vars,
    ann: select,
    ann: choice,
    ann: explore
);

annotation labelling_ff;

%-----------------------------------------------------------------------------%
%
% Variable selection annotations.
%

annotation input_order;
annotation first_fail;
annotation anti_first_fail;
annotation smallest;
annotation largest;
annotation occurrence;
annotation most_constrained;
annotation max_regret;
annotation dom_w_deg;
annotation impact;

%-----------------------------------------------------------------------------%
%
% Domain reduction strategies.
%

annotation indomain;
annotation indomain_min;
annotation indomain_max;
annotation indomain_middle;
annotation indomain_median;
annotation indomain_random;
annotation indomain_split;
annotation indomain_split_random;
annotation indomain_reverse_split;
annotation indomain_interval;
annotation outdomain_min;
annotation outdomain_median;
annotation outdomain_random;

%-----------------------------------------------------------------------------%
%
% Exploration strategies.
%

annotation complete;
annotation bbs(int: s);
annotation fail;
annotation lds(int: d);
annotation credit(int: c, ann: explore);
annotation dbs(int: d, ann: explore);
annotation restart_geometric(float: i, float: s, ann: search);
annotation limit(ann: measure, int: value, ann: search);
annotation time;

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%

% Include solver-specific redefinitions for any FlatZinc built-ins.
%
include "redefinitions.mzn"

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
