


function styleTextFields(parent) {
	if (parent) {
		parent.find("input.ui-textfield:text").textfield();
	}
	else {
		$("input.ui-textfield:text").textfield();
	}
}


function styleTextAreas(parent) {
	if (parent) {
		parent.find("textarea.ui-textarea").textarea();
	}
	else {
		$("textarea.ui-textarea").textarea();
	}
}


function styleComboBoxes(parent) {
	if (parent) {
		parent.find("select.ui-combobox").combobox();
	}
	else {
		$("select.ui-combobox").combobox();
	}
}


function styleYearSpinners(parent) {
	if (parent) {
		parent.find("input.ui-spinner-year").yearspinner();
	}
	else {
		$("input.ui-spinner-year").yearspinner();
	}
}


function styleMonthSpinners(parent) {
	if (parent) {
		parent.find("input.ui-spinner-month").monthspinner();
	}
	else {
		$("input.ui-spinner-month").monthspinner();
	}
}


/*
 * JQUERY-UI widget that applies the jquery-ui styles to an ordinary HTML 
 * input:text element.
 */
$.widget( "ui.textfield", {
	_create: function() {
		var textfield = this.element;
		if (textfield.attr("readonly")=="readonly") {
			textfield.addClass("ui-widget ui-widget-content ui-text-readonly");
		}
		else {
			textfield.addClass("ui-widget ui-widget-content ui-state-default ui-corner-all ui-text")
			textfield.hover(function(){
				$(this).addClass("ui-state-hover");
			},function(){
				$(this).removeClass("ui-state-hover");
			});
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus');
				}
			});
		}
	}
});

/*
 * JQUERY-UI widget that applies the jquery-ui styles to an ordinary HTML 
 * textarea element.
 */
$.widget( "ui.textarea", {
	_create: function() {
		var textfield = this.element;
		if (textfield.attr("readonly")=="readonly") {
			textfield.addClass("ui-widget ui-widget-content ui-text-readonly");
		}
		else {
			textfield.addClass("ui-widget ui-widget-content ui-state-default ui-corner-all ui-text")
			textfield.hover(function(){
				$(this).addClass("ui-state-hover");
			},function(){
				$(this).removeClass("ui-state-hover");
			});
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus');
				}
			});
		}
	}
});
  
/*
 * JQUERY-UI widget that 'converts' an ordinary HTML select element
 * into a (editable) combobox with optional support for auto-complete.
 */
$.widget( "ui.combobox", {
	_create: function() {
		var input,
		that = this,
		select = this.element.hide(),
		wicketCallbackURL = select.attr('wicket-callback-url'),
		editableAttr = select.attr('editable'),
		initialValue = select.attr('initial-value'),
		selected = select.children( ":selected" ),
		value = selected.val() ? selected.text() : 
			editableAttr=="true" && initialValue ? initialValue : "",
					wrapper = this.wrapper = $( "<span>" )
					.addClass( "ui-combobox-base" )
					.insertAfter( select );

		function removeIfInvalid(element) {
			var value = $( element ).val(),
			matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( value ) + "$", "i" ),
			valid = false;
			select.children( "option" ).each(function() {
				if ( $( this ).text().match( matcher ) ) {
					this.selected = valid = true;
					return false;
				}
			});
			if ( !valid ) {
				// remove invalid value, as it didn't match anything
				$( element )
				.val( "" )
				.attr( "title", value + " didn't match any item" )
				.tooltip( "open" );
				select.val( "" );
				setTimeout(function() {
					input.tooltip( "close" ).attr( "title", "" );
				}, 2500 );
				input.data( "autocomplete" ).term = "";
				return false;
			}
		}

		input = $( "<input>" )
		.appendTo( wrapper )
		.val( value )
		.attr( "title", "" )
		.autocomplete({
			delay: 0,
			minLength: 0,
			source: function( request, response ) {
				var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
				response( select.children( "option" ).map(function() {
					var text = $( this ).text();
					if ( this.value && ( !request.term || matcher.test(text) ) )
						return {
						label: text.replace(
								new RegExp(
										"(?![^&;]+;)(?!<[^<>]*)(" +
												$.ui.autocomplete.escapeRegex(request.term) +
												")(?![^<>]*>)(?![^&;]+;)", "gi"
								), "<strong>$1</strong>" ),
								value: text,
								option: this
					};
				}) );
			},
			select: function( event, ui ) {
				ui.item.option.selected = true;
				that._trigger( "selected", event, {
					item: ui.item.option
				});
			},
			change: function( event, ui ) {
				if (wicketCallbackURL) {
					var url = wicketCallbackURL;
					if (url.indexOf('?')==-1) {
						url += '?selectedValue=';
					}
					else {
						url += '&selectedValue=';
					}
					wicketAjaxGet(url + input.val(),function(){},function(){});
				}
				//if ( !ui.item )
				//  return removeIfInvalid( this );
			}
		})
		.addClass( "ui-widget ui-widget-content ui-corner-left" );

		if (editableAttr!="true") {
			input.attr("readonly","readonly");
		}

		if (input.attr("readonly")=="readonly") {
			input.addClass("ui-state-default ui-combobox-input-readonly");
		}
		else {
			input.addClass("ui-state-default ui-combobox-input");
			input.hover(function(){$(this).addClass("ui-state-hover ui-combobox-input-hover");},
					function(){$(this).removeClass("ui-state-hover ui-combobox-input-hover");
			});
		}

		input.data( "autocomplete" )._renderItem = function( ul, item ) {
			return $( "<li>" )
			.data( "item.autocomplete", item )
			.append( "<a>" + item.label + "</a>" )
			.appendTo( ul );
		};

		$( "<a>" )
		.attr( "tabIndex", -1 )
		.appendTo( wrapper )
		.button({
			icons: {
				primary: "ui-icon-triangle-1-s"
			},
			text: false
		})
		.removeClass( "ui-corner-all" )
		.addClass( "ui-corner-right ui-combobox-toggle" )
		.click(function() {
			// close if already visible
			if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
				input.autocomplete( "close" );
				removeIfInvalid( input );
				return;
			}

			// work around a bug (likely same cause as #5265)
			$( this ).blur();

			// pass empty string as value to search for, displaying all results
			input.autocomplete( "search", "" );
			input.focus();
		});

		input
		.tooltip({
			position: {
				of: this.button
			},
			tooltipClass: "ui-state-highlight"
		});
	},

	destroy: function() {
		this.wrapper.remove();
		this.element.show();
		$.Widget.prototype.destroy.call( this );
	}
});


