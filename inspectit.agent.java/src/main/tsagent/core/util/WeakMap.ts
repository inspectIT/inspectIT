/**
 * Weak Map definition.
 * A Weak Map weakly references its keys and strongly references the corresponding values.
 * This allows to "attach" custom values to arbitary objects withoutr causing memory leaks.
 */
interface IWeakMap<K, V> {
    set(key: K, value: V): void;
    get(key: K): V | null;
    delete(key: K): void;
}

/**
 * Global Class, might be available in ES6 environments.
 * @internal
 */
declare let WeakMap: any;

/**
 * Polyfill WeakMap implementation in case ES6 WeakMap is not supported.
 * This polyfil works by adding a hidden attribute (_inspectITData) to objects used as keys.
 * This object the nstores the values for all maps. Each WeakMap instance gets a unique ID, which is used
 * to get the corresponding data from the _inspectITData attribute.
 * 
 * @internal
 */
class WeakMapPolyfill<K extends { _inspectITData?: any[] }, V> implements IWeakMap<K, V> {

    /**
     * Each map gets its unique ID.
     */
    private static mapIdCounter = 0;
    private mapId = WeakMapPolyfill.mapIdCounter++;

    public set(key: K, value: V): void {
        if (!key._inspectITData) {
            const propName: keyof K = "_inspectITData";
            Object.defineProperty(key, propName, { enumerable: false, value: [] });
        }
        key._inspectITData![this.mapId] = value;
    }

    public get(key: K): V | null {
        if (!key._inspectITData) {
            return null;
        }
        return key._inspectITData[this.mapId];
    }

    public delete(key: K): void {
        if (!key._inspectITData) {
            return;
        }
        delete (key._inspectITData)[this.mapId];
    }
}

// The varianble holding the used implementation
let WeakMapImpl: { new <K, V>(): IWeakMap<K, V> };
// select the implementation depending on what is supported.
if (typeof WeakMap !== "undefined") {
    WeakMapImpl = WeakMap;
} else {
    WeakMapImpl = WeakMapPolyfill;
}