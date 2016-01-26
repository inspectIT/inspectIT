package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Every path can have another follower path. These classes are used to describe the way to find
 * a specific property in an object.
 * 
 * @author Patrice Bouillet
 * 
 */
public class PropertyPath {

	/**
	 * The name of this path.
	 */
	private String name;

	/**
	 * The path to continue.
	 */
	private PropertyPath pathToContinue;

	/**
	 * Creates a new instance and leaves the name empty.
	 */
	public PropertyPath() {
	}

	/**
	 * Creates a new instance and sets the name.
	 * 
	 * @param name
	 *            the name of this path.
	 */
	public PropertyPath(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPathToContinue(PropertyPath pathToContinue) {
		this.pathToContinue = pathToContinue;
	}

	public PropertyPath getPathToContinue() {
		return pathToContinue;
	}

	public boolean isMethodCall() {
		return name.endsWith("()");
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		if (null != pathToContinue) {
			return name + "-->" + pathToContinue.toString();
		} else {
			return name;
		}
	}

}