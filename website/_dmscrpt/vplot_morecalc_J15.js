/* From _ojnk_H/combo_wrap_B.js */
// Here we can call funcs in imported libraries, as long as export-names line up.

const EXKND = {
    EXK_VARIABLE : 1,
    EXK_FORMULA : 2    
};

function makeFormula (eSym,  eLabelText) {
    var formulaExObj = {
        sym : eSym,
        kind : eKind,
        txtLabel : eLabelText,
        txtNerd: null,  // For nerdamer
        txtMjs: null,   // For MathJS
        tekText: null,   // For Tak
        omJson: null    // Parsed in openMath-Json form
    }
}

function doMathDemoStuff(tgtId)  {
    var tgt = document.getElementById(tgtId)
    var divT = document.createElement('div');
    var sp01A = document.createElement('span');
    var sp01B = document.createElement('span');
    var sp01C = document.createElement('span');
    // Javascript literal problem:  'Sum:  \(\sum_{n=1}^{\infty} 2^{-n} = 1\) '
    // Need to JS-escape the TeX \ instruction using \\
    sp01A.innerHTML = 'Inline sum:  \\(\\sum_{n=1}^{\\infty} 2^{-n} = 1\\) '
    sp01B.innerHTML = 'Bstart <br/> Bend plus Asciimath</a> enclosed in back-ticks: `sum_(i=1)^n i^3=((n(n+1))/2)^2` DONE'
    sp01C.innerHTML = ' $$ \\iiiint_V \\mu(t,L,v,w) \\,dt\\,dL\\,dv\\,dw $$'
    divT.appendChild(sp01A)
    divT.appendChild(sp01B)
    divT.appendChild(sp01C)
    tgt.append(divT)
}


function doPlotDemoStuff(ourMthJs, exprTgtId, ourVegEmbRoot, vegPlotSel01, vegPlotSel02, vegPlotSel03) {
    //    var myAxDeps = axdp_v02()  // The exported handle func for the axdeps lib.
    //    console.log("Here is myAxDeps: ", myAxDeps)
    //    myAxDeps.iGfnc("[msg from ubiSim]")

    // If direct include, math-js root obj is:   math
    //   const ourMthJs = math; // myAxDeps.iMathJs
    // const ourPltlyRoot = myAxDeps.iPlotly
    // const ourVegEmbRoot = vegaEmbed; // myAxDeps.iVegEmb

    var txtE1 = 'sqrt(75 / 3) + det([[-1, 2], [3, 1]]) - sin(pi / 4)^2'
    var expr1 = ourMthJs.parse(txtE1)
    console.log('Parsed expr: ', expr1, 'from text:', txtE1)
    var texFlags = {parenthesis: 'keep', implicit: 'hide'}
    var tex01 = expr1.toTex(texFlags)
    var txw01 = '$$' + tex01 + '$$'
    console.log('Tex: ', tex01)
    const domTgt01 = document.getElementById(exprTgtId)
    domTgt01.innerHTML = txw01

    // Steps are parse, .compile, .eval, format
    // But  math.eval() is a combined operation of parse+compile+eval
    var cmp1 = expr1.compile()
    var evl1 = cmp1.evaluate()
    console.log('evl1: ', evl1)

    doStuffWithScope(ourMthJs)
    moreScope(ourMthJs)

    var dcm = dummyCalcMdl(ourMthJs)
    console.log("Dummy Calc Mdl:", dcm)
    var dInScp = dummyInScope()
    var dummyOut = dcm.evalAll(dInScp)
    console.log("Dummy Out:", dummyOut)
    var rangeDescs = makeRangeDescBlock(ourMthJs)
    console.log('Range Decs: ', rangeDescs)
    var evalOutBlk = evalOverRanges_ES6(dcm, dInScp, rangeDescs)
    console.log('Eval Out Blk: ', evalOutBlk)

    let wvSpec = mkWthrVwSpec();
    let bvSpec = mkBarVwSpec();
    ourVegEmbRoot.embed(vegPlotSel01, wvSpec);
    ourVegEmbRoot.embed(vegPlotSel02, bvSpec);

    var vpltDescTempl = makeVegaPlotDescTmpl()
    const vpltDatObj = {values : evalOutBlk}
    const vpltDesc = makeVegaPlotDesc(vpltDescTempl, vpltDatObj)

    ourVegEmbRoot.embed(vegPlotSel03, vpltDesc);

    // pltlDbl(ourPltlyRoot, 'pdbl4', 'Spline and Splattter Plot')
/*  Original head of trial_mathJs_H, relying on our agg import:
    var myAxDeps = axdp_v02()  // The exported handle func for the axdeps lib.
    console.log("Here is myAxDeps: ", myAxDeps)
    myAxDeps.iGfnc("[msg from ubiSim]")

    // If direct include, root obj is:   math
    const ourMthJs = myAxDeps.iMathJs
    const ourPltlyRoot = myAxDeps.iPlotly
    const ourVegEmbRoot = myAxDeps.iVegEmb
*/
}