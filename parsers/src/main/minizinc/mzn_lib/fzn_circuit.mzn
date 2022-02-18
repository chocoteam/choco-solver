%-----------------------------------------------------------------------------%
% Constrains the elements of 'x' to define a circuit where 'x[i] = j' means
% that 'j' is the successor of 'i'.
%-----------------------------------------------------------------------------%

predicate fzn_circuit(int: offset, array[int] of var int: x);

predicate fzn_circuit(array[int] of var int: x) =
  if min(index_set(x)) >= 0 then
    fzn_circuit(min(index_set(x)),x)
  else
    fzn_circuit(0,[x[i]-min(index_set(x)) | i in index_set(x)])
  endif;