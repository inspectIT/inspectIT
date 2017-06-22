/**
 * 64-bit integer encodded as Hex String.
 */
type IdNumber = string;

interface IDictionary<T> {
    [x: string]: T;
}

/**
 * Type to pass functions around without allowing them being called.
 */
type UncallableFunction = (this: never) => any;

// DTO type which is guaranteed to be serializable correctly via toJSON
type DTOtype = string | number | boolean | GenericDTO | DTOArray;
// tslint:disable-next-line
interface DTOArray extends Array<DTOtype> { }
// tslint:disable-next-line
interface GenericDTO { [k: string]: DTOtype }
/**
 * DTO which is only allowed to have the same properties as T.
 * Useful to avoid typos, unfortunately doesn't work for private members.
 */
type DTO<T> = {[k in keyof T]?: DTOtype};

/**
 * Configuration provided by the environment.
 */
interface IAgentConfiguration {
    eumManagementServer: string;
    activeAgentModules: string;
    relevancyThreshold: number;
    allowListenerInstrumentation: boolean;
    traceid?: IdNumber;
}

const SETTINGS: IAgentConfiguration = (window as any).inspectIT_settings;
// remove the settigns from the global object to avoid modification
delete (window as any).inspectIT_settings;

// add inspectIt to the Window interface
interface InspectIT {
    init?: () => void;
}

// tslint:disable-next-line
interface Window {
    inspectIT: InspectIT;
}
