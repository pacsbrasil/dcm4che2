$rhino()$
$logging()$

function UnitTest() {};

UnitTest.areEqual = function(val1, val2) {
    if(val1 === val2)
    {
        return true;   
    }
    else
    {
        if(val1.equals)
        {
            return val1.equals(val2);
        }
        else
        {
            return false;
        }
    }
};

UnitTest.prototype.runUnitTests = function() {
	var key;
	var errorCount = 0;
	for(key in this) {
	
		if(key.substring(0, 4) == "test")
		{
			this.runUnitTest(key);
		}
		else
		{
			console.info("Not executing " + key + " because it is not prefixed with \'test\'.");
		}
	};
	if( errorCount!=0 ) {
		throw new Error("Failed "+errorCount+" tests.");
	}
	console.info("Done all tests.")
};

/** Runs a single unit test */
UnitTest.prototype.runUnitTest = function(testName) {
    console.info("Test "+testName);
	this[testName]();
	console.info(testName+" passed.");
};

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
	java.lang.System.out.println(msg);
	throw new Error(msg);
};