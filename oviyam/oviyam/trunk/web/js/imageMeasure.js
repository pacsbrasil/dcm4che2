// This file contains the handlers called when the user clicks on the measure toolbar button.
var measureEnabled = false;
var tool = "ruler", drawing = false, beginPt,measurementOnMove = false, selectedShape = null, selectedHandle = -1;
var canvasCtx = null;
var canvasWidth, canvasHeight;
var instanceUid;

function measureOn(){	
	var drawCanvas = jQuery(jcanvas).parent().children().get(2);
    if(!measureEnabled) {
		measureEnabled = true; 
		doMouseWheel = false;  		
		jQuery("#ruler").addClass('toggleOff');
		jQuery("#lblLines").removeClass('imgOff').addClass('imgOn');
		jQuery("#line").attr('class','selected cmenuItem');
		
		if(winEnabled) {
			stopWLAdjustment();
		}

		if(moveEnabled) {
       		stopMove(jQuery("#move").get(0));
		}
	
		if(zoomEnabled) {
       		stopZoom(jQuery('#zoomIn').get(0));
		}

		if(scrollImages) {
			doStack(jQuery("#stackImage").get(0));
		}

		jQuery(drawCanvas).mousedown(function(e) {
			canvasMouseDownEvt(e);
		}).mousemove(function(e){
			canvasMouseMoveEvt(e);
		}).mouseup(function(e){
			canvasMouseUpEvt(e);
		}); 
		jQuery(drawCanvas).mousemove(function(e) {
			detectHandle(e);
		});
		jQuery(document).keydown(function(e) {
			keyEventHandler(e);
		});
		setTool(jQuery('#line'),'ruler');
		canvasCtx = drawCanvas.getContext('2d');
		canvasWidth = drawCanvas.width;
		canvasHeight = drawCanvas.height;
		measure();
		checkInstance();
		drawAllAnnotations();		
	    } else {
			measureEnabled = false;    		
			doMouseWheel = true;
		    jQuery('#ruler').removeClass('toggleOff');
			jQuery("#lblLines").removeClass('imgOn').addClass('imgOff');
			jQuery(drawCanvas).unbind('mousedown').unbind("mousemove").unbind('mouseup');
	    	jQuery(drawCanvas).unbind('mousemove');
			jQuery(document).unbind('keydown');
			clear();
    }        
}

function setTool(toolDiv,selectedTool) {
	this.tool = selectedTool;
	jQuery('.selected').removeClass('selected');
	jQuery(toolDiv).addClass('selected');
	jQuery('#rulerContext').hide();
}

function measure() {
	canvasCtx.lineWidth=2;
	canvasCtx.strokeStyle='red';
	canvasCtx.fillStyle='red';
	canvasCtx.font = '9pt Arial';
	init();
}

function checkInstance() {
	var param = getParameter(jQuery(jcanvas).parent().parent().find("#frameSrc").html(),"objectUID");
	if(param!=instanceUid) {
		reset();
		instanceUid = param;
	}
}

function canvasMouseDownEvt(e) {
	jQuery('.contextMenu').hide();//To hide the popup if showing		
	drawing = true;
	beginPt = new ovm.annotation.Point(e.offsetX,e.offsetY);
	measurementOnMove = false;
	e.stopPropagation();
	e.preventDefault();
	e.target.style.cursor = 'default';
	
	if(selectedShape!=null) {
		selectedHandle = detectSelectedHandle(beginPt);
		if(selectedHandle==-1) {
			if((selectedShape.getType()=="ruler" && !isLineIntersects(selectedShape.getBegin().getX(),selectedShape.getBegin().getY(), selectedShape.getEnd().getX(),selectedShape.getEnd().getY(), beginPt.getX(),beginPt.getY())) || (selectedShape.getType()=="rect" && !isInsideRect(selectedShape.getBegin(),selectedShape.getSize(),beginPt)) || (selectedShape.getType()=="oval" && !isInsideOval(selectedShape.getCenter().getX(),selectedShape.getCenter().getY(),selectedShape.getRadius().getX(),selectedShape.getRadius().getY(),beginPt.getX(),beginPt.getY()))){
				selectedShape = null;				
				drawAllAnnotations();
			}
		}
	} else {
		selectedShape = detectSelectedShape(beginPt);		
		if(selectedShape!=null) {
			drawSelectionHandles();
			return;
		}
	}
}

