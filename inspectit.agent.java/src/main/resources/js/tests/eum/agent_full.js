// STARTUP MODULE
window.inspectIT_settings = {
	eumManagementServer : ""	
};
var inspectIT = (function() {
	
	function init() {
		if (window.inspectIT_isRunning) {
			console.log("Injected script is already running, terminating new instance.");
			return;
		} else {
			window.inspectIT_isRunning = true;
		}
		
		// check if id exists and set if not
		inspectIT.cookies.checkCookieId();
		for (plugin in inspectIT.plugins) {
			inspectIT.plugins[plugin].init();
		}
		
		window.addEventListener("load", function() {
			for (plugin in inspectIT.plugins) {
				if (typeof inspectIT.plugins[plugin].onload !== "undefined") {
					inspectIT.plugins[plugin].onload();
				}
			}
		});
		
		document.addEventListener("DOMContentLoaded", function() {
			for (plugin in inspectIT.plugins) {
				if (typeof inspectIT.plugins[plugin].domready !== "undefined") {
					inspectIT.plugins[plugin].domready();
				}
			}
		});
	}
	
	return {
		start : init,
		plugins : {}
	};
})();

//UTILITY MODULE
inspectIT.util = (function () {
	var settings = window.inspectIT_settings;
	
	function getCurrentTimeStamp() {
		if (window.performance) {
			if (performance.timing.navigationStart != 0) {
				return Math.round(performance.now() + performance.timing.navigationStart);
			}
		}
		return Date.now();
	}
	
	function getOS() { // gets the operation system, null if we can't recognize it
		var os = null;
		var userAgent = navigator.userAgent;
		// mobile detection
		if(userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
			return "iOS";
		} else if(userAgent.match(/Android/i)) {
			return "Android";
		}
		// desktop detection
		if (navigator.appVersion.indexOf("Win") > -1) os="Windows";
		else if (navigator.appVersion.indexOf("Mac") > -1) os="Mac";
		else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) os="Linux";
		return os;
	}
	
	function getBrowserInformation() {
		// gets information about the browser of the user
		// feature detection
		var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
		var isFirefox = typeof InstallTrigger !== 'undefined';
		var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
		var isIE = /*@cc_on!@*/false || !!document.documentMode;
		var isEdge = !isIE && !!window.StyleMedia;
		var isChrome = Boolean(window.chrome);
		
		// get language
		var userLanguage = navigator.language || navigator.userLanguage;
		
		var retObj = {
			lang : userLanguage,
			os : getOS()
		};
		if (isOpera) {
			retObj.name = "Opera";
		} else if (isFirefox) {
			retObj.name = "Firefox";
		} else if (isSafari) {
			retObj.name = "Safari";
		} else if (isIE) {
			retObj.name = "Internet Explorer";
		} else if (isEdge) {
			retObj.name = "Edge";
		} else if (isChrome) {
			retObj.name = "Google Chrome";
		}
		return retObj;
	}
	
	function sendToEUMServer(dataObject, forceSynchronous) {
	}
	
	function getFunctionName(func) {
		if (!(typeof func === "function")) return null;
		if (func.hasOwnProperty("name")) return func.name; // ES6
		return "";
	}
	
	return {
		timestamp : getCurrentTimeStamp,
		os : getOS,
		browserinfo : getBrowserInformation,
		callback : sendToEUMServer,
		getFuncName : getFunctionName
	}
})();

