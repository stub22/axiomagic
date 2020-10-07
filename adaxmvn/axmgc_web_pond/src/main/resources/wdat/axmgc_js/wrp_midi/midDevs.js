
// See   https://webaudio.github.io/web-midi-api/#midiaccess-interface

var myMidAcc = null
var myInDevMgr = {imap: null, ivtest : true, gibber : false, arrByIdx : new Array(10), idev0 : null}
var myOutDevMgr = {omap: null, ovtest : true, gubber : false, arrByIdx : new Array(10), odev0 : null,
                  oTstChanIdx : 3, oTstPatchNum : 0}

function chkMidiDevs() { 
    console.log("calling nav.rqMA");
    var accessPipeEnd = navigator.requestMIDIAccess()
        .then(function(midAcc) {
            myMidAcc = midAcc
            console.log('midi access cback got midAcc=', midAcc);
            const inMap = midAcc.inputs
            const outMap = midAcc.outputs
            console.log("inMap:", inMap)
            console.log("outMap:", outMap)
            chkMidiInps(inMap)
            chkMidiOuts(outMap)
        })
    console.log("Passed access launch, ape=", accessPipeEnd)
}

function lastDigitOrNull(name) {
    if (name) {
        const lastChar = name.charAt(name.length - 1)
        // console.log("lastChar: ", lastChar)
        if (lastChar >= '0' && lastChar <= '9') {
            const lstDgt  = lastChar - '0'
            return lstDgt
        }
    }
    return null;
}
function chkMidiInps(inMap) { 

    console.log("chkMidiInps, inMap size=", inMap.size, "inDevMgr pre (unreliableAsync?)=", myInDevMgr)
    myInDevMgr.imap = inMap
    inMap.forEach( function( port, key ) {
      // var opt = document.createElement("option");
      // opt.text = port.name;
      // document.getElementById("inputportselector").add(opt);
        console.log("At key", key, "Found port", port)
        const devId = port.id
        const devIdx = lastDigitOrNull(devId)
        if (devIdx != null) {
            console.log("Assigned devIdx", devIdx, "for devId", devId)
            myInDevMgr.arrByIdx[devIdx] = port
            // This seems to be a queued log request, because myInDevMgr shows as complete every time.
            // console.log("Updated devMgr", myInDevMgr)
        } else {
            console.log("Last char of devId is not a digit, skipping in-dev", devId)
        }
        
    })
    console.log("END of chkMidiInps, final inDevMgr=", myInDevMgr)
        
}
function chkMidiOuts(outMap) { 
    console.log("BEGIN chkMidiOuts, outMap size=", outMap.size, "outDevMgr pre (unreliableAsync?)=",                myOutDevMgr);
    myOutDevMgr.omap = outMap
    outMap.forEach( function( port, key ) {
      // var opt = document.createElement("option");
      // opt.text = port.name;
      // document.getElementById("inputportselector").add(opt);
        console.log("At key", key, "Found port", port)
        const devId = port.id
        const devIdx = lastDigitOrNull(devId)
        if (devIdx != null) {
            console.log("Assigned devIdx", devIdx, "for devId", devId)
            myOutDevMgr.arrByIdx[devIdx] = port
            // Appears to be async:  console.log("Updated devMgr", myOutDevMgr)
        } else {
            console.log("Last char of devId is not a digit, skipping out-dev", devId)
        }  
    })
    console.log("END of chkMidiOuts, updated outDevMgr", myOutDevMgr)
}

function ensureInDevHndl(idx, currVal) {
    if (currVal) {
        return currVal
    } else {
        const dev = myInDevMgr.arrByIdx[idx];
        if (dev) {
            return dev
        } else {
            console.error("No in-dev hndl found at: ", idx)
            return null
        }
    }
}
function listenOnInDev0() {
    myInDevMgr.idev0 = ensureInDevHndl(0, myInDevMgr.idev0)
    const dev = myInDevMgr.idev0
    const devIdx = 0
    if (dev) {
        console.log("Registering handler for onmidimessage on dev", dev)
        dev.onmidimessage = function(m) {
            console.log("Rcvd message: ", m)
        }
    } else {
        console.error("Could not find MIDI input device #0")
    }
}
function closeMidiDevs() {
    console.log("Attemping to release all midi devs, callbacks, objects, using inDevMgr:", myInDevMgr)
    const dev = myInDevMgr.idev0
    if (dev) {
        const oldHndlr = dev.onmidimessage
        dev.onmidimessage = null
        console.log ("Set .onmidiMessage to null in", dev, "old handler was", oldHndlr)
        
    }
}
function otst() { 
    const odevID = "output-0"
    sendNoteOnOff(odevID)
}
function patchUp() {
    const odevID = "output-0"
    const outDev = myOutDevMgr.omap.get(odevID);
    const nextPatchNum = myOutDevMgr.oTstPatchNum + 1;
    const chanIdx = myOutDevMgr.oTstChanIdx;
    const patchCmd = 0xC0 + chanIdx;
    const patchMsg = [patchCmd, nextPatchNum]
    outDev.send(patchMsg)
    console.log("Sent patch msg:", patchMsg)
    myOutDevMgr.oTstPatchNum = nextPatchNum
}

