
/**
 * Module for dealing with the Navigation Timings API (https://developer.mozilla.org/en-US/docs/Web/API/Navigation_timing_API).
 * Collects the Navigation Timings, adds the Speedindex (if available) and hands it over to the Action module.
 */
inspectIT.navtimings = (function () {
	var rum_speedindex = null;
	var navTimingBlock = -1;
	
	/**
	 * Starts an Action child for getting the Navigation Timings.
	 */
	function collectNavigationTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			navTimingBlock = inspectIT.action.enterChild();
		}
	}
	
	/**
	 * Collects the Navigation Timings, adds the Speedindex (if available)
	 * and ends the Action child after handing the data over to the Action Module.
	 */
	function sendTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			// try to get speedindex
			if (typeof RUMSpeedIndex !== "undefined") {
				rum_speedindex = RUMSpeedIndex();
			}
			
			// get nav timings
			var objCallback = {
					type : "PageLoadRequest"
			}
			for (var key in window.performance.timing) {
				// this is really sad but otherwise toJSON doesn't work in all browsers
				objCallback[String(key) + "W"] = window.performance.timing[key];
			}
			objCallback["url"] = document.location.href;
			
			// add rum speedindex if possible
			if (rum_speedindex !== null && rum_speedindex["speedindex"] >= 0 && rum_speedindex["firstpaint"] >= 0) {
				objCallback["speedindex"] = rum_speedindex["si"];
				objCallback["firstpaint"] = rum_speedindex["fp"];
			}
		
			inspectIT.action.submitData(navTimingBlock, objCallback);
			inspectIT.action.leaveChild(navTimingBlock);
		}
	}
	
	inspectIT.plugins.navTimings = {
		init : collectNavigationTimings,
		onload : sendTimings
	};
})();