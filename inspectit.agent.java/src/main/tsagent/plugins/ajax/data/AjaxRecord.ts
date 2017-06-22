class AjaxRequestRecord extends RequestRecord {

    public status: number;
    public method: string;
    public isAsync: boolean;
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