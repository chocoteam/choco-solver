%-----------------------------------------------------------------------------%
% Constrains 'c' to be less than or equal to the number of occurrences of
% 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate fzn_count_leq(array[int] of var int: x, var int: y, var int: c);
