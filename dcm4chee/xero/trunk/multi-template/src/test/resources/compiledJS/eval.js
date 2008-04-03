/** This is what the "compiled" version of eval.tml should look like.
    Note that this is a javascript file, not an original template. 
  */
  
function eval(templates) {
};
eval.prototype = new Initial();

function eval_merge(context) {
	var ret = "<html><body>";
	context.clientV = new Object();
	context.clientV.v = 5;
	ret = ret + context.clientV.v;
	ret = ret+"</body></html>";
};
