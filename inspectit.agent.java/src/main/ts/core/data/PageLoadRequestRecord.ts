///<reference path="./RequestRecord.ts"/>

/**
 * Storage for the information provided by the Navigation Timings API.
 */
interface INavigationTimingsStore {
    navigationStart?: number;
    unloadEventStart?: number;
    unloadEventEnd?: number;
    redirectStart?: number;
    redirectEnd?: number;
    fetchStart?: number;
    domainLookupStart?: number;
    domainLookupEnd?: number;
    connectStart?: number;
    connectEnd?: number;
    secureConnectionStart?: number;
    requestStart?: number;
    responseStart?: number;
    responseEnd?: number;
    domLoading?: number;
    domInteractive?: number;
    domContentLoadedEventStart?: number;
    domContentLoadedEventEnd?: number;
    domComplete?: number;
    loadEventStart?: number;
    loadEventEnd?: number;
    speedIndex?: number;
    firstPaint?: number;

}

/**
 * Record for storing the initial load of the browser page.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest.
 */
class PageLoadRequestRecord extends RequestRecord {

    /**
     * The captrued navigation timings, if available and if the corresponding module is active.
     */
    public navigationTimings?: INavigationTimingsStore;

    /**
     * Stores the number of resourceloads which this initial load caused.
     * Captured by the resource-timings module.
     */
    public resourceCount: number = 0;

    protected readonly type = "pageLoadRequest";

    public getDTO() {
        const dto = super.getDTO();
        if (this.navigationTimings) {
            dto.navigationTimings = this.navigationTimings as any;
        }
        if (this.resourceCount !== 0) {
            dto.resourceCount = this.resourceCount;
        }
        return dto;
    }
}