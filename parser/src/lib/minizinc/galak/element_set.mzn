%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%

predicate element_set(var int: i, array[int] of var set of int: x,
        var set of int: y) =
    y = x[i];
