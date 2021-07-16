%-------------------------------------------------------------------------------------------%
% Constrains rectangles, given by their origins x,y and sizes dx,dy, to be non-overlapping.
%-------------------------------------------------------------------------------------------%

predicate fzn_diffn(array[int] of var int: x,  array[int] of var int: y,
                array[int] of var int: dx, array[int] of var int: dy);
