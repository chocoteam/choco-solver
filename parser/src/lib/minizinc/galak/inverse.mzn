%-----------------------------------------------------------------------------%
% Constrains two arrays of int variables, 'f' and 'invf', to represent
% inverse functions.  All the values in each array must be within the index
% set of the other array.
%-----------------------------------------------------------------------------%
predicate inverse(array[int] of var int: f,
                  array[int] of var int: invf) =
    inverseChanneling(f, invf);

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate inverseChanneling(array[int] of var int: f,
                  array[int] of var int: invf);

%-----------------------------------------------------------------------------%