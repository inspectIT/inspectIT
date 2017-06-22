///<reference path="../util/Util.ts"/>
///<reference path="../util/PRNG.ts"/>
///<reference path="../data/BeaconElement.ts"/>

/**
 * Service responsible for collecting the data and sending the actual beacons. This service should not be accessed externally, instead
 * the sending shhould be managed through the methods provided with the EUMElements.
 * 
 * This service is also responsible for the management of the sessionID and the tabID. If the corresponding IDs are not assigned, it
 * will request new IDs by sending special values to the server.
 * 
 * @internal
 */
namespace BeaconService {

    /**
     * The Beacon structure sent back to inspectIT.
     * Corresponds to rocks.inspectit.shared.all.communication.data.eum.Beacon.
     */
    interface IBeacon {
        tabID: IdNumber;
        sessionID: IdNumber;
        activeAgentModules: string;
        data?: DTO<BeaconElement>[];
    }

    /**
     * The cookie used for holding the inspectit session ID.
     */
    const SESSION_COOKIE_NAME = "inspectIT_session";

    /**
     * The URL to which the service wil ltry to send the beacons.
     */
    const BEACON_URL = SETTINGS.eumManagementServer;

    /**
     * The list of active modules in this agent.
     * Stored as it is also snet back to the server.
     */
    const ACTIVE_MODULES = SETTINGS.activeAgentModules;

    /**
     * Constant holding whether navigator.sendBeacon is supported.
     */
    const BEACON_API_SUPPORTED = (typeof navigator.sendBeacon !== "undefined");

    /**
     * The special value for sessionID and tabID to be sent in beacons for requesting a new ID.
     */
    const REQUEST_NEW_ID_MARKER = "-1";

    /**
     * After this amount of time of inactivity (no new data added to send), the beacon will be sent.
     */
    const TIME_WINDOW: number = 2500;

    /**
     * A element is guaranteed to be no longer buffered than this duration. This means if elements are added regularly to the queue, a
     * beacon will be sent at this frequency.
     */
    const MAX_TIME_WINDOW = 15000;

    // Failure handling
    /**
     * The inital backoff in case the beacon could not be sent in milliseconds.
     */
    const INITIAL_BACKOFF = 5 * 1000;
    /**
     * The maximum backoff after repeated failures in milliseconds.
     */
    const MAX_BACKOFF = 60 * 1000;
    // how often do we retry in case of errors until we give up?
    const MAX_FAILURES_IN_A_ROW = 8; // == about five minutes

    let dataToSend: DTO<BeaconElement>[] = [];

    let firstDataInQueueTimestamp: number | null = null;
    let lastDataInQueueTimestamp: number | null = null;

    // counts the number of consecutive sends which failed
    let failuresInARow = 0;

    // do not send until this timestamp is reached
    // used to prevent spamming in case of long-time entwork failures
    let backoffTimestamp = 0;

    /**
     * Flag whether beaconing has been disabled due to a failure.
     */
    let isDisabled = false;

    /**
     * Variables holding the sessionID and teh tabID assigned by the Java Agent.
     */
    let sessionID: IdNumber = REQUEST_NEW_ID_MARKER;
    let tabID: IdNumber = REQUEST_NEW_ID_MARKER;

    /**
     * This flag makes sure that only one beacon is sent at a time. It is true if we still are awaiting the response of a previously
     * sent beacon.
     */
    let awaitingResponse: boolean = false;

    /**
     * Regular timer for checking the queue and possibly sending a beacon.
     */
    let sendTimer: number | null = null;

    export function init() {
        Instrumentation.runWithout(() => {
            sendTimer = setInterval(sendConditionChecker, 1000);
        });
        const sessionCookie: IdNumber | null = Util.getCookie(SESSION_COOKIE_NAME);
        if (sessionCookie === null) {
            // send an empty beacon immediately to request a new session ID
            // - it seems like this page has been cached or the JS agent has been injected manually
            forceBeaconSend(false);
        } else {
            // session cookie available- read it
            sessionID = sessionCookie;
            // reset the PRNG based on the sessionID (guaranteed source of randomness)
            Util.PRNG.setSeed(sessionID + Util.timestampMS());
        }
    }

    /**
     * Disables the service in case of some kind of failure.
     * This makes the service just ignore queued data.
     */
    function disableBeaconService() {
        isDisabled = true;
        if (sendTimer) {
            clearInterval(sendTimer);
            sendTimer = null;
        }
        dataToSend = [];
    }

    /**
     * A timer executed every second to check the conditions for sending a new beacon. If the conditions are met, a beacon is sent.
     */
    function sendConditionChecker() {
        if (!awaitingResponse && dataToSend.length > 0) {
            const time = Util.timestampMS();
            if (time >= backoffTimestamp && ((time - firstDataInQueueTimestamp!) >= MAX_TIME_WINDOW || (time - lastDataInQueueTimestamp!) >= TIME_WINDOW)) {
                forceBeaconSend(false);
            }
        }
    }

