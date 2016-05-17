# samples
Choco-solver in practice
========================


This project hosts samples based on choco-solver.

To run a sample, please follow the steps:

1. Download [choco-solver-4.0.0.zip](https://github.com/chocoteam/choco-solver/releases/tag/choco-4.0.0) and unzip it
2. Go to the `sample` directory and create a directory named `lib` and another named `classes`
3. Copy/paste `choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar` into `lib`
4. In a console, compile a sample, for instance Nonogram.java:

    `javac -d classes \
           -sourcepath ./src/main/java/ \
           -cp .:choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar \
           src/main/java/org/chocosolver/samples/integer/Nonogram.java`
           
5. Then run it, with the optional arguments `-f -d rabbit` :
                
    `java -cp classes:choco-solver-4.0.0-SNAPSHOT-with-dependencies.jar \ 
            org.chocosolver.samples.integer.Nonogram -f -d rabbit`            
            
            