
/**
 * Record type for storing information about AJAX-Requests.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.AjaxRequest.
 */
class AjaxRequestRecord extends RequestRecord {

    /**
     * The HTTP status of the request.
     */
    public status: number;

    /**
     * The HTTP-method, e.g. "GET".
     */
    public method: string;

    /**
     * True, if this request was issued asynchronously.
     */
    public isAsync: boolean;

    /**
     * The URL of the browser page when this request was issued.
     */
    public baseUrl: string;

    protected type = "ajaxRequest";

    public getDTO() {
        const dto = super.getDTO();
        dto.status = this.status;
        dto.method = this.method;
        dto.isAsync = this.isAsync;
        dto.baseUrl = this.baseUrl;
        return dto;
    }
}