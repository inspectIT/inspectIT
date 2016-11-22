
QUnit.config.autostart = false;
QUnit.config.reorder = false;

var supportMap = {};


QUnit.begin(function( details ) {
    // check for Navigation Timing API
    supportMap["navtimings"] = typeof window.performance !== "undefined";
    // check for Resource Timings API
    if ( !('performance' in window) ||
        !('getEntriesByType' in window.performance) ||
        !(window.performance.getEntriesByType('resource') instanceof Array)
    ) {
        supportMap["restimings"] = false;
    } else {
        supportMap["restimings"] = true;
    }
    
    function test(){}
    supportMap["functionNames"] = inspectIT.util.getFunctionName(test) == "test";
    
    
});


function assertSent(assert, expectedElement, message) {
	for(var i=0; i<mocking.sentElements.length; i++) {
		var elem = mocking.sentElements[i];
		if(elem.id == expectedElement.id) {
			assert.deepEqual(elem,expectedElement, message);
			return;
		}
	}	
	assert.ok(false, message);
}

function assertNotSent(assert, expectedElement, message) {
	for(var i=0; i<mocking.sentElements.length; i++) {
		var elem = mocking.sentElements[i];
		if(elem.id == expectedElement.id) {
			assert.ok(false, message);
			return;
		}
	}	
	assert.ok(true, message);
}

QUnit.module("Core functionality.", {
    beforeEach : function() {
    },
    afterEach : function() {
    }
});

QUnit.test("Soft-Reference Freeing", function( assert ) {
	//has to be changed if the configuration of the soft references is changed
	var MAX_ACTIVE_REFERENCES = 1000;
	
	var parent = inspectIT.createEUMElement("test");
	var first = inspectIT.createEUMElement("test");
	var rest = [];
	first.setParent(parent);
	//go just at the limit
	for(var i = 0; i < MAX_ACTIVE_REFERENCES; i++) {
		var elem = inspectIT.createEUMElement("test");
		elem.setParent(parent);
		rest.push(elem);
	}
	//the reference of first to parent should have been evicted, therefore this call will not propagate
	//meaning that parent is not sent
	first.markRelevant();
	assertNotSent(assert, parent, "Reference cleared after the limit is reached.")
	//this one should still hold the reference, therefore propagate the relevancy
	rest[0].markRelevant();
	assertSent(assert, parent, "Reference not cleared before the limit is reached.")
});


QUnit.test("Element sending policies", function( assert ) {
	var elementA = inspectIT.createEUMElement("testRec");
	elementA.require("data");
	elementA.markComplete("data");
	
	assertNotSent(assert, elementA, "Do not send elements until marked as relevant.");
	elementA.markRelevant();
	assertSent(assert, elementA, "Send element after marked as relevant.");
	
	
	var elementB = inspectIT.createEUMElement("testRec");
	elementB.require("data");
	elementB.markRelevant();
	
	assertNotSent(assert, elementB, "Do not send elements until they have completed.");
	elementB.markComplete("data");
	assertSent(assert, elementB, "Send element after the last data was completed.");
	
});


QUnit.test("Trace building and Relevancy Inheritance", function( assert ) {
	
	var grandParent = inspectIT.createEUMElement("testRec");
	grandParent.require("data");
	
	var parent = inspectIT.createEUMElement("testRec");
	parent.require("data");

	var asyncChild = inspectIT.createEUMElement("testRec");
	asyncChild.require("data");
	
	//test synchronous call
	grandParent.buildTrace(true,function() {
		parent.buildTrace(true,function() {
		});
	});
	//test asynchronous call
	asyncChild.setParent(parent,true);
	
	//complete all
	grandParent.markComplete("data");
	parent.markComplete("data");
	asyncChild.markComplete("data");
	
	//test for relevancy inheritance: only the asyncChild is marked as relevant, it should stil ltrigger the sending of all parents
	asyncChild.markRelevant();
	

	assert.ok("enterTimestamp" in parent && "exitTimestamp" in parent && "enterTimestamp" in grandParent && "exitTimestamp" in grandParent, "Enter / Exit timestamp capturing");
	assert.ok(asyncChild.parentLocalID == parent.id && parent.parentLocalID == grandParent.id, "Parent IDs correctly captured");
	
	assertSent(assert, asyncChild, "Asynchronous child sent.");
	assertSent(assert, parent, "Parent sent.");
	assertSent(assert, grandParent, "GrandParent sent.");
	
	
});


