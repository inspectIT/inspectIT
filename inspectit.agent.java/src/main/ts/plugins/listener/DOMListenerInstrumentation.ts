///<reference path="./data/DomEventRecord.ts"/>

namespace DOMListenerInstrumentation {

    let eventRecordsMap: IWeakMap<Event, DomEventRecord>;

    const eventSelectors: IDictionary<EventSelector[]> = {};

    let navigationStart = 0;

    export function init() {

        eventRecordsMap = new WeakMapImpl<Event, DomEventRecord>();
        if (("performance" in window) && ("timing" in window.performance)) {
            navigationStart = window.performance.timing.navigationStart;
        }

        initElementSelectors();
        Instrumentation.addListenerInstrumentation({
            shouldInstrument(target, type) {
                return (Util.isDomElement(target) || target === window || target === document)
                    && (type in eventDictionary);
            },
            instrument(event, originalCallback, executeOriginalCallback) {
                // sanity check, do not instrument events which directly happened on window or document
                if (Util.isDomElement(event.target)) {
                    const target = event.target as HTMLElement;
                    const record = new ListenerExecutionRecord();
                    record.functionName = Util.getFunctionName(originalCallback);
                    record.eventType = event.type;
                    record.setParent(getOrCreateEventRecord(event));
                    record.buildTrace(true, executeOriginalCallback);
                } else {
                    executeOriginalCallback();
                }
            }

        });
    }

    function initElementSelectors() {
        const relevantEvents: IDictionary<boolean> = {};
        if (SETTINGS.domEventSelectors) {
            for (const config of SETTINGS.domEventSelectors) {
                const selector = new EventSelector(config);
                for (const event of selector.events) {
                    eventSelectors[event] = eventSelectors[event] || [];
                    eventSelectors[event].push(selector);
                    if (selector.markAlwaysAsRelevant) {
                        relevantEvents[event] = true;
                    }
                }
            }
        }
        Instrumentation.runWithout( () => {
            for (const event in relevantEvents) {
                document.addEventListener(event, (eventObj) => {
                    if (Util.isDomElement(eventObj.target)) {
                        getOrCreateEventRecord(eventObj);
                    }
                }, true);
            }
        });
    }

    function getOrCreateEventRecord(event: Event): DomEventRecord {
        let record = eventRecordsMap.get(event);
        if (record) {
            return record;
        } else {
            record = new DomEventRecord();
            if (event.timeStamp) {
                // event.tiemStamp is sometimes relative to navigationStart and sometimes to the epoche
                // to differentiate we compare thetimestamp with the epoche time from january 1st, 2000
                const epocheTimeStamp2000 = 946684800;
                if (event.timeStamp < epocheTimeStamp2000) {
                    record.enterTimestamp = event.timeStamp + navigationStart;
                } else {
                    record.enterTimestamp = event.timeStamp;
                }
            } else {
                record.enterTimestamp = Util.timestampMS();
            }
            record.baseUrl = window.location.href;
            record.setDuration(0);
            record.setParent(TraceBuilder.getCurrentParent());
            const eventName = event.type;
            record.eventType = eventName;
            eventRecordsMap.set(event, record);

            let isRelevant: boolean = false;
            for (const selector of (eventSelectors[eventName] || [])) {
                const match = selector.findMatch(event.target as Element);
                if (match) {
                    selector.extractAttributes(match, record.elementInfo);
                    isRelevant = isRelevant || selector.markAlwaysAsRelevant;
                }
            }
            for (const selector of (eventSelectors["*"] || [])) {
                const match = selector.findMatch(event.target as Element);
                if (match) {
                    selector.extractAttributes(match, record.elementInfo);
                }
            }
            if (isRelevant) {
                record.relevantThroughSelector = true;
                record.markRelevant();
            } else {
                record.relevantThroughSelector = false;
            }
            return record;
        }
    }

    /*tslint:disable object-literal-key-quotes*/
    /**
     * Holds the names of all relevant dom elemtns mapped to their corresponding inline-listeners.
     * This is used for example to fitler the listeners to instrument.
     *  see https://wiki.selfhtml.org/wiki/JavaScript/DOM/Event/%C3%9Cbersicht
     */
    const eventDictionary: IDictionary<keyof HTMLElement> = {

        // click / actiavtion events
        "activate": "onactivate",
        "deactivate": "ondeactivate",
        "beforeactivate": "onbeforeactivate",
        "click": "onclick",
        "dblclick": "ondblclick",
        "focus": "onfocus",
        "submit": "onsubmit",
        "beforedeactivate": "onbeforedeactivate",
        "blur": "onblur",
        "contextmenu": "oncontextmenu",
        "reset": "onreset",
        "select": "onselect",

        // touch events
        "touchcancel": "ontouchcancel",
        "touchend": "ontouchend",
        "touchmove": "ontouchmove",
        "touchstart": "ontouchstart",

        // keyboard / text input events
        "change": "onchange",
        "input": "oninput",
        "invalid": "oninvalid",
        "keydown": "onkeydown",
        "keypress": "onkeypress",
        "keyup": "onkeyup",

        // mouse button related events
        "drag": "ondrag",
        "dragend": "ondragend",
        "dragenter": "ondragenter",
        "dragleave": "ondragleave",
        "dragover": "ondragover",
        "dragstart": "ondragstart",
        "drop": "ondrop",

        // mouse-move and scroll events
        "mousedown": "onmousedown",
        "mouseenter": "onmouseenter",
        "mouseleave": "onmouseleave",
        "mousemove": "onmousemove",
        "mouseout": "onmouseout",
        "mouseover": "onmouseover",
        "mouseup": "onmouseup",
        "mousewheel": "onmousewheel",
        "scroll": "onscroll",

        // copy / paste interaction
        "copy": "oncopy",
        "cut": "oncut",
        "beforecopy": "onbeforecopy",
        "beforecut": "onbeforecut",
        "beforepaste": "onbeforepaste",
        "paste": "onpaste",
        "ratechange": "onratechange",

        // media interaction / loading
        "canplay": "oncanplay",
        "pause": "onpause",
        "play": "onplay",
        "playing": "onplaying",
        "canplaythrough": "oncanplaythrough",
        "cuechange": "oncuechange",
        "durationchange": "ondurationchange",
        "emptied": "onemptied",
        "ended": "onended",
        "seeked": "onseeked",
        "seeking": "onseeking",
        "stalled": "onstalled",
        "suspend": "onsuspend",
        "timeupdate": "ontimeupdate",
        "volumechange": "onvolumechange",
        "error": "onerror",
        "progress": "onprogress",
        "waiting": "onwaiting",

        "load": "onload",
        "abort": "onabort",
        "loadeddata": "onloadeddata",
        "loadedmetadata": "onloadedmetadata",
        "loadstart": "onloadstart",
    };

}
InspectITPlugin.registerPlugin(DOMListenerInstrumentation);