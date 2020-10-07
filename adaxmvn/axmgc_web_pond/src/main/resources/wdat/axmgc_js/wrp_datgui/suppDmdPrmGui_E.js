
function makeSuppDmdParamState() {
    let pvBlk = {
        P_qualCostPwr : 0.45,
        P_qtyCostPwr : 0.65,
        P_fixedCost : 0.4,
        P_qualDemandPwr : 1.4,
        P_priceDemandPwr : -0.7,
        C_qtyMax : 100

    }
    return pvBlk;
}

function makeParamUpDoer(prmSt, tgtScope, upFunc) {
    let dfunc = function() { 
        copyParamStateToScope(prmSt, tgtScope)
        upFunc(tgtScope)
    }
    return dfunc
}
function makeRangeUpDoer(rangeCounts, rangeUpFunc) {
    let dfunc = function() { 
        console.log("Range update:", rangeCounts)
        rangeUpFunc(rangeCounts)
    }
    return dfunc
}
function makeCback(doerFunc, prmName) {
    let cback = function (pVal) {
        console.log('change cback:', prmName, '=', pVal)
        doerFunc()
    }
    return cback
}
/*
       C_qtyMax : 100,
        P_qualCostPwr : 1.2,
        P_qtyCostPwr : 0.8,
        P_fixedCost : 0.4,
        P_qualDemandPwr : 1.4,
        P_priceDemandPwr : -0.7,
        X_qtyAbs : 65,
        X_quality : 0.7,
        X_unitPrice : 0.44
*/
function makeSuppDmdParamGui(prmSt, tgtScope, upFunc, rangeCounts, rangeUpFunc) { 
    let paramUpDoer = makeParamUpDoer(prmSt, tgtScope, upFunc)
    let rangeUpDoer = makeRangeUpDoer(rangeCounts, rangeUpFunc)
    var gui = new dat.GUI({ autoPlace: false });
    var fldr_smpCnt = gui.addFolder('Decision Sample Counts');
    fldr_smpCnt.add(rangeCounts, X_qtyAbs, 2, 7, 1).onChange(makeCback(rangeUpDoer, X_qtyAbs))
    fldr_smpCnt.add(rangeCounts, X_quality, 1, 6, 1).onChange(makeCback(rangeUpDoer, X_quality))
    fldr_smpCnt.add(rangeCounts, X_unitPrice, 2, 8, 1).onChange(makeCback(rangeUpDoer, X_unitPrice))

    var fldr_cost = gui.addFolder('Model Param Cost Factors');
    fldr_cost.add(prmSt, P_qualCostPwr, 0.1, 0.9, 0.05).onChange(makeCback(paramUpDoer,  P_qualCostPwr))
    fldr_cost.add(prmSt, P_qtyCostPwr, 0.01, 0.99, 0.02).onChange(makeCback(paramUpDoer,  P_qtyCostPwr))
    fldr_cost.add(prmSt, P_fixedCost, 0.2, 0.8, 0.1).onChange(makeCback(paramUpDoer,  P_fixedCost))
    fldr_cost.add(prmSt, P_qualDemandPwr, 0.2, 0.8, 0.1).onChange(makeCback(paramUpDoer,  P_qualDemandPwr))
    fldr_cost.add(prmSt, P_priceDemandPwr, -0.9, -0.1, 0.1).onChange(makeCback(paramUpDoer,  P_priceDemandPwr))
    fldr_cost.add(prmSt, C_qtyMax, 25, 250, 5).onChange(makeCback(paramUpDoer,  C_qtyMax))

    fldr_cost.open()

    fldr_smpCnt.open()
    return gui;
}
function makeSuppDmdRangeGui(upFunc) { 

}

function copyParamStateToScope(prmSt, scope) {
    // Foreach key in paramState.keys, set scope[key] = paramState[key]
    Object.assign(scope, prmSt)    
}



/*

controller.onChange(function(value) {
  // Fires on every change, drag, keypress, etc.
});

controller.onFinishChange(function(value) {
  // Fires when a controller loses focus.
  alert("The new value is " + value);
});

*/