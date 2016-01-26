package rocks.inspectit.shared.cs.ci.context;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.commons.collections.CollectionUtils;

import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPath;
import rocks.inspectit.shared.all.instrumentation.config.impl.PropertyPathStart;
import rocks.inspectit.shared.cs.ci.context.impl.FieldContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ParameterContextCapture;
import rocks.inspectit.shared.cs.ci.context.impl.ReturnContextCapture;

/**
 * Abstract class for all context captures possibilities - parameter, return or field.
 *
 * @author Ivan Senic
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ ReturnContextCapture.class, ParameterContextCapture.class, FieldContextCapture.class })
public abstract class AbstractContextCapture {

	/**
	 * Display name of caught value.
	 */
	@XmlAttribute(name = "display-name")
	private String displayName;

	/**
	 * List of paths to apply on caught object.
	 */
	@XmlElementWrapper(name = "paths", required = false)
	@XmlElement(name = "path")
	private List<String> paths;

	/**
	 * Returns string notation that should be passed to agent.
	 * <p>
	 * Note this is just a temporary utility method.
	 *
	 * @return Returns string notation that should be passed to agent.
	 */
	public abstract String getAgentStringNotation();

	/**
	 * Returns correctly set property path start.
	 *
	 * @return Returns correctly set property path start.
	 */
	public abstract PropertyPathStart getPropertyPathStart();

	/**
	 * Adds all defined paths to the property path (start).
	 *
	 * @param propertyPath
	 *            {@link PropertyPath}
	 */
	protected void addPaths(PropertyPath propertyPath) {
		if (CollectionUtils.isNotEmpty(paths)) {
			PropertyPath parent = propertyPath;
			for (String path : paths) {
				PropertyPath toContinue = new PropertyPath(path);
				parent.setPathToContinue(toContinue);
				parent = toContinue;
			}
		}
	}

	/**
	 * Gets {@link #displayName}.
	 *
	 * @return {@link #displayName}
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets {@link #displayName}.
	 *
	 * @param displayName
	 *            New value for {@link #displayName}
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets {@link #paths}.
	 *
	 * @return {@link #paths}
	 */
	public List<String> getPaths() {
		return paths;
	}

	/**
	 * Sets {@link #paths}.
	 *
	 * @param paths
	 *            New value for {@link #paths}
	 */
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((paths == null) ? 0 : paths.hashCode());
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
		AbstractContextCapture other = (AbstractContextCapture) obj;
		if (displayName == null) {
			if (other.displayName != null) {
				return false;
			}
		} else if (!displayName.equals(other.displayName)) {
			return false;
		}
		if (paths == null) {
			if (other.paths != null) {
				return false;
			}
		} else if (!paths.equals(other.paths)) {
			return false;
		}
		return true;
	}

}
