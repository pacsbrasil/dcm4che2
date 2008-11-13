$unittest(unittest="spt")$

var scriptXml = "$model/scripts(); format="js"$";

spt.testScriptPlugins = function() {
	var i = scriptXml.indexOf("src=\"js/sarissa.js\"");
	$assert(test="i>0",msg="Scripts generation must include sarissa.js.")$;
};

runUnitTests(spt);