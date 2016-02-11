package info.novatec.inspectit.agent.config.impl;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.analyzer.IMatcher;
import info.novatec.inspectit.agent.analyzer.impl.AnnotationMatcher;
import info.novatec.inspectit.agent.analyzer.impl.DirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.IndirectMatcher;
import info.novatec.inspectit.agent.analyzer.impl.InterfaceMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ModifierMatcher;
import info.novatec.inspectit.agent.analyzer.impl.SimpleMatchPattern;
import info.novatec.inspectit.agent.analyzer.impl.SuperclassMatcher;
import info.novatec.inspectit.agent.analyzer.impl.ThrowableMatcher;

import java.util.List;
import java.util.regex.Matcher;

import javassist.Modifier;

/**
 * Container for the values of a sensor configuration. It stores all the values defined in a config
 * file for later access.
 * 
 * @author Patrice Bouillet
 */
public class UnregisteredSensorConfig extends AbstractSensorConfig {

	/**
	 * The class pool analyzer.
	 */
	private final IClassPoolAnalyzer classPoolAnalyzer;

	/**
	 * The inheritance analyzer.
	 */
	private final IInheritanceAnalyzer inheritanceAnalyzer;

	/**
	 * If this config defines a superclass.
	 */
	private boolean superclass = false;

	/**
	 * If this config defines an interface.
	 */
	private boolean interf = false;

	/**
	 * If this config is virtual, so patterns are used here.
	 */
	private boolean virtual = false;

	/**
	 * Defines if all methods with the given sensorName are instrumented regardless of the
	 * signatures.
	 */
	private boolean ignoreSignature = false;

	/**
	 * Determines whether the exception sensor is activated.
	 */
	private boolean exceptionSensorActivated = false;

	/**
	 * Integer value defining the modifier. Values are defined in {@link Modifier} class. Default
	 * value is 0, which means that no modifiers were set.
	 */
	private int modifiers = 0;

	/**
	 * The matcher used to compare class name / method name and all method parameters.
	 */
	private IMatcher matcher;

	/**
	 * The sensor type of this configuration. As there can be only one, this is just a direct
	 * reference.
	 */
	private MethodSensorTypeConfig sensorTypeConfig;

	/**
	 * Annotation that is defining what has to be instrumented.
	 */
	private String annotationClassName;

