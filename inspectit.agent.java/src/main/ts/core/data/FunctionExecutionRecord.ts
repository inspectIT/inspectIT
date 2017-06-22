///<reference path="./TraceElement.ts"/>

/**
 * Base record type storing information about the execution of a javascript function.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.JSFunctionExecution.
 */
abstract class FunctionExecutionRecord extends TraceElement {

    /**
     * The name of the function, if available.
     */
    public functionName?: string;

    public getDTO() {
        const dto = super.getDTO();
        if (this.functionName) {
            dto.functionName = this.functionName;
        }
        return dto;
    }
}