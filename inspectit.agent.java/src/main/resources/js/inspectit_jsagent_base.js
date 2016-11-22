
//closure used to hide the data from the rest of the application
(function(){

	/**
	 * load the settings which are specified as an inline script in the html file.
	 */
	var SETTINGS = window.inspectIT_settings;
	
	/**
	 * Specifies the minimum duration in milliseconds a monitored call has to take to be marked as "relevant".
	 * See the createEUMElement function below for a definition of relevance.
	 */
	var MINIMUM_DURATION_FOR_RELEVANCY = SETTINGS.relevancyThreshold;
	
	/**
	 * stores the init-function of all registered plugins.
	 */
	var plugins = {};
	
	/**
	 * Initialization flag, used to prevent issues if the agent accidently was injected multiple times.
	 */
	var isInitialized = false;
	
	/**
	 * Holds the EUMElement of the PageLoadAction of this window.
	 * This variable is globally accessible through window.inspectit.pageLoadAction.
	 */
	var pageLoadAction;
	
	/**
	 * Holds the EUMElement of the PageLoadRequest of this window.
	 * This variable is globally accessible through window.inspectit.pageLoadRequest.
	 */
	var pageLoadRequest;
	
	/**
	 * Simple counter used for assigning unique local element IDs.
	 * Every time createEUMElement is called, this counter is incremented.
	 */
	var idCounter = 1;

	/**
	 * Initializes a pool of soft-reference allowing EUM-Elements to directly reference their parent.
	 * These Refernces have to be stored softly to prevent memory leaks in case of traces with an infinite depth.
	 * 
	 */
	var createParentReference = createLimitedReferencePool(1000);
	
	/**
	 * Method for registering a plugin, accesible through window.inspectIT.registerPlugin.
	 * This method will ensure that each plugin is only registered once in case of an accidential double injection of the agent.
	 * 
	 * @param name
	 * 		a unique name of the plugin to register (e.g. "navigationTimings") which is used to prevent double registration.
	 * 		If a plugin with this name has already been registered, this call is ignored.
	 * @param plugin
	 * 		The object representing this plugin.
	 * 		It may contain callbacks to interact iwth the agent base.
	 */
	function registerPlugin(name, plugin) {
		//prevent double and too late registration
		if( ! (name in plugins) && !isInitialized) {
			plugins[name] = plugin;
		}
	}
	
	/**
	 * Initializes the agent and all registered plugins.
	 * This method is called at the end of the agent script.
	 */
	function init() {
		if(isInitialized === true) {
			return;
		} else {
			isInitialized = true;
		}
		
		//check for required features
		if(typeof XMLHttpRequest == "undefined" || !("addEventListener" in window)) {
			return;
		}
		
		if(SETTINGS.allowListenerInstrumentation) {
			instrumentation.initListenerInstrumentation();			
		}
		
		beaconService.init();
		initPageLoadElements();
		
		// init plugins synchronously (e.g. isntrumentation)
		for (var pluginName in plugins) {
			if("init" in plugins[pluginName]) {
				plugins[pluginName].init();					
			}	
		}
		
		instrumentation.runWithout(function() {
			// init plugins asynchronously (e.g. capturing of data)
			setTimeout(function() {
				for (var pluginName in plugins) {
					if("asyncInit" in plugins[pluginName]) {
						plugins[pluginName].asyncInit();					
					}	
				}
			}, 50);
			//register beforeUnloadListener
			window.addEventListener("beforeunload", function (event) {
				for (var pluginName in plugins) {
					if("beforeUnload" in plugins[pluginName]) {
						plugins[pluginName].beforeUnload();					
					}	
				}
				beaconService.beforeUnload();
			});
		});
		
		
		//mark the compeltion
		pageLoadRequest.markComplete("agentBase");
		pageLoadAction.markComplete("agentBase");

	}
	
	/**
	 * Creates the pageload request and action elements.
	 * In addition, this method registeres a listeners isntrumentation on pagelaod events.
	 * This ensures that time consuming actions or AJAX requests happening there are correctly mapped to the pageLoadAction.
	 * 
	 */
	function initPageLoadElements() {
		pageLoadAction = createEUMElement("pageLoadAction");
		pageLoadRequest = createEUMElement("pageLoadRequest");
		pageLoadRequest.require("defaultTimings");
		
		//set an initial timestamp, if navtimings module is active this timestamp will be overwritten
		pageLoadRequest.setEnterTimestamp(util.timestampMS());
		inspectIT.instrumentation.runWithout(function(){
			window.addEventListener("load", function(){
				if(! (pageLoadRequest.exitTimestamp) ) {
					pageLoadRequest.setExitTimestamp(util.timestampMS());					
				}
				pageLoadRequest.markComplete("defaultTimings");
			});

			
		})
		//prevent the elements from being sent
		pageLoadAction.require("agentBase");
		pageLoadRequest.require("agentBase");
		
		pageLoadRequest.url = window.location.href;
		//set up the trace structure
		pageLoadAction.buildTrace(false, function() {
			pageLoadRequest.buildTrace(false);
		});
		
		pageLoadRequest.markRelevant(); //automatically marks the action as relevant
		
		function startInstrumentation(executeOriginalListener, originalCallback, event) {	
			if( (event.target === document && (event.type == "load" || event.type == "DOMContentLoaded"))
					|| (event.target === window && event.type == "load" )) {
				
				var listenerRecord = inspectIT.createEUMElement("listenerExecution");
				listenerRecord.require("listenerData");
				listenerRecord.setParent(pageLoadAction);

				var funcName = inspectIT.util.getFunctionName(originalCallback);
				if(funcName != "") {
					listenerRecord.functionName = funcName;
				}
				listenerRecord.eventType = event.type;
				
				//execute the listener and build the trace
				listenerRecord.buildTrace(true, executeOriginalListener);
			
				listenerRecord.markComplete("listenerData");
			} else {
				//not a dom element
				executeOriginalListener();
			}
		}
		inspectIT.instrumentation.instrumentEventListener(startInstrumentation);		
		
				

		window.inspectIT.pageLoadAction = pageLoadAction;
		window.inspectIT.pageLoadRequest = pageLoadRequest;
	}
	
	
	/**
	 * 
	 * Factory function for creating and initializing a new EUM Element which (possibly) will be sent through a beacon.
	 * This method automatically assigns a unique ID to the element and sets the type proeprty correctly.
	 * 
	 * For elements which are part of a trace, the created element contains all necessary utility methods for building the trace information.
	 * 
	 * The created element also autonomously manages the sending of itself:
	 * An element is automatically send if its data has been marked as complete and it is a "relevant" element.
	 * For an element to be relevant, one of the following three conditions must hold:
	 * 		a) the element has been explicitly marked as relevant using a element.markRelevant() call (e.g. done to force AJAX requests always being relevant)
	 * 		b) the element is part of a trace and any of it's child elements is relevant
	 * 		c) the duration of the element has been measured and is greater or equal to MINIMUM_DURATION_FOR_RELEVANCY
	 * The data completeness state is managed throught the element.require(key) and element.markComplete(key) calls.
	 * 	Calling require(key) causes the sending of the element to be postproned until markComplete(key) has been called with the same key (a unique string).
	 *  The typical usage pattern is for plugins to invoke element.require("myCoolPlugin") directly after the creation of the element.
	 *  This will ensure that the plugin can modify the element freely, afterwards to release this lock element.markComplete("myCoolPlugin")
	 *  is called to allow the element to be sent.	
	 * 
	 * @param typeName the type of the EUM element to construct, e.g. "ajaxRequest" or "listenerExecution".
	 * 
	 * @returns the newly created EUM element.
	 */
	function createEUMElement(typeName) {
		var eumElement = {
			type : typeName,
			id : idCounter
		}
		idCounter++;
		
		var incompletePlugins = {};
		
		/**
		 * The reference to the parent element is held solely for the propagation of markRelevant() calls.
		 */
		var parentElem = createParentReference();
		
		var wasSent = false;
		
		var isRelevant = false;
		
		/**
		 * Allows to manually set the trace information of this element, especially for asynchronous calls.
		 * Sets the parent call of this element to the specified newParent.
		 * 
		 * @param newParent
		 * 		the EUMElement object to configure as new parent
		 * @param isAsnychronous (optional)
		 * 		it this parameter is truthy, the call will be marked as asynchronous
		 */
		eumElement.setParent = function(newParent){
			parentElem.setTarget(newParent);
			if(newParent !== null) {
				this.parentLocalID = newParent.id;
				if(isRelevant) {
					newParent.markRelevant();
				}
			} else {
				delete this.parentLocalID;
			}
		};
		
		/**
		 * Forces the element to not be sent until markComplete has been called with the same key.
		 * @param key
		 * 		a string identifying the data to wait for
		 */
		eumElement.require = function(key){
			incompletePlugins[key] = true;
		};

		/**
		 * Marks data previously marked with require(key) as complete.
		 * If the element is relevant and this was the last incomplete dataset, this call will also result in the element being sent.
		 * @param key
		 * 		a string identifying the data to wait for
		 */
		eumElement.markComplete = function(key){
			delete incompletePlugins[key];
			trySend();
		};

		/**
		 * Explicitly marks this element as relevant.
		 * If the data of this element has been completely colelcted (no pending markComplete calls), markRelevant() will result in the element being sent.
		 */
		eumElement.markRelevant = function(){
			if(isRelevant) {
				return; //already relevant, nothing todo
			}
			isRelevant = true;
			if(parentElem.getTarget() !== null) {
				parentElem.getTarget().markRelevant(); //if an element is relevant, all parents of it are also relevant
				parentElem.setTarget(null); // release the reference, as it is not needed anymore
			}
			trySend();
		};
		
		/**
		 * Utility function for building a synchronous trace.
		 * Invoking this function will result in the following actions:
		 *  1.	The parent of this element will be set to the current parent held by the traceBuilder(see below).
		 *  2. if storeTimingsFlag is true, the duration of the executionCode-function will be captured and stored in the element
		 *  3. executionCode will be executed
		 *  
		 *  @param storeTimingsFlag
		 *  		true, if the timings of the call should be captured
		 *  @param executionCode
		 *  		the actual functionality, invoking all sub calls of this element
		 */
		eumElement.buildTrace = function(storeTimingsFlag, executionCode){
			
			var returnValue = undefined;
			
			traceBuilder.enterChild(this);
			if(storeTimingsFlag) {
				this.require("traceTimings"); //do not send the element before the timing has been completed
				this.setEnterTimestamp(util.timestampMS());
			}
			try {
				if(typeof executionCode !== "undefined") {
					returnValue = executionCode();
				}
			} finally {
				if(storeTimingsFlag) {
					this.setExitTimestamp(util.timestampMS());
					this.markComplete("traceTimings");
				}
				traceBuilder.finishChild();
			}
			return returnValue;
		};	
		
		/**
		 * @param timestamp sets the enter timestamp, possibly making this element relevant.
		 */
		eumElement.setEnterTimestamp = function(timestamp) {
			this.enterTimestamp = timestamp;	
			if(((typeof eumElement.enterTimestamp === "number") && (typeof eumElement.exitTimestamp === "number") 
					&& (eumElement.exitTimestamp - eumElement.enterTimestamp) >= MINIMUM_DURATION_FOR_RELEVANCY)) {
				this.markRelevant();
			}
		};

		/**
		 * @param timestamp sets the exit timestamp, possibly making this element relevant.
		 */
		eumElement.setExitTimestamp = function(timestamp) {
			this.exitTimestamp = timestamp;	
			if(((typeof eumElement.enterTimestamp === "number") && (typeof eumElement.exitTimestamp === "number") 
					&& (eumElement.exitTimestamp - eumElement.enterTimestamp) >= MINIMUM_DURATION_FOR_RELEVANCY)) {
				this.markRelevant();
			}
		};
		
		/**
		 * Private method checking the sending conditions.
		 */
		function trySend() {
			//check for data completeness
			if(Object.keys(incompletePlugins).length > 0) {
				return false;
			}
			//check for relevancy
			if(!isRelevant) {
				return false;
			}
			//prevent double sending
			if(wasSent) { 
				return false;
			}
			wasSent = true;
			beaconService.send(eumElement);
			return true;
		}
		
		return eumElement;
	}
	/**
	 * Service responsible for collecting the data and sending the actual beacons.
	 * This service should not be accessed externally, instead the sending shhould be managed through the methods provided with the EUMElements.
	 * 
	 * This service is also responsible for the management of the sessionID and the tabID.
	 * If the corresponding IDs are not assigned, it will request new IDs by sending special values to the server.
	 * 
	 */
	var beaconService = (function() {
		
		var SESSION_COOKIE_NAME = "inspectIT_cookieId";
		
		var BEACON_URL = SETTINGS.eumManagementServer;
		
		var activeModules = SETTINGS.activeAgentModules;
		
		var BEACON_API_SUPPORTED = (typeof navigator.sendBeacon !== "undefined") ;
		
		var dataToSend = [];
		
		var firstDataInQueueTimestamp = null;
		var lastDataInQueueTimestamp = null;
		
		/**
		 * Variables holding the sessionID and teh tabID assigned by teh Java Agent.
		 */
		var sessionID = "-1";
		var tabID = "-1";
		
		/**
		 * This flag makes sure that only one beacon is sent at a time.
		 * It is true if we stil lare awaiting the response of a previously sent beacon.
		 * 
		 */
		var awaitingResponse = false;

		/**
		 * Regular timer for checking the queue and possibly sending a beacon.
		 */
		var sendTimer = null;
		
		/**
		 * After this amount of time of inactivity (no new data added to send),
		 * the beacon will be sent.
		 */
		var TIME_WINDOW = 2500;

		/**
		 * A element is guaranteed to be no longer buffered than this duration.
		 * THis means if elements are added regularly to the queue, a beacon will be sent at this frequency.
		 */
		var MAX_TIME_WINDOW = 15000;
		
		function init() {
			var sessionCookie = inspectIT.util.getCookie(SESSION_COOKIE_NAME);
			if(sessionCookie === null) {
				//send an empty beacon immediately to request a new session ID - it seems like this page has been cached
				// or the JS agent has been injected manually
				forceBeaconSend();
			} else {
				//session cookie available- read it
				sessionID = sessionCookie;			
			}
			inspectIT.instrumentation.runWithout(function() {
				sendTimer = setInterval(sendConditionChecker, 1000);				
			});
		}

		/**
		 * A timer executed every second to check the conditions for sending a new beacon.
		 * If the conditions are met, a beacon is sent.
		 */
		function sendConditionChecker() {
			if(!awaitingResponse) {
				if(dataToSend.length > 0) {
					var time = inspectIT.util.timestampMS();
					if((time - firstDataInQueueTimestamp) >= MAX_TIME_WINDOW || (time - lastDataInQueueTimestamp) >= TIME_WINDOW) {
						forceBeaconSend();
					}
				}				
			}
		}
		
		/**
		 * Adds an element to the send queue and updates the timing information for the sending policy.
		 * @param element the element to send
		 */
		function send(element) {
			dataToSend.push(element);
			var time = inspectIT.util.timestampMS();
			lastDataInQueueTimestamp = time;
			//are we the first element in the queue?
			if(dataToSend.length === 1) {
				firstDataInQueueTimestamp = time;
			}
		}
		
		/**
		 * Sends a beacon, ignoring whether the conditions are met.
		 */
		function forceBeaconSend() {
			//disable instrumentation as we interact with APIs
			inspectIT.instrumentation.runWithout(function() {				
				var beaconObj = {
					tabID : tabID,
					sessionID : sessionID,
					activeAgentModules : activeModules
				}
				
				if(sessionID == "-1") {
					//we have to request a new session ID, as this page was probably cached
					//we therefore will send an empty beacon instead due to possible race condition
					//across multiple tabs within the same session
					beaconObj.data = [];
				} else {
					beaconObj.data = dataToSend;
					dataToSend = [];
					lastDataCollectionTimestamp = null;
					firstDataCollectionTimestamp = null;
				}
				
				//use the beacon API if we do not care about the response
				if ( BEACON_API_SUPPORTED && sessionID != "-1" && tabID != "-1") {
					navigator.sendBeacon(BEACON_URL, JSON.stringify(beaconObj));
				} else {
					var xhrPost = new XMLHttpRequest();
					xhrPost.open("POST", BEACON_URL, true);
					xhrPost.setRequestHeader("Content-Type", "application/json");
					xhrPost.addEventListener("loadend", function() {
						inspectIT.instrumentation.runWithout(function() {
							if (xhrPost.status === 200) {
								var responseObj = JSON.parse(xhrPost.responseText);
								
								if(tabID == "-1") {
									tabID = responseObj.tabID;
								}
								if(sessionID == "-1") {
									var sessionCookie = inspectIT.util.getCookie(SESSION_COOKIE_NAME);
									if(sessionCookie !== null){
										//ignore the received id and instead use the stored one
										sessionID = sessionCookie;
										awaitingResponse = false;
									} else {
										//possible race condition between multiple tabs here
										//we just wait a moment and then take the winner of this race condition
										document.cookie = SESSION_COOKIE_NAME+"="+responseObj.sessionID+"; path=/"
										setTimeout(function() {
											inspectIT.instrumentation.runWithout(function() {
												sessionID = inspectIT.util.getCookie(SESSION_COOKIE_NAME);
												awaitingResponse = false;
												checkBeaconSendTimeout();
											});
											awaitingResponse = false;
										},200);
									}
								} else {
									awaitingResponse = false;
								}
							} else {
								//error handling: add the failed data back to the send queue
								for (var i = 0; i < beaconObj.data.length; i++) {
									send(beaconObj.data[i]);
								}
								awaitingResponse = false;
							}
						});
					});
					xhrPost.send(JSON.stringify(beaconObj));
					awaitingResponse = true;
				}
			});
		}
		
		function beforeUnload() {
			//cancel timer
			inspectIT.instrumentation.runWithout(function() {
				clearInterval(sendTimer);				
			});
			if(dataToSend.length > 0) {
				forceBeaconSend();				
			}
		}
		
		return {
			init : init,
			send : send,
			beforeUnload: beforeUnload
		}
	})();
	
	/**
	 * Instrumentation module.
	 * 
	 * Allows to disable the instrumentation for a scope.
	 * Disabeling instrumentation is necessary, because plugins also make use of instrumented functions, such as setTimeout for example.
	 * Also provides the infrastructure for instrumenting listeners.
	 * 
	 */
	var instrumentation = (function(){
		
		var instrumentationCounter = 0;
		
		function isEnabled() {
			return (instrumentationCounter === 0);
		}
		
		function disable() {
			instrumentationCounter++;
		}
		
		function reenable() {
			instrumentationCounter--;
		}
		
		/**
		 * Disables the instrumentation, runs the given function and afterwards reenables the instrumentation.
		 * 
		 * @param func the function to execute without isntrumentation.
		 * @returns the return value of func
		 */
		function runWithout(func) {
			return (disableFor(func))();
		}
		
		/**
		 * Wraps the given function in a function which executes exactly the same task but with instrumentation disabled.
		 * @param func the function to wrap
		 * @returns the wrapper for func which has instrumentation disabled.
		 */
		function disableFor(func) {
						
			return function() {
				disable();
				var retVal = func.apply(this, arguments);
				reenable();
				return retVal;
			};
		}
		
		/**
		 * A list of all active listener instrumentations.
		 */
		var listenerInstrumentations = [];
		
		/**
		 * Adds a listener instrumentation.
		 * This instrumentation is applied to any listener, even if they were attached before this call.
		 * 
		 * @param instrumentationFunc 
		 * 		the instrumentation in form of a function with the parameters (executeOriginalListener, originalCallback, event)
		 * 		 - executeOriginalListener is a function without parameters which executes the original listener function
		 * 		 - originalCallback is the original function which was passed to addEventListener
		 *   	 - event is the event which was dispatched
		 *   
		 *   	the typical pattern for a listener is as follows:
		 *   	function myListener((executeOriginalListener, originalCallback, event) {
		 *   		if(event is one of the events I want to instrument) {
		 *   			//isntrumentation code...
		 *   				executeOriginalListener();
		 *   			//isntrumentation code...
		 *   		} else {
		 *   			executeOriginalListener();
		 *   		}
		 *   	}
		 * 	
		 * 
		 */
		function instrumentEventListener(instrumentationFunc) {
			listenerInstrumentations.push(instrumentationFunc);
		}
		
		/**
		 * removes a previously registered listener instrumentation.
		 * @param instrumentationFunc the registered isntrumentation
		 */
		function uninstrumentEventListener(instrumentationFunc) {
			var index = listenerInstrumentations.indexOf(instrumentationFunc);
			if (index != -1) {
				listenerInstrumentations.splice(index,1);
			}
		}
		
		function initListenerInstrumentation() {
			
			if( (typeof EventTarget !== "undefined") && EventTarget.prototype.hasOwnProperty("addEventListener")
					&& EventTarget.prototype.hasOwnProperty("removeEventListener")) {
				
				//Chrome & Firefox & Edge
				
				instrumentForPrototype(EventTarget.prototype);
			} else {
				//IE solution
				instrumentForPrototype(Node.prototype);
				instrumentForPrototype(XMLHttpRequest.prototype);
				instrumentForPrototype(window);
			}
			
			function instrumentForPrototype(prototypeToInstrument) {
				
				var uninstrumentedAddEventListener = prototypeToInstrument.addEventListener;
				prototypeToInstrument.addEventListener = function(type, callback, optionsOrCapture) {
					
					//check instrumentation disabled flag
					if(!isEnabled()) {
						return uninstrumentedAddEventListener.apply(this, arguments);
					}
					
					//fetch the existing instrumented callback or create a new one
					//every callback is only instrumented once
					var instrumentedCallback;
					if( "_inspectIT_instrumentedCallback" in callback) {
						instrumentedCallback = callback._inspectIT_instrumentedCallback;						
					} else {
						instrumentedCallback = function() {
							var originalArgs = arguments;
							var originalThis = this;
							
							var currentListenerIndex = -1;
							
							var returnValue;
							
							//recursive iterator
							//when this function is invoked, it either calls the next instrumentation or the actual callback if all instrumentations were executed
							function continueFunc() {
								currentListenerIndex++;
								if(currentListenerIndex < listenerInstrumentations.length) {
									//call the next listener
									listenerInstrumentations[currentListenerIndex].apply(window, extendedArgs);
								} else {
									//finally call the original callback and keep its return value
									returnValue = callback.apply(originalThis, originalArgs);
								}
								return returnValue;
							}
							
							var extendedArgs = [continueFunc,callback].concat(Array.prototype.slice.call(originalArgs));
							
							//start the call chain of calling first all instrumentations and finally callign the original callback
							continueFunc();
							
							return returnValue;
						};		
						
						//store the callback for future use as a unchangeable, non enumerateable property
						Object.defineProperty(callback, "_inspectIT_instrumentedCallback", { value : instrumentedCallback});
						
					}
					
				
					//Attach the instrumented listener
					var modifiedArgs = Array.prototype.slice.call(arguments);
					modifiedArgs[1] = instrumentedCallback;
					return uninstrumentedAddEventListener.apply(this, modifiedArgs);
				}

				var uninstrumentedRemoveEventListener = prototypeToInstrument.removeEventListener;
				
				prototypeToInstrument.removeEventListener = function(type, callback, optionsOrCapture) {

					//check instrumentation disabled flag
					if(!isEnabled()) {
						return uninstrumentedRemoveEventListener.apply(this, arguments);
					}
					
					if( "_inspectIT_instrumentedCallback" in callback) {
						var instrumentedCallback = callback._inspectIT_instrumentedCallback;
						var modifiedArgs = Array.prototype.slice.call(arguments);
						modifiedArgs[1] = instrumentedCallback;
						return uninstrumentedRemoveEventListener.apply(this, modifiedArgs);
					} else {
						return uninstrumentedRemoveEventListener.apply(this, arguments);						
					}					
				}
			}
		}
		
		return {
			initListenerInstrumentation : initListenerInstrumentation,
			isEnabled : isEnabled,
			disable : disable,
			reenable : reenable,
			disableFor : disableFor,
			runWithout : runWithout,
			instrumentEventListener : instrumentEventListener,
			uninstrumentEventListener : uninstrumentEventListener
		}
		
	})();
	
	
	var traceBuilder = (function() {

		
		/**
		 * The counter for setting the executionOrderIndex of trace elements.
		 * This index is used to preserve the order of trace elements when the
		 * precision of timestamps is not enough or timestamps for this element are not available.
		 * 
		 * This is automatically assigned when "enterChild" is called for the trace element.
		 */
		var executionOrderIndexCounter = 1;
		
		var callStack = [];

		/**
		 * The trace builder also allows "Trace Observers" to be attached.
		 * These are invoced before the beginning of a new trace element and after the end of it.
		 * This allows us to watch for certain variable modification, e.g. window.location.href and
		 * to correctly locate them within the existing trace.
		 */
		var traceObservers = [];
		
		function getCurrentParent() {
			if(callStack.length === 0) {
				return null;
			} else {
				return callStack[callStack.length - 1];
			}
		}
		
		function enterChild(eumElement) {
			//notify trace observers first
			for(var index = 0; index < traceObservers.length; index++) {
				traceObservers[index].preElementBegin(eumElement);
			}
			var parent = getCurrentParent();
			if(parent !== null && (typeof (eumElement.parentLocalID)) === "undefined") {
				eumElement.setParent(parent);		
			}
			callStack.push(eumElement);
			assignExecutionOrderIndex(eumElement);
		}

		function finishChild() {
			//notify trace observers first
			for(var index = 0; index < traceObservers.length; index++) {
				traceObservers[index].preElementFinish(callStack[callStack.length - 1]);
			}		
			callStack.pop();	
		}
		/**
		 * Registers a new trace observer.
		 * Trace observers are basically used to implement intelligent polling mechanisms.
		 * This ensures that the detection of changes to variables is in the correct position within the trace.
		 * 
		 * @param observer the observer, it has to contain the preElementBegin and preElementFinish callbacks.
		 */
		function addTraceObserver(observer) {
			traceObservers.push(observer);
		}
		
		/**
		 * Removes a previously registered trace observer.
		 * @param observer the registered observer
		 */
		function removeTraceObserver(observer) {
			var index = traceObservers.indexOf(observer);
			if (index != -1) {
				traceObservers.splice(index,1);
			}
		}
		
		/**
		 * Assigns a execution order index, overwriting any previously stored one.
		 * This method usually does not need to be manually called, if the trace is built using enterChild / finishChild.
		 * 
		 * @param eumElement the element to assign the index to
		 * @returns
		 */
		function assignExecutionOrderIndex(eumElement) {
			eumElement.executionOrderIndex = executionOrderIndexCounter;
			executionOrderIndexCounter++;
		}
		
		return {
			getCurrentParent : getCurrentParent,
			enterChild : enterChild,
			finishChild : finishChild,
			addTraceObserver : addTraceObserver,
			removeTraceObserver : removeTraceObserver
		}
		
	})();
	/**
	 * Utility module providing some often needed functionality.
	 * @return utility module with needed functionality.
	 */
	var util = (function () {
	
		/**
		 * Gets the current timestamp with the Performance API, if supported.
		 * Otherwise returns an approximate tiemstamp using Date.now();
		 * @return current timestamp
		 */
		var timestampMS;
		if (window.performance && performance.timing.navigationStart !== 0) {
			var navStart = performance.timing.navigationStart;
			timestampMS = function() {
				return performance.now() + navStart;
			}
		} else {
			timestampMS = Date.now;	//Date.now is a function		
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
		        if (c.indexOf(name) === 0) {
		            return c.substring(name.length,c.length);
		        }
		    }
		    return null;
		}
	
		/**
		 * Gets the Javascript function name. (Only works >=ES6).
		 * If the name is not available this will return an empty string.
		 */
		function getFunctionName(func) {
			if (!(typeof func === "function")) {
				return null;
			}
			if (func.hasOwnProperty("name")) { //ES 6
				if(func.name == "") {
					return "<anonymous>";
				} else {
					return func.name;					
				}
			}
			return "";
		}

		
		return {
			timestampMS : timestampMS,
			getFunctionName : getFunctionName,
			getCookie : getCookie
		}
		
	})();
	
	
	/**
	 * Creates a pool which limits the number of active references to mimic a soft-reference behaviour.
	 * References are evicted from the pool using a LRU policy when the maximum number of references is reached.
	 * A "use" in this policy is defined as calling reference.setTarget.
	 * 
	 * Also, references can be freed and removed from the pool manually by calling ref.setTarget(null);
	 * They are automatically added back to the pool when setTarget() is called with a non-null target.
	 * 
	 * @param maxActiveReferences the number of references allowed to coexist
	 * @returns a factory function for creating references which belong to this pool.
	 */
	function createLimitedReferencePool(maxActiveReferences) {
		
		function Reference() {
			this.next = null;
			this.previous = null;
			this.pointer= null
		}
		Reference.prototype.setTarget = function(target) {
			//first step - remove from the list
			if(this.pointer != null) {
				removeFromList(this);
			}
			//set the new target
			this.pointer = target;
			//add back to the list
			if(this.pointer != null) {
				addAsHeadToList(this);
			}
		}
		Reference.prototype.getTarget = function() {
			return this.pointer;
		}
		
		var activeReferencesCount = 0;
		
		//all active refences are stored in a doubly linked list
		var head = null;
		var tail = null;

		function removeFromList(ref) {
			if(head === ref) {
				head = ref.next;
			}
			if(tail === ref) {
				tail = ref.previous;
			}
			if(ref.next != null) {
				ref.next.previous = ref.previous;
			}
			if(ref.previous != null) {
				ref.previous.next = ref.next;
			}
			ref.next = null;
			ref.previous = null;
			activeReferencesCount--;
		}
		
		function addAsHeadToList(ref) {
			if(head != null) {
				head.previous = ref;
			}
			if(tail == null) {
				tail = ref;
			}
			ref.next = head;
			ref.previous = null;
			head = ref;
			activeReferencesCount++;
			//check the queue bounds
			while(activeReferencesCount > maxActiveReferences) {
				tail.setTarget(null);
			}
		}
		
		//return a factory function
		return function() {
			return new Reference();
		}
	}
	
	//prevent issues in case of double injection
	if(typeof window.inspectIT === "undefined") {
		window.inspectIT = {
			init : init,
			registerPlugin : registerPlugin,
			instrumentation : instrumentation,
			traceBuilder : traceBuilder,
			createEUMElement : createEUMElement,
			beaconService : beaconService, //only here to allow mocking
			util : util
		};
	}
	
})();