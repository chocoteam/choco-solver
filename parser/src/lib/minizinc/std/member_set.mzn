%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate member_set(array[int] of var set of int: x, var set of int: y) =
    exists(i in index_set(x)) ( x[i] == y );
