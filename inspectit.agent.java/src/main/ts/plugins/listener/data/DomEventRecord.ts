class DomEventRecord extends TraceElement {

    /**
     * Stores the event type, e.g. 'click'.
     */
    public eventType: string;

    /**
     * Stores the current location when this event occurred.
     */
    public baseUrl: string;

    /**
     * True, if this element became relevant because of a selector match.
     * Useful to identify events of interest for session analysis automatically.
     */
    public relevantThroughSelector: boolean = false;

    /**
     * Stores information about the element on which the event occured, e.g. its id or text.
     */
    public elementInfo: IDictionary<string> = {};

    protected type = "domEvent";

    public getDTO() {
        const dto = super.getDTO();
        dto.eventType = this.eventType;
        dto.elementInfo = this.elementInfo;
        dto.baseUrl = this.baseUrl;
        return dto;
    }
}