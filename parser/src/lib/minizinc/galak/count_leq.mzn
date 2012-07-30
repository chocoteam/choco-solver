%-----------------------------------------------------------------------------%
% Constrains 'c' to be less than or equal to the number of occurrences of
% 'y' in 'x'.
%-----------------------------------------------------------------------------%

predicate count_leq(array[int] of var int: x, var int: y, var int: c) =
    c <= sum(i in index_set(x)) ( bool2int(x[i] == y) );
%countLqChoco(x, y, c);

%predicate countLqChoco(array[int] of var int: x, var int: y, var int: c);


%-----------------------------------------------------------------------------%
%-----------------------------------------------------------------------------%
