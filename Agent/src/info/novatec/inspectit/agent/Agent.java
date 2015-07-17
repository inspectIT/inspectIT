package info.novatec.inspectit.agent;

/**
 * Another agent which is there for the sole purpose of classloading issues. The JavaAgent class can
 * be loaded via the AppClassLoader (and all of the classes of the fields and method signatures
 * etc.). Thus we need another class to be loaded in the bootstrap classloader. And as there is no
 * possibility in getting the bootstrap classloader, we need another class not defined nearly
 * anywhere in the JavaAgent class and loaded after the initialization is complete. Tada.
 * 
 * @author Patrice Bouillet
 * 
 */
public final class Agent {

	/**
	 * The real agent implementation to point to.
	 */
	public static IAgent agent; // NOPMD NOCHK

	/**
	 * Private constructor to prevent instantiation.
	 */
	private Agent() {
	}

}
