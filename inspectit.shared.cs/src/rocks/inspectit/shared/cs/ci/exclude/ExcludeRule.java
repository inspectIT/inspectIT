package info.novatec.inspectit.ci.exclude;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Defines a class name or pattern to exclude from instrumentation.
 * 
 * @author Ivan Senic
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "exclude-rule")
public class ExcludeRule {

	/**
	 * No-arg constructor.
	 */
	public ExcludeRule() {
	}

	/**
	 * Creates new {@link ExcludeRule}.
	 * 
	 * @param className
	 *            Name of the class or pattern.
	 */
	public ExcludeRule(String className) {
		this.className = className;
	}

	/**
	 * Class name/pattern.
	 */
	@XmlAttribute(name = "class-name", required = true)
	private String className;

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
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
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
		ExcludeRule other = (ExcludeRule) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		return true;
	}

}
