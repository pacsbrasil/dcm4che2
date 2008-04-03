/** This demonstrates what we want direct macros to look like.
  */
  
function directMacro(templates) {
};
directMacro.prototype = new Initial();

function directMacro_merge(context) {
	var ret = "<html><body>";
	ret = ret+directMacro(context);
	ret = ret+"</body></html>";
};

function directMacro_macDirect(context) {
	return "<h1>Direct Macro</h1>";
};

function directMacro_macIndirect(context) {
	return "<h1>Indirect Macro</h1>";
};