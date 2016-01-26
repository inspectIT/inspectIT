package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;

/**
 * This simple data class is returned as the result from a server side instrumentation of a single
 * class.
 * 
 * @author Ivan Senic
 * 
 */
public class InstrumentationDefinition {

	/**
	 * Fully qualified class name that instrumentation results applies to.
	 */
	private String className;

	/**
	 * {@link SensorInstrumentationPoint} that will be applied within the instrumented byte code.
	 */
	private Collection<MethodInstrumentationConfig> methodInstrumentationConfigs = Collections.emptyList();

	/**
	 * No arg-constructor.
	 */
	public InstrumentationDefinition() {
	}

	/**
	 * @param className
	 *            Fully qualified class name that instrumentation results applies to.
	 */
	public InstrumentationDefinition(String className) {
		this.className = className;
	}

	/**
	 * Gets {@link #className}.
	 * 
	 * @return {@link #className}
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Gets {@link #methodInstrumentationConfigs}.
	 * 
	 * @return {@link #methodInstrumentationConfigs}
	 */
	public Collection<MethodInstrumentationConfig> getMethodInstrumentationConfigs() {
		return methodInstrumentationConfigs;
	}

	/**
	 * Sets {@link #methodInstrumentationConfigs}.
	 * 
	 * @param methodInstrumentationConfigs
	 *            New value for {@link #methodInstrumentationConfigs}
	 */
	public void setMethodInstrumentationConfigs(Collection<MethodInstrumentationConfig> methodInstrumentationConfigs) {
		this.methodInstrumentationConfigs = methodInstrumentationConfigs;
	}

	/**
	 * Defines if instrumentation result is empty in terms that no instrumentation have to be
	 * performed with this instrumentation result.
	 * 
	 * @return If no instrumentation is needed with this result
	 */
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(methodInstrumentationConfigs);
	}

}
