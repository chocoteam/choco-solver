%-----------------------------------------------------------------------------%
% Constrains 'c' to be the number of occurrences of 'y' in 'x'. This
% is less general than the Beldiceanu version, which takes an extra
% relational operation argument, and so allows "equal" in the previous
% sentence to be something else, eg. greater-than.
%-----------------------------------------------------------------------------%
predicate count(array[int] of var int: x, var int: y, var int: c) =
    %Count(x, y, c);
    c = sum(i in index_set(x)) ( bool2int(x[i] == y) );
%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
%predicate count(array[int] of var int: x, var int: y, var int: c);

%-----------------------------------------------------------------------------%
