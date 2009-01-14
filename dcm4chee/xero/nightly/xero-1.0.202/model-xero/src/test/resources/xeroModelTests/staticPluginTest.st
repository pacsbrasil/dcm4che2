$unittest(unittest="spt")$

var scriptXml = "$model/scripts(version="1.0-alpha5"); format="js"$";

spt.testScriptPlugins = function() {
   console.info("%s",scriptXml);
	var i = scriptXml.indexOf("src=\"js/sarissa.js?v=1.0-alpha5\"");
	console.info("%s",i);
	$assert(test="i>0",msg="Scripts generation must include sarissa.js.")$;
};

runUnitTests(spt);