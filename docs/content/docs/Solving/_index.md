---
title: "Solving"
date: 2020-01-07T16:08:22+01:00
weight: 3
description: >
  What does your user need to know to solve a problem?
---

Up to here, we have seen how to model a problem with the `Model` object. To solve it, we need to use
the `Solver` object that is obtained from the model as follows:

```java
Solver solver = model.getSolver();
```

The `Solver` is in charge of alternating constraint-propagation with search, and possibly learning,
in order to compute solutions. This object may be configured in various ways.