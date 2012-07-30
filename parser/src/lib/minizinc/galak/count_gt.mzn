%-----------------------------------------------------------------------------%
% Constrains 'c' to be strictly greater than the number of occurrences of 'y'
% in 'x'.
%-----------------------------------------------------------------------------%

predicate count_gt(array[int] of var int: x, var int: y, var int: c) =
    c > sum(i in index_set(x)) ( bool2int(x[i] == y) );

%countGqChoco(x, y, c+1);

%predicate countGqChoco(array[int] of var int: x, var int: y, var int: c);
%-----------------------------------------------------------------------------%
