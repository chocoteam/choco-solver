%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%

predicate element_bool(var int: i, array[int] of var bool: x, var bool: y) =
    y = x[i];