	/**
	 * Default constructor which accepts 2 parameter.
	 * 
	 * @param classPoolAnalyzer
	 *            The class pool analyzer.
	 * @param inheritanceAnalyzer
	 *            The inheritance analyzer.
	 */
	public UnregisteredSensorConfig(IClassPoolAnalyzer classPoolAnalyzer, IInheritanceAnalyzer inheritanceAnalyzer) {
		this.classPoolAnalyzer = classPoolAnalyzer;
		this.inheritanceAnalyzer = inheritanceAnalyzer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetClassName(String targetClassName) {
		super.setTargetClassName(targetClassName);

		if (SimpleMatchPattern.isPattern(targetClassName)) {
			setVirtual(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTargetMethodName(String targetMethodName) {
		super.setTargetMethodName(targetMethodName);

		if (SimpleMatchPattern.isPattern(targetMethodName)) {
			setVirtual(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParameterTypes(List<String> parameterTypes) {
		if (null != parameterTypes) {
			super.setParameterTypes(parameterTypes);

			for (String parameter : parameterTypes) {
				if (SimpleMatchPattern.isPattern(parameter)) {
					setVirtual(true);
				}
			}
		}
	}

	/**
	 * Returns if this sensor configuration is defining a superclass.
	 * 
	 * @return Returns if this is a config for a superclass.
	 */
	public boolean isSuperclass() {
		return superclass;
	}

	/**
	 * Sets if this configuration defines a superclass. Defaults to false.
	 * 
	 * @param superclass
	 *            Setting if this configuration defines a superclass.
	 */
	public void setSuperclass(boolean superclass) {
		this.superclass = superclass;
	}

	/**
	 * Returns if this sensor configuration is defining a superclass.
	 * 
	 * @return Returns if this is a config for a superclass.
	 */
	public boolean isInterface() {
		return interf;
	}

	/**
	 * Sets if this configuration defines an interface. Defaults to false.
	 * 
	 * @param interf
	 *            Setting if this configuration defines an interface.
	 */
	public void setInterface(boolean interf) {
		this.interf = interf;
	}

	/**
	 * Is this configuration just virtual, as the method name or the names of the parameters contain
	 * symbols for pattern matching.
	 * 
	 * @return Returns if this configuration is virtual.
	 */
	public boolean isVirtual() {
		return virtual;
	}

	/**
	 * Setting the virtual state. Defaults to false.
	 * 
	 * @param virtual
	 *            The parameter to define the virtual state.
	 */
	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	/**
	 * Returns if this sensor config ignores the signatures of the methods that fit to the name.
	 * 
	 * @return If this config ignores the method signature.
	 */
	public boolean isIgnoreSignature() {
		return ignoreSignature;
	}

	/**
	 * If this sensor config ignores the signature.
	 * 
	 * @param ignoreSignature
	 *            The boolean value, default is false.
	 */
	public void setIgnoreSignature(boolean ignoreSignature) {
		this.ignoreSignature = ignoreSignature;
	}

	/**
	 * Returns the sensor type configuration.
	 * 
	 * @return The sensor type configuration.
	 */
	public MethodSensorTypeConfig getSensorTypeConfig() {
		return sensorTypeConfig;
	}

	/**
	 * Sets the sensor type configuration.
	 * 
	 * @param sensorTypeConfig
	 *            The sensor type configuration.
	 */
	public void setSensorTypeConfig(MethodSensorTypeConfig sensorTypeConfig) {
		this.sensorTypeConfig = sensorTypeConfig;
	}

	/**
	 * Defines whether exception sensor is activated.
	 * 
	 * @param exceptionSensorActivated
	 *            The flag indicating whether exception sensor is activated or not.
	 */
	public void setExceptionSensorActivated(boolean exceptionSensorActivated) {
		this.exceptionSensorActivated = exceptionSensorActivated;
	}

	/**
	 * Gets {@link #exceptionSensorActivated}.
	 * 
	 * @return {@link #exceptionSensorActivated}
	 */
	public boolean isExceptionSensorActivated() {
		return exceptionSensorActivated;
	}

	/**
	 * Returns the integer value that defines the modifiers of methods to be instrumented. The
	 * values are defined in {@link Modifier} class. Default value is 0, and this means no modifiers
	 * are set.
	 * 
	 * @return the modifiers int value
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets the integer value that defines the modifiers of methods to be instrumented. The values
	 * are defined in {@link Modifier} class. Default value is 0, and this means no modifiers are
	 * set.
	 * 
	 * @param modifiers
	 *            the modifier int value
	 */
	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Returns the matcher which is used by this sensor configuration.
	 * 
	 * @return The {@link Matcher}.
	 */
	public IMatcher getMatcher() {
		return matcher;
	}

	/**
	 * Annotation class name that is defining what needs to be instrumented.
	 * 
	 * @return the annotation class name
	 */
	public String getAnnotationClassName() {
		return annotationClassName;
	}

	/**
	 * Sets the annotation class name. If the annotation is set, the classes and method will be
	 * matched based on this annotation.
	 * 
	 * @param annotationClassName
	 *            the annotation to set
	 */
	public void setAnnotationClassName(String annotationClassName) {
		this.annotationClassName = annotationClassName;
	}

	/**
	 * Completes the whole configuration. Has to be called after all settings are set.
	 */
	public void completeConfiguration() {
		if (!virtual && !superclass && !interf) {
			matcher = new DirectMatcher(classPoolAnalyzer, this);
		} else if (superclass && !interf) {
			matcher = new SuperclassMatcher(inheritanceAnalyzer, classPoolAnalyzer, this);
		} else if (!superclass && interf) {
			matcher = new InterfaceMatcher(inheritanceAnalyzer, classPoolAnalyzer, this);
		} else if (virtual && !superclass && !interf) {
			matcher = new IndirectMatcher(classPoolAnalyzer, this);
		}

		if (null != annotationClassName) {
			matcher = new AnnotationMatcher(inheritanceAnalyzer, classPoolAnalyzer, this, matcher);
		}

		if (exceptionSensorActivated) {
			matcher = new ThrowableMatcher(inheritanceAnalyzer, classPoolAnalyzer, this, matcher);
		}

		if (modifiers != 0) {
			matcher = new ModifierMatcher(classPoolAnalyzer, this, matcher);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return super.toString() + " superclass:" + superclass + " interface:" + interf + " virtual:" + virtual;
	}

}
