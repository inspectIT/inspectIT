var supportMap = {};
var waitForUserAction = null;
var capturedActions = [];
var pageLoadAction = null;

QUnit.config.autostart = false;

// instrument action bundler
inspectIT.actionBundler.addAction = function(a) {
    if (a.specialType === "pageLoad") {
        pageLoadAction = a;
    } else {
        capturedActions.push(a);
        execAfter();
    }
}

inspectIT.util.sendToEUMServer = function(data) {
}

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
});

QUnit.module("Core functionality.", {
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});

QUnit.test("Action module test" , function( assert ) {
    var done = assert.async();
    
    var actionId = inspectIT.action.enterAction("plain");
    
    var child1 = inspectIT.action.enterChild(actionId);
    inspectIT.action.submitData(child1, {"data" : "data"});
    inspectIT.action.leaveChild(child1);
    
    var child2 = inspectIT.action.enterChild(actionId);
    inspectIT.action.submitData(child2, {"data" : "data2"});
    inspectIT.action.leaveChild(child2);
    
    inspectIT.action.leaveAction(actionId);
    
    waitForUserAction = function() {
        assert.ok(capturedActions.length === 1, "Action recognized.");
        assert.ok(capturedActions[0].contents.length === 2, "Childs recognized.");
        assert.ok(capturedActions[0].contents[0].data === "data" && capturedActions[0].contents[1].data === "data2", "Child data correct.");
        done();
    }
    
    if (capturedActions.length > 0) {
        execAfter();
    }
    
});

QUnit.test("Cookie module test", function( assert ) {
    // set cookie
    var cookieName = "inspectIT_cookieId";
    var id = "12345";
    
    document.cookie = cookieName + "=" + id;
    assert.ok(document.cookie != "", "Browser supports cookies for this test.")
    
    inspectIT.cookies.checkCookieId();
    var readID = inspectIT.cookies.getCurrentId();
    assert.ok(readID == id, "ID read was successful.")
});

QUnit.module("Speedindex module.", {
    beforeEach : function() {
    }
});

QUnit.test( "Speedindex & Firstpaint test", function( assert ) {
    if (supportMap["restimings"]) {
        var si = RUMSpeedIndex();
        assert.ok(si.hasOwnProperty("fp") && si.hasOwnProperty("si"), "Speedindex and Firstpaint calculation working!");
        assert.ok(si["fp"] >= 0 && si["si"] >= 0, "Calculation realistic.");
    } else {
        assert.ok(true, "Resource Timings API not supported!");
    }
});

QUnit.module("Asynchronous module.", {
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});

QUnit.test("Async instrumentation test", function(assert) {
    var done = assert.async();
    
    var actionId = inspectIT.action.enterAction("plain");
    sampleAjax(function() {
        setTimeout(function() {
            sampleAjax(function() {
                inspectIT.action.leaveAction(actionId);
               
                waitForUserAction = function() {
                    assert.ok(capturedActions.length === 1, "User action was captured.");
                    assert.ok(capturedActions[0].contents.length === 2, "First and second level ajax were captured.");
                    assert.ok(capturedActions[0].contents[0].type === "AjaxRequest" && capturedActions[0].contents[1].type === "AjaxRequest", "Both ajax calls were correct captured.");
                    done();
                }
                
                if (capturedActions.length > 0) {
                    execAfter();
                }
            });
        }, 200);
    });
});

QUnit.module("Navigation Timings module.", {
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});

QUnit.test("Pageload Action test", function( assert ) {
    assert.ok(pageLoadAction !== null, "Pageload successfully detected.");
    assert.ok(pageLoadAction.specialType === "pageLoad", "Pageload is correct.");
});

QUnit.module("AJAX module.", {
    beforeEach : function() {
        capturedActions = [];
    },
    afterEach : function() {
        capturedActions = [];
    }
});

QUnit.test("Easy ajax test", function( assert ) {
    var done = assert.async();
    var actionId = inspectIT.action.enterAction("plain");
    
    sampleAjax(function() {
        inspectIT.action.leaveAction(actionId);
        
        waitForUserAction = function() {
            assert.ok(capturedActions.length === 1, "User action was recognized.");
            assert.ok(capturedActions[0].contents.length === 1, "User action contents were correct.");
            assert.ok(capturedActions[0].contents[0].type === "AjaxRequest", "Type of ajax call was correct.");
            done();
        }
        
        if (capturedActions.length > 0) {
            execAfter();
        }
    });
});

QUnit.test("Stacked ajax test", function( assert ) {
    var done = assert.async();
    var actionId = inspectIT.action.enterAction("plain");
    
    sampleAjax(function() {
        sampleAjax(function() {
            inspectIT.action.leaveAction(actionId);
            
            waitForUserAction = function() {
                assert.ok(capturedActions.length === 1, "User action was correctly bundled.");
                assert.ok(capturedActions[0].contents.length === 2, "User action contents are correct.");
                assert.ok(capturedActions[0].contents[0].type === "AjaxRequest" && capturedActions[0].contents[1].type === "AjaxRequest", "Type of content childs are correct.");
                done();
            }
            
            if (capturedActions.length > 0) {
                execAfter();
            }
        });
    });
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
    if (supportMap["restimings"]) {
        // test it
        assert.ok(pageLoadAction !== null, "Pageload was recognized.");
        var j = 0;
        var jq = false;
        for (var i = 0; i < pageLoadAction["contents"].length; i++) {
            var content = pageLoadAction["contents"][i];
            if (content.type === "ResourceLoadRequest") {
                if (content.url.indexOf("jquery") !== -1) {
                    jq = true;
                }
                j++
            }
        }
        assert.ok(j > 0, "Resource Timings got collected.");
        assert.ok(jq, "JQuery Resource found.");
    } else {
        assert.ok(true, "Resource timings API not supported!");
    }
});

QUnit.module("Listener instrumentation module.", {
    before : function() {
        // create an element and add a listener
        var btn = document.createElement("button");
        btn.setAttribute("style", "visibility:hidden;");
        btn.setAttribute("id", "test_button");
        
        document.body.appendChild(btn);
        
        btn.addEventListener("click", function() {
           sampleAjax(function() {});
        });
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
    
    waitForUserAction = function() {
        assert.ok(capturedActions.length === 1, "Simulated user action has been captured.");
        assert.ok(capturedActions[0].contents.length === 2, "Sub ajax call has been detected.");
        
        var kj = false;
        var aj = false;
        for (var i = 0; i < capturedActions[0]["contents"].length; i++) {
            if (capturedActions[0]["contents"][i].type === "AjaxRequest") {
                aj = true;
            } else if (capturedActions[0]["contents"][i].type === "clickAction") {
                kj = true;
            }
        }
        assert.ok(kj, "Click action has been recognized.");
        assert.ok(aj, "Sub ajax call was detected.");
        done();
    }
    
    if (capturedActions.length > 0) {
        execAfter();
    }
});


// START TESTS
window.addEventListener("load", function() {
    QUnit.start();
});

// HELP FUNCTIONS
function sampleAjax(callback) {
    $.ajax({
        url : "http://www.google.de",
        type : "GET"
    }).always(function(data) {
        callback();
    });
}

function execAfter() {
    if (waitForUserAction !== null) {
        waitForUserAction();
        waitForUserAction = null;
    }
}