
$(document).ready(function() {
   $(document).on('$macro.to','#$interface.on',function() {
	$.getScript( "$macro.path" )
  	.done(function( script, textStatus ) {
    		console.log( textStatus );
  	})
  	.fail(function( jqxhr, settings, exception ) {
    		$( "div.log" ).text( "Triggered ajaxError handler." );
	});
    });	
});
