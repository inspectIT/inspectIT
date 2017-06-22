///<reference path="./data/AjaxRecord.ts"/>

namespace AjaxInstrumentation {

    /**
     * Maps the records to the corresponding AJAXs.
     */
    let ajaxRecordMap: IWeakMap<XMLHttpRequest, AjaxRequestRecord>;

    /**
     * Correlation header sent with each request, holding the spanid of the request.
     */
    const SPANID_HEADER = "inspectit_spanid";

    /**
     * Correlation header sent with each request, holding the traceid of the request.
     */
    const TRACEID_HEADER = "inspectit_traceid";

    export function init() {
        ajaxRecordMap = new WeakMapImpl<XMLHttpRequest, AjaxRequestRecord>();

        instrumentAjaxOpen();
        instrumentAjaxSend();
        instrumentAjaxListeners();
    }

    /**
     * Instruments XMLHttpRequest.open.
     * The instrumentation creates a new record for each request and places it in the ajaxRecordMap.
     */
    function instrumentAjaxOpen() {
        const uninstrumentedOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function (method, url, async) {
            if (Instrumentation.isEnabled()) {

                const record = new AjaxRequestRecord();
                ajaxRecordMap.set(this, record);
                record.method = method;
                record.url = url;
                record.isAsync = !!async;

                record.require("ajaxInfo");
                record.markRelevant();
            }
            return uninstrumentedOpen.apply(this, arguments);
        };
    }

    /**
     * Instruments XMLHttpRequest.send.
     * The instrumentation makes sure that all listeners are isntrumented and that the duration and status is captured correctly.
     */
    function instrumentAjaxSend() {
        const uninstrumentedSend = XMLHttpRequest.prototype.send;
        XMLHttpRequest.prototype.send = function () {
            const record = ajaxRecordMap.get(this);
            if (record && Instrumentation.isEnabled()) {
                record.baseUrl = window.location.href;
                record.setParent(TraceBuilder.getCurrentParent());

                this.setRequestHeader(SPANID_HEADER, record.getSpanId());
                this.setRequestHeader(TRACEID_HEADER, record.getTraceId());

                instrumentInlineListeners(this);

                if (record.isAsync) {
                    record.enterTimestamp = Util.timestampMS();
                    const request = this;
                    let durationMeasured = false;
                    // this gives us the time between sending the request
                    // and getting back the response (better than below)
                    // -> works in all modern browsers
                    Instrumentation.runWithout(() => {
                        request.addEventListener("progress", Instrumentation.disableFor((event: ProgressEvent) => {
                            if (record && !durationMeasured) {
                                const percentComplete = event.loaded / event.total;
                                if (percentComplete >= 1) { // -> we're finished
                                    durationMeasured = true;
                                    record.status = request.status;
                                    record.setDuration(Util.timestampMS() - record.enterTimestamp!);
                                    record.markComplete("ajaxInfo");
                                }
                            }
                        }));
                    });

                    // this gives us the time between send and finish of all
                    // javascript tasks executed after the request
                    // -> fallback solution if progress is not available
                    Instrumentation.runWithout(() => {
                        const fallbackListener = Instrumentation.disableFor(() => {
                            // check if the fallback is required
                            if (record && !durationMeasured) {
                                durationMeasured = true;
                                record.status = request.status;
                                record.setDuration(Util.timestampMS() - record.enterTimestamp!);
                                record.markComplete("ajaxInfo");
                            }
                        });
                        // use these instead of loadend as loadend support is not as wide spread
                        request.addEventListener("load", fallbackListener);
                        request.addEventListener("error", fallbackListener);
                        request.addEventListener("abort", fallbackListener);
                    });

                    uninstrumentedSend.apply(this, arguments);
                } else {
                    const args = arguments;
                    record.buildTrace(true, () => {
                        uninstrumentedSend.apply(this, args);
                    });
                    record.status = this.status;
                    record.markComplete("ajaxInfo");
                }
            } else {
                uninstrumentedSend.apply(this, arguments);
            }
        };
    }

    /**
     * List of all inline-listners to instrument.
     */
    const inlineAjaxListeners: Array<keyof XMLHttpRequest> = [
        "onabort",
        "onerror",
        "onload",
        "onloadend",
        "onloadstart",
        "onprogress",
        "ontimeout",
        "onreadystatechange",
    ];

    /**
     * helper function to instrument all inline listeners attached to a request.
     * @param request the request to instrument
     */
    function instrumentInlineListeners(request: XMLHttpRequest) {
        for (const listenerName of inlineAjaxListeners) {
            if (typeof request[listenerName] === "function") {
                (request as any)[listenerName] = Instrumentation.instrumentEventCallback(request[listenerName]);
            }
        }
    }

    /**
     * Helper function registering the hook for non-inline event listeners.
     */
    function instrumentAjaxListeners() {
        Instrumentation.addListenerInstrumentation({
            shouldInstrument(attachmentTarget, type) {
                return attachmentTarget instanceof XMLHttpRequest;
            },
            instrument(event, originalCallback, executeOriginalCallback) {
                const request = event.target as XMLHttpRequest;
                const record = ajaxRecordMap.get(request);
                if (record) {
                    const listenerRecord = new ListenerExecutionRecord();
                    listenerRecord.require("listenerData");
                    listenerRecord.setParent(record);
                    listenerRecord.functionName = Util.getFunctionName(originalCallback);
                    listenerRecord.eventType = event.type;
                    // execute the listener and build the corresponding trace
                    listenerRecord.buildTrace(true, executeOriginalCallback);
                    listenerRecord.markComplete("listenerData");
                }

            }
        });
    }
}
InspectITPlugin.registerPlugin(AjaxInstrumentation);