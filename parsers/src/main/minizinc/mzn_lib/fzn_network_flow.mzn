
predicate choco_fzn_network_flow(array[int] of int: arc,
                                 array[int] of int: balance,
                                 array[int] of var int: flow);

predicate fzn_network_flow(array[int,1..2] of int: arc,
	  	                   array[int] of int: balance,
                           array[int] of var int: flow) =
          choco_fzn_network_flow(array1d(arc),
                                 balance,
                                 flow);
