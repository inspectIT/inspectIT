package rocks.inspectit.shared.cs.ci.assignment;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import rocks.inspectit.shared.cs.ci.assignment.impl.ExceptionSensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.shared.cs.jaxb.DefaultValue.BooleanFalse;

/**
 * Class for class sensor assignment.
 *
 * @author Ivan Senic
 *
 * @param <T>
 *            Type of the sensor config that relates to the assignment.
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
	@XmlAttribute(name = "superclass")
	@XmlJavaTypeAdapter(BooleanFalse.class)
	private Boolean superclass = Boolean.FALSE;

	/**
	 * If interface marker is set.
	 */
	@XmlAttribute(name = "interface")
	@XmlJavaTypeAdapter(BooleanFalse.class)
	private Boolean interf = Boolean.FALSE;

	/**
	 * Annotation class.
	 */
	@XmlAttribute(name = "annotation")
	private String annotation;

	/**
	 * Return settings for the sensor assignment.
	 *
	 * @return Return settings for the sensor assignment.
	 */
	public abstract Map<String, Object> getSettings();

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
		return superclass.booleanValue();
	}

	/**
	 * Sets {@link #superclass}.
	 *
	 * @param superclass
	 *            New value for {@link #superclass}
	 */
	public void setSuperclass(boolean superclass) {
		this.superclass = Boolean.valueOf(superclass);
	}

	/**
	 * Gets {@link #interf}.
	 *
	 * @return {@link #interf}
	 */
	public boolean isInterf() {
		return interf.booleanValue();
	}

	/**
	 * Sets {@link #interf}.
	 *
	 * @param interf
	 *            New value for {@link #interf}
	 */
	public void setInterf(boolean interf) {
		this.interf = Boolean.valueOf(interf);
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
		result = (prime * result) + ((this.annotation == null) ? 0 : this.annotation.hashCode());
		result = (prime * result) + ((this.className == null) ? 0 : this.className.hashCode());
		result = (prime * result) + ((this.interf == null) ? 0 : this.interf.hashCode());
		result = (prime * result) + ((this.superclass == null) ? 0 : this.superclass.hashCode());
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
		if (this.annotation == null) {
			if (other.annotation != null) {
				return false;
			}
		} else if (!this.annotation.equals(other.annotation)) {
			return false;
		}
		if (this.className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!this.className.equals(other.className)) {
			return false;
		}
		if (this.interf == null) {
			if (other.interf != null) {
				return false;
			}
		} else if (!this.interf.equals(other.interf)) {
			return false;
		}
		if (this.superclass == null) {
			if (other.superclass != null) {
				return false;
			}
		} else if (!this.superclass.equals(other.superclass)) {
			return false;
		}
		return true;
	}

}
