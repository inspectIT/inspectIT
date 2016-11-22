
/**
 * Async module for instrumenting asynchronous Javascript functions setTimeout and setInterval.
 * 
 */
window.inspectIT.registerPlugin("asyncInstrumentation", (function() {
	
	var inspectIT = window.inspectIT;
	
	
	/**
	 * Instruments the setTimeout and clearTimeout function to be supported by the Action Bundling.
	 */
	function instrumentTimers() {
		
		var originalSetTimeout = window.setTimeout;
		window.setTimeout = function(callback, duration) {
			if(inspectIT.instrumentation.isEnabled()) {
				
				var setTimeoutTimestamp = inspectIT.util.timestampMS();
				var parentElement = inspectIT.traceBuilder.getCurrentParent();
				var funcName = inspectIT.util.getFunctionName(callback);
				
				var instrumentedCallback = function() {
					var timerLog = inspectIT.createEUMElement("timerExecution");
					timerLog.require("timerData");
					timerLog.setParent(parentElement);
					
					timerLog.initiatorCallTimestamp = setTimeoutTimestamp;
					timerLog.iterationNumber = 0;
					timerLog.functionName = funcName;
					timerLog.configuredTimeout = duration || 0;
					
					var originalThis = this;
					var originalArgs = arguments;
					
					timerLog.buildTrace(true, function() {
						callback.apply(originalThis,originalArgs);
					})
					
					timerLog.markComplete("timerData");
				}

				var modifiedArgs = Array.prototype.slice.call(arguments);
				modifiedArgs[0] = instrumentedCallback;
				
				return originalSetTimeout.apply(this,modifiedArgs);
			} else {
				return originalSetTimeout.apply(this,arguments);
			}		
			
		}
		
		var originalSetInterval = window.setInterval;
		window.setInterval = function(callback, duration) {
			if(inspectIT.instrumentation.isEnabled()) {
				
				var setIntervalTimestamp = inspectIT.util.timestampMS();
				var parentElement = inspectIT.traceBuilder.getCurrentParent();
				var funcName = inspectIT.util.getFunctionName(callback);
				
				var iterationCounter = 0;

				var instrumentedCallback = function() {
					var timerLog = inspectIT.createEUMElement("timerExecution");
					timerLog.require("timerData");
					timerLog.setParent(parentElement);
					
					timerLog.initiatorCallTimestamp = setIntervalTimestamp;
					timerLog.functionName = funcName;
					timerLog.configuredTimeout = duration || 0;
					iterationCounter++;
					timerLog.iterationNumber = iterationCounter;
					
					var originalThis = this;
					var originalArgs = arguments;
					
					timerLog.buildTrace(true, function() {
						callback.apply(originalThis,originalArgs);
					})
					
					timerLog.markComplete("timerData");
				}

				var modifiedArgs = Array.prototype.slice.call(arguments);
				modifiedArgs[0] = instrumentedCallback;
				
				return originalSetInterval.apply(this,modifiedArgs);
			} else {
				return originalSetInterval.apply(this,arguments);
			}		
			
		}
		
	}
		
	return {
		init: instrumentTimers
	}
})());