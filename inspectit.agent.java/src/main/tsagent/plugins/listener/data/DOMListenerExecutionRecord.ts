class DOMListenerExecutionRecord extends ListenerExecutionRecord {

    public elementType: string;
    public elementID?: string;

    protected type = "domListenerExecution";

    public getDTO() {
        const dto = super.getDTO();
        dto.elementType = this.elementType;
        if (this.elementID) {
            dto.elementID = this.elementID;
        }
        return dto;
    }
}