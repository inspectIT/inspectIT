package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Additional input definition data used for the combined invocations view.
 * 
 * @author Ivan Senic
 * 
 */
public class CombinedInvocationsInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of {@link InvocationSequenceData} templates that need to be combined.
	 */
	private List<InvocationSequenceData> templates;

	/**
	 * Gets {@link #templates}.
	 * 
	 * @return {@link #templates}
	 */
	public List<InvocationSequenceData> getTemplates() {
		return templates;
	}

	/**
	 * Sets {@link #templates}.
	 * 
	 * @param templates
	 *            New value for {@link #templates}
	 */
	public void setTemplates(List<InvocationSequenceData> templates) {
		this.templates = templates;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(templates);
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
		CombinedInvocationsInputDefinitionExtra that = (CombinedInvocationsInputDefinitionExtra) object;
		return Objects.equal(this.templates, that.templates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("templates", templates).toString();
	}

}
