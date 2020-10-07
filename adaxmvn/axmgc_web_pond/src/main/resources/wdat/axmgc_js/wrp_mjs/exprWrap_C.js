function OurExprWrap(mjsRoot, txt_outName, txt_mjsExpr) {
    const mjsParsed = mjsRoot.parse(txt_mjsExpr)
    const mjsCompiled = mjsParsed.compile()
    // console.log("Compiled: ", mjsCompiled, ' from input: ', txt_mjsExpr)
    const mjsTexInner = mjsParsed.toTex(MMM_texFlags)
    const mjxTexOuter = '$$' + mjsTexInner + '$$' 
    
    const ewrp = {
        outName : txt_outName,
        mjsTxt : txt_mjsExpr,
        mjsCmp : mjsCompiled,
        mjsTexInner : mjsTexInner,
        mjxTexOuter : mjxTexOuter,
        evalFunc : function(scope) {
            return this.mjsCmp.eval(scope)
        },
        evalAndStore : function(inScope, outScope) {
            const evResult = this.evalFunc(inScope)
            outScope[this.outName] = evResult
        }
    }
    
    return ewrp
}

function OurRangeArr(mjsRoot, symName, numVals, minVal, maxVal) {
    // TODO:  Use MathJS fractions
    const rangeLen = maxVal - minVal
    const stepSize = rangeLen / (numVals - 1)
    const flag_includeEndVal = true
    // Seems that this range object is not actually an array, but it acts sort of like one.
    // Aha, it is a MathJS "Matrix"!  
    // https://mathjs.org/docs/datatypes/matrices.html
    // Matrices can contain different types of values: numbers, complex numbers, units, or strings.
    // Different types can be mixed together in a single matrix.
    // Matrices contain functions map and forEach to iterate over all elements of the (multidimensional) matrix. 
    // Shows up in debug console with __proto__ i  and arrays of _data, _size, other stuff.
    
    // Whether we want Matrix or Array by default can be set in config:
    // https://mathjs.org/docs/core/configuration.html
    
    const rangeArrOrMatrix = mjsRoot.range(minVal, maxVal, stepSize, flag_includeEndVal)
    const rangeArr = ensureArrayNotMatrix(rangeArrOrMatrix)
    const rangeDesc = {
        sym : symName,
        min : minVal,
        max : maxVal,
        cnt : numVals,
        arr : rangeArr
    }
    return rangeDesc
}
// Returns flat array of outscopes for all cells, traversed like 'nested' ranges.
// Uses recursion and .flat (requires ES6, won't work in IE)
// calcMdl must implement  evalAll(wrkScope), returning a single outScope
function evalOverRanges_ES6(calcMdl, inScope, rangeDescArr) {
    const wrkScope = {}
    // Shallow clone
    Object.assign(wrkScope, inScope)
    const firstRD = rangeDescArr[0]
    const vlArr = firstRD.arr
//    console.log('vlArr: ', vlArr)
    var resultArr = []
    if (rangeDescArr.length == 1) {  // 1D base-case, no deeper ranges
        const outScopesArr = vlArr.map(v => {
            wrkScope[firstRD.sym] = v
            return calcMdl.evalAll(wrkScope) // A single outscope per input 
        })
        resultArr = outScopesArr // mapped array, one outscope for each v in 1D range
    } else {
        var subsPending = rangeDescArr.slice(1)
        const osNestedArr = vlArr.map(v => {
            wrkScope[firstRD.sym] = v
            return evalOverRanges_ES6(calcMdl, wrkScope, subsPending) // Recursive call returns array
        }) 
        let maxFlatLvls = 1
//        console.log("Nested array result, to be flattened using ES6 function .flat(): ", osNestedArr)
        resultArr = osNestedArr.flat(maxFlatLvls) // .flat requires recent (ES6) browser, and does not work in IE.
    }
//    console.log('evalOverRanges level=', rangeDescArr.length, ' result: ', resultArr)
    return resultArr
}

