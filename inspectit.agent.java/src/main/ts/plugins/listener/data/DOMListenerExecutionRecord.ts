
/**
 * Record type storing the execution of a DOM-Listener.
 * A DOM-Listener is an event listener attached to a DOM-Element, for example a click listener on a button.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.JSDomEventListenerExecution.
 */
class DOMListenerExecutionRecord extends ListenerExecutionRecord {

    /**
     * The type of the DOM element on which this listener occured, for example "INPUT" or "A".
     */
    public elementType: string;

    /**
     * The id-attribute of the element on which this event occurred, if available.
     */
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