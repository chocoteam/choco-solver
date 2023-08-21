predicate float_ceil(var float: x, var int: y) =
    y - 1 < x /\ x <= y;

predicate float_floor(var float: x, var int: y) =
    y <= x /\ x < y + 1;

predicate float_round(var float: x, var int: y) =
    if lb(x) >= 0 then float_floor(x+0.5, y)
    elseif ub(x) <= 0 then float_ceil(x-0.5, y)
    else if x < 0 then float_ceil(x-0.5, y) else float_floor(x+0.5, y) endif endif;
