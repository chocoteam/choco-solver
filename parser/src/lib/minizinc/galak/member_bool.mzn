%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate member_bool(array[int] of var bool: x, var bool: y) =
    exists(i in index_set(x)) ( x[i] == y );
