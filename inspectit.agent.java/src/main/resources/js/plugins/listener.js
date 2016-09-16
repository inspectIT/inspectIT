
/**
 * Instruments the addEventListener and removeEventListener functions for DOM nodes.
 * This helps us to keep track of user actions.
 */
(function () {

	if(typeof window.inspectIT.plugins.listener === "undefined") {
		/**
		 * The events which should get instrumented.
		 */
		var instrumentedEvents = {
			"click" : true,
			"onchange" : true,
			// "scroll" : true,
			//"onmouseover" : true,
			//"onmouseout" : true,
			"onkeydown" : true,
			//"onkeyup" : true,
			//"onkeypress" : true
		}
		
		// for removing events
		var activeEvents = {};
		var currId = 0;
		
		/**
		 * Instruments global listeners like document and window.
		 */
		function instrumentDocumentListener() {
			var docListeners = [
			    [document, false],
			    [window, false]
			];
			
			for (var i = 0; i < docListeners.length; i++) {
				instrumentAddListener(docListeners[i][0], docListeners[i][1]);
				instrumentRemoveListener(docListeners[i][0], docListeners[i][1]);
			}
		}
		
		/**
		 * Instruments event listeners for common DOM elements.
		 */
		function instrumentListener() {
			// FOR COMMON ELEMENTS
			var commonListeners = null;
			if (typeof EventTarget !== "undefined") {
				commonListeners = [
				    [Element, true],
				    [EventTarget, true]
				];
			} else {
				// for IE eventtarget is undefined
				commonListeners = [
				    [Element, true]
				];
			}
			
			for (var i = 0; i < commonListeners.length; i++) {
				instrumentAddListener(commonListeners[i][0], commonListeners[i][1]);
				instrumentRemoveListener(commonListeners[i][0], commonListeners[i][1]);
			}
		}
		
		/**
		 * Performs the instrumentation of the addEventListener function.
		 * @param base The node which should get instrumented
		 * @param prot whether the prototype should get instrumented or not
		 */
		function instrumentAddListener(base, prot) {
			if (typeof base === "undefined") return;
			if (prot && typeof base.prototype === "undefined") return;
			if ((prot && typeof base.prototype.addEventListener === "undefined") || (!prot && typeof base.addEventListener === "undefined")) return;
			
			if (prot) {
				var addEvListener = base.prototype.addEventListener;
				base.prototype.addEventListener = function(event, callback, bubble) {
					addListenerInstrumentation.call(this, addEvListener, event, callback, bubble);
				}
			} else {
				var addEvListener = base.addEventListener;
				base.addEventListener = function(event, callback, bubble) {
					addListenerInstrumentation.call(this, addEvListener, event, callback, bubble);
				}
			}
		}
		
		/**
		 * Performs the instrumentation of the removeEventListener function.
		 * @param base The node which should get instrumented
		 * @param prot whether the prototype should get instrumented or not
		 */
		function instrumentRemoveListener(base, prot) {
			if (typeof base === "undefined") return;
			if (prot && typeof base.prototype === "undefined") return;
			if ((prot && typeof base.prototype.removeEventListener === "undefined") || (!prot && typeof base.removeEventListener === "undefined")) return;
			
			if (prot) {
				var remEvListener = base.prototype.removeEventListener;
				base.prototype.removeEventListener = function(event, callback, opt) {
					removeListenerInstrumentation.call(this, remEvListener, event, callback, opt);
				}
			} else {
				var remEvListener = base.removeEventListener;
				base.removeEventListener = function(event, callback, opt) {
					removeListenerInstrumentation.call(this, remEvListener, event, callback, opt);
				}
			}
		}
		
		/**
		 * Function which gets called instead of the original removeEventListener function.
		 * @param realMethod the original method - which gets called after executing our own code
		 * @param event the name of the event
		 * @param callback the function which should get called when the event is raised
		 * @param opt optional parameters for the realMethod
		 */
		function removeListenerInstrumentation(realMethod, event, callback, opt) {
			if (event in instrumentedEvents && typeof callback.___id !== "undefined") {
				realMethod.call(this, event, activeEvents[callback.___id], opt);
				delete activeEvents[callback.___id];
			}
		}
		
		/**
		 * Function which gets called instead of the original addEventListener function.
		 * @param realMethod the original method
		 * @param event the name of the event
		 * @param callback the function which gets called when the event is raised
		 * @param bubble A Boolean value that specifies whether the event should be executed in the capturing or in the bubbling phase
		 */
		function addListenerInstrumentation(realMethod, event, callback, bubble) {
			var dataObj = {
				tagName : (typeof this.tagName !== "undefined" ? this.tagName : ""),
				elementId : (typeof this.id !== "undefined" ? this.id : ""),
				elementName : (typeof this.name !== "undefined" ? this.name : ""),
				methodName : inspectIT.util.getFuncName(callback),
				eventName : event,
				type : "clickAction"
			}
			
			if (event in instrumentedEvents) {
				// assign an id to the callback so we can access the instrumented function by the old func
				callback.___id = ++currId;
				activeEvents[currId] = function(e) {
					var currAction = inspectIT.action.enterAction("click");
					dataObj.beginTime = inspectIT.util.timestamp();
					
					callback.call(this, e);
					
					dataObj.endTime = inspectIT.util.timestamp();
					inspectIT.action.submitData(currAction, dataObj);
					inspectIT.action.leaveAction(currAction);
				}
				
				realMethod.call(this, event, activeEvents[currId], bubble);
			} else {
				// we dont want to instrument
				realMethod.call(this, event, callback, bubble);
			}
		}
		
		window.inspectIT.plugins.listener = {
			init : instrumentListener,
			domready : instrumentDocumentListener
		}
	}
})();