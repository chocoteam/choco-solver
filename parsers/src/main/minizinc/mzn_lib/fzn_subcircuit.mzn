%-----------------------------------------------------------------------------%
% Constrains the elements of 'x' to define a subcircuit where 'x[i] = j'
% means that 'j' is the successor of 'i' and 'x[i] = i' means that 'i'
% is not in the circuit.
%-----------------------------------------------------------------------------%

predicate fzn_subcircuit(int: offset, array[int] of var int: x);

predicate fzn_subcircuit(array[int] of var int: x) =
  if min(index_set(x)) >= 0 then
    fzn_subcircuit(min(index_set(x)),x)
  else
    fzn_subcircuit(0,[x[i]-min(index_set(x)) | i in index_set(x)])
  endif;

%-----------------------------------------------------------------------------%
