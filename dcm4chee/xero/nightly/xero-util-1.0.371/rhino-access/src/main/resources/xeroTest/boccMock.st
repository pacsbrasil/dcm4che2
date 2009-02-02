function BoccMock(){};

BoccMock.createMock = function(toMock)
{
    if(toMock instanceof Function)
    {
        return BoccMock.createMockFunction();
    }
};

BoccMock.createMockFunction = function()
{
    var mockFunc = function()
    {
        mockFunc.numCalls += 1;
        
        var args = new Array();
        for(var i = 0; i < mockFunc.arguments.length; i += 1)
        {
            args.push(mockFunc.arguments[i]);
        }
        mockFunc.callArgs.push(args);
        
        // bodyCallback trumps returns.
        if(mockFunc.bodyCallback)
        {
            return mockFunc.bodyCallback.apply(null, args);
        }
        else
        {
            return mockFunc.returns;
        }
    }
    mockFunc.numCalls = 0;
    mockFunc.numCallsExpected = 0;
    mockFunc.bodyCallback = null;
    mockFunc.callArgs = new Array();
    
    return mockFunc;
};

BoccMock.verify = function(mockToVerify, msg)
{
    if(mockToVerify instanceof Function)
    {
        BoccMock.verifyMockFunction(mockToVerify, msg);
    }
};

BoccMock.verifyMockFunction = function(mockFuncToVerify, msg)
{
    if(!msg)
    {
        msg = "Function not invoked the expected number of times.";
    }
    $assertEquals(expected="mockFuncToVerify.numCallsExpected", actual="mockFuncToVerify.numCalls", msg="msg")$;
};