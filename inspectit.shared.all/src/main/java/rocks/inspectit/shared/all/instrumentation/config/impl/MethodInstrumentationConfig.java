package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.instrumentation.classcache.MethodType;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationConfig;
import rocks.inspectit.shared.all.instrumentation.config.IMethodInstrumentationPoint;

/**
 * Method instrumentation configuration holds all needed information for one method instrumentation.
 *
 * @author Ivan Senic
 *
 */
public class MethodInstrumentationConfig implements IMethodInstrumentationConfig {

	/**
	 * The FQN name of the target class.
	 */
	private String targetClassFqn;

	/**
	 * The name of the target method.
	 */
	private String targetMethodName;

	/**
	 * The return type of the method.
	 */
	private String returnType;

	/**
	 * The parameter types (as the fully qualified name) of the method.
	 */
	private List<String> parameterTypes;

	/**
	 * {@link SensorInstrumentationPoint} for this {@link MethodInstrumentationConfig}. Can be only one
	 * per method.
	 */
	private SensorInstrumentationPoint sensorInstrumentationPoint;

	/**
	 * Collection of functional instrumentation belonging to this method instrumentation.
	 */
	private Set<SpecialInstrumentationPoint> functionalInstrumentations;

	/**
	 * No-args constructor.
	 */
	public MethodInstrumentationConfig() {
	}

	/**
	 * Constructs the method instrumentation config based on the given {@link MethodType}.
	 *
	 * @param methodType
	 *            {@link MethodType} from which to copy the method properties.
	 */
	public MethodInstrumentationConfig(MethodType methodType) {
		if (null == methodType) {
			throw new IllegalArgumentException("Method instrumentation config can not be created when passed method type is null.");
		}

		this.setTargetClassFqn(methodType.getClassOrInterfaceType().getFQN());
		this.setTargetMethodName(methodType.getName());
		this.setReturnType(methodType.getReturnType());
		this.setParameterTypes(new ArrayList<String>(methodType.getParameters()));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasInstrumentationPoints() {
		return sensorInstrumentationPoint != null || CollectionUtils.isNotEmpty(functionalInstrumentations);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<IMethodInstrumentationPoint> getAllInstrumentationPoints() {
		if (!hasInstrumentationPoints()) {
			return Collections.emptyList();
		}

		List<IMethodInstrumentationPoint> instrumentationPoints = new ArrayList<IMethodInstrumentationPoint>(1);
		if (null != sensorInstrumentationPoint) {
			instrumentationPoints.add(sensorInstrumentationPoint);
		}
		if (CollectionUtils.isNotEmpty(functionalInstrumentations)) {
			instrumentationPoints.addAll(functionalInstrumentations);
		}

		return instrumentationPoints;
	}

	/**
	 * Gets {@link #targetClassFqn}.
	 *
	 * @return {@link #targetClassFqn}
	 */
	public String getTargetClassFqn() {
		return targetClassFqn;
	}

	/**
	 * Sets {@link #targetClassFqn}.
	 *
	 * @param targetClassFqn
	 *            New value for {@link #targetClassFqn}
	 */
	public final void setTargetClassFqn(String targetClassFqn) {
		this.targetClassFqn = targetClassFqn;
	}

	/**
	 * Gets {@link #targetMethodName}.
	 *
	 * @return {@link #targetMethodName}
	 */
	public String getTargetMethodName() {
		return targetMethodName;
	}

	/**
	 * Sets {@link #targetMethodName}.
	 *
	 * @param targetMethodName
	 *            New value for {@link #targetMethodName}
	 */
	public final void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}

	/**
	 * Gets {@link #returnType}.
	 *
	 * @return {@link #returnType}
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * Sets {@link #returnType}.
	 *
	 * @param returnType
	 *            New value for {@link #returnType}
	 */
	public final void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Gets {@link #parameterTypes}.
	 *
	 * @return {@link #parameterTypes}
	 */
	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * Sets {@link #parameterTypes}.
	 *
	 * @param parameterTypes
	 *            New value for {@link #parameterTypes}
	 */
	public final void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	/**
	 * Gets {@link #sensorInstrumentationPoint}.
	 *
	 * @return {@link #sensorInstrumentationPoint}
	 */
	public SensorInstrumentationPoint getSensorInstrumentationPoint() {
		return sensorInstrumentationPoint;
	}

	/**
	 * Sets {@link #sensorInstrumentationPoint}.
	 *
	 * @param registeredSensorConfig
	 *            New value for {@link #sensorInstrumentationPoint}
	 */
	public void setSensorInstrumentationPoint(SensorInstrumentationPoint registeredSensorConfig) {
		this.sensorInstrumentationPoint = registeredSensorConfig;
	}

	/**
	 * Gets {@link #functionalInstrumentations}.
	 *
	 * @return {@link #functionalInstrumentations}
	 */
	public Collection<SpecialInstrumentationPoint> getFunctionalInstrumentations() {
		return functionalInstrumentations;
	}

	/**
	 * Adds functional instrumentation point.
	 *
	 * @param functionalInstrumentation
	 *            {@link SpecialInstrumentationPoint}
	 */
	public void addFunctionalInstrumentation(SpecialInstrumentationPoint functionalInstrumentation) {
		if (null == functionalInstrumentations) {
			functionalInstrumentations = new HashSet<SpecialInstrumentationPoint>(1);
		}
		functionalInstrumentations.add(functionalInstrumentation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functionalInstrumentations == null) ? 0 : functionalInstrumentations.hashCode());
		result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		result = prime * result + ((sensorInstrumentationPoint == null) ? 0 : sensorInstrumentationPoint.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + ((targetClassFqn == null) ? 0 : targetClassFqn.hashCode());
		result = prime * result + ((targetMethodName == null) ? 0 : targetMethodName.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MethodInstrumentationConfig other = (MethodInstrumentationConfig) obj;
		if (functionalInstrumentations == null) {
			if (other.functionalInstrumentations != null) {
				return false;
			}
		} else if (!functionalInstrumentations.equals(other.functionalInstrumentations)) {
			return false;
		}
		if (parameterTypes == null) {
			if (other.parameterTypes != null) {
				return false;
			}
		} else if (!parameterTypes.equals(other.parameterTypes)) {
			return false;
		}
		if (sensorInstrumentationPoint == null) {
			if (other.sensorInstrumentationPoint != null) {
				return false;
			}
		} else if (!sensorInstrumentationPoint.equals(other.sensorInstrumentationPoint)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!returnType.equals(other.returnType)) {
			return false;
		}
		if (targetClassFqn == null) {
			if (other.targetClassFqn != null) {
				return false;
			}
		} else if (!targetClassFqn.equals(other.targetClassFqn)) {
			return false;
		}
		if (targetMethodName == null) {
			if (other.targetMethodName != null) {
				return false;
			}
		} else if (!targetMethodName.equals(other.targetMethodName)) {
			return false;
		}
		return true;
	}

}
