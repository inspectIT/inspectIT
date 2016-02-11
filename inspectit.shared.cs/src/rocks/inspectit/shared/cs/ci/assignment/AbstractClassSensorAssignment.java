package info.novatec.inspectit.ci.assignment;

import info.novatec.inspectit.ci.assignment.impl.ExceptionSensorAssignment;
import info.novatec.inspectit.ci.assignment.impl.MethodSensorAssignment;
import info.novatec.inspectit.ci.sensor.ISensorConfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Class for class sensor assignment.
 * 
 * @author Ivan Senic
 * 
 * @param <T>Type of the sensor config that relates to the assignment.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ MethodSensorAssignment.class, ExceptionSensorAssignment.class })
public abstract class AbstractClassSensorAssignment<T extends ISensorConfig> implements ISensorAssignment<T>, Cloneable {

	/**
	 * Class name/pattern.
	 */
	@XmlAttribute(name = "class-name", required = true)
	private String className;

	/**
	 * If superclass marker is set.
	 */
	@XmlAttribute(name = "superclass", required = true)
	private boolean superclass;

	/**
	 * If interface marker is set.
	 */
	@XmlAttribute(name = "interface", required = true)
	private boolean interf;

	/**
	 * Annotation class.
	 */
	@XmlAttribute(name = "annotation")
	private String annotation;

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
	 * Gets {@link #superclass}.
	 * 
	 * @return {@link #superclass}
	 */
	public boolean isSuperclass() {
		return superclass;
	}

	/**
	 * Sets {@link #superclass}.
	 * 
	 * @param superclass
	 *            New value for {@link #superclass}
	 */
	public void setSuperclass(boolean superclass) {
		this.superclass = superclass;
	}

	/**
	 * Gets {@link #interf}.
	 * 
	 * @return {@link #interf}
	 */
	public boolean isInterf() {
		return interf;
	}

	/**
	 * Sets {@link #interf}.
	 * 
	 * @param interf
	 *            New value for {@link #interf}
	 */
	public void setInterf(boolean interf) {
		this.interf = interf;
	}

	/**
	 * Gets {@link #annotation}.
	 * 
	 * @return {@link #annotation}
	 */
	public String getAnnotation() {
		return annotation;
	}

	/**
	 * Sets {@link #annotation}.
	 * 
	 * @param annotation
	 *            New value for {@link #annotation}
	 */
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + (interf ? 1231 : 1237);
		result = prime * result + (superclass ? 1231 : 1237);
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
		AbstractClassSensorAssignment<?> other = (AbstractClassSensorAssignment<?>) obj;
		if (annotation == null) {
			if (other.annotation != null) {
				return false;
			}
		} else if (!annotation.equals(other.annotation)) {
			return false;
		}
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (interf != other.interf) {
			return false;
		}
		if (superclass != other.superclass) {
			return false;
		}
		return true;
	}

}
