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
	if( !id.indexOf ) id = ""+id;
	this.baseId = id;
	var amp = id.indexOf("&");
	if( amp>0 ) this.baseId = id.substring(0,amp);
	this.level = level;
};

/** Gets a child by the given name.  Adds the conversation id to the fetched child.
 */
NavConversation.prototype.getChild = function NC_getChild(id) {
	if(! this.children ) this.children = new Object();
	var child = this.children[id];
	if( child ) return child;
	child = new NavConversation(id,this.level+1);
	child.parent = this;
	this.children[id] = child;
	return child;
};

/** Gets the key that identifies the external match data. This key identifies the merge object to use.  More than one merge object
 * can point at the same external object, this this is not necessarily the external id to use. 
 */
NavConversation.prototype.getMergedId = function NC_getMergedId() {
	this.getExternalId();
	return this.mergeId;
};

/**
 * Gets the external object information key - this is the identifier that can be used to match up which
 * external object to look for.
 */
NavConversation.prototype.getExternalId = function() {
	if( this.externalId ) return this.externalId;
	this.mergeId = this.baseId;
	this.queryModifier = "";
	for(var name in this.modifier) {
		var qm = this.modifier[name];
		if( !qm ) continue;
		if( qm.modifier ) this.queryModifier = this.queryModifier + qm.modifier;
		if( qm.merged ) this.mergeId = this.mergeId + qm.merged;
	}
	this.externalId = this.baseId + this.queryModifier;
	return this.externalId; 
};

/** Sets the query modifier to use - query modifiers may or may not affect the actual remote data being returned, 
 * and the merged set used.  They affect the external query if modifier is non-null, 
 * and the merged data if isMerged.  If both modifier and isMerged are undefined, then the affect is to remove
 * the modifier.
 * isMerged can be a string to indicate the merged identity if it is different from the modifier, for instance to create
 * a second instance of merged data pointing to the same base external objects.
 */
NavConversation.prototype.setQueryModifier = function (name,modifier,isMerged) {
	this.externalId = undefined;
	this.mergedId = undefined;
	this.queryModifier = undefined;
	if( !(modifier || isMerged) ) {
		if( this.modifier ) this.modifier[name] = undefined;
		return;
	}
	if( !this.modifier ) this.modifier = new Object();
	if( isMerged===true ) isMerged = modifier; 
	var qm = {"modifier":modifier,"merged":isMerged,"name":name};
	this.modifier[name] = qm;
	return qm;
};

/** 
 * This assigns any modifiers for the query that come from a parent query, only the first time through.
 * Both first and subsequent times, returns the modifier object contain all parent level modifiers.
 * Returns the combined modifiers.
 */
NavConversation.prototype.assignDefaultModifiers = function NC_assignDefaultModifiers(external) {
	console.debug("Assign default modifiers, level",this.level);
	if( this.modifier!==undefined ) return this.getCombinedModifiers();
	if( !external ) return this.getCombinedModifiers();
	if( external.gspsLabel ) {
		this.setQueryModifier("pr","&gsps="+external.gspsLabel,true);
		console.info("gspsLabel found, setting presentation modifier %s", external.gspsLabel);
	}
	if( external.koUID ) {
		this.setQueryModifier("ko", "&ko=referenced&koUID="+external.koUID, external.koGSPS!=null);
		console.info("ko found, applying",external.koUID);
	}
	return this.getCombinedModifiers();
};

/** 
 * Gets the combined modifier object for this level - needs to create a new one every time, as 
 * parent modifiers can change.  Creates a modifier list with modifications at different levels
 * to allow searching for different result types and matching them up correctly.
 */
NavConversation.prototype.getCombinedModifiers = function NC_getCombinedModifiers() {
	if( this.level==0 ) return;
	var ret = this.parent.getCombinedModifiers();
	this.getExternalId();
	if( this.queryModifier ) {
		if(!ret ) ret = new Object();
		ret[this.level] = this.queryModifier;
	}
	return ret;	
};