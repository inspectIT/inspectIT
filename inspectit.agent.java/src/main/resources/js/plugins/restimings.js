/**
 * Module for dealing with the Resoure Timings API.
 * 
 */
window.inspectIT.registerPlugin("resTimings", (function() {

	var inspectIT = window.inspectIT;

	var resTimingsSupported = ("performance" in window) && ("timing" in window.performance) && ("getEntriesByType" in window.performance)
			&& typeof (window.performance.clearResourceTimings == "function")
			&& (window.performance.getEntriesByType("resource") instanceof Array);

	/**
	 * Starts the child action for gathering the loaded Resources.
	 */
	function collectResourceTimings() {
		if (resTimingsSupported) {

			var navStart = window.performance.timing.navigationStart;

			inspectIT.pageLoadRequest.require("resTimings");

			inspectIT.instrumentation.runWithout(function() {
				var onLoadCallback = inspectIT.instrumentation.disableFor(function() {
					// setTimeout is necessary as the load event also impacts the navigation and resource timings
					setInterval(resourcesPolling, 500);
				});
				window.addEventListener("load", onLoadCallback);

			});
		}
	}

	function resourcesPolling() {
		inspectIT.instrumentation.runWithout(function() {
			var navStart = window.performance.timing.navigationStart;
			var loadEnd = window.performance.timing.loadEventEnd;

			if (!inspectIT.pageLoadRequest.resourceCount) {
				inspectIT.pageLoadRequest.resourceCount = 0;
			}

			var resourceList = window.performance.getEntriesByType("resource");

			for (var i = 0; i < resourceList.length; i++) {
				var resource = resourceList[i];
				if (resource.initiatorType != "xmlhttprequest") {

					var resourceRequest = inspectIT.createEUMElement("resourceLoadRequest")
					resourceRequest.require("resTimings");
					resourceRequest.markRelevant();

					//Resource timings API provides timings relative to the navigation start
					var startTime = navStart + resource.startTime;

					if (startTime <= loadEnd) {
						inspectIT.pageLoadRequest.resourceCount++;
						resourceRequest.setParent(inspectIT.pageLoadRequest);
					}

					resourceRequest.url = resource.name;
					resourceRequest.setEnterTimestamp(startTime);
					resourceRequest.setDuration(resource.responseEnd);
					resourceRequest.initiatorType = resource.initiatorType;
					resourceRequest.transferSize = resource.decodedBodySize;
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