    /**
     * Adds an element to the send queue and updates the timing Binformation for the sending policy.
     * 
     * @param element
     *            the element to send
     */
    export function send<T extends BeaconElement>(element: DTO<T>) {
        if (isDisabled) {
            return;
        }
        dataToSend.push(element as any);
        const time = Util.timestampMS();
        lastDataInQueueTimestamp = time;
        // are we the first element in the queue?
        if (dataToSend.length === 1) {
            firstDataInQueueTimestamp = time;
        }
    }

    /**
     * Sends a beacon, ignoring whether the conditions are met.
     * 
     * @param useBeaconAPI  true, if the beacon API should be used. This means that no feedback regarding success / failure is given!
     */
    function forceBeaconSend(useBeaconAPI: boolean) {
        // disable instrumentation as we interact with APIs
        Instrumentation.runWithout(function () {
            const beaconObj: IBeacon = {
                tabID,
                sessionID,
                activeAgentModules: ACTIVE_MODULES
            };

            if (sessionID === REQUEST_NEW_ID_MARKER) {
                // we have to request a new session ID, as this page was probably cached.
                // we therefore will send an empty beacon instead due to a possible race condition
                // across multiple tabs within the same session
                beaconObj.data = [];
            } else {
                beaconObj.data = dataToSend;
                dataToSend = [];
                lastDataInQueueTimestamp = null;
                firstDataInQueueTimestamp = null;
            }

            // use the beacon API if we do not care about the response and network failures
            let beaconApiSuccess = false;
            if (useBeaconAPI && BEACON_API_SUPPORTED && sessionID !== REQUEST_NEW_ID_MARKER && tabID !== REQUEST_NEW_ID_MARKER) {
                beaconApiSuccess = navigator.sendBeacon(BEACON_URL, JSON.stringify(beaconObj));
            }
            if (!beaconApiSuccess) {
                const xhrPost = new XMLHttpRequest();
                xhrPost.open("POST", BEACON_URL, true);
                xhrPost.setRequestHeader("Content-Type", "application/json");
                let responseHandled = false;
                const responseHandler = Instrumentation.disableFor(function () {
                    // assert that only one of the three listeners is run
                    if (!responseHandled) {
                        responseHandled = true;
                        if (xhrPost.status === 200) {
                            failuresInARow = 0;

                            const responseObj = JSON.parse(xhrPost.responseText);

                            if (tabID === REQUEST_NEW_ID_MARKER) {
                                tabID = responseObj.tabID;
                                // reset the PRNG based on the tabID, the best source of randomness
                                Util.PRNG.setSeed(tabID + Util.timestampMS());
                            }
                            if (sessionID === REQUEST_NEW_ID_MARKER) {
                                const sessionCookie = Util.getCookie(SESSION_COOKIE_NAME);
                                if (sessionCookie !== null) {
                                    // ignore the received id and instead use the stored one
                                    sessionID = sessionCookie;
                                    awaitingResponse = false;
                                } else {
                                    // possible race condition between multiple tabs here
                                    // we just wait a moment and then take the winner of this race condition
                                    document.cookie = SESSION_COOKIE_NAME + "=" + responseObj.sessionID + "; path=/";
                                    setTimeout(function () {
                                        Instrumentation.runWithout(function () {
                                            sessionID = Util.getCookie(SESSION_COOKIE_NAME)!;
                                            awaitingResponse = false;
                                        });
                                        awaitingResponse = false;
                                    }, 200);
                                }
                            } else {
                                awaitingResponse = false;
                            }
                        } else {
                            failuresInARow++;
                            if (failuresInARow >= MAX_FAILURES_IN_A_ROW) {
                                disableBeaconService(); // give up ;(
                            } else {
                                // retry after backoff
                                backoffTimestamp = Util.timestampMS() + Math.min(MAX_BACKOFF, Math.pow(2, failuresInARow - 1) * INITIAL_BACKOFF);
                                if (beaconObj.data) {
                                    for (const element of beaconObj.data) {
                                        send(element);
                                    }
                                }
                            }
                            awaitingResponse = false;
                        }
                    }
                });
                xhrPost.addEventListener("load", responseHandler);
                xhrPost.addEventListener("error", responseHandler);
                xhrPost.addEventListener("abort", responseHandler);
                xhrPost.send(JSON.stringify(beaconObj));
                awaitingResponse = true;
            }
        });
    }

    /**
     * Try a final send before leaving the page.
     */
    export function beforeUnload() {
        // cancel timer
        Instrumentation.runWithout(function () {
            if (sendTimer) {
                clearInterval(sendTimer);
            }
        });
        if (dataToSend.length > 0) {
            forceBeaconSend(true);
        }
    }
}
