type OSName = "iOS" | "Android" | "Windows" | "Mac" | "Linux" | "Unknown";
type BrowserName = "Opera" | "Firefox" | "Safari" |
    "Internet Explorer" | "Edge" | "Google Chrome" | "Unknown";

/**
 * Record type storing meta information about the users browser.
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo.
 */
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