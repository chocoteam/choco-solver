%-----------------------------------------------------------------------------%
% Requires that the number of occurrences of 'cover[i]' in 'x' is 'counts[i]'.
%-----------------------------------------------------------------------------%

predicate fzn_global_cardinality(array[int] of var int: x,
	  		     array[int] of int: cover,
                 array[int] of var int: counts) =
    globalCardinalityChoco(x,cover,counts,false);


%-----------------------------------------------------------------------------%
% MiniZinc 1.1 and before used the following definition for the global
% cardinality constraint.  This version is deprecated and will be removed
% in a future release.
%
%predicate fzn_global_cardinality_old(array[int] of var int: x,
%                             array[int] of var int: c) =
%    forall(i in index_set(c)) ( count_eqchoco(x, i, c[i]) );
%
%include "fzn_count_eq.mzn";

%-----------------------------------------------------------------------------%


