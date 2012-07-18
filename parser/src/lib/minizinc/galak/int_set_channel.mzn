%-----------------------------------------------------------------------------%
% Requires that array of int variables 'x' and array of set variables 'y'
% are related such that '(x[i] = j) <-> (i in y[j])'.
%-----------------------------------------------------------------------------%
predicate int_set_channel(array[int] of var int: x,
                          array[int] of var set of int: y) =
    forall(i in index_set(x), j in index_set(y)) ( x[i] == j <-> i in y[j] );

