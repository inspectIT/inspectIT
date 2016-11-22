/**
 * This module is responsible for capturing browser meta information, such as the browser type, OS and language.
 */
window.inspectIT.registerPlugin("sessionmetainfo", (function() {
	
	var inspectIT = window.inspectIT;
	
	/**
	 * Gets the operating system by parsing the user agent.
	 * @return returns one of the following values: iOS|Android|Windows|Mac|Linux|null
	 */
	function getOS() { // gets the operation system, null if we can't recognize it
		var userAgent = navigator.userAgent;
		// mobile detection
		if(userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
			return "iOS";
		} else if(userAgent.match(/Android/i)) {
			return "Android";
		}
		// desktop detection
		if (navigator.appVersion.indexOf("Win") > -1){
			return "Windows";
		} else if (navigator.appVersion.indexOf("Mac") > -1) {
			return "Mac";
		} else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) {
			return "Linux";
		} else {
			return "Unknown";
		}
	}
	
	/**
	 * Detects which browser, which language and which operating system is used by the client with feature detection.
	 * @return The information about the user listed above.
	 */
	function collectBrowserMetaInfo() {
		var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
		var isFirefox = typeof InstallTrigger !== 'undefined';
		var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
		var isIE = /*@cc_on!@*/false || !!document.documentMode;
		var isEdge = !isIE && !!window.StyleMedia;
		var isChrome = Boolean(window.chrome);
		
		// get language
		var userLanguage = navigator.language || navigator.userLanguage;
		
		var metaInfo = inspectIT.createEUMElement("metaInfo");
		metaInfo.require("metaInfo");
		
		metaInfo.language = userLanguage;
		metaInfo.device = getOS();
		
		if (isOpera) {
			metaInfo.browser = "Opera";
		} else if (isFirefox) {
			metaInfo.browser = "Firefox";
		} else if (isSafari) {
			metaInfo.browser = "Safari";
		} else if (isIE) {
			metaInfo.browser = "Internet Explorer";
		} else if (isEdge) {
			metaInfo.browser = "Edge";
		} else if (isChrome) {
			metaInfo.browser = "Google Chrome";
		} else {
			metaInfo.browser = "Unknown";
		}
		
		metaInfo.markComplete("metaInfo");
		metaInfo.markRelevant();
	}
	
	return {
		asyncInit : collectBrowserMetaInfo
	}
})());