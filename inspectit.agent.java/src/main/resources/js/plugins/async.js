
/**
 * Async module for instrumenting asynchronous Javascript functions like setTimeout.
 * That allows us to be more precise with assigning requests to an user action.
 */
(function () {

	if(typeof window.inspectIT.plugins.async === "undefined") {
		var originalSetTimeout = window.setTimeout;
		var originalClearTimeout = window.clearTimeout;
		
		/**
		 * Instruments the setTimeout and clearTimeout function to be supported by the Action Bundling.
		 */
		function instrumentTimers() {
			var timerChildMap = {};
			
			window.setTimeout = function(f) {
				var enterAsync = inspectIT.action.enterChild();
				
				newFunction = function() {
					var innerEnter = inspectIT.action.enterChild(enterAsync);
					f.apply(this, arguments);
					inspectIT.action.leaveChild(innerEnter);
					inspectIT.action.leaveChild(enterAsync);
				}
				
				var args = Array.prototype.slice.call(arguments);
				args[0] = newFunction;
				
				var retVal = originalSetTimeout.apply(this, args);
				timerChildMap[retVal] = enterAsync;
				return retVal;
			}
			
			window.clearTimeout = function(id) {
				inspectIT.action.leaveChild(timerChildMap[id]);
				originalClearTimeout.apply(this, arguments);
			}
		}
		
		// adds the plugin
		window.inspectIT.plugins.asnyc = {
			init : instrumentTimers
		};
	}
})();