namespace NavigationTimingsCollector {

    export function init() {
        if (!(("performance" in window) && ("timing" in window.performance))) {
            return;
        }
        // force the beacon service to wait until we have collected the data
        pageLoadRequest.require("navigationTimings");

        const onLoadCallback = Instrumentation.disableFor(() => {

            // setTimeout is necessary as the load event also impacts the navvigation and resource timings
            setTimeout(Instrumentation.disableFor(() => {

                pageLoadRequest.navigationTimings = pageLoadRequest.navigationTimings || {};
                const navTimings = pageLoadRequest.navigationTimings!;
                const navApi = window.performance.timing;

                pageLoadRequest.enterTimestamp = navApi.navigationStart;
                pageLoadRequest.setDuration(navApi.loadEventEnd - navApi.navigationStart);

                navTimings.navigationStart = navApi.navigationStart;
                navTimings.unloadEventStart = navApi.unloadEventStart;
                navTimings.unloadEventEnd = navApi.unloadEventEnd;
                navTimings.redirectStart = navApi.redirectStart;
                navTimings.redirectEnd = navApi.redirectEnd;
                navTimings.fetchStart = navApi.fetchStart;
                navTimings.domainLookupStart = navApi.domainLookupStart;
                navTimings.domainLookupEnd = navApi.domainLookupEnd;
                navTimings.connectStart = navApi.connectStart;
                navTimings.connectEnd = navApi.connectEnd;
                navTimings.secureConnectionStart = navApi.secureConnectionStart;
                navTimings.requestStart = navApi.requestStart;
                navTimings.responseStart = navApi.responseStart;
                navTimings.responseEnd = navApi.responseEnd;
                navTimings.domLoading = navApi.domLoading;
                navTimings.domInteractive = navApi.domInteractive;
                navTimings.domContentLoadedEventStart = navApi.domContentLoadedEventStart;
                navTimings.domContentLoadedEventEnd = navApi.domContentLoadedEventEnd;
                navTimings.domComplete = navApi.domComplete;
                navTimings.loadEventStart = navApi.loadEventStart;
                navTimings.loadEventEnd = navApi.loadEventEnd;

                pageLoadRequest.markComplete("navigationTimings");
            }), 100);
        });
        Instrumentation.runWithout(() => {
            window.addEventListener("load", onLoadCallback);
        });
    }

}
InspectITPlugin.registerPlugin(NavigationTimingsCollector);