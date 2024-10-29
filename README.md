# vespa-x

## Minimal messsaging platform

Please quickly go through the code where comments explain some implementation choices

This is hardly a real-world use-case, and it would have been interesting to have a concurrent scenario, storage, etc.

I understand the purpose of this small project to be more about algorithm and data structure choices than about "architecture" or project structure.
It means that, whenever possible, I stick to primitive types, no beautiful interfaces, boilerplate, etc.
My usage of streaming apis feels a bit unefficient, with constant enumeration, re-enumeration, etc. Might be my mis-understanding of java streams apis, compared to the scala collections api. 

### Initial thoughts
* A single threaded system with a dataset fitting in-memory means that we can stick to mutable data structure (hurts my functional mindset, but it's the most efficient. No reason to pay the price of copy on write data structures if not needed). Of course, because the assignment is not multi-threaded, there is no need to synchronize anywhere
* I make the conscious choice to prioritize speed over memory. It means that data is duplicated to get better performance (for instance, "inverted index" from topics to posts).
* Instead of re-implementing structures which would intuitively fit, I choose to use the standard library ones, which are implemented the same way behind the scenes and will certainly be more performant than mine.
* In scenarios where sorting makes sense, I use a NavigableMap (= tree map, most likely a B+tree) to have fast key range scanning
* In non-sorted scenarios, like from topics to messages ids, simple HashMaps are more performant
* The timestamps being monotically increasing from an arbitrary epoch, it is very convenient to use it as message id. It means that finding messages over a period of time is just a key range scan on the navigable map holding messages.
* Duplicating the data over several structures means that we should not have any full "table" scan anywhere. only o(1)-ish accesses
* One annoying thing with the standard library maps is the fact that we can't have primitive types as keys. It makes unnecessary constant boxing/unboxing. In a real world scenario, bringing a library supporting primitive keys would be better.

A small suite of tests checks the basic features.

For convenience, a minimal cli interface is provided when running the main class.

### To run the project

If a local jdk 17 is installed, a gradle wrapper is provided to run the project, and can be used simply with `./gradlew run`. 
However, gradle output is polluting the console. A dockerfile is provided to run the cli via docker in a more ergonomic way:
```
docker build -t vespax:1.0 .
docker run --rm -i vespax:1.0
```

### To run the tests
To run the tests, just run the command `./gradlew test`
