(function(){

	if(typeof window.inspectIT === "undefined") {
			
		/**
		 * Defines the Core module with basic functionality. The functionality can be extended with plugins.
		 * @return Object containing Start function and plugin array.
		 */
		window.inspectIT = (function() {
			
			function init() {
				if(window.inspectIT.initialized === true) {
					return;
				} else {
					window.inspectIT.initialized = true;
				}
				
				//check for required features
				if(typeof XMLHttpRequest == "undefined" || !("addEventListener" in window)) {
					return;
				}
				
				// check if id exists and set if not - need to be first
				inspectIT.cookies.checkCookieId();
				
				// init plugins
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
		
		/**
		 * Utility module providing some often needed functionality.
		 * @return utility module with needed functionality.
		 */
		window.inspectIT.util = (function () {
			var settings = window.inspectIT_settings;
			
			/**
			 * Gets the current timestamp with the Performance API, if supported.
			 * @return current timestamp
			 */
			function getCurrentTimeStamp() {
				if (window.performance) {
					if (performance.timing.navigationStart != 0) {
						return performance.now() + performance.timing.navigationStart;
					}
				}
				return Date.now();
			}
			
			/**
			 * Gets the operating system by parsing the user agent.
			 * @return returns one of the following values: iOS|Android|Windows|Mac|Linux|null
			 */
			function getOS() { // gets the operation system, null if we can't recognize it
				var userAgent = navigator.userAgent;
				// mobile detection
				if(userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
					return "iOS";
				} else if(userAgent.match(/Android/i)) {
					return "Android";
				}
				// desktop detection
				if (navigator.appVersion.indexOf("Win") > -1){
					return "Windows";
				} else if (navigator.appVersion.indexOf("Mac") > -1) {
					return "Mac";
				} else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) {
					return "Linux";
				} else {
					return "Unknown";
				}
			}
			
			/**
			 * Detects which browser, which language and which operating system is used by the client with feature detection.
			 * @return The information about the user listed above.
			 */
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
				} else {
					retObj.name = "Unknown";
				}
				return retObj;
			}
			
			/**
			 * Sends a beacon to the EUM server.
			 * @param dataObject The beacon which should get sent to the server.
			 * @param forceSynchronous Whether the POST-Request to the EUM server should be synchronous. Default value is false.
			 */
			function sendToEUMServer(dataObject, forceSynchronous) {
				if (typeof navigator.sendBeacon !== "undefined") {
					navigator.sendBeacon(settings["eumManagementServer"], JSON.stringify(dataObject));
				} else {
					var xhrPost = new XMLHttpRequest();
					xhrPost.open("POST", settings["eumManagementServer"], !forceSynchronous);
					xhrPost.setRequestHeader("Content-Type", "application/json");
					xhrPost.send(JSON.stringify(dataObject));
				}
			}
			
			/**
			 * Gets the Javascript function name. (Only works >=ES6)
			 */
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
		
		/**
		 * Providing common functions to operate on cookies.
		 * @return Basic functionality for working with cookies.
		 */
		window.inspectIT.cookies = (function () {
			/**
			 * Detects whether a cookie is set or not.
			 * @return true if the cookie is present - false if not
			 */
			function hasCookie(name) {
				return getCookie(name) !== null;
			}
			
			/**
			 * Gets a cookie with a specified key.
			 * @param key The key of the cookie
			 * @return The value of the cookie which is specified by the key - null if the cookie doesn't exist
			 */
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
			
			/**
			 * Checks the cookie which contains the inspectIT Session ID.
			 * If it exists we send a message to the EUM server.
			 */
			function checkCookieId() {
				if (!inspectIT.action.hasActions()) {
					// Creates a new pageLoad User Action
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
			
			/**
			 * Gets the current Session ID by reading a cookie.
			 * @return current Session ID which can be null if there is no Session ID.
			 */
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
		window.inspectIT.actionBundler = (function () {
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
		
		/**
		 * Module for identifying User actions and their belonging requests.
		 */
		window.inspectIT.action = (function () {
			var actions = [];
			var actionChildIds = [];
			var finishedChilds = [];
			
			var offset = 0;
			
			/**
			 * Creates a new user action with no child requests
			 * @param specType the type of the user action (e.g. click / pageLoad)
			 * @return the ID of the created action
			 */
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
			
			/**
			 * Ends a user action and checks if it is finished - that means it will be checked if all child requests are finished.
			 * @param ID of the user action
			 */
			function leaveAction(enterId) {
				var actionId = getActionFromId(enterId);
				if (actionId >= 0) {
					finishedChilds[actionId].push(enterId);
					actionFinished(actionId); // check if finished
				}
			}
			
			/**
			 * Creates a new node for a child request which belongs to an user action.
			 * @param parentId ID of the user action
			 * @return ID of the created child.
			 */
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
			
			/**
			 * Finishes the child action and checks whether the user action is finished.
			 * @param ID of the child
			 */
			function leaveChild(enterId) {
				var actionId = getActionFromId(enterId);
				if (actionId >= 0) {
					finishedChilds[actionId].push(enterId);
					actionFinished(actionId); // check if finished
				} 
			}
			
			/**
			 * Checks whether an user action is finished.
			 * @param id ID of the user action
			 */
			function actionFinished(id) {
				if (actionChildIds[id].length == finishedChilds[id].length) {
					//  the action is finished
					finishAction(id);
				}
			}
			
			/**
			 * Checks if there are any user actions running at the moment.
			 */
			function hasActions() {
				return actions.length > 0;
			}
			
			/**
			 * Finishes an user action and creates the beacon which gets handed over to the Action Bundler module.
			 * @param id The ID of the user action
			 * @param sync whether the beacon should get sent synchronous or not - default is false (asynchronous)
			 */
			function finishAction(id, sync) {
				if (typeof sync === "undefined") sync = false;
				
				inspectIT.actionBundler.addAction(actions[id]);
				forceRemove(id);
				
				if (actions.length == 0) {
					// reset offset because there is no action atm
					offset = 0;
				}
			}
			
			/**
			 * Forces the remove of an user action.
			 * @param id ID of the user action.
			 */
			function forceRemove(id) {
				actions.splice(id, 1);
				finishedChilds.splice(id, 1);
				actionChildIds.splice(id, 1);
			}
			
			/**
			 * Assigns data, which should get sent to the EUM server, to an user action.
			 * @param entrId ID of the user action
			 * @param data The data which should be added to the user action
			 */
			function submitData(entrId, data) {
				var currentAction = getActionFromId(entrId);
				if (currentAction >= 0) {
					actions[currentAction].contents.push(data);
				} // otherwise we can't assign it to an action
			}
			
			/**
			 * Help function for retrieving the user action ID for a child request.
			 * @param id the ID of the child request
			 */
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
	}
})();
