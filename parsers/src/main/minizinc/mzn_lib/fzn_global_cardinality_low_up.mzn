%-----------------------------------------------------------------------------%
% Requires that for all `i', the value `cover[i]' appears at least `lbound[i]'
% and at most `ubound[i]' times in the array `x'.
%-----------------------------------------------------------------------------%

predicate choco_fzn_global_cardinality_low_up(array[int] of var int: x,
                    array[int] of int: cover,
				    array[int] of int: lbound,
                    array[int] of int: ubound,
                    bool: closed);

predicate fzn_global_cardinality_low_up(array[int] of var int: x,
                    array[int] of int: cover,
				    array[int] of int: lbound,
                    array[int] of int: ubound) =
    choco_fzn_global_cardinality_low_up(x,cover,lbound,ubound,false);

