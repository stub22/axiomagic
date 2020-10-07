/*
https://mathjs.org/docs/expressions/parsing.html

The scope is a regular JavaScript Object. 
The scope will be used to resolve symbols, and to write assigned variables or function.

math.eval('c = 2.3 + 4.5', scope)   
math.eval('f(x, y) = x^y')            // f(x, y)
math.eval('f(2, 3)') 

Vars: Each Decision and Param var has an allowed range.
If model has continuous, bounded dynamics (e.g. concave-ish), 
these range bounds may be thought of as mere plot-edge configs.  
However, if model dynamics are unbounded at the domain range 
edges, then these range bound params may creep up in significance.

Time vars get special treatment, but in many ways are like Decision/Param vars.

Decision Space has dimension dimD.
Decision Vars are often used as plot axes
Consider manufacturing + sales pipeline with three choice vars,
Here dimD = 3: 

X1 = qtyAbs     10   90  ;; Suppose User wants to tweak + plot using this quantity range.
X2 = quality     0.1 1.0 ;; assume already normalized
X3 = unitPrice   0.1 1.0 ;; assume already normalized

Param Space has dimension dimP.
Param Vars:
Here we set 4 exponential powers connecting intermediate Value Funcs for 
Cost and Demand to our decision funcs.  

P1 = qualCostPwr 0.5 to 1.5
P2 = qtyCostPwr  0.5 to 1.0 // 1.0 => No discount on var-cost for larger runs, 0.5 => var-cost ~ sqrt

P3 = qualDmdPwr  0.5 to 1.5 // 1.0 => Demand responds linearly to quality improvement
P4 = priceDmdPwr -1.5 to -0.5 // -1.0 => Elasticity 1.0

P5 = fixedRunCost // normalized so that totalCost makes sense

Value Funcs :  All funcs are normalized unless marked "Abs".   
qtyNrm = qtyAbs / qtyMax
vrblCost = quality ^ qualCostPwr * qtyNrm ^ qtyCostPwr
totalCost = fixedRunCost + vrblCost
unitCost = totalCost / qtyNrm

// Demand = normalized quantity public will buy at given quality and price.  
// Usually priceDmdPwr is negative. 
demandNrm = quality ^ qualDmdPwr * unitPrice ^ priceDmdPwr

totalRevenue = demandNrm * unitPrice

totalProfit = totalRevenue - totalCost

Optimization Constraint : demand == qtyMade // We want an EQUALITY constraint for lagrangian method. 

Goal:  Maximize totalProfit subject to contraint

// Plotting value funcs on decision axes
Plot: TotalCost(qtyAbs, quality)
      Demand(quality, unitPrice) 
      TotalProfit(qtyAbs, quality, unitPrice)
      
These functions take implicit arguments in the Param Vars, 
which may be supplied from MathJS.scope.

Advanced:  Treat params as explicit args, e.g. when fitting to a dataset

Data terminology:  
Frames and Slots (This is the kind of KB implicity adopted in JSON-first methodology).

paramFrame = JS obj with values for all param vars
choiceFrame = JS obj with values for all decision vars
scopeFrame = merged paramFrame and choiceFrame, may be passed to mathJS.eval
valueFrame = JS obj with results of evaluating all value funcs with scopeFrame
solutionFrame = JS obj containing a collection of (possible/verified) solutions
plotFrame = JS obj in plot-friendly shape

exprWrap = JS obj wrapping a MathJS / LaTeX / OpenMath / Symja ... expression
calcMdl = JS obj containing exprWraps
*/

const MMM_texFlags = {parenthesis: 'keep', implicit: 'hide'}

const X_qtyAbs = "X_qtyAbs"
const X_quality = "X_quality"
const X_unitPrice = "X_unitPrice"

const P_qualCostPwr = "P_qualCostPwr"
const P_qtyCostPwr = "P_qtyCostPwr"
const P_fixedCost = "P_fixedCost"
const P_qualDemandPwr = "P_qualDemandPwr"
const P_priceDemandPwr = "P_priceDemandPwr"

