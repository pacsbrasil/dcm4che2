	    var evtText1;
	    var windowLevelMatrix, windowLevelFilter, toWindowLevel;
	    var windowWidth, windowCenter;
	    var startWindowWidth;
	    var startWindowCenter;
	    var startX, startY;
	    var sourceWidth = 255;
	    var sourceStart = 0;
	    var matrix = new Array(20);
        function init() {
            windowWidth = 1.0;
            windowCenter = 0.5;
            evtText1 = document.getElementById("evtText1");
            windowLevelMatrix = document.getElementById("windowLevelMatrix");
            windowLevelFilter = document.getElementById("windowLevelFilter");
            toWindowLevel = document.getElementById("toWindowLevel");
            displayWindowLevel();
            for(i=0; i<matrix.length; i++) matrix[i] = 0.0;
            matrix[18] = 1.0;
            updateWindowLevel();
        }
        
        function displayWindowLevel() {
           evtText1.firstChild.nodeValue = "W: "+(windowWidth * sourceWidth) +" L: "+(windowCenter * sourceWidth + sourceStart);
        }
	
		
		function imageMouseUp(evt) {
		   updateWindowLevel(evt);
		   updateMatrixValue();
		}
		
		function updateMatrixValue() {
		   computeMatrix();
		   var strMatrix = "";
		   for(i=0; i<matrix.length; i++ ) strMatrix = strMatrix + " " + matrix[i];
		   toWindowLevel.setAttributeNS(null,"style", "");
		   windowLevelMatrix.setAttributeNS(null,"values", strMatrix);
		   toWindowLevel.setAttributeNS(null,"style", "filter: url(#windowLevelFilter);");
		}
		
		function computeMatrix() {
		   var matConst = windowWidth/2 - windowCenter;
		   var matMult = 1.0/windowWidth;
		   var j = 0;
		   for(i=0; i<3; i++) {
		      matrix[i*5+(j++)] = matMult;
		      matrix[i*5+4] = matConst;
		   }
		}
		
		function updateWindowLevel(evt) {
		   var deltaX = (evt.clientX - startX)/512.0;
		   var deltaY = (evt.clientY - startY)/512.0;
		   windowWidth = deltaX + startWindowWidth;
		   windowCenter = deltaY + startWindowCenter;
		   if( windowWidth <=0.01 ) windowWidth = 0.01;
		   if( windowWidth >=1 ) windowWidth = 1;
		   if( windowCenter <=0 ) windowCenter = 0;
		   if( windowCenter >=1 ) windowCenter = 1;
		   displayWindowLevel();
		}
		
		function imageMouseMove(evt) {
		   if( evt.button==0 ) {
  		     updateWindowLevel(evt);
  		     updateMatrixValue();
  		   }
		}

		function imageMouseDown(evt) {
			startX = evt.clientX;
			startY = evt.clientY;
			startWindowWidth = windowWidth;
			startWindowCenter = windowCenter;
		}
