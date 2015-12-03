## BitCoinMiner
A distributed BitCoinMiner using `Scala`and `Akka framework`

###How to run the program
The program has a standalone and a distributed implementation. Both of them can be run using the packaged JAR file or using SBT.

Standalone:

If you're using the JAR file provided, simply run using the following command:
```
Java -jar Project1.jar <NumberOfTrailingZeroes : Int>

```
for example :
```
Java -jar Project1.jar 4

```

**using SBT**:

```
sbt
run <Integer : numberOfTrailingZeros>
```

The one arugment taken is an Integer which indicated the number of trailing zeros to look for.


Distributed:
To run a 
