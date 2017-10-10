require.config({
		baseUrl :"WEB-INF/assets/libs",
		shim : {
        'jquery' : {
            exports : '$'
        },
        'bootstrap' : {
            deps : [ 'jquery' ],
            exports : 'bootstrap'
        },
        'datatables' : [ 'jquery' ],
        'datatables_bootstrap' : [ 'datatables' ],
    },
		paths : {
		 	//'jquery' : 'http://code.jquery.com/jquery-1.11.1.min',
        		//'bootstrap' : 'http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min',
        		//'datatables' : 'http://cdn.datatables.net/1.10.13/js/jquery.dataTables.min',
			jquery : 'jquery.min',
			bootstrap : 'bootstrap.min',
			backbone : 'backbone-min',
			underscore : 'underscore-min',
			datatables : 'jquery.dataTables',
			'datatables_bootstrap' : 'dataTables.bootstrap',
			//'datatables_bootstrap' :'http://cdn.datatables.net/1.10.13/js/dataTables.bootstrap.min'
		}
	});

require([ 'jquery','bootstrap','underscore','backbone','datatables'], function($,bootstrap,_, Backbone,datatables) {
	var AppRouter = Backbone.Router.extend({
		routes : {
			'*actions' : 'defaultAction'
		}
	});

	var app_router = new AppRouter;
	app_router.on('route:defaultAction',function(actions) {
		App._init();
	});

	App.router = app_router;
	Backbone.history.start();
});
