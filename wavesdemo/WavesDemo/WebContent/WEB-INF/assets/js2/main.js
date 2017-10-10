// Author: Thomas Davis <thomasalwyndavis@gmail.com>
// Filename: main.js

// Require.js allows us to configure shortcut alias
// Their usage will become more apparent futher along in the tutorial.
require.config({
	shim : {
		"bootstrap" : {
			"deps" : [ 'jquery' ]
		}
	},

	paths : {
		jquery : 'libs/jquery.min',
		
		backbone : 'libs/backbone-min',
		
		underscore : 'libs/underscore-min',
		bootstrap : 'libs/bootstrap.min',
//		metis_jq : 'libs/jquery.metisMenu',
//		morris : 'libs/morris/morris',
//		// raphael : 'libs/morris/raphael-2.1.0.min',
//		datatables_jq : 'libs/dataTables/jquery.dataTables',
//		datatables_bs : 'libs/dataTables/dataTables.bootstrap',
		
		templates : '../../../html'
	}

});

require([
// Load our app module and pass it to our definition function
'app',

], function(App) {
	// The "app" dependency is passed in as "App"
	// Again, the other dependencies passed in are not "AMD" therefore don't
	// pass a parameter to this function
	App.initialize();
});