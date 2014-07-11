var isSlider = false;

var instances = new Array();

jQuery(document).ready(function() {
    var ht = jQuery(window).height() - 3 + 'px';
    jQuery('body').css('height',ht );

    jQuery("#frameSrc").html(window.location.href);
    //initFrame();
    loadContextMenu();

    jQuery.get("UserConfig.do", {
        'settings':'viewerSlider',
        'todo':'READ'
    }, function(data){
        if(data.trim() == 'show') {
            isSlider = true;
            loadSlider();
        }
    });

    jQuery("#loopSlider").slider({
        max:1000,
        value:500
    });
    jQuery("#loopSlider").bind("slidechange", function(event, ui) {
        loopSpeed = ui.value;
    });

    jQuery('#containerBox .toolbarButton').hover(function() {
        var selected = jQuery('#containerBox').find('.current');
        jQuery(selected).attr('class', 'toolbarButton');
        jQuery(selected).children().attr('class', 'imgOff');
        jQuery(this).attr('class', 'toolbarButton current');
        jQuery(this).children().attr('class', 'imgOn');
        jQuery(this).css('cursor', 'pointer');
    }, function() {
        var selected = jQuery('#containerBox').find('.current');
        jQuery(selected).attr('class', 'toolbarButton');
        jQuery(selected).children().attr('class', 'imgOff');
        jQuery(this).css('cursor', 'auto');
    });


    jQuery('#ImagePane').bind('mousewheel', function(event, delta) {
        if(parent.doMouseWheel) {
            var frmBdr = jQuery(event.target).parent().parent().parent().parent().css('border');
            if(frmBdr.indexOf('none') >= 0) {
                setBorder(event.target);
            }
            var direction = event.originalEvent.wheelDelta > 0 ? 'Up' : 'Down';
            var iNo = null;
            if(direction == 'Up') {
                iNo = jQuery(parent.jcanvas).parent().parent().find('#totalImages').html();
                iNo = iNo.substring(iNo.indexOf(':')+1, iNo.indexOf("/"));
                prevImage(iNo-1);
            } else {
                iNo = jQuery(parent.jcanvas).parent().parent().find('#totalImages').html();
                iNo = iNo.substring(iNo.indexOf(':')+1, iNo.indexOf("/"));
                nextImage(iNo-1);

            }
         }
    });

    jQuery(document).bind('keydown', function(e) {
        var iNo = jQuery(parent.jcanvas).parent().parent().find('#totalImages').html();
        iNo = iNo.substring(iNo.indexOf(':')+1, iNo.indexOf("/"));

        if(e.keyCode == 38 || e.keyCode == 37) {
            prevImage(iNo-1);
        } else if(e.keyCode == 40 || e.keyCode == 39) {
            nextImage(iNo-1);
        }
    });    

    jQuery('body').css('background-color', parent.pat.bgColor);

    if(parent.scrollImages) {
        parent.startStack(jQuery('#canvasLayer2').get(0));
    }

    //To disable zooming
    if(parent.zoomEnabled) {
        //parent.doZoom(jQuery('#imageCanvas').get(0));
        parent.zoomEnabled = false;
        parent.doMouseWheel = true;
        jQuery('#zoomIn', parent.document).removeClass('toggleOff');
        jQuery('#zoomIn', parent.document).children().attr('class', 'imgOff');
    }

    jQuery('#applyWLDiv').click(function() {
        var qryStrTmp = window.location.href;
        var tmp_seruid = getParameter(qryStrTmp, 'seriesUID') + "_1";
        tmp_seruid = tmp_seruid.replace(/\./g, '_');

        var serCont = jQuery('#' + tmp_seruid, parent.document).parent();
        var wc_ww = jQuery('#windowLevel').html().split('/');

        var wind_center = wc_ww[0].match('WL:(.*)')[1].trim();
        var wind_width = wc_ww[1].match('WW:(.*)')[1].trim();

        var imgSrcTmp = serCont.children().get(0).src;
        if(imgSrcTmp.indexOf('Image.do') == -1) {
            serCont.children().each(function() {
                var imgSrc = 'Image.do?serverURL=' + parent.pat.serverURL;
                imgSrc += '&study=' + parent.pat.studyUID;
                imgSrc += '&series=' + jQuery(this).attr('seruid');
                imgSrc += '&object=' + jQuery(this).attr('sopuid');

                if(imgSrc.indexOf('windowCenter') >= 0) {
                    imgSrc = imgSrc.substring(0, imgSrc.indexOf('&windowCenter='));
                }

                imgSrc += '&windowCenter=' + wind_center + '&windowWidth=' + wind_width;
                jQuery(this).attr('src', imgSrc);
            });
        } else {
            serCont.children().each(function() {
                var imgSrc = jQuery(this).attr('src');

                if(imgSrc.indexOf('windowCenter') >= 0) {
                    imgSrc = imgSrc.substring(0, imgSrc.indexOf('&windowCenter='));
                }

                imgSrc += '&windowCenter=' + wind_center + '&windowWidth=' + wind_width;
                jQuery(this).attr('src', imgSrc);
            });
        }

        parent.wlApplied = true;

    });        
	initFrame();	
});  //for document.ready

