
 get_user_info : function(obj){
	
	var userid=$('#userid').val();
	var roles=$('#roles').val();
	
	var myObject = new Object();
	myObject.userid = userid;
	myObject.roles = roles;
	
	return myObject;
	
}