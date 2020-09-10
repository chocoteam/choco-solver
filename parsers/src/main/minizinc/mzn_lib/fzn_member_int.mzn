%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate fzn_member_int(array[int] of int: x, var int: y) = memberChoco(x,y);
predicate fzn_member_int(array[int] of var int: x, var int: y) = memberVarChoco(x,y);