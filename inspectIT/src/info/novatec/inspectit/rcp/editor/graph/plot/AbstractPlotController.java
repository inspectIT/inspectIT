package info.novatec.inspectit.rcp.editor.graph.plot;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.indexing.aggregation.IAggregator;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.IPreferenceGroup;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.SamplingRate;
import info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateControl;
import info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateControl.Sensitivity;
import info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateSelecterFactory;
import info.novatec.inspectit.rcp.editor.preferences.control.samplingrate.SamplingRateMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

/**
 * The abstract class of the {@link PlotController} interface to provide some standard methods.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public abstract class AbstractPlotController implements PlotController {

	/**
	 * The input definition.
	 */
	private InputDefinition inputDefinition;

	/**
	 * The root editor.
	 */
	private IRootEditor rootEditor;

	/**
	 * The sensitivity.
	 */
	private Sensitivity sensitivity = SamplingRateControl.DEFAULT_SENSITIVITY;

	/**
	 * The sampling rate mode identifier.
	 */
	private SamplingRateMode samplingRateMode = SamplingRateMode.TIMEFRAME_DIVIDER;

	/**
	 * {@inheritDoc}
	 */
	public void setInputDefinition(InputDefinition inputDefinition) {
		Assert.isNotNull(inputDefinition);

		this.inputDefinition = inputDefinition;
	}

	/**
	 * Returns the input definition.
	 * 
	 * @return The input definition.
	 */
	protected InputDefinition getInputDefinition() {
		Assert.isNotNull(inputDefinition);

		return inputDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRootEditor(IRootEditor rootEditor) {
		Assert.isNotNull(rootEditor);

		this.rootEditor = rootEditor;
	}

	/**
	 * @return the rootEditor
	 */
	protected IRootEditor getRootEditor() {
		Assert.isNotNull(rootEditor);

		return rootEditor;
	}

	/**
	 * Adjusts the sampling rate.
	 * 
	 * @param <E>
	 *            Type of element.
	 * @param from
	 *            The start time.
	 * @param to
	 *            The end time.
	 * @param dataObjects
	 *            the data objects.
	 * @param aggregator
	 *            {@link IAggregator} to be used.
	 * @return A {@link List} with the aggregated {@link DefaultData}.
	 */
	protected <E extends DefaultData> List<E> adjustSamplingRate(List<E> dataObjects, Date from, Date to, IAggregator<E> aggregator) {
		return samplingRateMode.adjustSamplingRate(dataObjects, from, to, sensitivity.getValue(), aggregator);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSamplingRate(SamplingRateMode mode, Sensitivity sensitivity) {
		this.samplingRateMode = mode;
		this.sensitivity = sensitivity;
	}

	/**
	 * Returns the sampling rate mode.
	 * 
	 * @return The sampling rate mode.
	 */
	protected SamplingRateMode getSamplingRateMode() {
		return samplingRateMode;
	}

	/**
	 * Returns the sensitivity of the sampling rate mode.
	 * 
	 * @return The sensitivity.
	 */
	protected Sensitivity getSensitivity() {
		return sensitivity;
	}

	/**
	 * {@inheritDoc}
	 */
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (PreferenceId.SAMPLINGRATE.equals(preferenceEvent.getPreferenceId())) {
			Map<IPreferenceGroup, Object> preferenceMap = preferenceEvent.getPreferenceMap();

			// get the selected sampling rate mode
			if (preferenceMap.containsKey(PreferenceId.SamplingRate.DIVIDER_ID)) {
				SamplingRate samplingRateIdEnum = (SamplingRate) preferenceMap.get(PreferenceId.SamplingRate.DIVIDER_ID);
				samplingRateMode = SamplingRateSelecterFactory.selectSamplingRateMode(samplingRateIdEnum);
			}

			if (preferenceMap.containsKey(PreferenceId.SamplingRate.SLIDER_ID)) {
				sensitivity = (Sensitivity) preferenceMap.get(PreferenceId.SamplingRate.SLIDER_ID);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default legend is not shown.
	 */
	public boolean showLegend() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
	}

}
