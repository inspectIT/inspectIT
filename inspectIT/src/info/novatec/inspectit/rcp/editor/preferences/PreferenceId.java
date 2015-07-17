package info.novatec.inspectit.rcp.editor.preferences;

/**
 * The enumeration set for the unique preference group ids. By adding new enumerations you should
 * also create an inner public enumeration class which contains the associated control ids.
 * 
 * @author Eduard Tudenhoefner
 * 
 */
public enum PreferenceId {

	/**
	 * The identifiers of the different control groups.
	 */
	TIMELINE, SAMPLINGRATE, LIVEMODE, UPDATE, ITEMCOUNT, FILTERDATATYPE, INVOCFILTEREXCLUSIVETIME, INVOCFILTERTOTALTIME, CLEAR_BUFFER, STEPPABLE_CONTROL, TIME_RESOLUTION, HTTP_AGGREGATION_REQUESTMETHOD, HTTP_URI_TRANSFORMING, INVOCATION_SUBVIEW_MODE;

	/**
	 * Inner enumeration for TIMELINE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum TimeLine implements IPreferenceGroup {
		/**
		 * The identifiers of the elements in the
		 * {@link info.novatec.inspectit.rcp.editor.preferences.control.TimeLineControl}.
		 */
		FROM_DATE_ID, TO_DATE_ID;

		/**
		 * Defines the default time line period displayed.
		 */
		public static final long TIMELINE_DEFAULT = 10 * 60 * 1000;
	}

	/**
	 * Inner enumeration for SAMPLINGRATE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum SamplingRate implements IPreferenceGroup {
		/**
		 * The identifiers of the elements in the
		 * {@link info.novatec.inspectit.rcp.editor.preferences.control.SamplingRateControl} .
		 */
		SLIDER_ID, DIVIDER_ID, TIMEFRAME_DIVIDER_ID;
	}

	/**
	 * Inner enumeration for LIVEMODE.
	 * 
	 * @author Eduard Tudenhoefner
	 * 
	 */
	public enum LiveMode implements IPreferenceGroup {
		/**
		 * The identifier for the live button.
		 */
		BUTTON_LIVE_ID, REFRESH_RATE;

		/**
		 * Defines if the live mode is active by default.
		 */
		public static final boolean ACTIVE_DEFAULT = false;
	}

	/**
	 * Inner enumeration for ITEMCOUNT.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum ItemCount implements IPreferenceGroup {
		/**
		 * The identifier for the item count.
		 */
		COUNT_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the FILTERDATATYPE.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum DataTypeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the sensor data selections.
		 */
		SENSOR_DATA_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the INVOCEXCLUSIVETIMESELECTION.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum InvocExclusiveTimeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the time selection.
		 */
		TIME_SELECTION_ID;
	}

	/**
	 * Inner enumeration for the INVOCTOTALTIMESELECTION.
	 * 
	 * @author Patrice Bouillet
	 * 
	 */
	public enum InvocTotalTimeSelection implements IPreferenceGroup {
		/**
		 * The identifier for the time selection.
		 */
		TIME_SELECTION_ID;
	}

	/**
	 * Inner enumeration for STEPPABLE_CONTROL.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum SteppableControl implements IPreferenceGroup {
		/**
		 * The identifier for the switch stepping control button.
		 */
		BUTTON_STEPPABLE_CONTROL_ID;
	}

	/**
	 * Inner enumeration for TIME_RESOLUTION.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum TimeResolution implements IPreferenceGroup {
		/**
		 * The identifier for the definition of decimal places.
		 */
		TIME_DECIMAL_PLACES_ID;
	}

	/**
	 * Inner enumeration for HTTP_AGGREGATION_REQUESTMETHOD.
	 * 
	 * @author Stefan Siegl
	 */
	public enum HttpAggregationRequestMethod implements IPreferenceGroup {
		/**
		 * The identifier for the switch stepping control button.
		 */
		BUTTON_HTTP_AGGREGATION_REQUESTMETHOD_ID;
	}

	/**
	 * Inner enumeration for HTTP_URI_TRANSFORMING.
	 * 
	 * @author Ivan Senic
	 * 
	 */
	public enum HttpUriTransformation implements IPreferenceGroup {

		/**
		 * The identifier that defines if URI transformation is active.
		 */
		URI_TRANSFORMATION_ACTIVE;

		/**
		 * Defines if the live mode is active by default.
		 */
		public static final boolean DEFAULT = false;
	}

	/**
	 * Inner enumeration for INVOCATION_SUBVIEW_MODE.
	 * 
	 * @author Ivan Senic
	 */
	public enum InvocationSubviewMode implements IPreferenceGroup {
		/**
		 * The identifier to state that the mode in subview of invocation is raw or not.
		 */
		RAW;
	}
}
