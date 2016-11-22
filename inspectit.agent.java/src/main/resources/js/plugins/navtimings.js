
/**
 * Module for dealing with the Navigation Timings API (https://developer.mozilla.org/en-US/docs/Web/API/Navigation_timing_API).
 * 
 * The navigation timings are added to the pageLoadRequest object.
 */
window.inspectIT.registerPlugin("navTimings", (function() {
	
	
	var inspectIT = window.inspectIT;
	
	var navTimingsSupported = ("performance" in window) && ("timing" in window.performance);
	
	/**
	 * Collects the navigation timings and stores them in the pageloadrequest.
	 */
	function collectNavigationTimings() {
		if (navTimingsSupported) {
			inspectIT.instrumentation.runWithout(function(){

				//force the beacon service to wait until we have collected the data
				inspectIT.pageLoadRequest.require("navigationTimings");
				
				
				var onLoadCallback = inspectIT.instrumentation.disableFor(function(){

					//setTimeout is necessary as the load event also impacts the navvigation and resource timings
					setTimeout(inspectIT.instrumentation.disableFor(function(){
						
						inspectIT.pageLoadRequest.navigationTimings = inspectIT.pageLoadRequest.navigationTimings || {};
						
						inspectIT.pageLoadRequest.setEnterTimestamp(window.performance.timing.navigationStart);
						inspectIT.pageLoadRequest.setExitTimestamp(window.performance.timing.loadEventEnd);
						for (var key in window.performance.timing) {
							// this is really sad but otherwise toJSON doesn't work in all browsers
							inspectIT.pageLoadRequest.navigationTimings[String(key) + "W"] = window.performance.timing[key];
						}	
						
						inspectIT.pageLoadRequest.markComplete("navigationTimings");
					}), 100);
				});
				
				window.addEventListener("load", onLoadCallback);
				
			});
		}
	}

	//init call returned
	return {
		init: collectNavigationTimings
	}
})());