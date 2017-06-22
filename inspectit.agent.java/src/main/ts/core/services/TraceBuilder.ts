///<reference path="../data/TraceElement.ts"/>

/**
 * Trace Observer, allows to monitor the trace generation and to possibly inject custom TraceElemetns before/after.
 */
interface ITraceObserver {
    /**
     * Called before the trace of the given element begins.
     */
    preElementBegin(element: TraceElement): void;
    /**
     * Called after the trace of the given element has finished.
     */
    preElementFinish(element: TraceElement): void;
}

/**
 * Service responsible for managing the call stack when building traces.
 * It also allows you to register ITraceObservers in order to monitor changes of variables during traces.
 */
namespace TraceBuilder {

    /**
     * Holds the current callstack.
     */
    const callStack: TraceElement[] = [];

    /**
     * Holds the list of active trace observers.
     */
    const observers: ITraceObserver[] = [];

    /**
     * Returns the current top of callstack.
     */
    export function getCurrentParent(): TraceElement | null {
        if (callStack.length === 0) {
            return null;
        } else {
            return callStack[callStack.length - 1];
        }
    }

    /**
     * Pushes the given element onto the execution stack.
     * If the element is currently handled as a root, it will get the previous stack top set as parent.
     * Otherwise the parent attribute is left unchanged.
     * 
     * @param element the element to push onto the stack.
     */
    export function enterElement(element: TraceElement): void {
        // notify trace observers first
        for (const observer of observers) {
            observer.preElementBegin(element);
        }
        const parent = getCurrentParent();
        // make sure to not override a set parent, e.g. for async traces
        if (parent !== null && element.isRoot()) {
            element.setParent(parent);
        }
        callStack.push(element);
    }

    /**
     * Removes the last element from the execution stack.
     */
    export function finishElement() {
        // notify trace observers first
        for (const observer of observers) {
            observer.preElementFinish(callStack[callStack.length - 1]);
        }
        callStack.pop();
    }

    /**
     * Registers a new trace observer.
     * @param observer the trace observer to register
     */
    export function addTraceObserver(observer: ITraceObserver) {
        observers.push(observer);
    }

    /**
     * Unregisters a trace observer.
     * @param observer the observer to unregister
     */
    export function removeTraceObserver(observer: ITraceObserver) {
        const index = observers.indexOf(observer);
        if (index !== -1) {
            observers.splice(index, 1);
        }
    }

}