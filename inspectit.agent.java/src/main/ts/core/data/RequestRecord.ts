///<reference path="./TraceElement.ts"/>

/**
 * Base record type for any kind of requests issued by the browser.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.AbstractRequest.
 */
abstract class RequestRecord extends TraceElement {

    public url: string;

    public getDTO() {
        const dto = super.getDTO();
        dto.url = this.url;
        return dto;
    }
}