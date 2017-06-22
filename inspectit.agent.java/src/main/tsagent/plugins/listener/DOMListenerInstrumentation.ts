///<reference path="./data/DOMListenerExecutionRecord.ts"/>

namespace DOMListenerInstrumentation {
    export function init() {
        Instrumentation.addListenerInstrumentation({
            shouldInstrument(target, type) {
                return (isDomElement(target) || target === window || target === document)
                    && (type in eventDictionary);
            },
            instrument(event, originalCallback, executeOriginalCallback) {
                // sanity check, do not instrument events which directly happened on window or document
                if (isDomElement(event.target)) {
                    const target = event.target as HTMLElement;
                    const record = new DOMListenerExecutionRecord();
                    record.functionName = Util.getFunctionName(originalCallback);
                    record.eventType = event.type;
                    record.elementType = target.nodeName;
                    if (target.id) {
                        record.elementID = target.id;
                    }
                    record.buildTrace(true, executeOriginalCallback);
                } else {
                    executeOriginalCallback();
                }
            }

        });
    }

    /**
     * Taken from http://stackoverflow.com/questions/384286/javascript-isdom-how-do-you-check-if-a-javascript-object-is-a-dom-object.
     */
    function isDomElement(o: any) {
        return (typeof HTMLElement === "object" ? o instanceof HTMLElement : // DOM2
            o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName === "string");
    }

    /*tslint:disable object-literal-key-quotes*/
    // see https://wiki.selfhtml.org/wiki/JavaScript/DOM/Event/%C3%9Cbersicht
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