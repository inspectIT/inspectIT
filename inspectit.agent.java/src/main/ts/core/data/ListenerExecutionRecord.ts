///<reference path="./FunctionExecutionRecord.ts"/>

/**
 * Base record type storing information about a JS event listener being executed.
 * An eventlsitener function has either been attached using addEventListener or using an inline callback, like "onload".
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.JSListenerExecution.
 */
class ListenerExecutionRecord extends FunctionExecutionRecord {

    /**
     * The type of the event, e.g. 'click' or 'load'.
     */
    public eventType: string;

    /**
     * True if this event is async.
     * Syncronous events are a special case, for example the modification of the location.href attribute triggers some events syncronously.
     */
    public isAsyncEvent: boolean = true;

    protected readonly type = "listenerExecution";

    public getDTO() {
        const dto = super.getDTO();
        dto.eventType = this.eventType;
        // do not send default values
        if (this.isAsyncEvent !== true) {
            dto.isAsyncEvent = this.isAsyncEvent;
        }
        return dto;
    }
}