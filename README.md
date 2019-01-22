# axiomagic meta-ui 
Axiomagic is an open source meta-system for interactive exploration.<br/>

## Lineage
AxMgc contains the latest (2018-19) work in authorable RDF system design by
Stu B22 and pals.   

This work builds on our previous projects, including [Glue-AI 1.x](http://glue.ai).  

Axiomagic is the foundation of Glue-AI 2.0.

## Gritty Summary
 Axiomagic provides both Client (AxCli) and Server (AxSrv) components, which may be deployed in an embedded (AxEmb) setup.
 * AxCli
   *  GUI + network + NUI component suite for general application use.
   *  Primarily used in HTML5 + JS environments.
   *  Uses [rdfjs N3](https://github.com/rdfjs/N3.js/) parser to read inbound turtle-RDF msgs.
   *  Optional MIDI connections for tweaking and switching of running components.
   *  Authentication and data privacy+portability integrations with WebId and [Solid](https://stub22.solid.community/). (link to stub22's community page)
 * AxSrv
   *  RDF-mediated layered system for state, query, goal-seeking and planning.
   *  Apps authored using lab server components, tested by running AxCli GUI with local info streams.
     * Publish apps as bundled deployments, connected to runtime cloud info streams.
   *  Laboratory webServer + dataRoute components use Akka + Scala, running on JVM, on Linux or MS-Win (untried on Mac).
   *  Deploy RDF-configured cloud functional components to Lambda and other cloud compute services.     
 * AxEmb
   *  Embedded components combine features of AxSrv and AxCli for a particular narrow use case.
   *  Preferred approach is compiling via Rust to LLVM executable, runnable on micro-OS.

## Fun Part : What, Why, How?
These components generate tweakable and routable multidimensional experiences for users,
employing color, sound, motion, music.  These experiences are defined in terms of timelines,
including the familiar concept of events.

Features are added by creating equations, functions, mappings, templates, rules, and schemas.
These definitions commonly use parameters, which may be tweaked by users at runtime.
The combination of formal mapping with playful tweaking is what we call FAF:

**Formal Adjustment Fun**

Now you know why we make Axiomagic: to have fun with Ax-FAF!  
But remember, we are also quite formal around here.

The underlying open source components allow us to build apps as interlocking tapestries of:
*  Client GUI code:  Uses Vue.js to update HTML+CSS GUI, renders client UI from graphs 
*  Client Graphs: RDF in browser via rdfjs, optional JSON-LD, talks to server agents
*  Server Agents: Akka via HTTP, WebSockets, generates custom HTML+data GUI per client
*  Server Graphs:  SHACL, GraphQuery, SPARQL, TinkerPop/Gremlin
*  Server Functions:  Scala, Lambda, proof systems embodying HOTT: Agda (haskell), Isabelle/HOL (ocaml), [MMT](https://uniformal.github.io/doc/)
## Support and Contact
Gitter chatrooms here: https://gitter.im/glue-v2/axiomagic
