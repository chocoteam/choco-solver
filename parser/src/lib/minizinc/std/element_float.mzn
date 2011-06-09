%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%

predicate element_float(var int: i, array[int] of var float: x, var float: y) =
    y = x[i];
