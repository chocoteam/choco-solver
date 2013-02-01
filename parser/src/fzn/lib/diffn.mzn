%-------------------------------------------------------------------------------------------%
% Constrains rectangles, given by their origins x,y and sizes dx,dy, to be non-overlapping.
%-------------------------------------------------------------------------------------------%

predicate diffn(array[int] of var int: x,  array[int] of var int: y,
                array[int] of var int: dx, array[int] of var int: dy) =
    diffnChoco(x,y,dx,dy);

%-------------------------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate diffnChoco(array[int] of var int: x, array[int] of var int: y
                     array[int] of var int: dx, array[int] of var int: dy);

%-------------------------------------------------------------------------------------------%