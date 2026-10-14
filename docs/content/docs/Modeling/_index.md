---
title: "Modeling"
linkTitle: "Modeling"
weight: 2
description: >
  What do you need to know to express a problem?
---

## The Model

The object `Model` is the key component. It should be imported :

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
import org.chocosolver.solver.Model;
{{< /tab >}} 
{{< tab header="Python" >}}
from pychoco import Model
{{< /tab >}} 
{{< /tabpane >}}

Then, an instance of a model is built as follows:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
Model model = new Model();
{{< /tab >}} 
{{< tab header="Python" >}}
model = Model()
{{< /tab >}} 
{{< /tabpane >}}

A name can be attached to an instance:

{{< tabpane langEqualsHeader=true >}} 
{{< tab header="Java" >}}
Model model = new Model("my problem");
{{< /tab >}} 
{{< tab header="Python" >}}
model = Model("my problem");
{{< /tab >}} 
{{< /tabpane >}}

`Model` is the top-level object that stores declared variables, posted constraints and gives access to the `Solver`. 

{{% pageinfo %}}
This should be the first instruction, prior to any other modeling instructions, as it is needed to declare variables and constraints.
{{% /pageinfo %}}




Once the model is created, variables and constraints can be defined.