//COOKIE MODULE
inspectIT.cookies = (function () {
	function hasCookie(name) {
		return getCookie(name) !== null;
	}
	
	function getCookie(key) {
		var name = key + "=";
	    var ca = document.cookie.split(';');
	    for(var i = 0; i <ca.length; i++) {
	        var c = ca[i];
	        while (c.charAt(0)==' ') {
	            c = c.substring(1);
	        }
	        if (c.indexOf(name) == 0) {
	            return c.substring(name.length,c.length);
	        }
	    }
	    return null;
	}
	
	function checkCookieId() {
		if (!inspectIT.action.hasActions()) {
			// NEW PAGELOADACTION
			var pageLoadAction = inspectIT.action.enterAction("pageLoad");
			window.addEventListener("load", function() {
				inspectIT.action.leaveAction(pageLoadAction);
			});
		}
		
		if (hasCookie("inspectIT_cookieId")) {
			var browserData = inspectIT.util.browserinfo();
			var sessionData = {
				type : "userSession",
				device : browserData.os,
				browser : browserData.name,
				language : browserData.lang,
				sessionId : inspectIT.cookies.getCurrentId()
			}
			inspectIT.util.callback(sessionData);
		}
	}
	
	function getCurrentId() {
		return getCookie("inspectIT_cookieId");
	}
	
	return {
		checkCookieId : checkCookieId,
		getCurrentId : getCurrentId,
	}
})();

/**
 * Module which handles the bundling of responses to the EUM server to keep the network overhead low.
 * @return Function for adding beacons which should get sent back to the EUM server
 */
inspectIT.actionBundler = (function () {
	var currentBundle = [];
	var timeoutTask = null;
	var taskFinished = true;
	var lastRequest = null;
	var TIMEWINDOW = 2500;
	var MAXTIMEWINDOW = 15000;
	
	/**
	 * Adds an action and decides whether we immediately send the Beacon or buffer it.
	 * @param User action which is finished and can be sent to the EUM server
	 */
	function addAction(action) {
		action.sessionId = inspectIT.cookies.getCurrentId();
		action.baseUrl = window.location.href;
		
		currentBundle.push(action);
		if (!taskFinished) {
			clearTimeout(timeoutTask);
		}
		
		var currStamp = inspectIT.util.timestamp();
		if (lastRequest != null && currStamp - lastRequest >= MAXTIMEWINDOW) {
			// send immediately
			finishBundle();
			taskFinished = true;
		} else {
			if (lastRequest == null) lastRequest = currStamp;
			timeoutTask = setTimeout(finishBundle, TIMEWINDOW);
			taskFinished = false;
		}
	}
	
	/**
	 * Private functions which handles the transmission of the buffered actions.
	 */
	function finishBundle() {
		inspectIT.util.callback(currentBundle);
		currentBundle = [];
		lastRequest = inspectIT.util.timestamp();
	}
	
	return {
		addAction : addAction
	}
})();

//ACTION MODULE
//Identifying user actions and send if they're complete
inspectIT.action = (function () {
	var actions = [];
	var actionChildIds = [];
	var finishedChilds = [];
	
	var offset = 0;
	
	// For action capturing
	function enterAction(specType) {
		actions.push({
			type : "userAction",
			specialType : specType,
			contents : []
		});
		actionChildIds.push([++offset]);
		finishedChilds.push([]);
		
		return offset;
	}
	
	function leaveAction(enterId) {
		var actionId = getActionFromId(enterId);
		if (actionId >= 0) {
			finishedChilds[actionId].push(enterId);
			actionFinished(actionId); // check if finished
		}
	}
	
	function enterChild(parentId) {
		var currentAction;
		if (typeof parentId === "undefined") {
			currentAction = getActionFromId(offset);
		} else {
			currentAction = getActionFromId(parentId);
		}
		if (currentAction >= 0) {
			actionChildIds[currentAction].push(++offset);
			return offset;
		}
	}
	
	function leaveChild(enterId) {
		var actionId = getActionFromId(enterId);
		if (actionId >= 0) {
			finishedChilds[actionId].push(enterId);
			actionFinished(actionId); // check if finished
		} 
	}
	// END For actipn capturing
	
	// determines wheter the action has finished or not
	function actionFinished(id) {
		if (actionChildIds[id].length == finishedChilds[id].length) {
			//  the action is finished
			finishAction(id);
		}
	}
	
	function hasActions() {
		return actions.length > 0;
	}
	
	function finishAction(id, sync) {
		if (typeof sync === "undefined") sync = false;
		
		inspectIT.actionBundler.addAction(actions[id]);
		forceRemove(id);
		
		if (actions.length == 0) {
			// reset offset because there is no action atm
			offset = 0;
		}
	}
	
	function forceRemove(id) {
		actions.splice(id, 1);
		finishedChilds.splice(id, 1);
		actionChildIds.splice(id, 1);
	}
	
	// submits data to a action
	function submitData(entrId, data) {
		var currentAction = getActionFromId(entrId);
		if (currentAction >= 0) {
			actions[currentAction].contents.push(data);
		} // otherwise we can't assign it to an action
	}
	
	// gets the action id from child id
	function getActionFromId(id) {
		for (var i = 0; i < actionChildIds.length; i++) {
			for (var j = 0; j < actionChildIds[i].length; j++) {
				if (actionChildIds[i][j] == id) return i;
			}
		}
		return -1;
	}
	
	return {
		enterAction : enterAction,
		leaveAction : leaveAction,
		enterChild : enterChild,
		leaveChild : leaveChild,
		submitData : submitData,
		hasActions : hasActions
	}
})();