const C_qtyMax = "C_qtyMax"

const V_qtyNrm = "V_qtyNrm"
const V_vrblCost= "V_vrblCost"
const V_totalCost = "V_totalCost"
const V_demand = "V_demand"
const V_maxProfit = "V_maxProfit"

//    const rd_xQtyAbs = OurRangeArr(mjsRoot, 'X_qtyAbs', 3, 10, 90)
//    const rd_xQuality = OurRangeArr(mjsRoot, 'X_quality', 3, 0.1, 0.9)
//    const rd_xUnitPrice = OurRangeArr(mjsRoot, 'X_unitPrice', 3, 0.1, 0.9)

const mySampleVars = {
    qtyAbs : {
        symName : X_qtyAbs,
        numVals : 3,
        minVal : 10,
        maxVal : 90
    },
    quality : {
        symName : X_quality,
        numVals : 5,
        minVal : 0.1,
        maxVal : 0.9
    },
    unitPrice : {
        symName : X_unitPrice,
        numVals : 4,
        minVal : 0.1,
        maxVal : 0.9
    },
    specsInDisplayOrder : function () {
        let specArr = [this.qtyAbs, this.quality, this.unitPrice]
        return specArr
    }
      
}
// Sticking the C value in here for a minute...
const myParamVars = {
    qualCostPwr : {
        symName : P_qualCostPwr
    },
    qtyCostPwr : {
        symName : P_qtyCostPwr
    },
    fixedCost : { 
        symName : P_fixedCost
    },
    qualDemandPwr : {
        symName : P_qualDemandPwr
    },
    priceDemandPwr : {
        symName : P_priceDemandPwr
    },
    qtyMax : {  
        symName : C_qtyMax     // This is more a range-bound, not really a "param"...
    },
    specsInDisplayOrder : function () {
        let specArr = [this.qualCostPwr, this.qtyCostPwr, this.fixedCost, this.qualDemandPwr, this.priceDemandPwr, this.qtyMax]
        return specArr
    }

}

