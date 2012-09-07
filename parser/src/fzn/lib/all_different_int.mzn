%-----------------------------------------------------------------------------%
% Constrains the array of objects 'x' to be all different.
%-----------------------------------------------------------------------------%

predicate all_different_int(array[int] of var int: x) =
    alldifferentChoco(x);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate alldifferentChoco(array[int] of var int: x);

%-----------------------------------------------------------------------------%