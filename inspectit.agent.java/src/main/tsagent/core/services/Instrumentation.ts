///<reference path="../util/WeakMap.ts"/>
///<reference path="../CommonTypes.ts"/>

/**
 * Instrumentation definition.
 */
interface IListenerInstrumentation {
    /**
     * Predicate-Method telling whether the instrumentation should be applied on the given attachment Target of the event and the given event type.
     * 
     * @param attachmenTarget the element onto which the listener to check for was attached
     * @param eventType the type name of the event, e.g. "click"
     * 
     * @return true, if this listener instrumentation shall be executed on the given element and event type
     */
    shouldInstrument(attachmenTarget: EventTarget, eventType: string): boolean;

    /**
     * Called when an instrumented listener is executed.
     * 
     * @param event the event for which the listener is executed.
     * @param originalCallback the callback function of the event listener
     * @param executeOriginalListener the method for executing the original listener (synchronously)
     */
    instrument(event: Event, originalCallback: UncallableFunction, executeOriginalListener: () => any): void;
}

/**
 * Instrumentation module.
 * 
 * Allows to disable the instrumentation for a scope. Disabling instrumentation is necessary, because plugins also make use of
 * instrumented functions, such as setTimeout for example. Also provides the infrastructure for instrumenting listeners.
 * 
 */
namespace Instrumentation {

    let instrumentationStackDepth = 0;

    export function isEnabled(): boolean {
        return (instrumentationStackDepth === 0);
    }

    export function disable() {
        instrumentationStackDepth++;
    }

    export function reenable() {
        instrumentationStackDepth--;
    }

    /**
     * Disables the instrumentation, runs the given function and afterwards reenables the instrumentation.
     * 
     * @param func
     *            the function to execute without isntrumentation.
     * @returns the return value of func
     */
    export function runWithout<T>(func: (this: void) => T): T {
        disable();
        const result = func();
        reenable();
        return result;
    }

    /**
     * Wraps the given function in a function which executes exactly the same task but with instrumentation disabled.
     * 
     * @param func
     *            the function to wrap
     * @returns the wrapper for func which has instrumentation disabled.
     */
    export function disableFor<T extends Function>(func: T): T {
        return (function (this: any) {
            disable();
            const result = func.apply(this, arguments);
            reenable();
            return result;
        }) as any;
    }

    /**
     * Marker to prevent instrumentation of already instrumented function
     */
    type InstrumentedCallback = Function & { _is_inspectIT_function?: true };

    /**
     * List of all registered listener instrumentations.
     */
    const listenerInstrumentations: IListenerInstrumentation[] = [];

    /**
     * Lookup table for getting the original callback belonging to aninstrumented one.
     */
    const callbackInstrumentations = new WeakMapImpl<Function, InstrumentedCallback>();

    /**
     * Cache for querying / storing the instrumentation applied to a given element and event type.
     */
    const activeEventInstrumentations = new WeakMapImpl<EventTarget, IDictionary<IListenerInstrumentation[]>>();

    /**
     * Registers a listener instrumentation.
     * 
     * @param instrumentation the instrumentation ro register.
     */
    export function addListenerInstrumentation(instrumentation: IListenerInstrumentation) {
        listenerInstrumentations.push(instrumentation);
    }

    /**
     * Checks if registered listener instrumentations apply on the given element and listener.
     * This check is only done once and afterwards cached for the element.
     * 
     * @param attachmentTarget the event target to check, the lement to which the listener was attached
     * @param eventType the type of the event, e.g. "click"
     */
    function getActiveEventInstrumentations(attachmentTarget: EventTarget, eventType: string): IListenerInstrumentation[] {
        let dict = activeEventInstrumentations.get(attachmentTarget);
        if (!dict) {
            dict = {};
            activeEventInstrumentations.set(attachmentTarget, dict);
        }
        if (!(eventType in dict)) {
            // never checked before, check available instrumentations
            dict[eventType] = [];
            for (const instr of listenerInstrumentations) {
                if (instr.shouldInstrument(attachmentTarget, eventType)) {
                    dict[eventType].push(instr);
                }
            }
        }
        return dict[eventType];
    }

