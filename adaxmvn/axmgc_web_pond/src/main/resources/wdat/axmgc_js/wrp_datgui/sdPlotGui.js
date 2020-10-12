// ---------------------------------------
// BEGIN: Code copied from inline JS block in sbox/axdem/suppDmd_v0e.html
// ---------------------------------------
	// ES6 = ES2015 is needed for const and let
	var myAxDeps = axdp_v02()  // The exported handle func for the axdeps lib.
	console.log("Here is myAxDeps: ", myAxDeps)
	myAxDeps.iGfnc("[msg from ubiSim]")

	// If direct include, root obj is:   math
	const ourMthJs = myAxDeps.iMathJs
	const ourPltlyRoot = myAxDeps.iPlotly
	const ourVegEmbRoot = myAxDeps.iVegEmb

//        displayJunk(ourMthJs, 'ex01')

	var dcm = dummyCalcMdl(ourMthJs)
	console.log("Dummy Calc Mdl:", dcm)
	const betterCalcMdl = buildCalcMdl(ourMthJs)
	console.log("Better Calc Mdl:", betterCalcMdl)

	const effSpcksArr = betterCalcMdl.eFncs.specsInEvalOrder()
	pushExprDisplays(effSpcksArr)

	const initSampCountKVO = dummySampleCounts()
	const rangeDescs = fancyRangeDescBlock(ourMthJs, initSampCountKVO)
	console.log('Range Decs: ', rangeDescs)

	var dInScp = dummyInScope()
	var dummyOut = dcm.evalAll(dInScp)
	console.log("Dummy Out:", dummyOut)

	var evalOutBlk = evalOverRanges_ES6(dcm, dInScp, rangeDescs)
	console.log('Eval Out Blk: ', evalOutBlk)

//        ourVegEmbRoot.embed('#vvd1', wthrVw);
//        ourVegEmbRoot.embed('#vvd2', barVl);

	var vpltDescTempl = makeVegaPlotDescTmpl()
	const vpltDatName = 'outBlk'
	const vpltDatObj = {
		name: vpltDatName,
		values : evalOutBlk
	}
	const vpltDesc = makeVegaPlotDesc(vpltDescTempl, vpltDatObj)

	var saveStuffHere = {
		lastRngDscs : rangeDescs,
		lastInScope : dInScp
	}

	const upVegPlt = function (eoBlk) {
		let vgChangeSet = ourVegEmbRoot.vega.changeset().remove(() => true).insert(eoBlk);
		saveStuffHere.savedView01.change(vpltDatName, vgChangeSet).run()
	}
	// This func will get called each time the input parameters change
	const paramUpdateCback = function(inScope) {
		console.log('paramUpdateCback() is running')
		let eoblk = evalOverRanges_ES6(dcm, inScope, saveStuffHere.lastRngDscs)
		upVegPlt(eoblk)
		saveStuffHere.lastInScope = inScope
	}
	const rangeUpdateCback = function(updSampleCountKVO) {
		console.log('rangeUpdateCback() is running')
		const upRngDescs = fancyRangeDescBlock(ourMthJs, updSampleCountKVO)
		let eoblk = evalOverRanges_ES6(dcm, saveStuffHere.lastInScope, upRngDescs)
		upVegPlt(eoblk)
		saveStuffHere.lastRngDscs = upRngDescs
	}
	ourVegEmbRoot.embed('#vvd3', vpltDesc).then(p => saveStuffHere.savedView01 = p.view);


	// https://plot.ly/javascript/plotlyjs-function-reference/#plotlynewplot
	// "Draws a new plot in an <div> element, overwriting any existing plot.
	// To update an existing plot in a <div>, it is much more efficient
	// to use Plotly.react than to overwrite it. "

	pltl3x3(myAxDeps.iPlotly, 'pl3dx3', 'Three related Z-surfaces')
//         pltlDbl(ourPltlyRoot, 'pdbl4', 'Spline and Splattter Plot')

	var tgtInSope = dummyInScope()
	var prmSt = makeSuppDmdParamState()
	var prmGui = makeSuppDmdParamGui(prmSt, tgtInSope, paramUpdateCback, initSampCountKVO, rangeUpdateCback)
	var prmPrnt = document.getElementById('param_scrn')
	prmPrnt.appendChild(prmGui.domElement)

// ---------------------------------------
// END:  Code copied from sbox/axdem/suppDmd_v0e.html
// ---------------------------------------