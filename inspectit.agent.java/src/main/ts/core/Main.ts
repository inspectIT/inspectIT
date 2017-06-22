
///<reference path="./data/PageLoadRequestRecord.ts"/>
///<reference path="./data/ListenerExecutionRecord.ts"/>
///<reference path="./services/Instrumentation.ts"/>
///<reference path="./services/BeaconService.ts"/>
///<reference path="./InspectITPlugin.ts"/>

window.inspectIT = {};

/**
 * Defines the legnht of the timeout when asyncInit is called on the plugins.
 * The purpose is to start non-critical tasks like the resoruce timings collection without blocking
 * the Dom parsing.
 * 
 * A short timeout is used to make sure other scripts also get their time to initialize to make sure
 * that asyncInit has no impact on the page rendering.
 * 
 * @internal
 */
const ASYNC_INIT_TIMEOUT = 50;

/**
 * Global variable holding the pageload request.
 */
let pageLoadRequest: PageLoadRequestRecord;

/**
 * Checks whether the browser supports this JS Agent.
 * @internal
 */
function checkBrowserRequirements(): boolean {
    return typeof XMLHttpRequest !== "undefined" && ("addEventListener" in window);
}

/**
 * Flag to prevent init being executed twice.
 * @internal
 */
let initCalled = false;

/**
 * Called after all plugins have registered themselves to initialize the agent.
 * @internal
 */
window.inspectIT.init = function () {
    // safety check as this method is reachable from the global scope
    if (initCalled) {
        return;
    } else {
        initCalled = true;
    }

    if (!checkBrowserRequirements()) {
        return;
    }

    Instrumentation.initListenerInstrumentation();
    BeaconService.init();
    initPageLoadElements();

    const plugins = InspectITPlugin.getRegisteredPlugins();
    for (const plugin of plugins) {
        if (plugin.init) {
            plugin.init();
        }
    }

    Instrumentation.runWithout(function () {
        // init plugins asynchronously (e.g. capturing of data)
        setTimeout(function () {
            for (const plugin of plugins) {
                if (plugin.asyncInit) {
                    plugin.asyncInit();
                }
            }
        }, ASYNC_INIT_TIMEOUT);
        // register beforeUnloadListener
        window.addEventListener("beforeunload", function (event) {
            for (const plugin of plugins) {
                if (plugin.beforeUnload) {
                    plugin.beforeUnload();
                }
            }
            BeaconService.beforeUnload();
        });
    });
};

/**
 * @internal
 */
function initPageLoadElements() {
    // check if correlation info is present and up-to-date
    let backendTraceID: IdNumber | undefined = SETTINGS.traceid;
    if (backendTraceID) {
        const cookieName = "inspectIT_traceid_" + backendTraceID;
        if (Util.getCookie(cookieName)) {
            // cookie present, we were not cached!
            Util.deleteCookie(cookieName);
        } else {
            // page was cached, do not perform a correlation
            backendTraceID = undefined;
        }
    }
    pageLoadRequest = new PageLoadRequestRecord(backendTraceID);
    pageLoadRequest.require("defaultTimings");

    // set an initial timestamp, if navtimings module is active this
    // timestamp will be overwritten
    const start = Util.timestampMS();
    pageLoadRequest.enterTimestamp = start;
    Instrumentation.runWithout(function () {
        window.addEventListener("load", function () {
            if (!(pageLoadRequest.getDuration())) {
                pageLoadRequest.setDuration(Util.timestampMS() - start);
            }
            pageLoadRequest.markComplete("defaultTimings");
        });
    });

    pageLoadRequest.url = window.location.href;
    pageLoadRequest.markRelevant(); // automatically marks the action as relevant

    Instrumentation.addListenerInstrumentation({

        shouldInstrument: (target, type) => {
            if (target === document) {
                return type === "load" || type === "DOMContentLoaded";
            } else if (target === window) {
                return type === "load";
            } else {
                return false;
            }
        },

        instrument: (event, originalCallback, executeOriginalCallback) => {
            const listenerRecord = new ListenerExecutionRecord();
            listenerRecord.setParent(pageLoadRequest);
            listenerRecord.functionName = Util.getFunctionName(originalCallback);
            listenerRecord.eventType = event.type;
            listenerRecord.buildTrace(true, executeOriginalCallback);
        }
    });
}
