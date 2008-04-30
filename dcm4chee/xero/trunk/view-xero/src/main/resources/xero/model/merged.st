/**
 * Contains the patient/study/series/image layout object information
 * Basically, the way this works is that the patient/study/series information is fetched
 * as a single series level query, while the image information is fetched as
 * a collection of relatively small image level queries - large enough to allow some immediate
 * navigation, but small enough to not fetch too much information right up front.
 * At each level, a floating box can be constructed with information about the next level position
 * and offsets - for example, at the study level, one can ask for series level objects in various
 * named sets, eg "Left" or just "Series" and then 3rd or some numbered position.  This may 
 * create one or more virtual series or levels
 */
 
function Merged(level, keyRef, offset) {
	// Listeners includes both explicit listeners, in implicitly, children of this object.
	this.children = new Object();
	if( level && level>0 ) {
		this.level = level;
		this.keyRef = keyRef;
		this.offset = offset;
		this.levelName = this.names[level];
		this.idVar = this.idVars[level];
		this.listeners = new Array();
	} else {
		this.conversation = new Object();
		// The id is an indicator that the parent is valid and has a valid location.
		this.id = "results";
	}
};

/** A key is used to identify a particular element in the set, typically a UID.
 * Offsets from this follow the natural list order.
 */
Merged.prototype.KEY="key";

/** A keySet is used to explicitly list the available/interesting items */
Merged.prototype.KEY_SET="keySet";

/** A position is used to indicate an offset position overall from the start - this does NOT
 * necessarily mean that all items are present in the returned set - it maybe required to
 * make specific queries that get offset values directly - in specific, images use positions to
 * indicate the images.
 */
Merged.prototype.POSITION="position";

Merged.prototype.names = ["result", "patient", "study", "series", "image"];
Merged.prototype.level =0;
Merged.prototype.idVars = [null,"PatientIdentifier", "StudyInstanceUID", "SeriesInstanceUID", "Position"];

Merged.prototype.keyTypes = {"key":"key", "keySet":"keySet", "position":"position"};
Merged.prototype.childTypes = ["patient", "study", "series", "image"];

/** Sets a given named navigation key object value. */
Merged.prototype.setNavigation = function Merged_setNavigation(key,value) {
	this.createConversation();
	if( this.conversation[key] == value ) {
		console.debug("setNavigation key is unchanged - continuing with doing anything.");
		return;
	}
	console.debug("setNavigation on %s is %s=%s", this.id, key, value);
	this.conversation[key] = value;

	this.invalidate(true);
};

/** Invalidates the given key element, if there are any children depending on it. */
Merged.prototype.invalidate = function Merged_invalidate(isTop) {
	if( !isTop ) {
		var oldId = this.id;
		// Compute the new id if we can
		this.id = this.parent.lookupId(this.keyRef,this.offset);
		if( this.id === oldId ) {
			console.debug("Unchanged id=%s", this.id);
			return;
		}
		console.debug("Changed data for old id %s is now %s",oldId,this.id);
	
		// Clear out anyone listening to changes in this object - no one can listen at level 0.
		var i,n=this.listeners.length;
		for(i=0; i<n; i++) {
			this.listeners[i][this.levelName] = null;
			this.listeners[i].id = this.id;
		}
	};
	
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.invalidate(false);		
	}
};

/** Given a particular key name, figure out what the child element should be. 
 */
