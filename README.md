# axiomagic tuning kit 
Axiomagic is an open source meta-system for interactive exploration.
<br/>Core components use Scala, Akka Http, RDF, and HTML/CSS/Javascript.
<br/>Upstream functional generators (Scala, Idris, ...) emit bounded, tunable, explainable smarts (model kernels) for use in downstream interactive web runtime.

## Uses 
Axiomagic is a toolkit for interactions with domain-specific data, deployed in browsers, calculators, simulators, and games.
<br/>Axmgc features apply naturally in science, finance, and edutainment.

## Lineage
This work builds on models and designs from previous projects, including the [Glue-AI 1.x](http://glue.ai) constellation 
from Stu B22 and pals.

Axiomagic is intended to be a thin layer of functions and tools, relying on the burgeoning capacity of today's open source components for server, cloud, browser, and edge devices.  Use it to build rich, sophsiticated applications, or quick, narrow investigations.

Implementation Technology:  Scala, RDF, Akka, Idris, and HTML/JS. 
## Example Applications
Some open science experiments helping drive our requirements for Axiomagic:

#### Scientific Study Examples  
 * http://gravax.fun - Interactive model analysis tool tackling e.g. 
   * https://gitlab.com/stub22/open-sci-cosmo - Gravitation at galactic scale
   * https://gitlab.com/stub22/open-sci-proof - Models of variable axioms (= the ultimate version of what we call "tuning")

#### Practical Engineering Examples
 * https://gitlab.com/stub22/open-fin-onto - Open financial computing, grounded in FIBO and related ontologies
 * https://gitlab.com/stub22/defogo - Wildfire analysis leveraging IoT and cloud
  
## Runtime Pacakaging
 Axiomagic provides both Client (AxCli) and Server (AxSrv) components, which may be selectively combined into an embedded (AxEmb) setup.
 * AxCli
   *  GUI + network + NUI component suite for general application use.
   *  Primarily used in HTML5 + JS environments.
   *  Uses [rdfjs N3](https://github.com/rdfjs/N3.js/) parser to read inbound turtle-RDF msgs.
   *  Optional MIDI connections for tweaking and switching of running components.
   *  Authentication and data privacy+portability integrations with WebId and [Solid](https://stub22.solid.community/). (link to stub22's community page)
 * AxSrv
   *  RDF-mediated logic core for state, query, goal-seeking and planning.
   *  Apps authored using lab server components, tested by running AxCli GUI with local info streams.
     * Publish apps as bundled deployments, attached to cloud sources for domain data.
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

**F.ormal A.djustment F.un**

Now you know why we make Axiomagic: to have fun times doing Ax-FAF!  
But remember, we are also quite formal around here.

The underlying open source components allow us to build apps as interlocking tapestries of:
*  Client GUI code:  Uses Vue.js to update HTML+CSS GUI, renders client UI from graphs 
*  Client Graphs: RDF in browser via rdfjs, optional JSON-LD, talks to server agents
*  Server Agents: Akka via HTTP, WebSockets, generates custom HTML+data GUI per client
*  Server Graphs:  SHACL, GraphQuery, SPARQL, TinkerPop/Gremlin
*  Server Functions:  Scala, AWS-Lambda, and similar functions compiled via proof systems embodying modern type theory.  See "Research Direction" section below.
<br/><b>WHERE</b>:  ClientGraphs and ServerGraphs are coherently supplied from upstream logic of our functional proof generators (Idris {Haskell, Ocaml}, deep Scala, univalence). 

## Getting Started

As of 2019-January, Axiomagic contains a skeleton of scala projects, built with maven.
We use the Intellij IDEA for a friendly coding experience.  

The axmgc.web.pond project contains the runnable object AxmgcPonderApp, in package axmgc.web.pond.

This app launches a web service running Akka HTTP.  The service delivers test web pages and RDF-backed query results.

Our javascript UI prototypes are not yet committed to this repository.

_Coming Soon:  Screenshots, plus perhaps a video?_

## Research Direction (as of 2019-Feb)

Conceptually Axiomagic embraces dependent types, theorem provers, univalence as discussed broadly here:
* [open-sci-proof](https://gitlab.com/stub22/open-sci-proof/)

Our current upstream compiler prototype uses primarily [Idris](https://www.idris-lang.org) (agda, ocaml, haskell)), progressing towards a Scala/Rust/StrongTyped2020 module for [MMT](https://uniformal.github.io/doc/) or equivalent.

## Support and Contact
Gitter chatrooms: https://gitter.im/glue-v2/axiomagic
