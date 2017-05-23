/**
 * Utility module.
 */
namespace Util {

    /**
     * @return the timestamp in milliseconds since the epoche
     */
    export let timestampMS: () => number;
    if (window.performance && performance.timing.navigationStart !== 0) {
        timestampMS = function () {
            return performance.now() + performance.timing.navigationStart;
        };
    } else {
        timestampMS = Date.now.bind(Date); // Date.now is a function
    }

    /**
     * Fetches a cookie value based on the given cookie name.
     * 
     * @param key the name of the cookie
     * @return the value of thecookie or null if the cookie is not set
     */
    export function getCookie(key: string): string | null {
        const name = key + "=";
        const ca = document.cookie.split(";");
        for (let c of ca) {
            while (c.charAt(0) === " ") {
                c = c.substring(1);
            }
            if (c.indexOf(name) === 0) {
                return c.substring(name.length, c.length);
            }
        }
        return null;
    }

    /**
     * Deletes the given cookie immediately.
     * @param key the name of the cookie to delete.
     */
    export function deleteCookie(key: string) {
        document.cookie = key + "=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;";
    }

    /**
     * Returns the name of the given function or undefined if this is not possible.
     * Anonymous function will yield "<anonymous>" as result.
     * 
     * @param func the function of which the name should be queried
     */
    export function getFunctionName(func: Function & { name?: string }): string | undefined {
        if (typeof func === "function") {
            if (func.hasOwnProperty("name")) { // ES 6
                if (func.name === "") {
                    return "<anonymous>";
                } else {
                    return func.name;
                }
            }
        }
        return undefined;
    }

    const matchesFunc = Element.prototype.matches || Element.prototype.msMatchesSelector ||
        Element.prototype.webkitMatchesSelector || function () { return false; };

    export function elementMatchesSelector(element: Element, selector: string): boolean {
        try {
            return matchesFunc.call(element, selector);
        } catch (e) {
            return false;
        }
    }

    /**
     * Taken from http://stackoverflow.com/questions/384286/javascript-isdom-how-do-you-check-if-a-javascript-object-is-a-dom-object.
     */
    export function isDomElement(o: any) {
        return (typeof HTMLElement === "object" ? o instanceof HTMLElement : // DOM2
            o && typeof o === "object" && o !== null && o.nodeType === 1 && typeof o.nodeName === "string");
    }
}