jQuery('#canvasDiv').ready(function() {
    var queryString = window.location.href;
    var frameSize = getParameter(queryString, 'frameSize');
    if(frameSize == null) {
        setBorder(document.getElementById('imageCanvas'));
    } 
});

function loadSlider() {
    var qsTmp = window.location.href;
    var serUID = getParameter(qsTmp, 'seriesUID');
    var noOfInstances = 0;
    
	var seriesData = JSON.parse(sessionStorage[parent.pat.studyUID]);		
	for(var i=0;i<seriesData.length;i++) {
		if((seriesData[i])['seriesUID']==serUID) {
			if((seriesData[i])['totalInstances']>1) {
				jQuery('#trackbar1').slider({
					range: "min",
					value: imgInc+1,
					min: 1,
					max: (seriesData[i])['totalInstances'],
					slide: onTick
				});
			} else {
				jQuery('#footer').hide();
			}
			jQuery('.ui-slider-handle').css('height', '10px');
   			jQuery('.ui-slider-handle').css('width', '10px');
		    jQuery('.ui-slider-horizontal').css('height', '.4em');
		    jQuery('.ui-slider-horizontal').css('top', '8px');
		    jQuery('.ui-slider-horizontal').css('cursor', 'pointer');
			break;
		}
	}
}

/*function sliderHandler(trans, results) {

    var row = results.rows.item(0);

    if(row['NoOfSeriesRelatedInstances'] > 1) {
        jQuery('#trackbar1').slider( {
            range: "min",
            value: imgInc+1,
            min: 1,
            max: row['NoOfSeriesRelatedInstances'],
            slide: onTick
        });
    } else {
        jQuery('#footer').hide();
    }

    jQuery('.ui-slider-handle').css('height', '10px');
    jQuery('.ui-slider-handle').css('width', '10px');
    jQuery('.ui-slider-horizontal').css('height', '.4em');
    jQuery('.ui-slider-horizontal').css('top', '8px');
    jQuery('.ui-slider-horizontal').css('cursor', 'pointer');

}*/

function onTick(event, ui) {
    nextImage(ui.value - 2);
}

function initFrame() {
	jQuery('#patName').html(parent.pat.pat_Name);
	jQuery('#patID').html(parent.pat.pat_ID);
	jQuery("#patGender").html(parent.pat.pat_gender);
	jQuery("#studyDate").html(parent.pat.studyDate);
	jQuery("#studyDesc").html(parent.pat.studyDesc);
	imageHandler();
}

function getParameter(queryString, parameterName) {
    //Add "=" to the parameter name (i.e. parameterName=value);
    var parameterName = parameterName + "=";
    if(queryString.length > 0) {
        //Find the beginning of the string
        var begin = queryString.indexOf(parameterName);
        if(begin != -1) {
            //Add the length (integer) to the beginning
            begin += parameterName.length;
            var end = queryString.indexOf("&", begin);
            if(end == -1) {
                end = queryString.length;
            }
            return unescape(queryString.substring(begin, end));
        }

        return null;
    }
}

function setBorder(canvas) {
    var frames = jQuery(parent.document).find('iframe');
    for(var i=0; i<frames.length; i++) {
        jQuery(frames[i]).contents().find('#contextmenu1').css('display', 'none');
    }
    frames = null;

    if(parent.selectedFrame != null) {
        parent.selectedFrame.css('border','none');
    }
    parent.selectedFrame = jQuery('canvas').parent().parent().parent().parent();
    parent.selectedFrame.css('border','2px solid rgb(0, 255, 0)');

    var modality = null;

    if(jQuery(canvas).attr('id') != 'imageCanvas') {
        parent.jcanvas = jQuery(canvas).parent().find('#imageCanvas').get(0);

        modality = jQuery('#modalityDiv').html();

        if(parent.displayScout) {
            if(modality.indexOf("CT") >= 0) {
                Localizer.hideScoutLine();
                Localizer.drawScoutLineWithBorder();
            } else {
                MRLocalizer.hideScoutLine();
                MRLocalizer.drawScoutLineWithBorder();
            }
        }

        if(parent.scrollImages) {
            parent.startStack(jQuery('#canvasLayer2').get(0));
        }

        return;
    }

    parent.jcanvas = canvas;

    if(parent.displayScout) {
        modality = parent.pat.modality;
        if(modality.indexOf("CT") >= 0) {
            Localizer.hideScoutLine();
            Localizer.drawScoutLineWithBorder();
        } else {
            MRLocalizer.hideScoutLine();
            MRLocalizer.drawScoutLineWithBorder();
        }
    }

    if(parent.scrollImages) {
        parent.startStack(jQuery('#canvasLayer2').get(0));
    }
}