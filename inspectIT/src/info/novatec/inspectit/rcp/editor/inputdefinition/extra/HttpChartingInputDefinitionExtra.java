package info.novatec.inspectit.rcp.editor.inputdefinition.extra;

import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.util.data.RegExAggregatedHttpTimerData;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.base.Objects;

/**
 * Input definition extra for the HTTP charting editors.
 * 
 * @author Ivan Senic
 * 
 */
public class HttpChartingInputDefinitionExtra implements IInputDefinitionExtra {

	/**
	 * List of templates that defines what will be included in charting.
	 */
	private List<HttpTimerData> templates;

	/**
	 * List of {@link RegExAggregatedHttpTimerData} if regular expression should be defined.
	 */
	private List<RegExAggregatedHttpTimerData> regExTemplates;

	/**
	 * Defines if plotting should be based on the {@link InspectIT} tag value.
	 * 
	 * @see HttpTimerData#hasInspectItTaggingHeader()
	 */
	boolean plotByTagValue;

	/**
	 * Gets {@link #templates}.
	 * 
	 * @return {@link #templates}
	 */
	public List<HttpTimerData> getTemplates() {
		return templates;
	}

	/**
	 * Sets {@link #templates}.
	 * 
	 * @param templates
	 *            New value for {@link #templates}
	 */
	public void setTemplates(List<HttpTimerData> templates) {
		this.templates = templates;
	}

	/**
	 * Gets {@link #regExTemplates}.
	 * 
	 * @return {@link #regExTemplates}
	 */
	public List<RegExAggregatedHttpTimerData> getRegExTemplates() {
		return regExTemplates;
	}

	/**
	 * Sets {@link #regExTemplates}.
	 * 
	 * @param regExTemplates
	 *            New value for {@link #regExTemplates}
	 */
	public void setRegExTemplates(List<RegExAggregatedHttpTimerData> regExTemplates) {
		this.regExTemplates = regExTemplates;
	}

	/**
	 * Gets {@link #plotByTagValue}.
	 * 
	 * @return {@link #plotByTagValue}
	 */
	public boolean isPlotByTagValue() {
		return plotByTagValue;
	}

	/**
	 * Sets {@link #plotByTagValue}.
	 * 
	 * @param plotByTagValue
	 *            New value for {@link #plotByTagValue}
	 */
	public void setPlotByTagValue(boolean plotByTagValue) {
		this.plotByTagValue = plotByTagValue;
	}

	/**
	 * @return Returns if reg ex transformation should be included in the graph.
	 */
	public boolean isRegExTransformation() {
		return CollectionUtils.isNotEmpty(regExTemplates);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(templates, regExTemplates, plotByTagValue);
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
		HttpChartingInputDefinitionExtra that = (HttpChartingInputDefinitionExtra) object;
		return Objects.equal(this.templates, that.templates) && Objects.equal(this.regExTemplates, that.regExTemplates) && Objects.equal(this.plotByTagValue, that.plotByTagValue);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("templates", templates).add("regExTemplates", regExTemplates).add("plotByTagValue", plotByTagValue).toString().toString();
	}
}
