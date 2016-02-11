package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;

import org.eclipse.swt.graphics.Image;

import com.google.common.base.Objects;

/**
 * A component can be used in any tree based views.
 * 
 * @author Patrice Bouillet
 * 
 */
public abstract class Component {

	/**
	 * The name of the component.
	 */
	private String name;

	/**
	 * The tooltip of the component, if there is any.
	 */
	private String tooltip = "";

	/**
	 * The image of the component, if there is any.
	 */
	private Image image;

	/**
	 * The parent component.
	 */
	private Component parent;

	/**
	 * The input definition if there is one. This is used to create the appropriate view.
	 */
	private InputDefinition inputDefinition;

	/**
	 * If the component is enabled.
	 */
	private boolean enabled = true;

	/**
	 * Gets {@link #name}.
	 * 
	 * @return {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name}.
	 * 
	 * @param name
	 *            New value for {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets {@link #tooltip}.
	 * 
	 * @return {@link #tooltip}
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * Sets {@link #tooltip}.
	 * 
	 * @param tooltip
	 *            New value for {@link #tooltip}
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * Gets {@link #image}.
	 * 
	 * @return {@link #image}
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Sets {@link #image}.
	 * 
	 * @param image
	 *            New value for {@link #image}
	 */
	public void setImage(Image image) {
		this.image = image;
	}

	/**
	 * Gets {@link #parent}.
	 * 
	 * @return {@link #parent}
	 */
	public Component getParent() {
		return parent;
	}

	/**
	 * Sets {@link #parent}.
	 * 
	 * @param parent
	 *            New value for {@link #parent}
	 */
	public void setParent(Component parent) {
		this.parent = parent;
	}

	/**
	 * Gets {@link #inputDefinition}.
	 * 
	 * @return {@link #inputDefinition}
	 */
	public InputDefinition getInputDefinition() {
		return inputDefinition;
	}

	/**
	 * Sets {@link #inputDefinition}.
	 * 
	 * @param inputDefinition
	 *            New value for {@link #inputDefinition}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		this.inputDefinition = inputDefinition;
	}

	/**
	 * Gets {@link #enabled}.
	 * 
	 * @return {@link #enabled}
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets {@link #enabled}.
	 * 
	 * @param enabled
	 *            New value for {@link #enabled}
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(name, tooltip, inputDefinition, parent, enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		Component that = (Component) object;
		return Objects.equal(this.name, that.name) && Objects.equal(this.tooltip, that.tooltip) && Objects.equal(this.inputDefinition, that.inputDefinition) && Objects.equal(this.parent, that.parent)
				&& Objects.equal(this.enabled, that.enabled);
	}

}
