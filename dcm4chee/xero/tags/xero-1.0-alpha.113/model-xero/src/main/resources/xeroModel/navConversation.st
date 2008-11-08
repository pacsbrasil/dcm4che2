/**
 * Defines the navigation conversation information 
 * This allows one to figure out:
 * 	  The external UID/key that would match a given child element for each nav key item
 *	  The query UID that would be used to actually query for the element, and which must be part of the URL
 * 	    in order for any external matches to occur.
 *	  The presentation UID to use to find the presentation object
 */
function NavConversation(id, level) {
	if(!level) {
		level = 0;
		id = "results";
	}
	this.id = id;
	this.level = level;
};

/** Gets a child by the given name */
NavConversation.prototype.getChild = function NC_getChild(id) {
	if(! this.children ) this.children = new Object();
	var child = this.children[id];
	if( child ) return child;
	child = new NavConversation(id,this.level+1);
	child.parent = this;
	this.children[id] = child;
	return child;
};

/** Gets the matching query key */
NavConversation.prototype.getMatchId = function NC_getMatchId() {
	if( this.matchId ) return this.matchId;
	this.matchId = this.id;
	if( typeof(this.matchId)=="number" ) this.matchId = ""+this.matchId;
	var q = this.matchId.indexOf("?");
	if( q>=0 ) this.matchId = this.matchId.substring(0,q);
	return this.matchId;
};

/** Gets the matching url key -this can end up being a sub-string, so don't count on having it be the only value in the id */
NavConversation.prototype.getUrlId = function NC_getUrlId() {
	if( this.urlId ) return this.urlId;
	this.urlId = this.getPresentationId();
	if( this.queryModifier ) this.urlId = this.urlId + this.queryModifier;
	return this.urlId; 
};

/** Sets the presentation information to use, typically something like &gsps=NAME to indicate to use a named GSPS set. */
NavConversation.prototype.setPresentation = function NC_setPresentation(modifier) {
	if( modifier==this.presentation ) return false;
	this.presentation = modifier;
	this.presentationId = undefined;
	this.urlId = undefined; 
	return true;
};

/** Sets any query modifiers -these are things that affect the URL used, but leave the conversation/session contexts alone */
NavConversation.prototype.setQueryModifier = function NC_setQueryModifier(modifier) {
	if( modifier==this.queryModifier ) return false;
	this.queryModifier = modifier;
	this.urlId = undefined;
	return true;
};

/** Gets the presentation id - used to find the presentation child */
NavConversation.prototype.getPresentationId = function NC_getPresentationId() {
	if(!this.presentationId ) {
		if( !this.presentation ) return this.getMatchId();
		this.presentationId = this.getMatchId() + this.presentation;
	} 
	return this.presentationId;
};

/** 
 * This assigns any modifiers for the query that come from a parent query, only the first time through.
 * Both first and subsequent times, returns the modifier object contain all parent level modifiers.
 * Returns the combined modifiers.
 */
NavConversation.prototype.assignDefaultModifiers = function NC_assignDefaultModifiers(external) {
	console.debug("Assign default modifiers",this.level);
	if( this.queryModifier!==undefined || this.presentation!==undefined ) return this.getCombinedModifiers();
	if( !external ) return this.getCombinedModifiers();
	if( external.gspsLabel ) {
		this.setPresentation("&gsps="+external.gspsLabel);
		console.info("gspsLabel found, setting presentation modifier %s", this.presentation);
	}
	if( external.koLabel ) this.setQueryModifier("&koUID="+external.koLabel);
	return this.getCombinedModifiers();
};

/** 
 * Gets the combined modifier object for this level - needs to create a new one every time, as 
 * parent modifiers can change.
 */
NavConversation.prototype.getCombinedModifiers = function NC_getCombinedModifiers() {
	if( this.level==0 ) return;
	var ret = this.parent.getCombinedModifiers();
	if( this.presentation || this.queryModifier ) {
		if(!ret ) ret = new Object();
		ret[this.level] = "";
		if( this.presentation ) ret[this.level] = this.presentation;
		if( this.queryModifier ) ret[this.level] = ret[this.level] + this.queryModifier;
	}
	return ret;	
};