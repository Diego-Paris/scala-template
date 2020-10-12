# median-filter

Simple median filter implemented in Scala utilizing Akka actors. One completes the filter sequentially and the other concurrently.

## To run using sbt
in working hello-world-template directoy run command: sbt run

## Results

Filtering owlsaltpepper.png (1590x1062)
17.0614% increase 
Actor 1 completed in 2175 ms
Actor 2 completed in 1858 ms

Filtering grainy.png (316x303)
7.09877% increase
Actor 1 completed in 347 seconds
Actor 2 completed in 324 seconds
