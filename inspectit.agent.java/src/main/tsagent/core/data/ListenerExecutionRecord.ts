///<reference path="./FunctionExecutionRecord.ts"/>

class ListenerExecutionRecord extends FunctionExecutionRecord {

    public eventType: string;
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