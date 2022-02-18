%-----------------------------------------------------------------------------%
% Requires that the number of occurences of 'i' in 'x' is 'counts[i]'.
% The elements of 'x' must take their values from 'cover'.
%-----------------------------------------------------------------------------%

predicate choco_fzn_global_cardinality(array[int] of var int: x,
	  		     array[int] of int: cover,
                 array[int] of var int: counts,
                 bool: closed);

predicate fzn_global_cardinality_closed(array[int] of var int: x,
                array[int] of int: cover,
                array[int] of var int: counts) =
    choco_fzn_global_cardinality(x,cover,counts,true);

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