function canvasMouseMoveEvt(e) {
	if(drawing) {
		drawAllAnnotations();
		if(selectedShape==null) {   // New Shape 
			switch(tool) {
				case 'ruler':
					createNewRuler(e);
					break;
				case 'rect':
					createNewRect(e);
					break;
				case 'oval':
					createNewOval(e);
					break;
			}
		} else {
			if(selectedHandle==-1 || (selectedShape.getType()=="ruler" && selectedHandle==2)) {  //Moving an annotation
				measurementOnMove = true;
				switch(selectedShape.getType()) { 
					case 'ruler':
						moveRuler(e);
						break;
					case "rect":
						moveRect(e);
						break;
					case "oval":
						moveOval(e);
						break;
					}
				} else {    //Resizing Annotations
					switch(selectedShape.getType()) {
						case 'ruler':
							resizeRuler(e);
							break;
						case 'rect':
							resizeRect(e);
							break;
						case 'oval':
							resizeOval(e);							
							break;
				}
			}
		}
	}
}

function canvasMouseUpEvt(e) {
	if(selectedShape==null) { // New Shape
		switch (tool) {
		case 'ruler':
			addNewLine(beginPt, new ovm.annotation.Point(e.offsetX,e.offsetY));
			break;
		case 'rect':
			var endPt = new ovm.annotation.Point(e.offsetX-beginPt.getX(),e.offsetY-beginPt.getY());
			if(beginPt.getX()<beginPt.getX()+endPt.getX() && beginPt.getY()<beginPt.getY()+endPt.getY()) {
				addNewRect(beginPt,endPt);
			} else {
				if(beginPt.getX()<beginPt.getX()+endPt.getX()) {
					endPt = new ovm.annotation.Point(endPt.getX(),2);
					addNewRect(beginPt,endPt);
				} else {
					endPt = new ovm.annotation.Point(2,endPt.getY());
					addNewRect(beginPt,endPt);
				}
			}			
			break;
		case 'oval':
			var endPt = new ovm.annotation.Point(e.offsetX-beginPt.getX(),e.offsetY-beginPt.getY());
			if(beginPt.getX()<beginPt.getX()+endPt.getX() && beginPt.getY()<beginPt.getY()+endPt.getY()) {
				addNewOval(beginPt,endPt);
			} else {
				if(beginPt.getX()<beginPt.getX()+endPt.getX()) {
					endPt = new ovm.annotation.Point(endPt.getX(),2);					
				} else {
					endPt = new ovm.annotation.Point(2,endPt.getY());
				}
				addNewOval(beginPt,endPt);
			}
			break;
		}
		drawAllAnnotations();
	} else {
		switch (selectedShape.getType()) {
		case 'ruler':
			selectedShape.setLength((calculateLength(selectedShape.getBegin(), selectedShape.getEnd())/10).toFixed(3)+" cm");
			drawAllAnnotations();
			drawSelectionHandles();
			break;
		case 'rect':
			measureRect();
			drawAllAnnotations();
			drawSelectionHandles();
			break;
		case 'oval':
			measureOval();
			drawAllAnnotations();
			drawSelectionHandles();			
			break;
		}
	}
	drawing = false;
	measurementOnMove = false;
}

function detectHandle(e) {
	if(selectedShape!=null) {
		e.stopPropagation();
		e.preventDefault();
		if(!measurementOnMove) {
			var selected = detectSelectedHandle(new ovm.annotation.Point(e.offsetX,e.offsetY));
			if(selectedShape.getType()!="ruler") {
				switch (parseInt(selected)) {
				case 0:
					e.target.style.cursor = 'nw-resize';
					break;
				case 1:						
					e.target.style.cursor = 'n-resize';
					break;
				case 2:						
					e.target.style.cursor = 'ne-resize';
					break;
				case 3:						
					e.target.style.cursor = 'w-resize';
					break;
				case 4:						
					e.target.style.cursor = 'e-resize';
					break;
				case 5:						
					e.target.style.cursor = 'sw-resize';
					break;
				case 6:						
					e.target.style.cursor = 's-resize';
					break;
				case 7:						
					e.target.style.cursor = 'se-resize';
					break;
				default :
					e.target.style.cursor = 'default';						
					break;
				}
			} else {
				switch (parseInt(selected)) {
				case 0:
					e.target.style.cursor = 'crosshair';
					break;
				case 1:
					e.target.style.cursor = 'crosshair';
					break;
				case 2:
					e.target.style.cursor = 'move';
					break;
				default:
					e.target.style.cursor = 'default';
					break;
				}
			}
		} else {
			e.target.style.cursor = 'move';
		}
	}
}

function clear() {
	canvasCtx.clearRect(0,0,canvasWidth,canvasHeight);
}
function reset() {
	clearAnnotations();
	drawing = false;
	measurementOnMove = false;
	selectedShape = null;
	canvasCtx = jQuery(jcanvas).parent().children().get(2).getContext('2d');
	measure();
	clear();	
}