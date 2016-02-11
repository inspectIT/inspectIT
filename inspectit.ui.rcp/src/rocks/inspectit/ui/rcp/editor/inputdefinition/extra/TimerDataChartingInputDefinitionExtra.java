package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import info.novatec.inspectit.communication.data.TimerData;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Input definition extra for displaying many timer data templates on the chart.
 * 
 * @author Ivan Senic
 * 
 */
public class TimerDataChartingInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of templates that defines what will be included in charting.
	 */
	private List<TimerData> templates;

	/**
	 * Gets {@link #templates}.
	 * 
	 * @return {@link #templates}
	 */
	public List<TimerData> getTemplates() {
		return templates;
	}

	/**
	 * Sets {@link #templates}.
	 * 
	 * @param templates
	 *            New value for {@link #templates}
	 */
	public void setTemplates(List<TimerData> templates) {
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
		TimerDataChartingInputDefinitionExtra that = (TimerDataChartingInputDefinitionExtra) object;
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
