%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate member_float(array[int] of var float: x, var float: y) =
    exists(i in index_set(x)) ( x[i] == y );
