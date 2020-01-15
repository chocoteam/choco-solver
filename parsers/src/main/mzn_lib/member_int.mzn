%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate member_int(array[int] of int: x, var int: y) = memberChoco(x,y);
predicate member_int(array[int] of var int: x, var int: y) = memberVarChoco(x,y);

predicate member_int_reif(array[int] of int: x, var int: y, var bool: b) = memberReifChoco(x,y,b);
predicate member_int_reif(array[int] of var int: x, var int: y, var bool: b) = memberVarReifChoco(x,y,b);