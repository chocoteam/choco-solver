%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate choco_member_int_reif(array[int] of int: x, var int: y, var bool: b);
predicate choco_member_int_var_reif(array[int] of var int: x, var int: y, var bool: b);

predicate fzn_member_int_reif(array[int] of int: x, var int: y, var bool: b) = choco_member_int_reif(x,y,b);
predicate fzn_member_int_reif(array[int] of var int: x, var int: y, var bool: b) = choco_member_int_var_reif(x,y,b);