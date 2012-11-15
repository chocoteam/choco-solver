%-----------------------------------------------------------------------------%
% Constrains 'c' to be the number of occurrences of 'y' in 'x'. 
%-----------------------------------------------------------------------------%

predicate count_eq(array[int] of var int: x, var int: y, var int: c) =
    c = sum(i in index_set(x)) ( bool2int(x[i] == y) );
%    count_eqchoco(x,y,c);

predicate count(array[int] of var int: x, int: y, var int: c) =
    count_eqchoco(x, y, c);

predicate count_eqchoco(array[int] of var int: x, int: y, var int: c);

%-----------------------------------------------------------------------------%