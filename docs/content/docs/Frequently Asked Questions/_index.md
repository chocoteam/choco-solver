---
title: "FAQ"
author: "Charles Prud'homme"
date: 2024-02-19T15:08:22+01:00
weight: 6
description: >
  Frequently Asked Questions
---

### How can I cite Choco-solver?
The [Considerations](/docs/considerations) page indicates the correct reference to use when quoting choco-solver in an article.

### Can a constraint be removed once resolution has begun?

First and foremost, it should be noted that choco-solver was not initially designed to safely manage the removal of constraints or variables. 
The standard scenario for using the library is as follows: declaration of the model, that is adding variables and constraints then search for solutions (satisfaction or optimisation). It may be possible to declare variables and constraints during resolution, but this is not a problem since adding constraints simply reduces the search space.

Before resolution has begun, it is possible to remove a constraint. This is done using the `unpost()` method for constraints that have been added explicitly using the `post()` command. For constraints that have been added implicitly, for example reified, deletion is not possible. 

Once the resolution begins, or more precisely as soon as the first propagation is triggered, removing/unposting a constraint becomes problematic.
The search is Depth-First which implies the variable domains to be saved and restored. So removing a constraint potentially makes the variables' domain inconsistent upon backtracks. In fact, for a constraint removal to be possible, it would have to be possible to restore the values that have been removed from the variable domain by its filtering algorithm

One way around this problem is to save the branch of the search tree (i.e. the path of decisions leading to the current state), go back to the initial node of the search, remove the constraint, apply each decision in order, saving and propagating, until the desired state is reached.

Note that during the resolution process, it is possible to add a constraint temporarily, using the `postTemp` method in the model.
Its lifetime is limited to the induced sub-tree, since it is automatically removed at the backtrack without any intervention. 
