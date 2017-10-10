 initialize_page : function(data){
	
	
	$('#total_service').html('1111');
	$('#total_running1').html('2222');
	$('#total_running').html('3333');
	$('#total_stopping').html('4444');
	
	alert('init page');
	var json = {};
	json.domain='all';
	json.type='domain';
	json.action='details';
	return json;
	
}