function sendNoteOnOff(odevID) {
    const outDev = myOutDevMgr.omap.get(odevID);
    const chanIdx = myOutDevMgr.oTstChanIdx;
    const onVel = 0x68;
    sendOnOffPair(outDev, chanIdx, 60, onVel, 100.0, 250.0)
    const afterTime = window.performance.now()
    sendOnOffPair(outDev, chanIdx, 67, onVel, 150.0, 200.0) 
}

function sendOnOffPair(outDev, chanIdx, noteNum, onVel,  delayMsec, durMsec) {
    const noteOnCmd = 0x90 + chanIdx;
    const noteOffCmd = 0x80 + chanIdx;
    const noteOnMsg = [noteOnCmd, noteNum, onVel];   
    const noteOffMsg = [noteOffCmd, noteNum, 0x40];
    const beforeTime = window.performance.now()
    const onTime = beforeTime + delayMsec;
    const offTime = onTime + durMsec;
    outDev.send( noteOnMsg, onTime );  //omitting the timestamp means send immediately.
    outDev.send( noteOffMsg, offTime); 
    const afterTime = window.performance.now()
    console.log("Sent to outChanIdx=", chanIdx, "note=", noteNum, 
                    "onTime=", onTime, "offTime=", offTime, "afterTime=", afterTime)
}
function mapScale(idevID, odevID) {
    var inDev = myInDevMgr.imap.get(idevID);
    var outDev = myOutDevMgr.omap.get(odevID);
    if (inDev) {
        const oldHndlr = inDev.onmidimessage
        inDev.onmidimessage = function(m) {
            // console.log("Rcvd message: ", m)
            const [command, key, vel] = m.data
            const cmdHi = command / 16
            const cmdLo = command % 16
    //        console.log("cmdHi=", cmdHi, "cmdLo=", cmdLo, "key=", key, "vel=", vel)
            switch(cmdHi) {
                case 8: 
                    console.log("Note OFF on chan", cmdLo, "at noteNum", key, "vel=", vel)
                break;
                case 9: 
                    console.log("Note ON on chan", cmdLo, "at noteNum", key, "vel=", vel)
                    if (vel > 0) {
                        const ochnIdx = myOutDevMgr.oTstChanIdx
                        sendOnOffPair(outDev, ochnIdx, key, vel, 0.0, 160.0)
                    }
                break;
                case 10: 
                    console.log("Poly aftertouch on chan", cmdLo, "at noteNum", key, "pval=", vel)
                break;
                case 11: 
                    console.log("CC-param on chan", cmdLo, "at cc#", key, "pval=", vel)
                break;
                case 12: 
                    console.log("Prog-chg on chan", cmdLo, "key", key, "pval=", vel)
                break;
                case 13: 
                    console.log("Chan aftertouch on chan", cmdLo, "at noteNum", key, "pval=", vel)
                break;
                case 14: 
                    console.log("Pitch bend on chan", cmdLo, "key=", key, "pval=", vel)
                break;
    
                default:
                    console.error("Unexpected cmdHi=", cmdHi)                
            }
        }
        console.log("Mapper setup from inDev=", inDev, "to outDev=", outDev)
    } else {
        console.error("Could not find MIDI input device at idevID=", idevID)
    }

}

/*
Code examples in Medium article by Kacper Kula
function connectToDevice(device) {
  console.log('Connecting to device', device);
  device.onmidimessage = function(m) {
    const [command, key, velocity] = m.data;
    if (command === 145) {      debugEl.innerText = 'KEY UP: ' + key;
    } else if(command === 129) {      debugEl.innerText = 'KEY DOWN';
    el.addEventListener('click', connectToDevice.bind(null, inp));
// connectToDevice.bind(null, e));
   access.onstatechange = function(e) {
        replaceElements(Array.from(this.inputs.values()));
*/
   
   