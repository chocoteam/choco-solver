%-----------------------------------------------------------------------------%
% Requires that the array 'x' is lexicographically less than or equal to
% array 'y'.  Compares them from first to last element, regardless of indices
%-----------------------------------------------------------------------------%

predicate lex_lesseq_set(array[int] of var set of int: x,
                         array[int] of var set of int: y) =
    let { int: lx = min(index_set(x)),
          int: ux = max(index_set(x)),
          int: ly = min(index_set(y)),
          int: uy = max(index_set(y)),
          int: size = max(ux - lx, uy - ly),
          array[0..size] of var bool: b }
          % b[i] is true if the lexicographical order holds from position i on.
    in
    b[0]
    /\
    forall(i in 0..size) (
        b[i] = ( x[lx + i] <= y[ly + i]
                 /\
                 if i = size then true
                 else x[lx + i] <  y[ly + i] \/ b[i+1] endif
               )
    );


predicate lex_leq_set(array[int] of var set of int: x,
                      array[int] of var set of int: y) =
          lex_lesseq(x, y);

%-----------------------------------------------------------------------------%
