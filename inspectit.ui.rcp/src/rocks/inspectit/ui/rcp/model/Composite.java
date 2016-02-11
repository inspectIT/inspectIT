package info.novatec.inspectit.rcp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite extends the component functionality by allowing to add children to this node.
 * 
 * @author Patrice Bouillet
 * 
 */
public class Composite extends Component {

	/**
	 * The components arranged under this composite node.
	 */
	private List<Component> components = new ArrayList<Component>();

	/**
	 * Returns all children.
	 * 
	 * @return The children.
	 */
	public List<Component> getChildren() {
		return components;
	}

	/**
	 * Sets the children of this composite.
	 * 
	 * @param children
	 *            The children to set.
	 */
	public void setChildren(List<Component> children) {
		this.components = children;
	}

	/**
	 * Adds a child to this composite at the end of the list.
	 * 
	 * @param child
	 *            The child to add.
	 */
	public void addChild(Component child) {
		components.add(child);
		child.setParent(this);
	}

	/**
	 * Returns <code>true</code> if children are available under this composite.
	 * 
	 * @return <code>true</code> if children are available.
	 */
	public boolean hasChildren() {
		return !components.isEmpty();
	}

}
