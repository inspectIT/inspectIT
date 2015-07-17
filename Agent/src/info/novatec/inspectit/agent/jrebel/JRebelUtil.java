package info.novatec.inspectit.agent.jrebel;

import info.novatec.inspectit.agent.analyzer.IClassPoolAnalyzer;
import info.novatec.inspectit.agent.analyzer.IInheritanceAnalyzer;
import info.novatec.inspectit.agent.config.impl.PropertyAccessor.PropertyPathStart;
import info.novatec.inspectit.agent.config.impl.UnregisteredSensorConfig;
import info.novatec.inspectit.communication.data.ParameterContentType;

import org.apache.commons.collections.CollectionUtils;

/**
 * Small utility provide JRebel compatibility.
 * 
 * @author Ivan Senic
 * 
 */
public final class JRebelUtil {

	/**
	 * The method that substitutes the constructor in the jRebel created classes.
	 */
	private static final String JREBEL_CONSTRUCTOR_INIT_METHOD = "__init__";

	/**
	 * Suffix added to every jRebel created class.
	 */
	private static final String JREBEL_SUFFIX_PATTERN = "$$M$*";

	/**
	 * Private constructor for util class.
	 */
	private JRebelUtil() {
	}

	/**
	 * Returns the JRebel {@link UnregisteredSensorConfig} based on the original
	 * {@link UnregisteredSensorConfig}. This new configuration should match all the JRebel enhanced
	 * classes that match the original classes provided by original {@link UnregisteredSensorConfig}
	 * .
	 * 
	 * @param original
	 *            Original {@link UnregisteredSensorConfig}.
	 * @param classPoolAnalyzer
	 *            {@link IClassPoolAnalyzer}
	 * @param inheritanceAnalyzer
	 *            {@link IInheritanceAnalyzer}
	 * @return {@link UnregisteredSensorConfig} that matches the new/updated JRebel classes.
	 */
	public static UnregisteredSensorConfig getJRebelSensorConfiguration(UnregisteredSensorConfig original, IClassPoolAnalyzer classPoolAnalyzer, IInheritanceAnalyzer inheritanceAnalyzer) {
		UnregisteredSensorConfig jRebelSensorConfig = new UnregisteredSensorConfig(classPoolAnalyzer, inheritanceAnalyzer);

		// class name must end with jrebel suffix pattern
		jRebelSensorConfig.setTargetPackageName(original.getTargetPackageName());
		jRebelSensorConfig.setTargetClassName(original.getTargetClassName() + JREBEL_SUFFIX_PATTERN);

		// if we have a constructor then we need to instrument the init method in fact
		if (original.isConstructor()) {
			jRebelSensorConfig.setTargetMethodName(JREBEL_CONSTRUCTOR_INIT_METHOD);
		} else {
			jRebelSensorConfig.setTargetMethodName(original.getTargetMethodName());
		}

		// if we are not ignoring the signature of methods then add the original class name as first
		// parameter
		if (!original.isIgnoreSignature()) {
			jRebelSensorConfig.getParameterTypes().add(original.getTargetClassName());
			jRebelSensorConfig.getParameterTypes().addAll(original.getParameterTypes());
		}

		// constructor is always false
		jRebelSensorConfig.setConstructor(false);

		// copy other stuff
		jRebelSensorConfig.setIgnoreSignature(original.isIgnoreSignature());
		jRebelSensorConfig.setExceptionSensorActivated(original.isExceptionSensorActivated());
		jRebelSensorConfig.setSuperclass(original.isSuperclass());
		jRebelSensorConfig.setInterface(original.isInterface());
		jRebelSensorConfig.setAnnotationClassName(original.getAnnotationClassName());
		jRebelSensorConfig.setModifiers(original.getModifiers());
		jRebelSensorConfig.getSettings().putAll(original.getSettings());
		jRebelSensorConfig.setIgnoreSignature(original.isIgnoreSignature());
		jRebelSensorConfig.setVirtual(true);
		jRebelSensorConfig.setSensorTypeConfig(original.getSensorTypeConfig());

		// if property path is ParameterContentType.PARAM, we must increase the signature position
		// by 1, since all parameters are shifted by 1
		if (CollectionUtils.isNotEmpty(original.getPropertyAccessorList())) {
			for (PropertyPathStart pathStart : original.getPropertyAccessorList()) {
				if (pathStart.getContentType() != ParameterContentType.PARAM) {
					jRebelSensorConfig.getPropertyAccessorList().add(pathStart);
				} else {
					PropertyPathStart newPathStart = new PropertyPathStart();
					newPathStart.setName(pathStart.getName());
					newPathStart.setContentType(pathStart.getContentType());
					newPathStart.setSignaturePosition(pathStart.getSignaturePosition() + 1);
					newPathStart.setPathToContinue(pathStart.getPathToContinue());
					jRebelSensorConfig.getPropertyAccessorList().add(newPathStart);
				}
			}
		}
		jRebelSensorConfig.setPropertyAccess(original.isPropertyAccess());

		return jRebelSensorConfig;
	}
}