// AJAX MODULE
inspectIT.ajax = (function () {
	var settings = window.inspectIT_settings;
	
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
	
	// add to plugins
	inspectIT.plugins.ajax = {
		init : instrumentAjax
	};
})();

// INSTRUMENTATION FOR TIMER (setTimeout etc.)
inspectIT.async = (function () {
	
	var originalSetTimeout = window.setTimeout;
	var originalClearTimeout = window.clearTimeout;
	
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
	
	inspectIT.plugins.asnyc = {
		init : instrumentTimers
	};
})();


// LISTENER MODULE
inspectIT.listener = (function () {
	var instrumentedEvents = {
		"click" : true,
		"onchange" : true,
		// "scroll" : true,
		"onmouseover" : true,
		"onmouseout" : true,
		"onkeydown" : true,
		"onkeyup" : true,
		"onkeypress" : true
	}
	
	// for removing events
	var activeEvents = {};
	var currId = 0;
	
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
	
	function removeListenerInstrumentation(realMethod, event, callback, opt) {
		if (event in instrumentedEvents && typeof callback.___id !== "undefined") {
			realMethod.call(this, event, activeEvents[callback.___id], opt);
			delete activeEvents[callback.___id];
		}
	}
	
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
	
	inspectIT.plugins.listener = {
		init : instrumentListener,
		domready : instrumentDocumentListener
	}
})();


