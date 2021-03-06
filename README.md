OceanOS
=======

Meta-Actors with Akka and OSGI

This project is an exploration of the synergies between an actor system like [Akka](http://akka.io), and a micro SOA platform like [OSGI](http://www.osgi.org).

Actors can be seen as micro services. They communicate asynchronously with other actors in the system, but provide an unit of synchrony and consistent state with the rule that they can only process one message at a time.

Composing a system out of actors, is to design a message flow. 

`actor1 --(msg)-> actor2`

Meta-Actors
---------

OceanOS provides some primitives to compose these message flows into a new kind of meta-actor.

`meta-actor = flow ( actor1 --(msg)-> actor2 )`

`actor3 --(msg)-> meta-actor --(msg)-> actor4`

These meta-actors keep the rule of consistent state transitions with a mechanism of [event sourcing](http://martinfowler.com/eaaDev/EventSourcing.html) and serialization. They also define the network topology of message flows, removing the flow dependencies from the actor themselves, and allowing to reconfigurable topologies at runtime. 

They keep the asynchronous nature of the message flow they represent. This means that, if the message flow has parallel branches, they will still be executed in parallel. It is up to the message flow designer to introduce points of synchrony with the primitives provided. The primitives are still being developed. Currently only pipelines, conditional branching, transforms, and parallel syncs are implemented with `(~>, filter, map and merge)`. Please see `ws.oceanos.core` tests for examples. 

Build
--------

The project uses 
  `maven 3.0.3`

On the project root do 
  `mvn clean install`

Change to the directory 
  `cd ws.oceanos.launcher`

Execute the embed OSGI platform with
  `java -jar target/ws.oceanos.launcher-0.0.1-SNAPSHOT.jar`

Point your browser at 
  `http://localhost:8080`

For an example client code see OSGIHelloWorldServlet in module ws.oceanos.web or the tests in module ws.oceanos.core.
