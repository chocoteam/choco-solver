predicate choco_fzn_network_flow_cost(array [int] of int: arc,
                                 array[int] of int: balance,
                                 array[int] of int: weight,
                                 array[int] of var int: flow, var int: cost);

predicate fzn_network_flow_cost(array[int,1..2] of int: arc,
	  	                        array[int] of int: balance,
	  	                        array[int] of int: weight,
	  	                        array[int] of var int: flow,
	  	                        var int: cost)=
            choco_fzn_network_flow_cost(array1d(arc),
                                        balance,
                                        weight,
                                        flow,
                                        cost);
