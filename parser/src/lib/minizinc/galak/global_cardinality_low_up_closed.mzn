%-----------------------------------------------------------------------------%
% Requires that the number of occurrences of 'cover[i]' in 'x' is between
% 'lbound[i]' and 'ubound[i]'.
% The elements of 'x' must take their values from 'cover'.
%-----------------------------------------------------------------------------%

predicate global_cardinality_low_up_closed(array[int] of var int: x,
                                           array[int] of int: cover,
				           array[int] of int: lbound,
                                           array[int] of int: ubound) =
%    forall(i in index_set(x))( x[i] in { d | d in cover } )
%    /\
%    global_cardinality_low_up(x, cover, lbound, ubound)
%    /\
%    % Implied condition
%    length(x) in sum(lbound)..sum(ubound);
    globalCardinalityLowUpClosedChoco(x,cover,lbound,ubound);


predicate globalCardinalityLowUpClosedChoco(array[int] of var int: x,
                                           array[int] of int: cover,
				           array[int] of int: lbound,
                                           array[int] of int: ubound);



include "global_cardinality_low_up.mzn";

%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
