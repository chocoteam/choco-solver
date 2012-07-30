%-----------------------------------------------------------------------------%
% Constrains 'm' to be the maximum of the values in 'x'.
% Assumptions: |x| > 0.
%-----------------------------------------------------------------------------%

include "maximum_int.mzn";
include "maximum_float.mzn";

predicate maximum(var int: m, array[int] of var int: x) =
    maximum_int(m, x);

predicate maximum(var float: m, array[int] of var float: x) =
    maximum_float(m, x);

    % XXX: currently doesn't work:  same problem as 'minimum' above.
%predicate maximum(var set of int: m, array[int] of var set of int: x) =
%    let { int: l = min(index_set(x)),
%          int: u = max(index_set(x)),
%          set of int: uy = ub(x),
%          array[l..u] of var set of uy: y } in
%    y[l] = x[l] /\
%    m = y[u] /\
%    forall (i in l+1 .. u) ( y[i] == max(x[i],y[i-1]) );

