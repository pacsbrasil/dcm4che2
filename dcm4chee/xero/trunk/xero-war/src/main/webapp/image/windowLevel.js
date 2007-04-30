/**
 * Handles window levelling in the display area, with a server round-trip used
 * on every image display (instead of an SVG image display).
 */

/**
 * Updates the window level according to the new client X,Y positions
 */
function updateWL(x,y) {
   var deltaX = (x-this.startX)/512.0;
   var deltaY = (y-this.startY)/512.0;
   this.windowWidth = deltaX*this.range + this.startWidth;
   this.windowCenter = deltaY*this.range + this.startCenter;
   if( this.windowWidth <=0.001 ) this.windowWidth = 0.001;
   if( this.windowWidth >= this.range ) this.windowWidth = this.range;
   if( this.windowCenter <= this.minValue ) this.windowCenter = this.minValue;
   if( this.windowCenter >= this.maxValue ) this.windowCenter = this.maxValue;
   if( isNaN(this.windowWidth) || isNaN(this.windowCenter) ) {
      alert("Resetting window width and center due to isNan");
      this.windowWidth = this.originalWidth;
      this.windowCenter = this.originalCenter;
   }
}

/**
 * Starts the window levelling
 */
 function startWL(x,y) {
  this.startX = x;
  this.startY = y;
  this.startWidth = this.windowWidth;
  this.startCenter = this.windowCenter;
  this.lastTime = new Date().getTime();
  this.sinceCompleteTime = -1;
  this.loadingImage = this.imageNode;
  this.quality = 0.8;
 }

/**
 * Handles the move move event for window levelling.
 */
function wlMouseMove(evt) {
  if( this.startX!=0 ) {
    this.updateWL(evt.clientX, evt.clientY);
    this.updateWLScreen(false);
  }
}

/**
 * Handles the mouse down event for window levelling.
 */
function wlMouseDown(evt) {
  this.startWL(evt.clientX, evt.clientY);
  if( evt.preventDefault ) {
     evt.preventDefault();
  }
  else if( body.setCapture ) {
    body.setCapture();
  }
}

/**
 * Handles the mouse up event for window levelling.  It is possible to get multiple mouse
 * up events because of the mouse being over or not over the image.
 */
function wlMouseUp(evt) {  
  if( this.startX==0 ) return;
  this.quality = 1.0;
  this.updateWL(evt.clientX, evt.clientY);
  this.startX = 0;
  this.startY = 0;
  this.updateWLScreen(true);
  if( body.releaseCapture ) {
    body.releaseCapture();
  }
}

/**
 * Updates the window level value on the screen - that is, changes the source URL or otherwise
 * re-loads the image.
 */
function updateWLScreen(isDone)
{
  var url="/xero/wlwado?requestType=WADO&studyUID="+studyUID+"&seriesUID="+seriesUID+"&objectUID="+imageUID+"&rows=512&windowCenter="+this.windowCenter+"&windowWidth="+this.windowWidth + "&imageQuality="+this.quality;  
  var isTime = false;
  var currentTime = new Date().getTime();
  var delay = currentTime - this.lastTime;
  if( delay>800 ) {
    isTime = true;
  }
  else if( this.loadingImage.complete ) {
    if( this.sinceCompleteTime == -1 ) {
      this.sinceCompleteTime = currentTime;
    }
    else if( this.sinceCompleteTime + 50 < currentTime ) {
      isTime = true;
    }
  }
  if( isDone || isTime) {
    this.loadingImage = new Image();
    this.loadingImage.src = url;
    this.startCount = this.startCount + 1;
    this.totalTime = this.totalTime + currentTime - this.lastTime;
    this.lastTime = currentTime;
    this.sinceCompleteTime = -1;
    this.avgTime = this.totalTime/this.startCount;
    this.wlCurrentValue.innerHTML = "C:"+this.startCount+" L:"+Math.round(this.windowWidth)+" C:"+Math.round(this.windowCenter) + " A:"+Math.round(this.avgTime) + " Q:"+Math.round(this.quality*100)/100;
    this.updateImage(url);
    if( delay > 200 && this.quality > 0.25 ) this.quality = this.quality * 0.98;
    if( delay > 500 && this.quality > 0.25 ) this.quality = this.quality * 0.9;
    if( delay < 120 && this.quality < 0.90 ) this.quality = this.quality * 1.02;
  }
}

/**
 * Creates a window level object
 */
function windowLevelBase(minValue, maxValue) {
  this.image = new Image();
  this.wlCurrentValue = document.getElementById("wlCurrentValue");
  this.range = maxValue - minValue;
  this.windowWidth = this.range;
  this.totalTime = 0;
  this.windowCenter = minValue + this.range/2.0;
  this.originalWidth = this.windowWidth;
  this.originalCenter = this.windowCenter;
  this.minValue = minValue;
  this.maxValue = maxValue;
  this.startX = 0;
  this.startY = 0;
  this.updateWL = updateWL;
  this.startWL = startWL;
  this.mouseDown = wlMouseDown;
  this.mouseUp = wlMouseUp;
  this.mouseMove = wlMouseMove;
  this.mouseOut = wlMouseUp;
  this.updateWLScreen = updateWLScreen;
  this.startCount = 0;
}
 