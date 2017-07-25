package rocks.inspectit.shared.all.instrumentation.config.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	 * {@link SensorInstrumentationPoint} for this {@link MethodInstrumentationConfig}. Can be only
	 * one per method.
	 */
	private SensorInstrumentationPoint sensorInstrumentationPoint;

	/**
	 * Special instrumentation belonging to this method instrumentation. Can be only one per method.
	 */
	private SpecialInstrumentationPoint specialInstrumentationPoint;

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
	@Override
	public boolean hasInstrumentationPoints() {
		return (sensorInstrumentationPoint != null) || (specialInstrumentationPoint != null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<IMethodInstrumentationPoint> getAllInstrumentationPoints() {
		if (!hasInstrumentationPoints()) {
			return Collections.emptyList();
		}

		List<IMethodInstrumentationPoint> instrumentationPoints = new ArrayList<IMethodInstrumentationPoint>(1);
		if (null != specialInstrumentationPoint) {
			instrumentationPoints.add(specialInstrumentationPoint);
		}
		if (null != sensorInstrumentationPoint) {
			instrumentationPoints.add(sensorInstrumentationPoint);
		}

		return instrumentationPoints;
	}

	/**
	 * Gets {@link #targetClassFqn}.
	 *
	 * @return {@link #targetClassFqn}
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
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
	 * Gets {@link #specialInstrumentationPoint}.
	 *
	 * @return {@link #specialInstrumentationPoint}
	 */
	public SpecialInstrumentationPoint getSpecialInstrumentationPoint() {
		return this.specialInstrumentationPoint;
	}

	/**
	 * Sets {@link #specialInstrumentationPoint}.
	 *
	 * @param specialInstrumentationPoint
	 *            New value for {@link #specialInstrumentationPoint}
	 */
	public void setSpecialInstrumentationPoint(SpecialInstrumentationPoint specialInstrumentationPoint) {
		this.specialInstrumentationPoint = specialInstrumentationPoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.parameterTypes == null) ? 0 : this.parameterTypes.hashCode());
		result = (prime * result) + ((this.returnType == null) ? 0 : this.returnType.hashCode());
		result = (prime * result) + ((this.sensorInstrumentationPoint == null) ? 0 : this.sensorInstrumentationPoint.hashCode());
		result = (prime * result) + ((this.specialInstrumentationPoint == null) ? 0 : this.specialInstrumentationPoint.hashCode());
		result = (prime * result) + ((this.targetClassFqn == null) ? 0 : this.targetClassFqn.hashCode());
		result = (prime * result) + ((this.targetMethodName == null) ? 0 : this.targetMethodName.hashCode());
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
		if (this.parameterTypes == null) {
			if (other.parameterTypes != null) {
				return false;
			}
		} else if (!this.parameterTypes.equals(other.parameterTypes)) {
			return false;
		}
		if (this.returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!this.returnType.equals(other.returnType)) {
			return false;
		}
		if (this.sensorInstrumentationPoint == null) {
			if (other.sensorInstrumentationPoint != null) {
				return false;
			}
		} else if (!this.sensorInstrumentationPoint.equals(other.sensorInstrumentationPoint)) {
			return false;
		}
		if (this.specialInstrumentationPoint == null) {
			if (other.specialInstrumentationPoint != null) {
				return false;
			}
		} else if (!this.specialInstrumentationPoint.equals(other.specialInstrumentationPoint)) {
			return false;
		}
		if (this.targetClassFqn == null) {
			if (other.targetClassFqn != null) {
				return false;
			}
		} else if (!this.targetClassFqn.equals(other.targetClassFqn)) {
			return false;
		}
		if (this.targetMethodName == null) {
			if (other.targetMethodName != null) {
				return false;
			}
		} else if (!this.targetMethodName.equals(other.targetMethodName)) {
			return false;
		}
		return true;
	}

}
