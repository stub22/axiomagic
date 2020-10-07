
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

/*  var txtE1 = exprText; //  'sqrt(75 / 3) + det([[-1, 2], [3, 1]]) - sin(pi / 4)^2'
    var expr1 = ourMthJs.parse(txtE1)
    console.log('Parsed expr: ', expr1, 'from text:', txtE1)
    const domTgt01 = document.getElementById('ex01')
    // Could be TeX or MathML, depending on settings
*/

function pushMthDspTxt(prsdExpr, domId) {
    let domTgt = document.getElementById(domId)
    pushMathDisplayText(prsdExpr, domTgt)
}
    
function pushMathDisplayText(prsdExpr, domTgt) {
    let txw01 = getTexBlock(expr1)
    console.log('LaTeX-block: ', txw01)
    domTgt.innerHTML = txw01

}
function getTexBlock(prsdExpr) {
    let texFlags = {parenthesis: 'keep', implicit: 'hide'}
    let tex01 = expr1.toTex(texFlags)
    let txw01 = '$$' + tex01 + '$$'
    return txw01
}

function ensureArrayNotMatrix(mtrxOrArr) {
    if (Array.isArray(mtrxOrArr)) {
        return mtrxOrArr
    } else {
        const matrixDimsArr = mtrxOrArr.size // mtrx.size returns an Array of sizes for dimensions
        var rsltArr = new Array(matrixDimsArr[0]) 
        mtrxOrArr.forEach ((value, index, matrix)  => {rsltArr[index] = value})
        console.log('Converted matrix with sizes=', matrixDimsArr, ' to array with length ', rsltArr.length)
        return rsltArr
    }
}


// Suppose we have expressions in Math.js form
// which we render via Tex and display with MathJax.

// In some cases we want to evaluate an expression over a range
// of values.

// Math.js offers the idea of a "scope" to use when evaluating an expr.

// We can expect user-controlled param values to affect an expr using scope.
// https://mathjs.org/docs/reference/functions/compile.html

function doStuffWithScope(mjsRoot) {
    const code1 = mjsRoot.compile('sqrt(3^2 + 4^2)')
    let r1 = code1.eval() // 5

    let scope = {a: 3, b: 4}
    const code2 = mjsRoot.compile('a * b') // 12
    let r2 = code2.eval(scope) // 12
    scope.a = 5
    let r3 = code2.eval(scope) // 20

//    const nodes = mjsRoot.compile(['a = 3', 'b = 4', 'a * b'])
//    let r4 = nodes[2].eval() // 12
    
    console.log('results: ', r1, r2, r3)
}
function moreScope(mjsRoot) {
    let scope = {
      obj: {
        prop: 42,
        bonus: 9
      }
    }

    // retrieve properties
    let r1 = mjsRoot.eval('obj.prop', scope)          // 42
    let r2 = mjsRoot.eval('obj["prop"]', scope)       // 42

    // set properties (returns the whole object, not the property value!)
    let r3 = mjsRoot.eval('obj.prop = 43', scope)     // {prop: 43}
    let r4 = mjsRoot.eval('obj["prop"] = 43', scope)  // {prop: 43}
    let r5 = scope.obj                             // {prop: 43}
    console.log('results: ', r1, r2, r3, r4, r5)
}
function displayJunk(mjsRoot, domId) { 
    var txtE1 = 'sqrt(65 / 3) + det([[-1, 2], [3, 1]]) - sin(pi / 4)^2'
    var expr1 = ourMthJs.parse(txtE1)
    console.log('Parsed expr: ', expr1, '\nfrom text:', txtE1)
    var texFlags = {parenthesis: 'keep', implicit: 'hide'}
    var tex01 = expr1.toTex(texFlags)
    var txw01 = '$$' + tex01 + '$$'
    console.log('TeX: ', tex01)
    const domTgt01 = document.getElementById(domId)
    domTgt01.innerHTML = txw01
    // Steps are parse, .compile, .eval, format
    // But  math.eval() is a combined operation of parse+compile+eval  
    var cmp1 = expr1.compile()
    var evl1 = cmp1.eval()
    console.log('evl1: ', evl1)
    //     , 'e1f: ', e1f)
    //var e1f = math.format(e1v)
    doStuffWithScope(ourMthJs)
    moreScope(ourMthJs)    
}