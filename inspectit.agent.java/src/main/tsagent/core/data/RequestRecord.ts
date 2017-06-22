///<reference path="./TraceElement.ts"/>

abstract class RequestRecord extends TraceElement {

    public url: string;

    public getDTO() {
        const dto = super.getDTO();
        dto.url = this.url;
        return dto;
    }
}