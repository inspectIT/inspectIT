/**
 * Record type storing information about a resource-load request issued by the browser.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest.
 */
class ResourceLoadRequestRecord extends RequestRecord {

    /**
     * The initator tag, for example "IMG" or "SCRIPT".
     * Can be "Unknown" in case it is not known.
     */
    public initiatorType: string;

    /**
     * The size of the resource in bytes.
     */
    public transferSize: string;

    /**
     * The URL of the browser page when this request was issued.
     */
    public baseUrl: string;

    protected type = "resourceLoadRequest";

    public getDTO() {
        const dto = super.getDTO();
        dto.initiatorType = this.initiatorType;
        dto.transferSize = this.transferSize;
        dto.baseUrl = this.baseUrl;
        return dto;
    }
}