class ResourceLoadRequestRecord extends RequestRecord {

    public initiatorType: string;
    public transferSize: string;
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