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
		this.listeners = new Array();
	} else {
		this.conversation = new Object();
		// The id is an indicator that the parent is valid and has a valid location.
		this.id = "results";
	}
	this.childType = this.childTypes[this.level];
	this.childIdVar = this.idVars[this.level];
};

/** A key is used to identify a particular element in the set, typically a UID.
 * Offsets from this follow the natural list order.
 */
Merged.prototype.KEY="key";

Merged.prototype.isDebug = false;
if( Merged.prototype.isDebug ) Merged.prototype.debug = console.debug;
else Merged.prototype.debug = function() {};

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
Merged.prototype.idVars = ["PatientIdentifier", "StudyInstanceUID", "SeriesInstanceUID", "Position"];

Merged.prototype.keyTypes = {"key":"key", "keySet":"keySet", "position":"position"};
Merged.prototype.childTypes = ["patient", "study", "series", "image"];
Merged.prototype.IMAGE_QUERY_SIZE=64;
Merged.prototype.QUERY_SIZE=16;

Merged.prototype.QUERY = "/wado2/image.xml";

/** Sets external data for the the study.
 * There are 3 types of external data that can be queried for:
 *    Patient/Study - this is the initial information on the studies selected to be displayed
 * and contains no series/image information.
 *    Initial Study/Series/Image - contains all series and within each series, a given number of images.
 *    Subsequent Images - contains additional images not found in the first study/series query.
 * Clear is true if existing information is to be removed.
 * Returns undefined if no additional data is needed, or an array of the required queries for
 * data.  
 */  
Merged.prototype.mergeExternal = function Merged_mergeExternal(external, ret) {
	if( this.external!==external ) {
		this.external = external;
		this.fireChanged();
	}
	this.debug("Adding external data to %s at level %d", this.id,this.level);
	var child,childKey;
	var childExt,url,position;
	for(childKey in this.children) {
		child = this.children[childKey];
		if(!child.id) {
			child.invalidate(false);
		}
		if( child.id!==undefined && external.children) {
			if( child.id===null ) continue;
			childExt = external.children[child.id];
			if( childExt ) {
				ret = child.mergeExternal(childExt,ret);
				continue;
			}
			// So we don't have any external data for the child object, but we DO have it's id
			// That has to mean that it is an image query, and that this is the series level,
			if(!ret) ret = new Object();
			if( this.level===3 ) {
				this.debug("Adding image level query at %d of size %d initial offset %d", child.id, this.IMAGE_QUERY_SIZE, this.QUERY_SIZE);
				position = child.id - this.QUERY_SIZE;
				position -= (position % this.IMAGE_QUERY_SIZE);
				position += this.QUERY_SIZE;
				url = this.QUERY+"?SeriesInstanceUID="+this.id+"&Count="+this.IMAGE_QUERY_SIZE+"&Position="+position;
				this.debug("Adding %s to queries.",url); 
				ret[url] = url;
			}
			else if( this.level===1 ) {
				// This happens when the user views a NEW study in the same patient
				url = this.STUDY_QUERY+"?StudyInstanceUID="+child.id;
				this.debug("Adding new study query %s",url);
				ret[url] = url;
			}
			else {
				console.error("Unknown id/query this level %d in for child id %s", this.level, child.id);
			}
			continue;
		}
		// We don't have a child id - that has to mean that we need to make an initial study
		// query
		if( this.level===2 ) {
			if( !ret ) ret =new Object();
			url = this.QUERY+"?Position=0&Count="+this.QUERY_SIZE+"&StudyInstanceUID="+this.id;
			ret[url] = url;
			this.debug("Adding %s to queries.", url);
			return ret; 
		}
		else {
			assert(false);
			throw new Error("Unknown Merged query level "+this.level);
		} 
	}
	return ret;
}; 

/** Sets a given named navigation key object value. */
Merged.prototype.setNavigation = function Merged_setNavigation(key,value) {
	this.createConversation();
	if( this.conversation[key] == value ) {
		this.debug("setNavigation key is unchanged - continuing with doing anything.");
		return;
	}
	this.debug("setNavigation on %s is %s=%s", this.id, key, value);
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
			return;
		}
		this.debug("Changed data for old id %s is now %s",oldId,this.id);
		this.external = undefined;
		this.conversation = undefined;
		this.fireChanged();	
	};
	
	var childKey, child;
	for(childKey in this.children ) {
		child = this.children[childKey];
		child.invalidate(false);		
	}
};

Merged.prototype.fireChanged = function Merge_fireChanged() {
	if( this.level===0 ) return;
	var i,n=this.listeners.length;
	var listener;
	for(i=0; i<n;i++) {
		this.listeners[i].mergedListener(this);
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
		// Key type - one case is to avoid any searching at all, although it doesn't
		// allow you to find out much extra information in the child.
		if( offset===0 && val ) {
			console.info("Returning 0 offset information directly.");
			return val;
		}
		if( !this.external ) return undefined;
		var extChildren = this.getExtChildren();
		if( !extChildren ) {
			console.info("Found no children defined for %s under the name %s", this.id,this.childType);
			return undefined;
		}
		if( val ) {
			var posn = this.findExternal(val);
			if( posn!==undefined ) {
				offset = offset + posn;
			} else {
				console.warn("Didn't find offset %d for %s at position %s",offset,key,posn);
				return null;
			}
		}
		if( extChildren.length > offset && offset>=0 ) {
			return extChildren[offset][this.childIdVar];
		}
		return null;
	}
	else {
		key.a.b;
		throw new Error("Unknown key type "+key);
	}
};

/** Gets the external children list.
 * Normally, this is just the simple children objects, but
 * for the series child list (of a study), it only returns series that contain images.  
 * Other types of listeners/data is needed for non-image series.
 */
Merged.prototype.getExtChildren = function Merged_getExtChildren() {
	var ret = this.external[this.childType];
	if( this.level!==1 || !ret) return ret;
	var seriesImage = this.external._seriesImage;
	// The available series doesn't change unless the entire object is refreshed, once the
	// first set of series has been loaded.
	if( seriesImage ) return seriesImage;
	seriesImage = new Array();
	var ser, i,n=ret.length;
	for(i=0; i<n; i++) {
		ser = ret[i];
		if( !ser.image ) continue;
		seriesImage.push(ser);
	}
	this.external._seriesImage = seriesImage;
	return seriesImage;
};

/** Finds the position in the external data having the given id value */
Merged.prototype.findExternal = function Merged_findExternal(val) {
	var extChildren = this.external[this.childType];
	var i,n=extChildren.length;
	for(i=0; i<n; i++) {
		if( extChildren[i][this.childIdVar]==val ) return i;
	}
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


/** Assigns results information to the various levels.
 */

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
 * position/offset.  The given function is assigned ot obj as mergedListener and called.  A given
 * object can't have 2 or more functions without some other means of combining them.
 * @return the child object that is being listened to.
 */
Merged.prototype.addChildListener = function Merged_addChildListener(obj, listenerF, key, offset) {
	return this.getChild(key,offset).addListener(obj,listenerF);
};

Merged.prototype.addListener = function Merged_addListener(obj,listenerF) {
	obj.mergedListener = listenerF;
	this.listeners.push(obj);
	obj.mergedListener(this);
	return this;
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
			this.listeners[i].mergedListener(undefined);
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