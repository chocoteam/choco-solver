%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate fzn_member_int(array[int] of int: x, var int: y);
predicate fzn_member_int_var(array[int] of var int: x, var int: y);

predicate fzn_member_int(array[int] of int: x, var int: y) = fzn_member_int(x,y);
predicate fzn_member_int(array[int] of var int: x, var int: y) = fzn_member_int_var(x,y);