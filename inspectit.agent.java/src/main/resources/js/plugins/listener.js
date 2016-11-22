/**
 * Instruments all listeners attached to DOM events.
 */

window.inspectIT.registerPlugin("listenerInstrumentation", (function() {

	var inspectIT = window.inspectIT;

	function initInstrumentation() {
		function domInstrumentation(executeOriginalListener, originalCallback, event) {
			if (isDomElement(event.target)) {

				var listenerRecord = inspectIT.createEUMElement("domListenerExecution");
				listenerRecord.require("listenerData");

				var funcName = inspectIT.util.getFunctionName(originalCallback);
				if (funcName != "") {
					listenerRecord.functionName = funcName;
				}
				listenerRecord.eventType = event.type;

				listenerRecord.elementType = event.target.nodeName;
				if ("id" in event.target) {
					listenerRecord.elementID = event.target.id;
				}

				// execute the listener while building the trace
				listenerRecord.buildTrace(true, executeOriginalListener);

				listenerRecord.markComplete("listenerData");
			} else {
				// not a dom element
				executeOriginalListener();
			}
		}

		inspectIT.instrumentation.instrumentEventListener(domInstrumentation);

	}
	/**
	 * Taken from http://stackoverflow.com/questions/384286/javascript-isdom-how-do-you-check-if-a-javascript-object-is-a-dom-object.
	 */
	function isDomElement(o) {
		return (typeof HTMLElement === "object" ? o instanceof HTMLElement : // DOM2
		o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName === "string");
	}

	return {
		init : initInstrumentation
	}

})());