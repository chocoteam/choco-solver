%-----------------------------------------------------------------------------%
% Requires that 's' precede 't' in the array 'x'.
%
% For integer variables this constraint means that if any element of 'x'
% is equal to 't', then another element of 'x' with a  lower index is equal
% to 's'.
% For set variables this constraint means that if an element of 'x'
% contains 't' but not 's', then another element of 'x' with lower index contains
% 's' but not 't'.
%-----------------------------------------------------------------------------%

include "value_precede_int.mzn";
include "value_precede_set.mzn";

predicate value_precede(int: s, int: t, array[int] of var int: x) =
    value_precede_int(s, t, x);
predicate value_precede(int: s, int: t, array[int] of var set of int: x) =
    value_precede_set(s, t, x);
