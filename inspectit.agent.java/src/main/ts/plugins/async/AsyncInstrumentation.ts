///<reference path="./data/TimerExecutionRecord.ts"/>

namespace AsyncInstrumentation {

    type timerHandler = (...args: any[]) => void;
    type timerFunction = (...args: any[]) => void;

    export function init() {
        window.setTimeout = instrumentTimerFunction(window.setTimeout, false);
        window.setInterval = instrumentTimerFunction(window.setInterval, true);
    }

    /**
     * Instruments setTimeout and setInterval.
     * @param originalFunc teh original setTimeout / setInterval function.
     * @param isIntervalTimer true, if it is setInterval, false for setTimeout
     */
    function instrumentTimerFunction(originalFunc: ((this: any, handler: timerHandler, duration: number) => number), isIntervalTimer: boolean) {
        return function (this: any, handler: timerHandler, duration: number) {
            if (Instrumentation.isEnabled()) {

                const inititatorTimestamp = Util.timestampMS();
                const parent = TraceBuilder.getCurrentParent();

                let iterationCounter = 0;

                const instrumentedCallback = function (this: any) {

                    const record = new TimerExecutionRecord();
                    record.setParent(parent);
                    record.initiatorCallTimestamp = inititatorTimestamp;
                    record.functionName = Util.getFunctionName(handler);
                    record.iterationNumber = isIntervalTimer ? ++iterationCounter : iterationCounter;
                    record.configuredTimeout = duration || 0;

                    const originalThis = this;
                    const originalArgs = arguments;
                    return record.buildTrace(true, function () {
                        handler.apply(originalThis, originalArgs);
                    });
                };
                const modifiedArgs: any[] = Array.prototype.slice.call(arguments);
                modifiedArgs[0] = instrumentedCallback;

                return originalFunc.apply(this, modifiedArgs);
            } else {
                return originalFunc.apply(this, arguments);
            }
        };
    }

}
InspectITPlugin.registerPlugin(AsyncInstrumentation);
