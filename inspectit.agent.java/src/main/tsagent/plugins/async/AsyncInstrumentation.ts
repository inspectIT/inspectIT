///<reference path="./data/TimerExecutionRecord.ts"/>

namespace AsyncInstrumentation {

    export function init() {
        instrumentSetTimeout();
        instrumentSetInterval();
    }

    function instrumentSetTimeout() {
        const originalSetTimeout = window.setTimeout;
        window.setTimeout = function (callback: Function, duration?: number) {
            if (Instrumentation.isEnabled()) {

                const inititatorTimestamp = Util.timestampMS();
                const parent = TraceBuilder.getCurrentParent();

                const instrumentedCallback = function (this: any) {

                    const record = new TimerExecutionRecord();
                    record.setParent(parent);
                    record.initiatorCallTimestamp = inititatorTimestamp;
                    record.functionName = Util.getFunctionName(callback);
                    record.iterationNumber = 0;
                    record.configuredTimeout = duration || 0;

                    const originalThis = this;
                    const originalArgs = arguments;
                    return record.buildTrace(true, function () {
                        callback.apply(originalThis, originalArgs);
                    });
                };
                const modifiedArgs: any[] = Array.prototype.slice.call(arguments);
                modifiedArgs[0] = instrumentedCallback;

                return originalSetTimeout.apply(this, modifiedArgs);
            } else {
                return originalSetTimeout.apply(this, arguments);
            }
        };
    }

    function instrumentSetInterval() {
        const originalSetInterval = window.setInterval;
        window.setInterval = function (callback: Function, duration?: number) {
            if (Instrumentation.isEnabled()) {

                const inititatorTimestamp = Util.timestampMS();
                const parent = TraceBuilder.getCurrentParent();

                let iterationCounter = 0;

                const instrumentedCallback = function (this: any) {
                    const record = new TimerExecutionRecord();
                    record.setParent(parent);
                    record.initiatorCallTimestamp = inititatorTimestamp;
                    record.functionName = Util.getFunctionName(callback);
                    iterationCounter++;
                    record.iterationNumber = iterationCounter;
                    record.configuredTimeout = duration || 0;

                    const originalThis = this;
                    const originalArgs = arguments;
                    return record.buildTrace(true, function () {
                        callback.apply(originalThis, originalArgs);
                    });
                };

                const modifiedArgs = Array.prototype.slice.call(arguments);
                modifiedArgs[0] = instrumentedCallback;

                return originalSetInterval.apply(this, modifiedArgs);
            } else {
                return originalSetInterval.apply(this, arguments);
            }
        };
    }

}
InspectITPlugin.registerPlugin(AsyncInstrumentation);