QUnit.test("Listener Instrumentation Mechanism", function( assert ) {
	
	var done = assert.async();
	
	var target = document.createDocumentFragment();
	
	var testEvent;
	if(!(document.createEvent)) {
		
		testEvent = new CustomEvent("test");		
	} else {
		//IE polyfill
		(function () {
		  function CustomEvent ( event, params ) {
			params = params || { bubbles: false, cancelable: false, detail: undefined };
			var evt = document.createEvent( 'CustomEvent' );
			evt.initCustomEvent( event, params.bubbles, params.cancelable, params.detail );
			return evt;
		   }

		  CustomEvent.prototype = window.Event.prototype;

		  
		testEvent = new CustomEvent("test");	
		})();
	}
	
	var listenerACount = 0;
	var listenerBCount = 0;
	
	var instrumentationACount = 0;
	var instrumentationBCount = 0;
	
	function listenerA(event) {
		if(listenerACount == 0) {
			assert.strictEqual(event, testEvent, "Correct Event passed to listenerA");
		}
		listenerACount++;
	}
	
	function listenerB(event) {
		//check the event variable only once
		if(listenerBCount == 0) {
			assert.strictEqual(event, testEvent, "Correct Event passed to listenerB");			
		}
		listenerBCount++;
	}
	
	//these should count as different listeners
	target.addEventListener("test",listenerA,true);
	target.addEventListener("test",listenerA,false);

	//these should count as the same listeners (-> only one instrumentation)
	target.addEventListener("test",listenerB);
	target.addEventListener("test",listenerB);
	
	//add instrumentation
	function instrumentation(executeOriginalListener, originalCallback, event) {
		if(event === testEvent) {
			if(originalCallback === listenerA) {
				instrumentationACount++;
			} else if(originalCallback === listenerB) {
				instrumentationBCount++;
			}
		}
		executeOriginalListener();
	}
	inspectIT.instrumentation.instrumentEventListener(instrumentation);
	
	//fire two events
	target.dispatchEvent(testEvent);
	target.dispatchEvent(testEvent);
	
	setTimeout(function() {
		//listenerA should have been called four times, listenerB
		assert.equal(listenerACount,4,"ListenerA was executed the correct number of times");
		assert.equal(listenerBCount,2,"ListenerB was executed the correct number of times");
		
		assert.equal(listenerACount,instrumentationACount,"Instrumentation of listenerA was executed correctly");
		assert.equal(listenerBCount,instrumentationBCount,"Instrumentation of listenerB was executed correctly");
		
		//remove listenerA and check againg
		target.removeEventListener("test",listenerA,true);
		target.removeEventListener("test",listenerA,false);
		target.dispatchEvent(testEvent);
		
		setTimeout(function() {
			assert.equal(listenerACount,4,"ListenerA was not executed anymore after removal");
			assert.equal(listenerBCount,3,"ListenerB was executed the correct number of times");
			
			assert.equal(listenerACount,instrumentationACount,"Instrumentation of listenerA not executed anymore");
			assert.equal(listenerBCount,instrumentationBCount,"Instrumentation of listenerB was executed correctly");
			
			//remove the instrumentation but keep listener B
			inspectIT.instrumentation.uninstrumentEventListener(instrumentation);
			target.dispatchEvent(testEvent);
			setTimeout(function() {
				assert.equal(listenerBCount,4,"ListenerB was executed the correct number of times");
				assert.equal(instrumentationBCount,3,"Instrumentation of listenerB was not executed after removal");
				done();
			}, 500);
			
		}, 500);		
		
	}, 500);
		
	
});


//###################################################################################################################################################
//--------------------------------------------------------MODULE TESTS-------------------------------------------------------------------------------
//###################################################################################################################################################


QUnit.module("SpeedIndex Module", {
    beforeEach : function() {
    },
    afterEach : function() {
    }
});

QUnit.test( "Speedindex & Firstpaint test", function( assert ) {
	if (!supportMap["navtimings"]) {
		assert.ok(true, "Navigation timings not available.")		
		return;
	}
	var done = assert.async();
    setTimeout(function() {
    	var found = false;
    	for(var i=0; i<mocking.sentElements.length; i++) {
    		var elem = mocking.sentElements[i];
    		if(elem.type == "pageLoadRequest") {
    			found = true;
    			assert.ok("navigationTimings" in elem, "NavigationTimings collection avaialble");
    			if("navigationTimings" in elem) {
        			assert.ok(elem.navigationTimings.speedIndex > 0, "SpeedIndex available.");
        			assert.ok(elem.navigationTimings.firstPaint > 0, "FirstPaint available.");    				
    			}
    		}
    	}
    	if(!found) {	
    		assert.ok(false, "PageLoadRequest was not sent.")
    	}
    	done();
    	
    }, 1000);
});


QUnit.module("Resource Timings API module.", {
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});

QUnit.test("Check resources test" , function( assert ) {
	if (!supportMap["restimings"]) {
		assert.ok(true, "Resource timings not available.")		
		return;
	}
	var done = assert.async();
    setTimeout(function() {
    	var found = false;
    	for(var i=0; i<mocking.sentElements.length; i++) {
    		var elem = mocking.sentElements[i];
    		if(elem.type == "resourceLoadRequest") {
				 if (elem.url.indexOf("jquery") !== -1) {
					 found = true;
	             }
    		}
    	}
    	assert.ok(found, "JQuery load captured.")
    	done();
    	
    }, 1000);
});


