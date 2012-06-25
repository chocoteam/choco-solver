predicate all_equal_set(array[int] of var set of int: x) =
	forall(i, j in index_set(x) where i < j) ( x[i] = x[j] );
