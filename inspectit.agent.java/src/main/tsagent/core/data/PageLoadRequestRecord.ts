///<reference path="./RequestRecord.ts"/>

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

class PageLoadRequestRecord extends RequestRecord {

    public navigationTimings?: INavigationTimingsStore;
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