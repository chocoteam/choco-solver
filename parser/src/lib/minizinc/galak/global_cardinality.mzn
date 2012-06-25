%-----------------------------------------------------------------------------%
% Requires that the number of occurrences of 'cover[i]' in 'x' is 'counts[i]'.
%-----------------------------------------------------------------------------%

predicate global_cardinality(array[int] of var int: x,
	  		     array[int] of int: cover,
                             array[int] of var int: counts) =
    assert(index_set(cover) = index_set(counts),
        "global_cardinality: cover and counts must have identical index sets",
    forall(i in index_set(cover))( count(x, cover[i], counts[i]) )
    /\
    % Implied constraint
    length(x) >= sum(counts)
    );


% MiniZinc 1.1 and before used the following definition for the global
% cardinality constraint.  This version is deprecated and will be removed
% in a future release.
%
predicate global_cardinality_old(array[int] of var int: x,
                             array[int] of var int: c) =
    forall(i in index_set(c)) ( count(x, i, c[i]) );

include "count.mzn";

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
