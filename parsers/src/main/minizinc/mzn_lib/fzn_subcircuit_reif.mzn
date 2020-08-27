%-----------------------------------------------------------------------------%
% Constrains the elements of 'x' to define a subcircuit where 'x[i] = j'
% means that 'j' is the successor of 'i' and 'x[i] = i' means that 'i'
% is not in the circuit.
%-----------------------------------------------------------------------------%

predicate fzn_subcircuit_reif(array[int] of var int: x, var bool: b) =
    abort("Reified subcircuit/1 is not supported.");

%-----------------------------------------------------------------------------%
