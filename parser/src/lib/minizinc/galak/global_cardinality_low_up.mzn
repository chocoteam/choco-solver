%-----------------------------------------------------------------------------%
% Requires that for all `i', the value `cover[i]' appears at least `lbound[i]'
% and at most `ubound[i]' times in the array `x'.
%-----------------------------------------------------------------------------%

predicate global_cardinality_low_up(array[int] of var int: x,
                                    array[int] of int: cover,
				    array[int] of int: lbound,
                                    array[int] of int: ubound) =
%     forall(i in index_set(cover)) (
%         sum(j in index_set(x)) ( bool2int(x[j] = cover[i]) )
%         in lbound[i]..ubound[i]
%     );
    globalCardinalityLowUpChoco(x,cover,lbound,ubound);

predicate globalCardinalityLowUpChoco(array[int] of var int: x,
                                    array[int] of int: cover,
                    array[int] of int: lbound,
                                    array[int] of int: ubound);

