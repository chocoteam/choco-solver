# samples
Choco-solver in practice
========================


This project hosts samples based on choco-solver.

To run a sample, please follow the steps:

1. Download the source code of [samples](https://github.com/chocoteam/samples/releases/tag/samples-4.0.1)
2. Download [choco-solver-4.0.0.zip](https://github.com/chocoteam/choco-solver/releases/tag/choco-4.0.0) and unzip it
3. Go to the `samples` directory and create a directory named `lib` and another named `classes`
4. Copy/paste `choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar` into `lib`
6. In a console, compile a sample, for instance `Nonogram.java`:

    ```bash
    javac -d classes \
           -sourcepath ./src/main/java/ \
           -cp .:\
               lib/choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar \
           src/main/java/org/chocosolver/samples/integer/Nonogram.java
    ```
   
6. Then run the resolution, with the optional arguments `-f -d rabbit` :
                
    ```bash
    java -cp classes:\
             lib/choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar \
            org.chocosolver.samples.integer.Nonogram -f -d rabbit
    ```            