$.widget( "ui.yearspinner", $.ui.spinner, {
    options: {
        min: 0,
        max: 199,
        step: 1,
        numberFormat: "n0"
    },
    _create: function( ) {
    	this._super();
    	
		var textfield = this.element;
		if (textfield.attr("readonly")=="readonly") {
			textfield.addClass("ui-text-readonly");
		}
		else {
			textfield.addClass("ui-text");
			textfield.hover(function(){
				$(this).addClass("ui-text-hover");
			},function(){
				$(this).removeClass("ui-text-hover");
			});
		}
    },
    _value: function( value, allowAny ) {
    	this._super(value, allowAny);
    	var wicketCallbackURL = this.element.attr('wicket-callback-url');
		if (wicketCallbackURL) {
			var url = wicketCallbackURL;
			if (url.indexOf('?')==-1) {
				url += '?value=';
			}
			else {
				url += '&value=';
			}
			wicketAjaxGet(url + value,function(){},function(){});
		}
    },
    _parse: function( value ) {
        if ( typeof value === "string" ) {
            var yearsText = this.element.attr('loc-years');
            if (yearsText) {
            	value = value.replace(yearsText,"");
            }
            if ( Number( value ) == value ) {
                return Number( value );
            }
        }
        return value;
    },
    _format: function( value ) {
    	var yearsText = this.element.attr('loc-years');
    	if (yearsText) {
    		return value + " " + yearsText;
    	}
    	else {
    		return value;
    	}
    }
});


$.widget( "ui.monthspinner", $.ui.spinner, {
    options: {
        min: 0,
        max: 12,
        step: 1,
        numberFormat: "n0"
    },
    _create: function( ) {
    	this._super();
    	
		var textfield = this.element;
		if (textfield.attr("readonly")=="readonly") {
			textfield.addClass("ui-text-readonly");
		}
		else {
			textfield.addClass("ui-text");
			textfield.hover(function(){
				$(this).addClass("ui-text-hover");
			},function(){
				$(this).removeClass("ui-text-hover");
			});
		}
    },
    _value: function( value, allowAny ) {
    	this._super(value, allowAny);
    	var wicketCallbackURL = this.element.attr('wicket-callback-url');
		if (wicketCallbackURL) {
			var url = wicketCallbackURL;
			if (url.indexOf('?')==-1) {
				url += '?value=';
			}
			else {
				url += '&value=';
			}
			wicketAjaxGet(url + value,function(){},function(){});
		}
    },
    _parse: function( value ) {
        if ( typeof value === "string" ) {
            var yearsText = this.element.attr('loc-months');
            if (yearsText) {
            	value = value.replace(yearsText,"");
            }
            if ( Number( value ) == value ) {
                return Number( value );
            }
        }
        return value;
    },
    _format: function( value ) {
    	var yearsText = this.element.attr('loc-months');
    	if (yearsText) {
    		return value + " " + yearsText;
    	}
    	else {
    		return value;
    	}
    }
});
