interface IAttribDescriptor {
    name: string;
    storagePrefix: string | null;
}

class EventSelector {

    public readonly events: string[];
    public readonly markAlwaysAsRelevant: boolean;
    public readonly ancestorLevelsToCheck: number;

    private readonly storagePrefix: string;
    private readonly selector: string;
    private readonly attributesToExtract: IAttribDescriptor[];

    public constructor(config: [string, string, string, boolean, number]) {
        this.events = config[0].split(",");
        this.selector = config[1];
        this.attributesToExtract = this.parseAttribDescriptors(config[2]);
        this.markAlwaysAsRelevant = config[3];
        this.ancestorLevelsToCheck = config[4];
    }

    public findMatch(mostInnerElem: Node): Element | null {
        let currentAncestorLevel = 0;
        let currentElem: Node | null = mostInnerElem;
        do {
            if (Util.isDomElement(currentElem) && Util.elementMatchesSelector(currentElem as Element, this.selector)) {
                return currentElem as Element;
            }
            currentAncestorLevel++;
            currentElem = currentElem.parentNode;
        } while (currentElem != null && (currentAncestorLevel <= this.ancestorLevelsToCheck || this.ancestorLevelsToCheck === -1));
        return null;
    }

    public extractAttributes(elem: Element, storage: IDictionary<string>) {
        for (const attribDescr of this.attributesToExtract) {
            const fullName = (attribDescr.storagePrefix ? attribDescr.storagePrefix + "." : "") + attribDescr.name;
            if (!(fullName in storage)) {
                if (attribDescr.name === "$label") {
                    const label = this.getLabelText(elem);
                    if (label) {
                        storage[fullName] = label;
                    }
                } else {
                    if (elem.hasAttribute(attribDescr.name)) {
                        let htmlAttr = elem.getAttribute(attribDescr.name);
                        if (htmlAttr == null) {
                            htmlAttr = "";
                        }
                        storage[fullName] = htmlAttr.toString();
                    } else if ((elem as any)[attribDescr.name] !== undefined && (elem as any)[attribDescr.name] !== "") {
                        storage[fullName] = (elem as any)[attribDescr.name].toString();
                    }
                }
            }
        }
    }

    private parseAttribDescriptors(commaSeparatedList: string) {
        const result: IAttribDescriptor[] = [];
        for (const encoded of commaSeparatedList.split(",")) {
            const splitted = encoded.split(".");
            if (splitted.length === 1) {
                result.push({ name : splitted[0], storagePrefix : null});
            } else {
                result.push({ name : splitted[1], storagePrefix : splitted[0]});
            }
        }
        return result;
    }

    private getLabelText(elem: any) {
        if ((typeof elem.parentElement) === "object" && (typeof elem.parentElement.getElementsByTagName) === "function") {
            const parent = (elem as Node).parentElement;
            if (parent !== null) {
                const labels = parent.getElementsByTagName("LABEL");
                for (let i = 0; i < labels.length; i++) {
                    const label = labels.item(i);
                    if (label.getAttribute("for") === elem.id) {
                        return (label as any).innerText || (label as any).textContent;
                    }
                }
            }
        }
        return null;
    }

}