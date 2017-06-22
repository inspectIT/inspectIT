
/**
 * Interfacce all inspectIT plugins must implement.
 * The plugin can choose which callsbacks it wants to use.
 */
interface InspectITPlugin {

    /**
     * Called synchronously when initializing the agent.
     * This should only be used to initialize instrumentations for performance reasons.
     */
    init?(): void;

    /**
     * Asynchronous inititialization, can be used to perform additional stuff which does not
     * need to be executed before the instruemtned application.
     */
    asyncInit?(): void;

    /**
     * Called before the page is unloaded.
     */
    beforeUnload?(): void;
}

namespace InspectITPlugin {

    /**
     * List of registered plugins.
     */
    const plugins: InspectITPlugin[] = [];

    export function registerPlugin(plugin: InspectITPlugin) {
        plugins.push(plugin);
    }

    export function getRegisteredPlugins() {
        return plugins;
    }
}