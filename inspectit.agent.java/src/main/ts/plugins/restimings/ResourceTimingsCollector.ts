///<reference path="./data/ResourceLoadRequestRecord.ts"/>

namespace ResourceTimingsCollector {

    const resTimingsSupported = ("performance" in window) && ("timing" in window.performance) && ("getEntriesByType" in window.performance)
        && (typeof window.performance.clearResourceTimings) === "function"
        && (window.performance.getEntriesByType("resource") instanceof Array);

    export function init() {
        if (resTimingsSupported) {
            const navStart = window.performance.timing.navigationStart;

            pageLoadRequest.require("resTimings");

            Instrumentation.runWithout(function () {
                const onLoadCallback = Instrumentation.disableFor(function () {
                    // use a regular timer to poll the resources lsit
                    // this timer runs the entire time to make sure that for example IMGs added via javascript are also monitored
                    setInterval(resourcesPolling, 500);
                });
                window.addEventListener("load", onLoadCallback);
            });
        }
    }

    /**
     * Regular task execute to fetch the current list of resource laod requests.
     */
    function resourcesPolling() {
        Instrumentation.runWithout(function () {
            const navStart = window.performance.timing.navigationStart;
            const loadEnd = window.performance.timing.loadEventEnd;

            if (!pageLoadRequest.resourceCount) {
                pageLoadRequest.resourceCount = 0;
            }

            const resourceList: PerformanceResourceTiming[] = window.performance.getEntriesByType("resource");

            for (const resource of resourceList) {
                if (resource.responseEnd !== 0 && resource.initiatorType !== "xmlhttprequest") {

                    const resourceRequest = new ResourceLoadRequestRecord();
                    resourceRequest.require("resTimings");
                    resourceRequest.markRelevant();

                    // Resource timings API provides timings relative to the navigation start
                    const startTime = navStart + resource.startTime;

                    if (startTime <= loadEnd) {
                        pageLoadRequest.resourceCount++;
                        resourceRequest.setParent(pageLoadRequest);
                    }

                    resourceRequest.url = resource.name;
                    resourceRequest.enterTimestamp = startTime;
                    resourceRequest.setDuration(resource.duration);
                    if (resource.initiatorType) {
                        resourceRequest.initiatorType = resource.initiatorType;
                    } else {
                        resourceRequest.initiatorType = "Unknown";
                    }
                    resourceRequest.transferSize = (resource as any).decodedBodySize;
                    resourceRequest.baseUrl = window.location.href;

                    resourceRequest.markComplete("resTimings");
                }
            }
            window.performance.clearResourceTimings();
            pageLoadRequest.markComplete("resTimings");
        });
    }

}
InspectITPlugin.registerPlugin(ResourceTimingsCollector);