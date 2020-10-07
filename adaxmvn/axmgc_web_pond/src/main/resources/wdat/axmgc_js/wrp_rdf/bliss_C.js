
function doCoolStuff(ptrN3) {
    // Note the object destructuring syntax used to pull out fields
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Destructuring_assignment
    const { DataFactory } = ptrN3;
    const { namedNode, literal, defaultGraph, quad } = DataFactory;
    const myQuad = quad(
      namedNode('https://ruben.verborgh.org/profile/#me'),
      namedNode('http://xmlns.com/foaf/0.1/givenName'),
      literal('Ruben', 'en'),
      defaultGraph(),
    );
    console.log(myQuad.subject.value);         // https://ruben.verborgh.org/profile/#me
    console.log(myQuad.object.value);          // Ruben
    console.log(myQuad.object.datatype.value); // http://www.w3.org/1999/02/22-rdf-syntax-ns#langString
    console.log(myQuad.object.language);       // en
}
function xhrError() {
    console.error(this.statusText); 
}
function startXHR(url, lstnr) {
    "use strict";
    var req = new XMLHttpRequest(); // a new request
    req.addEventListener("load", lstnr);
    req.open("GET", url, true); // true => async
    req.send(null);
    return req.responseText;
}
var myPtrN3
function startFetchTurtle(ptrN3, trtlURL) { 
    myPtrN3 = ptrN3
    const url = trtlURL // "./tdat/box_ui_cfg_02.ttl"
    startXHR(url, gotTurtle)
}
function gotTurtle() {
    const trtlTxt = this.responseText;
    console.log("got turtle:", trtlTxt);
    
    var stored = syncParseAndStoreTurtle(myPtrN3, trtlTxt);
    console.log("gt Store:", stored)
    ensureUiNms(myPtrN3)
    viewStoreUiCnf(myPtrN3, stored);
}
function syncParseAndStoreTurtle(ptrN3, ttlStrng) {
    const store = new ptrN3.Store(); // "new" necessary when import is indirect
    const pqs = syncParseToQuadArr(ptrN3, ttlStrng);
    // asyncParseToStore(ptrN3, store)
    console.log("Sync-parsed quads:", pqs)
    store.addQuads(pqs)
    // console.log("Built store from sync-parsed qs:", store)
    return store;
}
function asyncParseToStore(ptrN3, store, trtlTxt) { 
    const parser = new ptrN3.Parser();
    parser.parse(ttlStrng, (error, quad, prefixes) => {
        if (quad) {
            console.log("Adding quad to store: ", quad);
            store.addQuad(quad);
        } else {
            console.log("# Last callback gets the prefixes: ", prefixes);
        }
    });
}
function syncParseToQuadArr(ptrN3, trtlTxt) { 
    const parser = new ptrN3.Parser();
    var quads = parser.parse(trtlTxt);
    return quads;
}
/*
The callback's first argument is an optional error value, the second is a quad. If there are no more quads, the callback is invoked one last time with null for quad and a hash of prefixes as third argument. 
Pass a second callback to parse to retrieve prefixes as they are read. 
If no callbacks are provided, parsing happens synchronously.
*/
