## BitCoinMiner
A distributed BitCoinMiner using `Scala`and `Akka framework`

###How to run the program
The program has a standalone and a distributed implementation. Both of them can be run using the packaged JAR file or using SBT.

Standalone:

**Using JAR file** :
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


###Distributed:
To run a distributed implementation, you can start the Master actor and then slave actors for doing the work in different machine.
The slave  takes in 2 parameters, One the number of trailing zero's and other IP address of the Server (Master).

Example for runnig distributed implementation:

**Using JAR file **:

```
Java -jar Project1.jar 4  //On the Main Machine to start server
Java -jar Project1.jar 4 x.x.x.x  //On slave machines to start slaves

```

**Using SBT file**:

```
sbt 
run 4 //to run the Master

run 4 x.x.x.x //to run the slave on different machine

```


