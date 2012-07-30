%-----------------------------------------------------------------------------%
% MiniZinc standard library (Draft for version MiniZinc 2.0)
%-----------------------------------------------------------------------------%
% This file contains built-in operations that can be expressed in MiniZinc,
% and so are not implemented as true built-ins within the compiler.

%-----------------------------------------------------------------------------%
%
% Search annotations
%

annotation bool_search(
    array[int] of var bool: vars,
    ann: select,
    ann: choice
);

annotation int_search(
    array[int] of var int: vars,
    ann: select,
    ann: choice
);

annotation set_search(
    array[int] of var set of int: vars,
    ann: select,
    ann: choice,
);

annotation float_search(
    array[int] of var float: vars,
    float: prec,
    ann: select,
    ann: choice
);

annotation int_search_all(
    ann: select,
    ann: choice
);

annotation set_search_all(
    ann: select,
    ann: choice
);

annotation seq_search(array[int] of ann: s);
annotation par_search(array[int] of ann: s);

annotation sample_search(
    ann: select,
    ann: limit
);

annotation backdoor_search(
    ann: select,
    float: ratio,
    ann: limit,
    array[int] of ann: s
);

% Search with limits and restart annotations
annotation limit_search(ann: measure, int: value, ann: search);
annotation restart_geometric(float: factor, float: value, ann: search);
annotation restart_luby(float: factor, float: value, ann: search);

% Sequential variable selection strategies
annotation seq_vss(array[int] of ann: select);

% Multiple domain selection strategies
annotation dss(array[int] of ann: choice);

% Combine and manipulate variable selection scores
annotation weight_score(ann: select, float: weight);
annotation sum_score(array[int] of ann: weight_score);

% Measures to limit search (e.g., run-time, number of search nodes, fails)
annotation measure;

%-----------------------------------------------------------------------------%
%
% Variable selection annotations.
%

annotation input_order;
annotation reverse_input_order;

annotation random_order;

annotation min_lb;
annotation min_ub;
annotation max_lb;
annotation max_ub;

annotation min_dom_size;
annotation max_dom_size;

annotation min_degree;
annotation max_degree;

annotation min_lb_regret;
annotation max_lb_regret;
annotation min_ub_regret;
annotation max_ub_regret;

annotation min_dom_size_degree;
annotation max_dom_size_degree;
annotation min_dom_size_weighted_degree;
annotation max_dom_size_weighted_degree;

annotation min_impact;
annotation max_impact;

annotation min_activity;
annotation max_activity;

%-----------------------------------------------------------------------------%
%
% Domain reduction strategies.
%

annotation assign_lb;
annotation assign_ub;
annotation exclude_lb;
annotation exclude_ub;

annotation assign_mean;
annotation exclude_mean;

annotation assign_median;
annotation exclude_median;

annotation assign_random;
annotation exclude_random;

annotation assign_impact_min;
annotation assign_impact_max;
annotation exclude_impact_min;
annotation exclude_impact_max;

annotation assign_activity_min;
annotation assign_activity_max;
annotation exclude_activity_min;
annotation exclude_activity_max;

annotation include_min;
annotation include_max;
annotation exclude_min;
annotation exclude_max;

annotation enumerate_lb;
annotation enumerate_ub;

annotation bisect_low;
annotation bisect_high;
annotation bisect_median_low;
annotation bisect_median_high;
annotation bisect_random_low
annotation bisect_random_high;
annotation bisect_interval_low;
annotation bisect_interval_high;
annotation bisect_impact_min;
annotation bisect_impact_max;
annotation bisect_activity_min;
annotation bisect_activity_max;

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%

% Include solver-specific redefinitions for any FlatZinc built-ins.
%
include "redefinitions.mzn"

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