// TIMINGS MODULE
inspectIT.timings = (function () {
	var rum_speedindex = null;
	var navTimingBlock = -1;
	
	function collectNavigationTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			navTimingBlock = inspectIT.action.enterChild();
		}
	}
	
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
			if (rum_speedindex !== null && rum_speedindex["speedindex"] !== null && rum_speedindex["firstpaint"] !== null) {
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


// TIMINGS MODULE
inspectIT.timings = (function () {
	
	var resourceTimingsBlock = -1;
	
	function collectResourceTimings() {
		if (("performance" in window) && ("getEntriesByType" in window.performance) && (window.performance.getEntriesByType("resource") instanceof Array)) {
			resourceTimingsBlock = inspectIT.action.enterChild();
			//increase the buffer size to make sure everythin is captured
			window.performance.setResourceTimingBufferSize(500);
		}
	}
	
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


/******************************************************************************
Copyright (c) 2014, Google Inc.
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of the <ORGANIZATION> nor the names of its contributors
    may be used to endorse or promote products derived from this software
    without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
******************************************************************************/

/******************************************************************************
*******************************************************************************
  Calculates the Speed Index for a page by:
  - Collecting a list of visible rectangles for elements that loaded
    external resources (images, background images, fonts)
  - Gets the time when the external resource for those elements loaded
    through Resource Timing
  - Calculates the likely time that the background painted
  - Runs the various paint rectangles through the SpeedIndex calculation:
    https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/metrics/speed-index
  TODO:
  - Improve the start render estimate
  - Handle overlapping rects (though maybe counting the area as multiple paints
    will work out well)
  - Detect elements with Custom fonts and the time that the respective font
    loaded
  - Better error handling for browsers that don't support resource timing
*******************************************************************************
******************************************************************************/

var RUMSpeedIndex = function(win) {
  win = win || window;
  var doc = win.document;
    
  /****************************************************************************
    Support Routines
  ****************************************************************************/
  // Get the rect for the visible portion of the provided DOM element
  var GetElementViewportRect = function(el) {
    var intersect = false;
    if (el.getBoundingClientRect) {
      var elRect = el.getBoundingClientRect();
      intersect = {'top': Math.max(elRect.top, 0),
                       'left': Math.max(elRect.left, 0),
                       'bottom': Math.min(elRect.bottom, (win.innerHeight || doc.documentElement.clientHeight)),
                       'right': Math.min(elRect.right, (win.innerWidth || doc.documentElement.clientWidth))};
      if (intersect.bottom <= intersect.top ||
          intersect.right <= intersect.left) {
        intersect = false;
      } else {
        intersect.area = (intersect.bottom - intersect.top) * (intersect.right - intersect.left);
      }
    }
    return intersect;
  };

  // Check a given element to see if it is visible
  var CheckElement = function(el, url) {
    if (url) {
      var rect = GetElementViewportRect(el);
      if (rect) {
        rects.push({'url': url,
                     'area': rect.area,
                     'rect': rect});
      }
    }
  };

  // Get the visible rectangles for elements that we care about
  var GetRects = function() {
    // Walk all of the elements in the DOM (try to only do this once)
    var elements = doc.getElementsByTagName('*');
    var re = /url\(.*(http.*)\)/ig;
    for (var i = 0; i < elements.length; i++) {
      var el = elements[i];
      var style = win.getComputedStyle(el);

      // check for Images
      if (el.tagName == 'IMG') {
        CheckElement(el, el.src);
      }
      // Check for background images
      if (style['background-image']) {
        re.lastIndex = 0;
        var matches = re.exec(style['background-image']);
        if (matches && matches.length > 1)
          CheckElement(el, matches[1].replace('"', ''));
      }
      // recursively walk any iFrames
      if (el.tagName == 'IFRAME') {
        try {
          var rect = GetElementViewportRect(el);
          if (rect) {
            var tm = RUMSpeedIndex(el.contentWindow);
            if (tm) {
              rects.push({'tm': tm,
                          'area': rect.area,
                          'rect': rect});
            }
        }
        } catch(e) {
        }
      }
    }
  };

  // Get the time at which each external resource loaded
  var GetRectTimings = function() {
    var timings = {};
    var requests = win.performance.getEntriesByType("resource");
    for (var i = 0; i < requests.length; i++)
      timings[requests[i].name] = requests[i].responseEnd;
    for (var j = 0; j < rects.length; j++) {
      if (!('tm' in rects[j]))
        rects[j].tm = timings[rects[j].url] !== undefined ? timings[rects[j].url] : 0;
    }
  };

  // Get the first paint time.
  var GetFirstPaint = function() {
    // If the browser supports a first paint event, just use what the browser reports
    if ('msFirstPaint' in win.performance.timing)
      firstPaint = win.performance.timing.msFirstPaint - navStart;
    if ('chrome' in win && 'loadTimes' in win.chrome) {
      var chromeTimes = win.chrome.loadTimes();
      if ('firstPaintTime' in chromeTimes && chromeTimes.firstPaintTime > 0) {
        var startTime = chromeTimes.startLoadTime;
        if ('requestTime' in chromeTimes)
          startTime = chromeTimes.requestTime;
        if (chromeTimes.firstPaintTime >= startTime)
          firstPaint = (chromeTimes.firstPaintTime - startTime) * 1000.0;
      }
    }
    // For browsers that don't support first-paint or where we get insane values,
    // use the time of the last non-async script or css from the head.
    if (firstPaint === undefined || firstPaint < 0 || firstPaint > 120000) {
      firstPaint = win.performance.timing.responseStart - navStart;
      var headURLs = {};
      var headElements = doc.getElementsByTagName('head')[0].children;
      for (var i = 0; i < headElements.length; i++) {
        var el = headElements[i];
        if (el.tagName == 'SCRIPT' && el.src && !el.async)
          headURLs[el.src] = true;
        if (el.tagName == 'LINK' && el.rel == 'stylesheet' && el.href)
          headURLs[el.href] = true;
      }
      var requests = win.performance.getEntriesByType("resource");
      var doneCritical = false;
      for (var j = 0; j < requests.length; j++) {
        if (!doneCritical &&
            headURLs[requests[j].name] &&
           (requests[j].initiatorType == 'script' || requests[j].initiatorType == 'link')) {
          var requestEnd = requests[j].responseEnd;
          if (firstPaint === undefined || requestEnd > firstPaint)
            firstPaint = requestEnd;
        } else {
          doneCritical = true;
        }
      }
    }
    firstPaint = Math.max(firstPaint, 0);
  };

  // Sort and group all of the paint rects by time and use them to
  // calculate the visual progress
  var CalculateVisualProgress = function() {
    var paints = {'0':0};
    var total = 0;
    for (var i = 0; i < rects.length; i++) {
      var tm = firstPaint;
      if ('tm' in rects[i] && rects[i].tm > firstPaint)
        tm = rects[i].tm;
      if (paints[tm] === undefined)
        paints[tm] = 0;
      paints[tm] += rects[i].area;
      total += rects[i].area;
    }
    // Add a paint area for the page background (count 10% of the pixels not
    // covered by existing paint rects.
    var pixels = Math.max(doc.documentElement.clientWidth, win.innerWidth || 0) *
                 Math.max(doc.documentElement.clientHeight, win.innerHeight || 0);
    if (pixels > 0 ) {
      pixels = Math.max(pixels - total, 0) * pageBackgroundWeight;
      if (paints[firstPaint] === undefined)
        paints[firstPaint] = 0;
      paints[firstPaint] += pixels;
      total += pixels;
    }
    // Calculate the visual progress
    if (total) {
      for (var time in paints) {
        if (paints.hasOwnProperty(time)) {
          progress.push({'tm': time, 'area': paints[time]});
        }
      }
      progress.sort(function(a,b){return a.tm - b.tm;});
      var accumulated = 0;
      for (var j = 0; j < progress.length; j++) {
        accumulated += progress[j].area;
        progress[j].progress = accumulated / total;
      }
    }
  };

  // Given the visual progress information, Calculate the speed index.
  var CalculateSpeedIndex = function() {
    if (progress.length) {
      SpeedIndex = 0;
      var lastTime = 0;
      var lastProgress = 0;
      for (var i = 0; i < progress.length; i++) {
        var elapsed = progress[i].tm - lastTime;
        if (elapsed > 0 && lastProgress < 1)
          SpeedIndex += (1 - lastProgress) * elapsed;
        lastTime = progress[i].tm;
        lastProgress = progress[i].progress;
      }
    } else {
      SpeedIndex = firstPaint;
    }
  };

  /****************************************************************************
    Main flow
  ****************************************************************************/
  var rects = [];
  var progress = [];
  var firstPaint;
  var SpeedIndex;
  var pageBackgroundWeight = 0.1;
  try {
    var navStart = win.performance.timing.navigationStart;
    GetRects();
    GetRectTimings();
    GetFirstPaint();
    CalculateVisualProgress();
    CalculateSpeedIndex();
  } catch(e) {
  }
  /* Debug output for testing
  var dbg = '';
  dbg += "Paint Rects\n";
  for (var i = 0; i < rects.length; i++)
    dbg += '(' + rects[i].area + ') ' + rects[i].tm + ' - ' + rects[i].url + "\n";
  dbg += "Visual Progress\n";
  for (var i = 0; i < progress.length; i++)
    dbg += '(' + progress[i].area + ') ' + progress[i].tm + ' - ' + progress[i].progress + "\n";
  dbg += 'First Paint: ' + firstPaint + "\n";
  dbg += 'Speed Index: ' + SpeedIndex + "\n";
  console.log(dbg);
  */
  return {
	  si : SpeedIndex,
	  fp : firstPaint
  }
};

inspectIT.start();