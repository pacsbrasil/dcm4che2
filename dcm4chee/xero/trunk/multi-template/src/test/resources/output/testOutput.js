/** This is a for-example rendering of testOutput.tml as a javascript file,
 * probably referenced via something like http://HOST/xero/output/testOutput.tjs
 * The JS is always the last stage rendering, so it can be run directly as final output.
 */
 
// define the inherit parts to ensure they are available in at least some fashion - some of those can be directly included (which ones?) while
// others need to be defined a subsequent time.

function testOutput(templateInfo) {
	
};

testOutput.prototype = new Templates();
testOutput.prototype.merge = function testOutput_merge(context) {
	return "...."+"....";
};
