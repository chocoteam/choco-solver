%-----------------------------------------------------------------------------%
% Requires that the number of distinct values in 'x' is 'n'.
%-----------------------------------------------------------------------------%
predicate nvalue(var int: n, array[int] of var int: x) =
%    let { int: lx = lb_array(x),
%          int: ux = ub_array(x),
%        } in
%        n == sum(j in lx..ux) (
%	   bool2int(exists(i in index_set(x)) ( x[i] = j ))
%        );
nvalueChoco(n,x);

predicate nvalueChoco(var int: n, array[int] of var int: x);
