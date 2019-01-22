# axiomagic meta-ui 
Axiomagic is an open source meta-system for interactive exploration.<br/>

## Lineage
AxMgc contains the latest (2018-19) work in authorable RDF system design by
Stu B22 and pals.   

This work builds on our previous projects including glue.ai and appadapter.org.  

## Gritty Summary
 Axiomagic provides both Client (AxCli) and Server (AxSrv) components.
 * AxCli
   *  GUI + network + NUI component suite for general application use.
   *  Primarily used in HTML5 + JS environments.
   *  Uses [rdfjs N3](https://github.com/rdfjs/N3.js/) parser to read inbound turtle-RDF msgs.
 * AxSrv
   *  RDF-mediated layered system for state, query, goal-seeking and planning.
   *  Laboratory webServer + dataRoute components use Akka + Scala, running on JVM, on Linux or MS-Win (untried on Mac).
   *  Produces and configure cloud functional components for Lambda, etc.
   *  Includes embedded components to be compiled via Rust to LLVM.
   *  Lab components runnable AxSrv generates deployments and info streams usable in the AxCli GUIs.

## Fun Part : What and Why?
These components generate tweakable and routable multidimensional experiences for users,
employing color, sound, motion, music.

## Support and Contact
Gitter chatrooms here: https://gitter.im/glue-v2/axiomagic
