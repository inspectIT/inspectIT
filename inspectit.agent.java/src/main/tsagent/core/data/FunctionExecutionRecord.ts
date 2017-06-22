///<reference path="./TraceElement.ts"/>

abstract class FunctionExecutionRecord extends TraceElement {

    public functionName?: string;

    public getDTO() {
        const dto = super.getDTO();
        if (this.functionName) {
            dto.functionName = this.functionName;
        }
        return dto;
    }
}