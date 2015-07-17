package info.novatec.inspectit.agent.analyzer;

/**
 * This interface is used to delegate the analysis and instrumentation of the given bytecode from
 * the javaagent.
 * 
 * @author Patrice Bouillet
 * 
 */
public interface IByteCodeAnalyzer {

	/**
	 * The method returns the instrumented bytecode of the class which is passed to this method as
	 * the first parameter.
	 * 
	 * @param byteCode
	 *            The bytecode, which is necessary to check if a parent class is registered by a
	 *            sensor and needs to be installed for every child class.
	 * @param className
	 *            The class name.
	 * @param classLoader
	 *            The class loader.
	 * @return The instrumented byte code or <code>null</code> if nothing was done (or an error
	 *         happened)
	 */
	byte[] analyzeAndInstrument(byte[] byteCode, String className, ClassLoader classLoader);

}
