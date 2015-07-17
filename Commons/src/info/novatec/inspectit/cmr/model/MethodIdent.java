package info.novatec.inspectit.cmr.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Method Ident class is used to store the information of the Agent(s) about an instrumented
 * method into the database.
 * 
 * @author Patrice Bouillet
 * 
 */
public class MethodIdent implements Serializable {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 5670026321320934522L;

	/**
	 * The id of this instance (if persisted, otherwise <code>null</code>).
	 */
	private Long id;

	/**
	 * The timestamp which shows when this information was created on the CMR.
	 */
	private Timestamp timeStamp;

	/**
	 * The one-to-many association to the {@link MethodIdentToSensorType}.
	 */
	private Set<MethodIdentToSensorType> methodIdentToSensorTypes = new HashSet<MethodIdentToSensorType>(0);

	/**
	 * The many-to-one association to the {@link PlatformIdent} object.
	 */
	private PlatformIdent platformIdent;

	/**
	 * The name of the package.
	 */
	private String packageName;

	/**
	 * The name of the class.
	 */
	private String className;

	/**
	 * The name of the method.
	 */
	private String methodName;

	/**
	 * All method parameters stored in a List, converted to a VARCHAR column in the database via
	 * ListStringType.
	 */
	private List<String> parameters = new ArrayList<String>(0);

	/**
	 * The return type.
	 */
	private String returnType;

	/**
	 * The modifiers.
	 */
	private int modifiers;

	/**
	 * Returns true if any of the {@link MethodIdentToSensorType} objects in the
	 * {@link #methodIdentToSensorTypes} is marked as active. Returns false otherwise.
	 * 
	 * @return Returns true if any of the {@link MethodIdentToSensorType} objects in the
	 *         {@link #methodIdentToSensorTypes} is marked as active. Returns false otherwise.
	 */
	public boolean hasActiveSensorTypes() {
		for (MethodIdentToSensorType methodIdentToSensorType : methodIdentToSensorTypes) {
			if (methodIdentToSensorType.isActive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets {@link #id}.
	 * 
	 * @return {@link #id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets {@link #id}.
	 * 
	 * @param id
	 *            New value for {@link #id}
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #timeStamp}.
	 * 
	 * @return {@link #timeStamp}
	 */
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Sets {@link #timeStamp}.
	 * 
	 * @param timeStamp
	 *            New value for {@link #timeStamp}
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets {@link #methodIdentToSensorTypes}.
	 * 
	 * @return {@link #methodIdentToSensorTypes}
	 */
	public Set<MethodIdentToSensorType> getMethodIdentToSensorTypes() {
		return methodIdentToSensorTypes;
	}

	/**
	 * Sets {@link #methodIdentToSensorTypes}.
	 * 
	 * @param methodIdentToSensorTypes
	 *            New value for {@link #methodIdentToSensorTypes}
	 */
	public void setMethodIdentToSensorTypes(Set<MethodIdentToSensorType> methodIdentToSensorTypes) {
		this.methodIdentToSensorTypes = methodIdentToSensorTypes;
	}

	/**
	 * Gets {@link #platformIdent}.
	 * 
	 * @return {@link #platformIdent}
	 */
	public PlatformIdent getPlatformIdent() {
		return platformIdent;
	}

	/**
	 * Sets {@link #platformIdent}.
	 * 
	 * @param platformIdent
	 *            New value for {@link #platformIdent}
	 */
	public void setPlatformIdent(PlatformIdent platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #parameters}.
	 * 
	 * @return {@link #parameters}
	 */
	public List<String> getParameters() {
		return parameters;
	}

	/**
	 * Sets {@link #parameters}.
	 * 
	 * @param parameters
	 *            New value for {@link #parameters}
	 */
	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets {@link #packageName}.
	 * 
	 * @return {@link #packageName}
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Sets {@link #packageName}.
	 * 
	 * @param packageName
	 *            New value for {@link #packageName}
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
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
	 * Sets {@link #className}.
	 * 
	 * @param className
	 *            New value for {@link #className}
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets {@link #methodName}.
	 * 
	 * @return {@link #methodName}
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Sets {@link #methodName}.
	 * 
	 * @param methodName
	 *            New value for {@link #methodName}
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
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
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * Gets {@link #modifiers}.
	 * 
	 * @return {@link #modifiers}
	 */
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets {@link #modifiers}.
	 * 
	 * @param modifiers
	 *            New value for {@link #modifiers}
	 */
	public void setModifiers(int modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Returns the Fully qualified name (FQN) of the class {@link MethodIdent} is holding
	 * information for.
	 * 
	 * @return Fully qualified name (FQN) string.
	 */
	public String getFQN() {
		return packageName + '.' + className;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + modifiers;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
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
		MethodIdent other = (MethodIdent) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (modifiers != other.modifiers) {
			return false;
		}
		if (packageName == null) {
			if (other.packageName != null) {
				return false;
			}
		} else if (!packageName.equals(other.packageName)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (returnType == null) {
			if (other.returnType != null) {
				return false;
			}
		} else if (!returnType.equals(other.returnType)) {
			return false;
		}
		if (timeStamp == null) {
			if (other.timeStamp != null) {
				return false;
			}
		} else if (!timeStamp.equals(other.timeStamp)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return packageName + "." + className + "#" + methodName + parameters + " : " + returnType;
	}

}
