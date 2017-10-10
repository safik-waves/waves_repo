define([
	'jquery',
	'underscore',
  'backbone',
  'bootstrap',
//	'metis_jq',
//	'morris',
////	'raphael',
//	'datatables_jq',
//	'datatables_bs',
  'text!../../../../html/dashboard.html'
], function($, _, Backbone, dashboard){

  var DASHBOARD = Backbone.View.extend({
    el: $("#page"),

    render: function(){
       this.$el.html(dashboard);
    }
 });

  return DASHBOARD 	;
  
});