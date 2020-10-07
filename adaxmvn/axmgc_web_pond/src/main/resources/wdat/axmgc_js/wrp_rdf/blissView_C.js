const myUicUris = {
    // {t-type},  n-oun | a-dj | v-erb,
    // resource relation is a verb
    // p-roperty literal is separate 
    tnBox: "Box",
    vHasParent : "hasParent", // verb of rel Box-Box 
    pnHasTitle : "hasTitle",   // property literal noun or "part-noun"
    pnBoxChr :  "inBoxChr",
    
    prnBui : "buiBB",
    prxBui : "http://www.appdapter.org/2019/Q1/boxUi_BB#",
    prnBty : "btyBB", 
    prxBty : "http://www.appdapter.org/2019/Q1/boxType_BB#"
    

}
//         { namedNode, literal, defaultGraph, quad } : 
// const { namedNode, literal, defaultGraph, quad } = DataFactory;    
function makeUiNms (n3top) {
    // qName is unused so far
    const titlePredQName = myUicUris.prnBty + ":" + myUicUris.pnHasTitle
    const titlePredUri = myUicUris.prxBty + myUicUris.pnHasTitle
    const chrPredUri = myUicUris.prxBty + myUicUris.pnBoxChr

    const df = n3top.DataFactory;
    var nms = {
        datFact : df,
        titlePredNode :  df.namedNode(titlePredUri),
        chrPredNode : df.namedNode(chrPredUri)
    }
    console.log("uiNms:", nms)
    return nms;
}
var myUiNms = null;
function ensureUiNms(n3top) {
    if (!myUiNms) {
        myUiNms = makeUiNms(n3top)   
    }
}
function viewStoreUiCnf(n3top, uicStr) {
    ensureUiNms(n3top); 
    var boxTitles = uicStr.getQuads(null, myUiNms.titlePredNode, null)
    console.log("At predNode", myUiNms.titlePredNode, "\nFound titles:", boxTitles)
    var boxChrs = uicStr.getQuads(null, myUiNms.chrPredNode, null)
    console.log("At predNode", myUiNms.chrPredNode, "\nFound chrs:", boxChrs)
}