# axiomagic meta-ui 
Axiomagic is an open source meta-system for interactive exploration.<br/>

## Lineage
AxMgc contains the latest (2018-19) work in authorable RDF system design by
Stu B22 and pals.   

This work builds on our previous projects including glue.ai and appadapter.org.  

## Gritty Summary
 Axiomagic provides both Client (AxCli) and Server (AxSrv) components, which may be deployed in an embedded (AxEmb) setup.
 * AxCli
   *  GUI + network + NUI component suite for general application use.
   *  Primarily used in HTML5 + JS environments.
   *  Uses [rdfjs N3](https://github.com/rdfjs/N3.js/) parser to read inbound turtle-RDF msgs.
   *  Optional MIDI connections for tweaking and switching of running components.
 * AxSrv
   *  RDF-mediated layered system for state, query, goal-seeking and planning.
   *  Laboratory webServer + dataRoute components use Akka + Scala, running on JVM, on Linux or MS-Win (untried on Mac).
   *  Produces and configure cloud functional components for Lambda, etc.     
   *  Lab components runnable AxSrv generates deployments and info streams usable in the AxCli GUIs.
 * AxEmb
   *  Embedded components combine features of AxSrv and AxCli for a particular narrow use case.
   *  Preferred approach is compiling via Rust to LLVM executable, runnable on micro-OS.

## Fun Part : What, Why, How?
These components generate tweakable and routable multidimensional experiences for users,
employing color, sound, motion, music.

Features are added by creating equations, functions, mappings, templates, rules, schemas,
employing a substrate of:
*  Client GUI code:  Uses Vue.js to update HTML+CSS GUI, renders client UI from graphs 
*  Client Graphs: RDF in browser via rdfjs, optional JSON-LD, talks to server agents
*  Server Agents: Akka via HTTP, WebSockets, generates custom HTML+data GUI per client
*  Server Graphs:  SHACL, GraphQuery, SPARQL, TinkerPop/Gremlin
*  Server Functions:  Scala, Lambda, proof systems incl Haskell, Agda, [MMT](https://uniformal.github.io/doc/)
## Support and Contact
Gitter chatrooms here: https://gitter.im/glue-v2/axiomagic
