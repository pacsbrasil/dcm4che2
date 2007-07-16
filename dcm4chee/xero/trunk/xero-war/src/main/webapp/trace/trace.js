// JavaScript Document


/////////////////////////////////////////////////
// To be used by the page being debugged.
var debugWin = window.parent.parent.traceFrame;

function trace(msg)
{
	if (debugWin != undefined && debugWin != null){
		debugWin.trace(msg);
	}
	
	//alert("traceMsg: " + msg);
}

function assert(bTest, msg)
{
	if (!bTest){
		window.alert("Assert failed: " + msg);
	}
}