    /**
     * Instruments the given event-listener callback.
     * Does nothing if the callback is already instrumented.
     * 
     * @param originalCallback the callback to instrument
     * @return the instrumented callback
     */
    export function instrumentEventCallback<T extends Function>(originalCallback: T): T {
        // check if listener instrumentation is enabled
        if (!SETTINGS.allowListenerInstrumentation) {
            return originalCallback;
        }
        // prevent instrumentation of instrumentation
        if ((originalCallback as InstrumentedCallback)._is_inspectIT_function) {
            return originalCallback;
        }
        // check if already instrumented
        let instrumentedCallback = callbackInstrumentations.get(originalCallback);
        if (!instrumentedCallback) {
            instrumentedCallback = function (this: any, event: Event) {
                // sanity check if parameters are available
                if (!event) {
                    return originalCallback.apply(this, arguments);
                } else {
                    let returnValue: any;

                    const filteredInstrumentations = getActiveEventInstrumentations(event.currentTarget, event.type);
                    const originalArgs = arguments;
                    const originalThis = this;

                    let currentListenerIndex = -1;
                    // recursive iterator
                    // when this function is invoked, it either calls the next instrumentation or the actual callback
                    // if all instrumentations were executed
                    const continueFunc = function () {
                        currentListenerIndex++;
                        if (currentListenerIndex < filteredInstrumentations.length) {
                            // call the next listener
                            filteredInstrumentations[currentListenerIndex].instrument(event, originalCallback as any, continueFunc);
                        } else {
                            // finally call the original callback and keep its return value
                            returnValue = originalCallback.apply(originalThis, originalArgs);
                        }
                        return returnValue;
                    };

                    // start the call chain of calling first all instrumentations and finally calling the original callback
                    continueFunc();

                    return returnValue;
                }
            } as any;
            instrumentedCallback!._is_inspectIT_function = true;
            callbackInstrumentations.set(originalCallback, instrumentedCallback!);
        }
        return instrumentedCallback! as T;
    }

    export function initListenerInstrumentation() {

        if (!SETTINGS.allowListenerInstrumentation) {
            return;
        }

        if ((typeof EventTarget !== "undefined") && EventTarget.prototype.hasOwnProperty("addEventListener")
            && EventTarget.prototype.hasOwnProperty("removeEventListener")) {
            // Chrome, Safari, Firefox, Edge
            instrumentForPrototype(EventTarget.prototype);
        } else {
            // IE11 solution
            instrumentForPrototype(Node.prototype);
            instrumentForPrototype(XMLHttpRequest.prototype);
            instrumentForPrototype(window);
        }

        function instrumentForPrototype(prototypeToInstrument: EventTarget) {

            const uninstrumentedAddEventListener = prototypeToInstrument.addEventListener;
            (prototypeToInstrument as any).addEventListener = function (this: EventTarget, type: string, callback: Function) {

                // check instrumentation disabled flag
                if (!isEnabled()) {
                    return uninstrumentedAddEventListener.apply(this, arguments);
                }

                // fetch the existing instrumented callback or create a new one
                // every callback is guaranteed to be only instrumented once
                const instrumentedCallback = instrumentEventCallback(callback);

                // Attach the instrumented listener
                const modifiedArgs = Array.prototype.slice.call(arguments);
                modifiedArgs[1] = instrumentedCallback;
                return uninstrumentedAddEventListener.apply(this, modifiedArgs);
            };

            const uninstrumentedRemoveEventListener = prototypeToInstrument.removeEventListener;

            (prototypeToInstrument as any).removeEventListener = function (this: EventTarget, type: string, callback: Function) {

                // check instrumentation disabled flag
                if (!isEnabled()) {
                    return uninstrumentedRemoveEventListener.apply(this, arguments);
                }

                const instrumentedCallback = callbackInstrumentations.get(callback);

                if (instrumentedCallback) {
                    const modifiedArgs = Array.prototype.slice.call(arguments);
                    modifiedArgs[1] = instrumentedCallback;
                    return uninstrumentedRemoveEventListener.apply(this, modifiedArgs);
                } else {
                    return uninstrumentedRemoveEventListener.apply(this, arguments);
                }
            };
        }
    }
}