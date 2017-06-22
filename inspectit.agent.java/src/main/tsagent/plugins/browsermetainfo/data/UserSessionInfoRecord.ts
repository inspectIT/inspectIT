type OSName = "iOS" | "Android" | "Windows" | "Mac" | "Linux" | "Unknown";
type BrowserName = "Opera" | "Firefox" | "Safari" |
    "Internet Explorer" | "Edge" | "Google Chrome" | "Unknown";

class UserSessionInfoRecord extends BeaconElement {

    public browser: BrowserName;
    public device: OSName;
    public language: string;

    public type = "metaInfo";

    public getDTO() {
        const dto = super.getDTO();
        dto.browser = this.browser;
        dto.device = this.device;
        dto.language = this.language;
        return dto;
    }

}