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
     * Called agter the trace of the given element has finished.
     */
    preElementFinish(element: TraceElement): void;
}

namespace TraceBuilder {

    const callStack: TraceElement[] = [];
    const observers: ITraceObserver[] = [];

    export function getCurrentParent(): TraceElement | null {
        if (callStack.length === 0) {
            return null;
        } else {
            return callStack[callStack.length - 1];
        }
    }

    export function enterChild(element: TraceElement): void {
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

    export function finishChild() {
        // notify trace observers first
        for (const observer of observers) {
            observer.preElementFinish(callStack[callStack.length - 1]);
        }
        callStack.pop();
    }

    export function addTraceObserver(observer: ITraceObserver) {
        observers.push(observer);
    }

    export function removeTraceObserver(observer: ITraceObserver) {
        const index = observers.indexOf(observer);
        if (index !== -1) {
            observers.splice(index, 1);
        }
    }

}