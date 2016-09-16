
/**
 * Plugin for instrumenting Ajax requests.
 * Initializes a inspectIT plugin which instruments Ajax calls.
 */
inspectIT.ajax = (function () {
	var settings = window.inspectIT_settings;
	
	/**
	 * Function which performs the Ajax instrumentation with overriding the XMLHttpRequest functions.
	 */
	function instrumentAjax() {
		// instrument xmlhttprequest
		XMLHttpRequest.prototype.uninstrumentedOpen = XMLHttpRequest.prototype.open;
		XMLHttpRequest.prototype.uninstrumentedSend = XMLHttpRequest.prototype.send;
		XMLHttpRequest.prototype.open = function(method, url, async, user, pw) {
			//only instrument if it wasnt a request to our management server
			if (url != settings["eumManagementServer"]) {
				//all methods in here will have access to this object (principle of closures)
				//a record is created for every open-call (should only happen once per XMLHTTPRequest)
				var ajaxRecord = {
					type : "AjaxRequest",
					method : method,
					url : url,
					async : async,
					status : 200,
					startTime : inspectIT.util.timestamp()
				};
				
				// action capturing
				var currAjaxAction = inspectIT.action.enterChild();
				var currAjaxCallbackAction = null;
				
				//the "this" keyword will point to the actual XMLHTTPRequest object, so we overwrite the send method
				//of just this object (makes sure we use our specific ajaxRecord object)
				this.send = function(arg) {
					ajaxRecord.beginTime = inspectIT.util.timestamp();
					if (arg != undefined) {
						ajaxRecord.requestContent = arg.toString();
					} else {
						ajaxRecord.requestContent = null;
					}
					//apply is a more safe way of calling the actual method, making sure that all
					//arguments passed to this method will be passed to the real method
					return XMLHttpRequest.prototype.uninstrumentedSend.apply(this, arguments);
				};
				
				// this gives us the time between sending the request
				// and getting back the response (better than below)
				// -> works in all modern browsers
				this.addEventListener("progress", function(oEvent) {
					 if (!ajaxRecord.sent && oEvent.lengthComputable) {
						 var percentComplete = oEvent.loaded / oEvent.total;
						 if (percentComplete >= 1) { // -> we're finished
							 ajaxRecord.status = this.status;
							 ajaxRecord.baseUrl = window.location.href;
							 ajaxRecord.endTime = inspectIT.util.timestamp();
							 inspectIT.action.submitData(currAjaxAction, ajaxRecord);
							 ajaxRecord.sent = true;
							 currAjaxCallbackAction = inspectIT.action.enterChild(currAjaxAction);
						 }
					 }
				});
				
				// this gives us the time between send and finish of all
				// javascript tasks executed after the request
				// -> fallback solution if length not computable
				this.addEventListener("loadend", function() {
					if (!ajaxRecord.sent) {
						ajaxRecord.status = this.status;
						ajaxRecord.endTime = inspectIT.util.timestamp();
						inspectIT.action.submitData(currAjaxAction, ajaxRecord);
						ajaxRecord.sent = true;
					}
					
					// ajax finished
					inspectIT.action.leaveChild(currAjaxAction);
					if (currAjaxCallbackAction != null) {
						inspectIT.action.leaveChild(currAjaxCallbackAction);
					}
				});
			}
			return XMLHttpRequest.prototype.uninstrumentedOpen.apply(this, arguments);
		}
	}
	
	// APPLYING THE PLUGIN
	inspectIT.plugins.ajax = {
		init : instrumentAjax
	};
})();