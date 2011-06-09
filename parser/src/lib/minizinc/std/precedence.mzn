%-----------------------------------------------------------------------------%
% precedence ensures that first occurrence of i is before that of j for i<j
% and that the first value used is the value 1; it is used to break value
% symmetry.
%-----------------------------------------------------------------------------%

predicate precedence(array[int] of var int: x) =
    let { int: lx = min(index_set(x)),
          int: ux = max(index_set(x)),
	  array[0..ux-lx+1] of var int: y }
    in (
    	   y[0] = 0 /\
	   forall (i in 0..ux-lx) (
	        y[i+1] <= y[i]+1 /\
		y[i+1] = max(y[i],x[lx+i]) /\
		x[lx+i] <= y[i]+1
	   )
    );
