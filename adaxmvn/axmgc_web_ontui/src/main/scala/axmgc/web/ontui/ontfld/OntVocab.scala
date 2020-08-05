package axmgc.web.ontui.ontfld

// Copied from fibodemo module, but not yet used.

trait OntVocab // Scala source file marker

trait StdGenVocab {
	val baseUriTxt_owl = "http://www.w3.org/2002/07/owl#"
	val baseUriTxt_rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	val baseUriTxt_rdfs = "http://www.w3.org/2000/01/rdf-schema#"
	val baseUriTxt_skos = "http://www.w3.org/2004/02/skos/core#"
	val baseUriTxt_xml = "http://www.w3.org/XML/1998/namespace"
	val baseUriTxt_xsd = "http://www.w3.org/2001/XMLSchema#"

	val propLN_owlImports = "imports"
	val propLN_rdfType = "type"
}
trait FiboVocab extends StdGenVocab {
	val ontFldHd = "" // "gdat/"
	val path_fiboOnt = ontFldHd + "fibo_ont/fibo_2018Q4_all_4MB.ttl"

	val baseUriTxt_omgSm = "http://www.omg.org/techprocess/ab/SpecificationMetadata/"

}