QUnit.module("Async Module", {
    beforeEach : function() {
    },
    afterEach : function() {
    }
});


QUnit.test("setTimeout instrumentation test", function(assert) {

	if(!supportMap["functionNames"]) {
		assert.ok(true, "Skipping test, function names not supported");
		return;
	}
	
    var done = assert.async();
    
    function timerA() {
    	setTimeout(timerB, 200);
    }
    
    function timerB() {
    	var start = inspectIT.util.timestampMS();
    	//active waiting to make this relevant
    	while( ( inspectIT.util.timestampMS() - start) < 200) {}
    	inspectIT.instrumentation.runWithout(function() {
    		setTimeout(checkSentData,0);
    	});
    }
    
    function checkSentData() {
    	var timerAElem = null;
    	var timerBElem = null;
    	for(var i=0; i<mocking.sentElements.length; i++) {
    		var elem = mocking.sentElements[i];
    		if(elem.type == "timerExecution") {
    			if(elem.functionName == "timerA") {
    				timerAElem = elem;
    			} else if(elem.functionName == "timerB") {
    				timerBElem = elem;
    			}
    		}
    	}
    	
    	assert.notEqual(timerAElem, null, "Timer A captured.")
    	assert.notEqual(timerBElem, null, "Timer B captured.")
    	
    	if(timerAElem != null && timerBElem != null) {
    		assert.equal(timerAElem.configuredTimeout, 100, "Correct configured timeout captured");
    		assert.equal(timerBElem.parentLocalID, timerAElem.id, "Parent relationship captured");
    	}
    	done();
    }
    
    setTimeout(timerA, 100);
    
});

QUnit.module("Ajax Module", {
    beforeEach : function() {
    },
    afterEach : function() {
    }
});

//HELP FUNCTIONS
function sampleAjax(callback) {

	var oReq = new XMLHttpRequest();
	oReq.addEventListener("loadend", callback);
	oReq.open("GET", "http://www.google.com");
	oReq.send();
}

QUnit.test("Ajax capturing", function(assert) {
	
    var done = assert.async();

    var parent = inspectIT.createEUMElement("test");
    parent.require("nodata");
    parent.markComplete("nodata");
    parent.markRelevant();
    
    var startElem = mocking.sentElements.length;
    
    parent.buildTrace(false, function() {
    	sampleAjax(function() {
        	var start = inspectIT.util.timestampMS();
        	//active waiting to make this relevant
        	while( ( inspectIT.util.timestampMS() - start) < 200) {}
        	inspectIT.instrumentation.runWithout(function() {
        		setTimeout(checkSentData,0);
        	});
    	});
    });

    function checkSentData() {
    	var ajaxElem = null;
    	var listenerElem = null;
    	for(var i=startElem; i<mocking.sentElements.length; i++) {
    		var elem = mocking.sentElements[i];
    		if(elem.type == "ajaxRequest") {
    			ajaxElem = elem;
    		} else if (elem.type == "listenerExecution") {
    			listenerElem = elem;
    		}
    	}
    	
    	assert.notEqual(ajaxElem, null, "Ajax captured.");
    	assert.notEqual(listenerElem, null, "Ajax listener captured.");
    	
    	if(ajaxElem != null && listenerElem != null) {
    		assert.equal(ajaxElem.parentLocalID, parent.id, "Parent relationship between ajax and initiator captured");
    		assert.equal(listenerElem.parentLocalID, ajaxElem.id, "Parent relationship between ajax and listener captured");
    	}
    	done();
    }
    
});

function myClickListener() {
	//NOP loop to make this reelvant
	var start = inspectIT.util.timestampMS();
	while( ( inspectIT.util.timestampMS() - start) < 200) {}
}

QUnit.module("Listener instrumentation module.", {
    before : function() {
        // create an element and add a listener
        var btn = document.createElement("button");
        btn.setAttribute("style", "visibility:hidden;");
        btn.setAttribute("id", "test_button");
        
        document.body.appendChild(btn);
        btn.addEventListener("click",myClickListener);
        
    },
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});


QUnit.test("Click capture test" , function( assert ) {
    var done = assert.async();
    
    var el = document.getElementById("test_button");
    el.click();
    
    setTimeout(function() {
    	var record = null;
    	for(var i=0; i<mocking.sentElements.length; i++) {
    		var elem = mocking.sentElements[i];
    		if(elem.type == "domListenerExecution") {
				 if (elem.elementID == "test_button") {
					 record = elem;
	             }
    		}
    	}
    	assert.ok(record != null, "Click Listener execution captured.");
    	if(record) {
    		if(supportMap["functionNames"]) {
    	    	assert.ok(record.functionName == "myClickListener", "Listener function name captured.");    			
    		}
    	}
    	done();
    	
    }, 1000);
});


//START TESTS
window.addEventListener("load", function() {
 QUnit.start();
});