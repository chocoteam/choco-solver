%-----------------------------------------------------------------------------%
% Constrains 'm' to be the maximum of the values in 'x'.
% Assumptions: |x| > 0.
%-----------------------------------------------------------------------------%

predicate maximum_int(var int: m, array[int] of var int: x) =
%    let { int: l = min(index_set(x)),
%          int: u = max(index_set(x)),
%          int: ly = lb_array(x),
%          int: uy = ub_array(x),
%          array[l..u] of var ly..uy: y } in
%    y[l] = x[l] /\
%    m = y[u] /\
%    forall (i in l+1 .. u) ( y[i] == max(x[i],y[i-1]) );
maximumChoco(m,x);

predicate maximumChoco(var int: m, array[int] of var int: x);