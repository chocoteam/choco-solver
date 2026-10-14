---
title: "Project Euler, Problem 103"
type: docs
date: 2021-02-02T09:21:16+01:00
author: Charles Prud'homme
weight: 104
description: >  
  Project Euler, Problem 103
---


Thanks to [Mathieu Vavrille](https://www.univ-nantes.fr/mathieu-vavrille) for his modelling of [problem 103 of the Euler project](https://projecteuler.net/problem=103), "Special subset sums: optimum", using Constraint Programming and Choco.



{{< tabpane langEqualsHeader=true >}} 
{{< tab "Java" >}}
public class Euler {

  public static void main(final String[] args) {
    System.out.println(euler103());
  }

  /** Euler 103, Special subset sums */
  private static String euler103() {
    int n = 7;
    int bound = 40;
    List<Set<Integer>> allPartitions = partitions(n-1); // Enumerates all the partitions of {0, ..., n-1}
    Model model = new Model();
    IntVar[] mainSet = model.intVarArray("set", n, 0, 100); // Numbers in the set
    for (int i = 0; i < n-1; i++) {
      mainSet[i].lt(mainSet[i+1]).post(); // Order the numbers in the set
    }
    IntVar[] partitionSums = model.intVarArray("partition", allPartitions.size(), 0, n*bound); // Create a variable representing the sum of each subset
    for (int i = 0; i < partitionSums.length; i++)
      model.sum(allPartitions.get(i).stream()
                .map(id -> mainSet[id])
                .toArray(IntVar[]::new), "=", partitionSums[i]).post();
  for (int i = 0; i < allPartitions.size(); i++)
      for (int j = i+1; j < allPartitions.size(); j++)
        if (Collections.disjoint(allPartitions.get(i), allPartitions.get(j))) { // Enforce the constraint for distinct subsets
          if (allPartitions.get(i).size() > allPartitions.get(j).size())
            partitionSums[i].gt(partitionSums[j]).post();
          else if (allPartitions.get(i).size() == allPartitions.get(j).size())
            partitionSums[i].ne(partitionSums[j]).post();
          else if (allPartitions.get(i).size() < allPartitions.get(j).size())
            partitionSums[i].lt(partitionSums[j]).post();
        }
  model.setObjective(false, partitionSums[partitionSums.length-1]); // Objective is the sum of the variables in the set
  Solver solver = model.getSolver();
  String result = "";
  while (solver.solve()) { // Solve
    result = Arrays.stream(mainSet)
      .map(var -> Integer.toString(var.getValue()))
      .collect(Collectors.joining());
  }
  return result;
  }
  
  private static List<Set<Integer>> partitions(final int n) {
    if (n == 0) {
      List<Set<Integer>> parts = new ArrayList<Set<Integer>>();
      parts.add(new HashSet<Integer>());
      parts.add(Set.of(0));
      return parts;
    }
    List<Set<Integer>> smaller = partitions(n-1);
    for (int i = 0; i < 1<<n; i++) {
      Set<Integer> currentAdded = new HashSet<Integer>(smaller.get(i));
      currentAdded.add(n);
      smaller.add(currentAdded);
    }
    return smaller;
  }
  
}
{{< /tab >}}
{{< /tabpane >}}

