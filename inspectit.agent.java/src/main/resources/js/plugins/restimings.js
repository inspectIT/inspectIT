
/**
 * Module for dealing with the Resoure Timings API.
 * 
 */
window.inspectIT.registerPlugin("resTimings", (function() {

	var inspectIT = window.inspectIT;
	
	var resTimingsSupported = ("performance" in window) && ("timing" in window.performance) && ("getEntriesByType" in window.performance) && typeof (window.performance.clearResourceTimings == "function");
	
	/**
	 * Starts the child action for gathering the loaded Resources.
	 */
	function collectResourceTimings() {
		if (resTimingsSupported &&  (window.performance.getEntriesByType("resource") instanceof Array)) {

			var navStart =  window.performance.timing.navigationStart;
			
			inspectIT.pageLoadRequest.require("resTimings");
			
			inspectIT.instrumentation.runWithout(function(){
				var onLoadCallback = inspectIT.instrumentation.disableFor(function(){
					//setTimeout is necessary as the load event also impacts the navigation and resource timings
					setInterval(resourcesPolling, 500);
				});
				window.addEventListener("load", onLoadCallback);
				
			});
		}
	}
	

	function resourcesPolling() {
		inspectIT.instrumentation.runWithout(function(){
			var navStart =  window.performance.timing.navigationStart;
			var loadEnd =  window.performance.timing.loadEventEnd;

			if(!inspectIT.pageLoadRequest.resourceCount) {
				inspectIT.pageLoadRequest.resourceCount = 0;
			}

			var resourceList = window.performance.getEntriesByType("resource");
			
			for (var i = 0; i < resourceList.length; i++) {
				if(resourceList[i].responseEnd !== 0 && resourceList[i].initiatorType != "xmlhttprequest") {
					
					var resourceRequest = inspectIT.createEUMElement("resourceLoadRequest")
					resourceRequest.require("resTimings");
					resourceRequest.markRelevant();
					
					var startTime = navStart+resourceList[i].startTime;
					
					if(startTime <= loadEnd) {
						inspectIT.pageLoadRequest.resourceCount++;
						resourceRequest.setParent(inspectIT.pageLoadAction);						
					}
					
					resourceRequest.url = resourceList[i].name;
					resourceRequest.setEnterTimestamp(startTime);
					resourceRequest.setExitTimestamp(navStart+resourceList[i].responseEnd);
					resourceRequest.initiatorType = resourceList[i].initiatorType;
					resourceRequest.transferSize = resourceList[i].decodedBodySize;
					resourceRequest.baseUrl = window.location.href;
					
					resourceRequest.markComplete("resTimings");
				}							
			}
			window.performance.clearResourceTimings();
			inspectIT.pageLoadRequest.markComplete("resTimings");
		});
	}
	
	return {
		init : collectResourceTimings
	}
})());