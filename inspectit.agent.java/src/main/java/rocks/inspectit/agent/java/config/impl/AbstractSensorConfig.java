package rocks.inspectit.agent.java.config.impl;

import java.util.List;

/**
 * Abstract class for both our normal and special sensor configs.
 *
 * @author Ivan Senic
 *
 */
public class AbstractSensorConfig {

	/**
	 * The method id.
	 */
	private long id;

	/**
	 * The name of the target class.
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
	 * Gets {@link #id}.
	 *
	 * @return {@link #id}
	 */
	public long getId() {
		return this.id;
	}

	/**
	 * Sets {@link #id}.
	 *
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #targetClassFqn}.
	 *
	 * @return {@link #targetClassFqn}
	 */
	public String getTargetClassFqn() {
		return this.targetClassFqn;
	}

	/**
	 * Sets {@link #targetClassFqn}.
	 *
	 * @param targetClassFqn
	 *            New value for {@link #targetClassFqn}
	 */
	public void setTargetClassFqn(String targetClassFqn) {
		this.targetClassFqn = targetClassFqn;
	}

	/**
	 * Gets {@link #targetMethodName}.
	 *
	 * @return {@link #targetMethodName}
	 */
	public String getTargetMethodName() {
		return this.targetMethodName;
	}

	/**
	 * Sets {@link #targetMethodName}.
	 *
	 * @param targetMethodName
	 *            New value for {@link #targetMethodName}
	 */
	public void setTargetMethodName(String targetMethodName) {
		this.targetMethodName = targetMethodName;
	}

	/**
	 * Gets {@link #returnType}.
	 *
	 * @return {@link #returnType}
	 */
	public String getReturnType() {
		return this.returnType;
	}

	/**
	 * Sets {@link #returnType}.
	 *
	 * @param returnType
	 *            New value for {@link #returnType}
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Gets {@link #parameterTypes}.
	 *
	 * @return {@link #parameterTypes}
	 */
	public List<String> getParameterTypes() {
		return this.parameterTypes;
	}

	/**
	 * Sets {@link #parameterTypes}.
	 *
	 * @param parameterTypes
	 *            New value for {@link #parameterTypes}
	 */
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (this.id ^ (this.id >>> 32));
		result = (prime * result) + ((this.parameterTypes == null) ? 0 : this.parameterTypes.hashCode());
		result = (prime * result) + ((this.returnType == null) ? 0 : this.returnType.hashCode());
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
		AbstractSensorConfig other = (AbstractSensorConfig) obj;
		if (this.id != other.id) {
			return false;
		}
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
