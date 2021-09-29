# axiomagic exploration toolkit 
AxioMagic is an open source toolkit for interactive knowledge exploration webapps.
<br/>Core components use Scala, Akka Http, RDF, and HTML/CSS/Javascript.


## Uses 
Axiomagic is a toolkit for interactions with domain-specific data, deployed in browsers, calculators, simulators, and games.
<br/>Axmgc features apply naturally in science, finance, and edutainment.

## Lineage and Structure
This work builds on models and designs from previous projects, including the [Glue-AI 1.x](http://glue.ai) constellation 
from Stu B22 and pals.

Axiomagic is intended to be a thin layer of functions and tools, relying on the burgeoning capacity of today's open source components for server, cloud, browser, and edge devices.  Use it to build rich, sophsiticated applications, or quick, narrow investigations.

 * Interactive Pipeline Technology  
   * Presentation data mapping over curated models
   * Deployed as library or service
   * Scala, RDF, Akka, HTML/JS

 * Upstream Math/Proof Technology
   * Seed knowledge graphs built from varied inputs
   * Dependent types, constraint logic, proofs
   * Idris, F star, Lean, Agda

## Prototype status  (updated 2021-09-28)

###  Current Pipeline Test Launchers
  * [axmgc.web.pond.AxmgcPonderApp](adaxmvn/axmgc_web_pond/src/main/scala/axmgc/web/pond/AxmgcPonder.scala)
  * [axmgc.web.ontui.ontfld.TstOdocGen](adaxmvn/axmgc_web_ontui/src/main/scala/axmgc/web/ontui/ontfld/TstOdocGen.scala)
  * [axmgc.dmo.ksrc.lean_mthlb.TestLeanTreeScan](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/dmo/ksrc/lean_mthlb/TestLeanTreeScan.scala)
  * [axmgc.dmo.xpr.io.prqt.TestPrqtIoEasily](https://github.com/stub22/axiomagic/blob/xprmnt_mlrn_djl_mxnet/adaxmvn/axmgc_dmo_bgdt/src/main/scala/axmgc/xpr/io/prqt/TestPrqtIoEasily.scala) (but subproj axiomagic.dmo.bgdt is not yet merged to master branch)
  * [axmgc.dmo.fin.ontdmp.borkl.RunTstBorkl](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/dmo/fin/ontdmp/borkl/RunTstBorkl.scala)
  * [axmgc.dmo.fin.ontdmp.TstOntDmps](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/dmo/fin/ontdmp/TstOntDmps.scala)
  * [axmgc.xpr.exd.TstExdLnch](adaxmvn/axmgc_dmo_hvol/src/main/scala/axmgc/xpr/exd/TstExdLnch.scala)
  * [axmgc.xpr.pdm.LnchMndTst](adaxmvn/axmgc_dmo_hvol/src/main/scala/axmgc/xpr/pdm/LnchMndTst.scala)
  * [axmgc.xpr.sym_mth.TstSymCalculus](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/xpr/sym_mth/TstSymCalculus.scala)
  * [axmgc.xpr.nlg.TestNlg](adaxmvn/axmgc_dmo_hvol/src/main/scala/axmgc/xpr/nlg/TestNlg.scala)
  * [axmgc.xpr.vis_js.RunNavItemMakerTests](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/xpr/vis_js/SampleNavItemStuff.scala)
  * [axmgc.xpr.dkp_shacl.DkpDtstLnch](adaxmvn/axmgc_dmo_fibo/src/main/scala/axmgc/xpr/dkp_shacl/DkpDtstLnch.scala)

## Features and Applications

### Fun Part : What, Why, How?
AxioMagic components generate tweakable and routable data-interactions for users,
employing color, sound, motion, and even music.  Interactions generally take the form
of web components accessing structured data.

Interactions are assembled from equations, functions, mappings, templates, rules, and schemas.
These definitions commonly use parameters, which may be tweaked by users at runtime.

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
   *  Authentication and data privacy+portability integrations with WebId and [Solid](https://pod.inrupt.com/stub22/profile/card#me). (Link is to stub22's pod-profile)
 * AxSrv
   *  RDF-mediated logic core for state, query, goal-seeking and planning.
   *  Apps authored using lab server components, tested by running AxCli GUI with local info streams.
     * Publish apps as bundled deployments, attached to cloud sources for domain data.
   *  Laboratory webServer + dataRoute components use Akka + Scala, running on JVM, on Linux or MS-Win (untried on Mac).
   *  Deploy RDF-configured cloud functional components to Lambda and other cloud compute services.     
 * AxEmb
   *  Embedded components combine features of AxSrv and AxCli for a particular narrow use case.
   *  Preferred approach is compiling via Rust to LLVM executable, runnable on micro-OS.



