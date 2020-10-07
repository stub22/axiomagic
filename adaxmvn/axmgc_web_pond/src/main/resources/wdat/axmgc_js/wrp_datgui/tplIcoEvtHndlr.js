// ---------------------------------------
// Begin functions moved from WebXml.scala
// ---------------------------------------
function routeEvt(evt) {
    // alert('Ancestor got click, evtTgt=' + event.target)
    // Seems that target for keypress is always the body...
    var evtTyp = evt.type
    var evtTgt = evt.target
    var etID = evtTgt.id
    var currTgt = evt.currentTarget
    var ctID = currTgt.id
    var dbgYes = (! evtTyp.includes("mouse"))
    if (dbgYes) {
    		var dbgTxt = "routeEvt{type=" + evtTyp + ", target=" + evtTgt + ", etID=" + etID + ", currTgt=" + currTgt + ", ctID=" + ctID + "}"
    		console.log(dbgTxt)
    }
    var prevC = evtTgt.style.color
    var nextC =  makeRandomColor()
    if (dbgYes) {
    		var clrDbg = "changing color from " + prevC + " to " + nextC
    		console.log(clrDbg)
    }
    evtTgt.style.color = nextC
	   evtTgt.style.fill = nextC
}

function makeRandomColor(){
// https://stackoverflow.com/questions/1484506/random-color-generator
    var c = '';
    while (c.length < 6) {
        c += (Math.random()).toString(16).substr(-6).substr(-1)
    }
    return '#'+c;
}


function attchHndlrs(domElmt) {
    var ourEvtNms = ['click', 'mouseover', 'mouseout', 'mousemove', 'keypress']
    // https://javascript.info/bubbling-and-capturing
    // Optional 3rd arg is boolean, where true => capture-handler, but dflt=false => bubble handler
    // is preferred.
    console.log("handler names are: ", ourEvtNms)
    console.log("attaching handlers to element: ", domElmt)
    ourEvtNms.forEach(function(nm) {
        domElmt.addEventListener(nm, routeEvt)
    })
}
function attchHndlrsAtId(domID) {
		console.log("looking up dom el for event handlers at: ", domID)
    var domEl = document.getElementById(domID)
    attchHndlrs(domEl)
}

var myTicker = null
var myIntervalMsec = 1000
function startTicker () {
    if (myTicker == null) {
        myTicker = window.setInterval(myTickFunc, myIntervalMsec);
    } else {
        console.log("Ticker already running, ignoring START rq")
    }
}
function stopTicker () {
    if (myTicker != null) {
        window.clearInterval(myTicker)
        myTicker = null;
    } else {
        console.log("Ticker isn't running, ignoring STOP rq")
    }
}
function myTickFunc() {
  var d = new Date();
  document.getElementById("tt_out").innerHTML = d.toLocaleTimeString();
}
// ---------------------------------------
// End functions moved from WebXml.scala
// ---------------------------------------