///<reference path="../CommonTypes.ts"/>
///<reference path="../services/BeaconService.ts"/>

/**
 * Base class for all elements which (possibly) will be sent via a beacon.
 *
 * BeaconElements autonomously manage the sending of itself: An element is automatically send if its data has been marked as
 * complete and it is a "relevant" element. A element can be marked as relevant using the "markRelevant" method.
 * The data completeness state is managed throught the require(key) and markComplete(key) calls.
 * Calling require(key) causes the sending of the element to be postponed until markComplete(key) has been called
 * with the same key (a unique string). The typical usage pattern is for plugins to invoke element.require("myCoolPlugin")
 * directly after the creation of the element. This will ensure that the plugin can modify the element freely,
 * afterwards to release this lock element.markComplete("myCoolPlugin") is called to allow the element to be sent.
 * 
 * Corresponds to rocks.inspectit.shared.all.communication.data.eum.EUMBeaconElement.
 *
 */
abstract class BeaconElement {
    /**
     * Defines the type name of this element, which is used to identify the type when decoding the beacon.
     */
    protected abstract readonly type: string;

    /**
     * Set holding the symbolic names of missing features, preventing the element from being sent.
     */
    private missingData: IDictionary<1> = {};

    private isRelevantFlag = false;
    private wasSent = false;

    /**
     * Forces the element to not be sent until markComplete has been called with the same key.
     * 
     * @param key a string identifying the data to wait for
     */
    public require(key: string): void {
        this.missingData[key] = 1;
    }

    /**
     * Marks data previously marked with require(key) as complete. If the element is relevant and this was the last incomplete dataset,
     * this call will also result in the element being sent.
     * 
     * @param key a string identifying the data to wait for
     */
    public markComplete(key: string): void {
        delete (this.missingData)[key];
        this.trySend();
    }

    /**
     * Explicitly marks this element as relevant. If the data of this element has been completely collected (no pending markComplete
     * calls), markRelevant() will result in the element being sent.
     */
    public markRelevant() {
        if (!this.isRelevantFlag) {
            this.isRelevantFlag = true;
            this.trySend();
        }
    }

    public isRelevant() {
        return this.isRelevantFlag;
    }

    /**
     * Builds a DTO representation of this element.
     * This for examples excludes values which are the default and therefore do not need to be sent.
     */
    public getDTO(): DTO<this> {
        const dto: DTO<this> = {};
        (dto as DTO<any>).type = this.type;
        return dto;
    }

    private isComplete(): boolean {
        for (const k in this.missingData) {
            return false; // reached when missingData has at least one entry
        }
        return true;
    }

    private trySend(): void {
        if (this.isRelevantFlag && this.isComplete() && !this.wasSent) {
            BeaconService.send(this.getDTO());
            this.wasSent = true;
        }
    }
}
