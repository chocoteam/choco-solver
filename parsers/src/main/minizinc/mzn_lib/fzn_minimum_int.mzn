%-----------------------------------------------------------------------------%
% Constrains 'm' to be the minimum of the values in 'x'.
% Assumptions: |x| > 0.
%-----------------------------------------------------------------------------%

predicate fzn_minimum_int(var int: m, array[int] of var int: x);

