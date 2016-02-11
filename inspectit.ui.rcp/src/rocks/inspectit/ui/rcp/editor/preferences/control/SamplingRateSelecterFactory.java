package info.novatec.inspectit.rcp.editor.preferences.control;

import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.SamplingRate;
import info.novatec.inspectit.rcp.editor.preferences.control.samplingrate.SamplingRateMode;

/**
 * The factory for sampling rate mode selection.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public final class SamplingRateSelecterFactory {

	/**
	 * The private constructor.
	 */
	private SamplingRateSelecterFactory() {
	}

	/**
	 * Returns the {@link SamplingRateMode}.
	 * 
	 * @param samplingRateIdEnum
	 *            The {@link SamplingRate}.
	 * @return The {@link SamplingRateMode}.
	 */
	public static SamplingRateMode selectSamplingRateMode(SamplingRate samplingRateIdEnum) {
		switch (samplingRateIdEnum) {
		case TIMEFRAME_DIVIDER_ID:
			return SamplingRateMode.TIMEFRAME_DIVIDER;
		default:
			return SamplingRateMode.TIMEFRAME_DIVIDER;
		}
	}

}
