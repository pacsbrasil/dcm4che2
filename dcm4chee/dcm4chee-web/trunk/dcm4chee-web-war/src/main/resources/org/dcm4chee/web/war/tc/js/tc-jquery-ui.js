
/*

TRADITIONAL GREEN:
http://jqueryui.com/themeroller/#!zThemeParams=5d00000100ee05000000000000003d8888d844329a8dfe02723de3e5701fa198449035fc0613ff729a37dd818cf92b1f6938fefa90282d04ae436bb72367f5909357c629e832248af2c086db4ab730aa4cced933a88449eca61786b7f3b23d47f58a712d809b6088edfb34a841fb318be8af5d9cd22085295c2342e3561a4ec5e4997eca3049a2b6ba73495abeb7f107d1d0249a49829214905f6676c0c9341bf1d9179fbc057bd8b6061a0e72f917803c6df0f6aa0c9aebfc59234c179aabfc1e48f61ac7a3fd6c310090b873711d1f3c2dfaff1ef4818c1b4582d9c68fe396a757fd6560010fc49ac840c941a46d1a9af171a75a3c0fcf8f5b4c175505a09e4281c697ecbe2576458a0a502cc61f03a9cd84c807337d9891a9d6c17454e92863f9fa793281b24856e10441433074b44af2efd6658adcd2d541663c59b5d791747e5f601b148e1549fe7624490e092024b95955200150bbd5f2e2a1224e1ab6f6c77a805f578f8ed52d7d28799958ddef0113213b0245285d3b447078861e1864a6e0e5149bedec71590c2ac244d472b6fe954ba2c26020f0a6fb83931477f379ab21349bdf13f80b4c0735cb8f57a3e680f210b6d01b854669c037f4d5e61145cb4f551614ad5e14c45278d32e3db4d32746073089047855c5a9beee785ffefb1f31

TRADITIONAL BLUE
http://jqueryui.com/themeroller/#!zThemeParams=5d00000100ee05000000000000003d8888d844329a8dfe02723de3e5701fa198449035fc0613ff729a37dd818cf92b1f6938fefa90282d04ae436bb72367f5909357c629e832248af2c086db4ab730aa4cced933a88449eca61786b7f3b23d47f58a712d809b6088edfb34a806fff004ac151a471a6afdc3cef51c6e65c22e2e1f3fa549c051bc6b868a0f6c70154c5c120f17aa0bfd291da1e30e89ee2688c3a20538ac38c5e779cce1414cee648e1bf08a1205e38027047133283e33f1e6a0a95aab303d90b51e170fd3fb4b52244d1765a0d365457e8e938be40b2a9fdf076c0c0508de1385f84494a429344a7973a89b35b4452cde4dff4256d124f2556045bb0067d73b9449fb7ab071f0e0754818280c8e6860154eae4d1dcde3fd93136903acf21c27f5e6c2a2efd1adb32f1ab545728cb36473097239700bfbbda38ea8248841c15ab20a70ddc15ddb1de64858a6ee001d6d788994242cab1ab283840cdc8529bd0a22086b0464a12246c5b4b18625b526e205e5efe0c5bb71629a10a1b43b412795f544c7ec81e315691e458b838efd84b05d2a9109c204263dbf46774ee1d7038f58b2052851c89d32c3d2eeda2875a37743d82388d6bced9cc8924b9908dc8e1a5116f2548539fee2bfd36233c8cb437d5bac8cd2adc1a0c73de70a8cffc5f763d2

 */

function styleButtons(parent) {
	if (parent) {
		parent.find(".button").button();
	}
	else {
		$(".button").button();
	}
}

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
			textfield.addClass("ui-widget ui-widget-content ui-input-readonly");
		}
		else {
			textfield.addClass("ui-widget ui-widget-content ui-state-default ui-corner-all ui-input")
			textfield.hover(function(){
				$(this).addClass("ui-input-hover");
			},function(){
				$(this).removeClass("ui-input-hover");
			});
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
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
			textfield.addClass("ui-widget ui-widget-content ui-input-readonly");
		}
		else {
			textfield.addClass("ui-widget ui-widget-content ui-state-default ui-corner-all ui-input")
			textfield.hover(function(){
				$(this).addClass("ui-input-hover");
			},function(){
				$(this).removeClass("ui-input-hover");
			});
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
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
			input.addClass("ui-state-default ui-input");
			input.hover(function(){$(this).addClass("ui-state-hover ui-input-hover");},
					function(){$(this).removeClass("ui-state-hover ui-input-hover");
			});
			
			input.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				}
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
			textfield.addClass("ui-input-readonly");
		}
		else {
			textfield.addClass("ui-text");
			textfield.hover(function(){
				$(this).addClass("ui-input-hover");
			},function(){
				$(this).removeClass("ui-input-hover");
			});
			
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				}
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
			textfield.addClass("ui-input-readonly");
		}
		else {
			textfield.addClass("ui-input");
			textfield.hover(function(){
				$(this).addClass("ui-input-hover");
			},function(){
				$(this).removeClass("ui-input-hover");
			});
			textfield.bind({
				focusin: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				},
				focusout: function() {
					$(this).toggleClass('ui-state-focus ui-input-focus');
				}
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


function style(parent) {
	styleButtons(parent);
	styleComboBoxes(parent);
	styleTextFields(parent);
	styleTextAreas(parent);
	styleYearSpinners(parent);
	styleMonthSpinners(parent);
}


function initUI(parent) {
	style(parent);
};

