///<reference path="./BeaconElement.ts"/>
///<reference path="../util/Util.ts"/>
///<reference path="../util/PRNG.ts"/>
///<reference path="../util/SoftReference.ts"/>
///<reference path="../services/TraceBuilder.ts"/>

/**
 * Base class for all BeaconElements which can participate in a trace.
 * TraceElements have a specialized way of treating markRelevant() calls:
 * As soon as an element gets relevant, all its parents are marked as relevant too.
 * 
 * In addition, TraceElements get automaticalyl relevant if their duration exceeds the configured relevancy threshold.
 * 
 */
abstract class TraceElement extends BeaconElement {

    public static readonly UNKNOWN_EXECUTION_ORDER_INDEX = -1;

    private static readonly MINIMUM_DURATION_FOR_RELEVANCY = SETTINGS.relevancyThreshold;

    private static readonly REF_POOL = new SoftReferencesPool<TraceElement>(1000);

    /**
     * Coutner used for assigning execution order indices.
     */
    private static executionOrderIndexCounter = 1;

    /**
     * Execution order index, defining the order of the element in correlation to its siblings under the parent trace element.
     */
    public executionOrderIndex: number = TraceElement.UNKNOWN_EXECUTION_ORDER_INDEX;

    /**
     * Timestamp and duration are optional values as they might not always be known.
     */
    public enterTimestamp?: number;
    private duration?: number;

    /**
     * Generate the ID and use as default for trace and parent.
     */
    private id: IdNumber = Util.PRNG.nextIdNumber();
    private traceId: IdNumber = this.id;
    private parentId: IdNumber = this.id;

    /**
     * Weakly reference the parent for the markRelevant() call propagation.
     */
    private parentRef: SoftReference<TraceElement> = TraceElement.REF_POOL.newReference();

    /**
     * Constructor which optionally allows to specify a traceID to use.
     * @param traceId the traceid to use
     */
    public constructor(traceId?: IdNumber) {
        super();
        if (traceId) {
            this.traceId = traceId;
        }
    }

    public getDuration() {
        return this.duration;
    }

    public getSpanId() {
        return this.id;
    }
    public getTraceId() {
        return this.traceId;
    }

    /**
     * Sets the duration.
     * This will cause the element to become relevant if the duration exceeds the relevancy threshold.
     * 
     * @param durationMS the duration in milliseconds
     */
    public setDuration(durationMS: number) {
        this.duration = durationMS;
        if (durationMS >= TraceElement.MINIMUM_DURATION_FOR_RELEVANCY) {
            this.markRelevant();
        }
    }

    /**
     * Allows to manually specify the parent, for example i ncase of an asynchronous trace element.
     * 
     * @param newParent the new parent element
     * @param assignExecutionOrderIndex flag whether this element should receive an execution order index
     */
    public setParent(newParent: TraceElement | null, assignExecutionOrderIndex = true) {
        this.parentRef.setTarget(newParent);
        if (newParent !== null) {
            this.parentId = newParent.id;
            this.traceId = newParent.traceId;
            if (this.isRelevant()) {
                newParent.markRelevant();
            }
            // assign executionOrderIndex
            if (assignExecutionOrderIndex) {
                this.executionOrderIndex = TraceElement.executionOrderIndexCounter++;
            }
        } else {
            // parent and traceID default to the value of "id"
            this.traceId = this.id;
            this.parentId = this.id;
        }
    }

    public isRoot(): boolean {
        return this.parentId === this.id;
    }

    public markRelevant() {
        if (this.isRelevant()) {
            return;
        }
        super.markRelevant();
        const parent = this.parentRef.getTarget();
        if (parent) {
            parent.markRelevant();
            this.parentRef.setTarget(null);
        }
    }

    /**
     * Utility function for building a synchronous trace. Invoking this function will result in the following actions: 
     *      1. The parent of this element will be set to the current parent held by the traceBuilder(see below). 
     *      2. if storeTimingsFlag is true, the duration of the executionCode-function will be captured and stored in the element 
     *      3. functionToTrace will be executed
     * 
     * @param captureTimings
     *            true, if the timings of the call should be captured
     * @param functionToTrace
     *            the actual functionality, invoking all sub calls of this element
     */
    public buildTrace<T>(captureTimings: boolean, functionToTrace?: () => T) {
        let returnValue: T | undefined;
        TraceBuilder.enterChild(this);
        if (captureTimings) {
            this.require("traceTimings"); // do not send the element before the timing has been completed
            this.enterTimestamp = Util.timestampMS();
        }
        try {
            if (functionToTrace) { // check for null or undefined
                returnValue = functionToTrace();
            }
        } finally {
            if (captureTimings) {
                this.setDuration(Util.timestampMS() - this.enterTimestamp!);
                this.markComplete("traceTimings");
            }
            TraceBuilder.finishChild();
        }
        return returnValue;
    }

    public getDTO(): DTO<this> {
        const dto = super.getDTO() as DTO<any>;
        dto.id = this.id;
        if (this.parentId !== this.id) {
            dto.parentId = this.parentId;
        }
        if (this.traceId !== this.id) {
            dto.traceId = this.traceId;
        }
        if (this.enterTimestamp) {
            dto.enterTimestamp = this.enterTimestamp;
        }
        if (typeof (this.duration) !== "undefined") {
            dto.duration = this.duration;
        }
        if (this.executionOrderIndex !== TraceElement.UNKNOWN_EXECUTION_ORDER_INDEX) {
            dto.executionOrderIndex = this.executionOrderIndex;
        }
        return dto;
    }
}