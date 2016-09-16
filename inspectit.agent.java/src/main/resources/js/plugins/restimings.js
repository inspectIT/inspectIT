
/**
 * Module for dealing with the Resoure Timings API.
 * Collects the loaded Resources and hands the data over the Action module.
 */
inspectIT.restimings = (function () {
	
	var resourceTimingsBlock = -1;
	
	/**
	 * Starts the child action for gathering the loaded Resources.
	 */
	function collectResourceTimings() {
		if (("performance" in window) && ("getEntriesByType" in window.performance) && (window.performance.getEntriesByType("resource") instanceof Array)) {
			resourceTimingsBlock = inspectIT.action.enterChild();
			//increase the buffer size to make sure everythin is captured
			window.performance.setResourceTimingBufferSize(500);
		}
	}
	
	/**
	 * Collects the loaded Resources, ends the Child action
	 * and hands over the data to the action module.
	 */
	function sendAndClearTimings() {
		//add event listener, which is called after the site has fully finished loading
		if (("performance" in window) && ("getEntriesByType" in window.performance) && (window.performance.getEntriesByType("resource") instanceof Array)) {
			var timingsList = [];
			var resourceList = window.performance.getEntriesByType("resource");
			for ( i = 0; i < resourceList.length; i++) {
				timingsList.push({
					url : resourceList[i].name,
					startTime : Math.round(resourceList[i].startTime),
					endTime : Math.round(resourceList[i].responseEnd),
					initiatorType : resourceList[i].initiatorType,
					transferSize : resourceList[i].decodedBodySize,
					initiatorUrl : window.location.href
				});
			}
			if (timingsList.length > 0) {
				for (var i = 0; i < timingsList.length; i++) {
					timingsList[i].type = "ResourceLoadRequest";
					inspectIT.action.submitData(resourceTimingsBlock, timingsList[i]);
				}
			}
			//clear the timings to make space for new ones
			window.performance.clearResourceTimings();
			
			inspectIT.action.leaveChild(resourceTimingsBlock);
		}
	}
	
	inspectIT.plugins.resTimings = {
		init : collectResourceTimings,
		onload : sendAndClearTimings
	};
})();