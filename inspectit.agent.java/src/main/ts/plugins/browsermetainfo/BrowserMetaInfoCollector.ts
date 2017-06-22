
///<reference path="./data/UserSessionInfoRecord.ts"/>

// Declare possible available vars
declare let InstallTrigger: any;

/**
 * Module for collection metainfomration about the users browser.
 */
namespace BrowserMetaInfoCollector {

    export function asyncInit() {

        const wnd = window as any;
        const doc = document as any;

        const infoRecord = new UserSessionInfoRecord();

        // some pre-matching
        const isiOS = isIOS();
        const iOSSafari = isiOS && !/(CriOS|OPiOS)/.test(navigator.userAgent) && /Safari/.test(navigator.userAgent);
        const iOSChrome = isiOS && /CriOS/.test(navigator.userAgent);

        const isOpera = (!!wnd.opr && !!wnd.addons) || !!wnd.opera || navigator.userAgent.indexOf(" OPR/") >= 0
            || navigator.userAgent.indexOf("OPiOS") >= 0;
        const isFirefox = typeof InstallTrigger !== "undefined";
        const isSafari = iOSSafari || Object.prototype.toString.call(wnd.HTMLElement).indexOf("Constructor") > 0;
        const isIE = /* @cc_on!@ */false || !!doc.documentMode;
        const isEdge = !isIE && !!wnd.StyleMedia;
        const isChrome = iOSChrome || Boolean(wnd.chrome);

        // get language
        infoRecord.language = navigator.language || (navigator as any).userLanguage;
        infoRecord.device = getOS();

        if (isOpera) {
            infoRecord.browser = "Opera";
        } else if (isFirefox) {
            infoRecord.browser = "Firefox";
        } else if (isSafari) {
            infoRecord.browser = "Safari";
        } else if (isIE) {
            infoRecord.browser = "Internet Explorer";
        } else if (isEdge) {
            infoRecord.browser = "Edge";
        } else if (isChrome) {
            infoRecord.browser = "Google Chrome";
        } else {
            infoRecord.browser = "Unknown";
        }

        infoRecord.markRelevant();
    }

    /**
     * Returns the name of the OS the user is running.
     */
    function getOS(): OSName {
        const userAgent = navigator.userAgent;
        // mobile detection
        if (isIOS()) {
            return "iOS";
        } else if (userAgent.match(/Android/i)) {
            return "Android";
        }
        // desktop detection
        if (navigator.appVersion.indexOf("Win") > -1) {
            return "Windows";
        } else if (navigator.appVersion.indexOf("Mac") > -1) {
            return "Mac";
        } else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) {
            return "Linux";
        } else {
            return "Unknown";
        }
    }

    function isIOS() {
        return /(iPad|iPhone|iPod).*WebKit/.test(navigator.userAgent) && !(window as any).MSStream;
    }

}
InspectITPlugin.registerPlugin(BrowserMetaInfoCollector);