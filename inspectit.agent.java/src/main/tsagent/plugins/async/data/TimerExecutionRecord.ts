class TimerExecutionRecord extends FunctionExecutionRecord {

    public initiatorCallTimestamp: number;
    public configuredTimeout: number;
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