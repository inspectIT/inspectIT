
/**
 * Record type storing information about the execution of a JS timer created with setInterval or setTimeout.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.JSTimerExecution.
 */
class TimerExecutionRecord extends FunctionExecutionRecord {

    /**
     * The timestamp when setTimeout or setInterval was called.
     */
    public initiatorCallTimestamp: number;

    /**
     * The timeout or interval this timer was configured for.
     */
    public configuredTimeout: number;

    /**
     * The iteration of this execution.
     * It is zero for setTimeout and increasing starting from one for each setInterval execution.
     */
    public iterationNumber: number;

    protected type = "timerExecution";

    public getDTO() {
        const dto = super.getDTO();
        dto.initiatorCallTimestamp = this.initiatorCallTimestamp;
        dto.configuredTimeout = this.configuredTimeout;
        dto.iterationNumber = this.iterationNumber;
        return dto;
    }

}