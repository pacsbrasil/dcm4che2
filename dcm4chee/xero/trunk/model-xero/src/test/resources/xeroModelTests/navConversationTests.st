$unittest()$
$navConversation()$

var navConversationTests = new Object();

navConversationTests.createNavConversationTest = function() {
	var nc = new NavConversation();
};

/** Tests that the id/position information is correct for a simple child element with no modifications/changes */
navConversationTests.simpleChildTests = function() {
	var nc = new NavConversation();
	var c1 = nc.getChild("mypid");
	assertNotNull(c1,"c1");
	assertEquals(c1.id,"mypid","mypid", "base id");
	assertEquals(c1.getMatchId(),"mypid", "match id");
	assertEquals(c1.getUrlId(),"mypid", "url id");
	assertEquals(c1.getPresentationId(),"mypid", "presentation id");
};

/** Tests that the id/position information is correct for an extended child with all of presentation changes,
 * navigation changes and a copy of the basic object 
 */
navConversationTests.complexChildTest = function() {
	var nc = new NavConversation();
	var c1 = nc.getChild("mypid?c=1");
	var presMod = "&gsps=g";
	var queryMod = "&koUID=2";
	c1.setPresentation(presMod);
	c1.setQueryModifier(queryMod);
	assertNotNull(c1,"c1");
	assertEquals(c1.id,"mypid?c=1","mypid?c=1", "base id");
	assertEquals(c1.getMatchId(),"mypid", "match id");
	var ui = c1.getUrlId();
	if( (ui!="mypid"+presMod+queryMod) && (ui!="mypid"+queryMod+presMod) ) {
		fail("UrlId must be mypid + "+presMod+" + "+ queryMod +" in either order, but is "+ui);
	};	
	assertEquals(c1.getPresentationId(),"mypid"+presMod, "presentation id");
};

runUnitTests(navConversationTests);