const myEffectFuncs = {
    // Note:  .exprWrap child is added to each entry, during buildCalcMdl

    qtyNrm : {
        symName : V_qtyNrm,
        mjsDirectTxt : 'X_qtyAbs / C_qtyMax',
        mjsGumboTxt : X_qtyAbs + '/' + C_qtyMax,
        mjsFancyTxt : [X_qtyAbs, '/', C_qtyMax].join(''),
        mjxTgtDomId : 'mth_qtyNrm'
    }, 
/*
        vrblCost : OurExprWrap(mjsRoot, V_vrblCost, 'X_quality ^ P_qualCostPwr * V_qtyNrm ^ P_qtyCostPwr'),
        totalCost : OurExprWrap(mjsRoot, V_totalCost, 'P_fixedCost + V_vrblCost'),
        demand : OurExprWrap(mjsRoot, V_demand, 'X_quality ^ P_qualDemandPwr * X_unitPrice ^ P_priceDemandPwr'),
*/    
    vrblCost : {
        symName: V_vrblCost, 
        mjsGumboTxt: X_qtyAbs,
        mjsFancyTxt : [X_quality, '^', P_qualCostPwr, '*', V_qtyNrm, '^', P_qtyCostPwr].join(''),
        mjxTgtDomId : 'mth_vrblCost'
    }, 
    totalCost : {
        symName: V_totalCost,
        mjsGumboTxt: X_qtyAbs,
        mjsFancyTxt : [P_fixedCost, '+', V_vrblCost].join(''),
        mjxTgtDomId : 'mth_totalCost'
    },
    demand : { 
        symName: V_demand,
        mjsGumboTxt: X_qtyAbs,
        mjsFancyTxt : [X_quality, '^', P_qualDemandPwr, '*', X_unitPrice, '^', P_priceDemandPwr].join(''),
        mjxTgtDomId : 'mth_demand'
    },
    maxProfit: { 
        symName: V_maxProfit,
        mjsFancyTxt : [V_demand, '*', X_unitPrice, '-', V_totalCost].join(''),
        mjxTgtDomId : 'mth_maxProfit'
    },
    specsInEvalOrder : function () {
        let specArr = [this.qtyNrm, this.vrblCost, this.totalCost, this.demand, this.maxProfit]
        return specArr
    }    
}
function buildExprDomEl(entSpck, tagName) {
    // Assume entSpck contains an .exprWrap prop, which contains an .mjxTexOuter prop.
    let spckTag = document.createElement(tagName);  
    let exWrp = entSpck.exprWrap
    let exprMthTxt = exWrp.mjxTexOuter
    spckTag.innerHTML = exprMthTxt
    return spckTag
}
function pushExprDisplay(entSpck, parentDomId, tagName) {
    let builtDomEl = buildExprDomEl(entSpck, tagName)
    let tgtDomEl = document.getElementById(parentDomId)
    if (tgtDomEl) {
        tgtDomEl.appendChild(builtDomEl)
    } else {
        console.error('Cannot locate exprDisplayTgt at ', parentDomId)
    }
}
function smrtPushExprDisplay(entSpck, tagName) {
    // Assume entSpck contains an .mjxTgtDomId prop
    let builtDomEl = buildExprDomEl(entSpck, tagName)
    let tgtDomId = entSpck.mjxTgtDomId
    pushExprDisplay(entSpck, tgtDomId, tagName)
}
function pushExprDisplays(spckSeq) {
    let tagName = 'span'
    for (let spck of spckSeq) {
        smrtPushExprDisplay(spck, tagName)
    }
}
/*
function gatherExprs(spckSeq, parentDomEl, tagName) {
    for (let spck of exprSeq)  {        
    }
    let sp01B = document.createElement(tagName);  
}
*/
function buildCalcMdl(mjsRoot) {
    let x = mySampleVars
    let p = myParamVars
    let e = myEffectFuncs
    let eSpecks = e.specsInEvalOrder()
    let eWraps = eSpecks.map(spck => {
        var bestTxt = spck.mjsFancyTxt
        if (!bestTxt) {
            bestTxt = spck.mjsGumboTxt
        }
        console.log("bestTxt: ", bestTxt)
        let exWrap = new OurExprWrap(mjsRoot, spck.symName, bestTxt)
        spck.exprWrap = exWrap
        return exWrap
    })
   
    let calcMdl = {
        smplVars : x,
        prmVars : p,
        eFncs : e,
        eSpcs : eSpecks,
        eWrps : eWraps,
        evalAllFx : function (inScope) {
            let exprsInOrder = this.eWrps
            let allFx = evalAllInScope(inScope, exprsInOrder)
            return allFx
        }
    }
    return calcMdl
}
// TODO:  All symbolic strings should appear only once in the code, hehngh?
function dummyCalcMdl (mjsRoot) { 
    const cm = {
        qtyNrm : OurExprWrap(mjsRoot, V_qtyNrm, 'X_qtyAbs / C_qtyMax'),
        vrblCost : OurExprWrap(mjsRoot, V_vrblCost, 'X_quality ^ P_qualCostPwr * V_qtyNrm ^ P_qtyCostPwr'),
        totalCost : OurExprWrap(mjsRoot, V_totalCost, 'P_fixedCost + V_vrblCost'),
        demand : OurExprWrap(mjsRoot, V_demand, 'X_quality ^ P_qualDemandPwr * X_unitPrice ^ P_priceDemandPwr'),
        // Problem:  V_demand may exceed V_qtyNrm
        maxProfit : OurExprWrap(mjsRoot, V_maxProfit, 'V_demand * X_unitPrice - V_totalCost'),
        evalAll : function(inScope) {
            return evalAllInScope(inScope, this.exprSeq)
/*            const wrkScope = {}
            // Shallow clone
            Object.assign(wrkScope, inScope)
            // for-of iterates collection
            for (let expWrp of this.exprSeq) {
                // console.log("Evaluating: ", expWrp, " with scope: ", wrkScope)
                expWrp.evalAndStore(wrkScope, wrkScope)
            }
            return wrkScope
*/            
        }        
    }
    const exprsInOrder = [cm.qtyNrm, cm.vrblCost, cm.totalCost, cm.demand, cm.maxProfit]
    cm.exprSeq = exprsInOrder
    return cm
}
function evalAllInScope(inScope, exprSeq) {
    const wrkScope = {}
    // Shallow clone
    Object.assign(wrkScope, inScope)
    // for-of iterates collection, in order (unlike for-in)
    for (let expWrp of exprSeq) {
        // console.log("Evaluating: ", expWrp, " with scope: ", wrkScope)
        expWrp.evalAndStore(wrkScope, wrkScope)
    }
    return wrkScope
}
function dummyInScope() {
    const inScope = {
        C_qtyMax : 100,
        P_qualCostPwr : 1.2,
        P_qtyCostPwr : 0.8,
        P_fixedCost : 0.4,
        P_qualDemandPwr : 1.4,
        P_priceDemandPwr : -0.7,
        X_qtyAbs : 65,
        X_quality : 0.7,
        X_unitPrice : 0.44
        
    }
    return inScope
}
function dummySampleCounts() { 
    let countKVO =  {
        X_qtyAbs : 4, 
        X_quality : 3,
        X_unitPrice : 3
    }
    return countKVO
}
function makeRangeDescBlock(mjsRoot) {
    return buildRangeDescBlock(mjsRoot, 3, 3, 3)
}
function fancyRangeDescBlock(mjsRoot, countKVO) {
    return buildRangeDescBlock(mjsRoot, countKVO[X_qtyAbs], countKVO[X_quality], countKVO[X_unitPrice])
}
function buildRangeDescBlock(mjsRoot, numSamp_qty, numSamp_qual, numSamp_price) {
    const rd_xQtyAbs = OurRangeArr(mjsRoot, X_qtyAbs, numSamp_qty, 10, 90)
    const rd_xQuality = OurRangeArr(mjsRoot, X_quality, numSamp_qual, 0.1, 0.9)
    const rd_xUnitPrice = OurRangeArr(mjsRoot, X_unitPrice, numSamp_price, 0.1, 0.9)
    const rdArr = [rd_xQtyAbs, rd_xQuality, rd_xUnitPrice]
    return rdArr
}
function makeVegaPlotDesc(vpltTempl, datObj) {
    const vpltDesc = {}
    // Shallow clone
    Object.assign(vpltDesc, vpltTempl)
    vpltDesc.data = datObj
    return vpltDesc   
}
// Everything except the data.values
function makeVegaPlotDescTmpl() { 
    const vpd = {

        data : null,
        repeat: {
            column: [X_qtyAbs,X_quality,X_unitPrice,],
            row: [V_qtyNrm, V_totalCost, V_demand, V_maxProfit]
        },
        spec: {
// Below here is still JS, but now in JSON style with quoted prop-syms.
            "width": 150,
            "height": 150,
            "mark": "point",
            "encoding": {
                  "x": {
                    "field": {"repeat": "column"},
                    "type": "quantitative",
                    "scale": {"zero": false}
                  },
                  "y": {
                    field: {"repeat": "row"},
                    type: "quantitative",
                    scale: {"zero": false}
                  },
                color: {
                    "field": X_quality,
                    "type": "quantitative"
                }
            }
        } 
    }        
    return vpd
}


/*
document.getElementById('form').onsubmit = function (event) {
    event.preventDefault()
    draw()
}
*/

/*
  "mark": {
    "type": "circle",
    "opacity": 0.8,
    "stroke": "black",
    "strokeWidth": 1
  },
  "encoding": {
    "x": {
      "field": "Year",
      "type": "ordinal",
      "axis": {"labelAngle": 0}
    },
    "y": {"field": "Entity", "type": "nominal", "axis": {"title": ""}},
    "size": {
      "field": "Things",
      "type": "quantitative",
      "legend": {"title": "Annual Global Stuff", "clipHeight": 30},
      "scale": {"range": [0, 5000]}
    },
    "color": {"field": "Entity", "type": "nominal", "legend": null}
*/