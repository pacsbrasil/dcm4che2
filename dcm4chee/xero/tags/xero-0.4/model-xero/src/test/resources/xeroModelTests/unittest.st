$rhino()$
$logging()$

var suite = new Object();

function assert(bool,msg) {
	if( bool ) return;
	fail("Assertion failure:"+msg);
};

function assertEquals(val, expected, varname) {
	if( val===expected ) return;
	fail("Expected value "+expected+" was "+val+" for variable "+varname);
};

function assertFloat(val, expected, varname) {
	if( Math.abs(val-expected)<0.0001 ) return;
	fail("Expected value "+expected+" was "+val+" for variable "+varname);
};

function assertNotNull(val, varname) {
	if( val!=null ) return;
	fail("Expected non-null value but was null for variable "+varname);
};
function assertNull(val, varname) {
	if( val==null ) return;
	fail("Expected null value but was "+val+" for variable "+varname);
};

function fail(msg) {
	print(msg);print("\n");
	throw new Error(msg);
};

function runUnitTests(suite) {
	var key, func;
	var errorCount = 0;
	for(key in suite) {
		console.info("Testing "+key);
			func = suite[key];
			func();
		console.info(key+" passed");
	};
	if( errorCount!=0 ) {
		throw new Error("Failed "+errorCount+" tests.");
	}
	console.info("Done all tests.")
};