Merged.prototype.lookupId = function Merged_lookupId(key,offset) {
	if( !this.id ) return null;
	this.createConversation();
	
	var val = this.conversation[key];
	var type = this.keyTypes[key];
	if( type===this.KEY_SET ) {
		console.info("Looking up a keyset type mapping ");
		if(! val ) return undefined;
		if( offset < val.length && offset >=0 ) return val[offset];
		return null;
	}
	else if( type===this.POSITION ) {
		if(! this.external ) return undefined;
		if(!val) val = 0;
		if( val+offset<0 ) return null;
		if(! this.external.Viewable ) return undefined;
		if( this.external.Viewable > val+offset ) return val+offset;
		return null;
	}
	else if( type===this.KEY ) {
		console.info("Looking up a key type mapping found %s",val);
		// Key type - one case is to avoid any searching at all, although it doesn't
		// allow you to find out much extra information in the child.
		if( offset===0 && val ) return val;
		if( !this.external ) return undefined;
		var extChildren = this.external[this.childTypes[this.level]];
		if( !extChildren ) return undefined;
		if( val ) {
			var posn = this.findExternal(val);
			if( posn ) {
				offset = offset + posn;
			}
			else {
				console.warn("Can't find child item "+posn);
				return null;
			}
		}
		if( extChildren.length > offset && offset>=0 ) return extChildren[offset];
		return null;
	}
	else {
		throw new Error("Unknown key type "+key);
	}
};

/** Finds the position in the external data having the given id value */
Merged.prototype.findExternal = function Merged_findExternal(val) {
	var extChildren = this.external[this.childTypes[this.level]];
	var i,n=extChildren.length;
	var searchId = this.idVars[this.level+1];
	for(i=0; i<n; i++) {
		if( extChildren[i][searchId]==val ) return i;
	}
	return null;
}


/** Gets the conversational context for this object, or creates one if there isn't one yet. 
 * Throws an exception if the navigation information for the parent has not yet been retrieved - 
 * that is, if the navigation is currently undefined.
 */
Merged.prototype.createConversation = function Merged_createConversation() {
	if( this.conversation ) return;
	if( !this.id ) throw new Error("This merged object doesn't yet have an ID - set the parent navigation/information first for "+this.keyRef);
	this.parent.createConversation();
	this.conversation = this.parent.conversation[this.id]
	if( this.conversation ) return;
	this.conversation = new Object();
	this.parent.conversation[this.id] = this.conversation;
};


/** Assigns results information to the various levels - causes the actual
 * information to display to be decided.  
 */
Merged.prototype.assign = function Merged_assign(results) {
	this.results = results;
	var i,n = this.listeners.length;
	if(!this.name) this.name = this.names[this.level];
	for(i=0; i<n; i++) {
		this.listeners[i][this.name] = results;
	}
	
};

/** Gets the given merged child element.  The object will be disconnected from this merged object
 * if on the next apply, no child elements are found reflecting that child information.
 */
Merged.prototype.getChild = function Merged_getChild(key,offset) {
	var ckey = key+"://"+offset;
	var child =this.children[ckey];
	if( ! child ) {
		child = new Merged(this.level+1, key, offset);
		child.parent = this;
		this.children[ckey] = child;
	}
	return child;
};

/**
 * Indicate that a given object is a listener for this level of information - when a new assignment
 * is made, the values in the child object will be changed.  Multiple objects can listen for the same
 * position/offset.  A change is indicated by calling the "setResult" method if present, or by 
 * assigning the result level attribute otherwise eg
 *	  patient/study/series/image - whatever the level name is, it contains a unified view of the data 
 *		for that particular object - call it <result> generically
 *	  <result>.<series/study/patient> is the parent unified view object
 *	  <result>.conversation is the conversational context associated values
 *	  <result>.session is the session context associated values
 *	  
 */
Merged.prototype.addListener = function Merged_addListener(listener, key, offset) {
	var child = this.getChild(key,offset);
	child.listeners.push(listener);
	listener.merged = child;
};

/**
 * Remove ALL listeners - used when the listeners are being destroyed/cleared and recreated.
 * Only removes listeners from this level and below.  This also removed all child Merged objects.
 * Any previously created/used merged objects are no longer valid. 
 */
Merged.prototype.removeAllListeners = function Merged_removeAllListeners() {
	if( this.level>0 ) {
		var i,n=this.listeners.length;
		for(i=0; i<n; i++) {
			this.listeners[i].merged = undefined;
			this.listeners[this.names[this.level]] = undefined;
		}
		this.listeners = new Array();
	}
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.removeAllListeners();
	}
	this.children = new Object();
};