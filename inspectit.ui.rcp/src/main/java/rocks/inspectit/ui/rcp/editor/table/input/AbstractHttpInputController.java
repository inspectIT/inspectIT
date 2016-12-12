package rocks.inspectit.ui.rcp.editor.table.input;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StyledString;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.cs.cmr.service.IHttpTimerDataAccessService;
import rocks.inspectit.ui.rcp.editor.inputdefinition.InputDefinition;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId;
import rocks.inspectit.ui.rcp.editor.preferences.PreferenceId.LiveMode;
import rocks.inspectit.ui.rcp.preferences.PreferencesConstants;
import rocks.inspectit.ui.rcp.preferences.PreferencesUtils;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;

/**
 * Reduce the redundancy in http controllers. Still as the de-facto standard of realizing
 * controllers is based on the enum to create the columns lots of code needs to be doubled. We can
 * address this in the future in a better way.
 *
 * @author Stefan Siegl
 */
public abstract class AbstractHttpInputController extends AbstractTableInputController {

	/**
	 * Http Timer data access service.
	 */
	protected IHttpTimerDataAccessService httptimerDataAccessService;

	/**
	 * List of Timer data to be displayed.
	 */
	protected List<HttpTimerData> timerDataList = new ArrayList<>();

	/**
	 * Template object used for querying.
	 */
	protected HttpTimerData template;

	/**
	 * Empty styled string.
	 */
	protected final StyledString emptyStyledString = new StyledString();

	/**
	 * Date to display invocations from.
	 */
	protected Date fromDate = null;

	/**
	 * Date to display invocations to.
	 */
	protected Date toDate = null;

	/**
	 * Are we in live mode.
	 */
	protected boolean autoUpdate = LiveMode.ACTIVE_DEFAULT;

	/**
	 * Decimal places.
	 */
	protected int timeDecimalPlaces = PreferencesUtils.getIntValue(PreferencesConstants.DECIMAL_PLACES);

	/**
	 * Flag identifying whether the aggregation should take the request method into account.
	 */
	protected boolean httpCatorizationOnRequestMethodActive = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new HttpTimerData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());

		httptimerDataAccessService = inputDefinition.getRepositoryDefinition().getHttpTimerDataAccessService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canOpenInput(List<? extends Object> data) {
		if (null == data) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		return timerDataList;
	}

	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		if (null == preferenceEvent) {
			return;
		}

		switch (preferenceEvent.getPreferenceId()) {
		case TIMELINE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.TO_DATE_ID);
			}
			break;
		case LIVEMODE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
			}
			break;
		case TIME_RESOLUTION:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID)) {
				timeDecimalPlaces = (Integer) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeResolution.TIME_DECIMAL_PLACES_ID);
			}
			break;
		case HTTP_AGGREGATION_REQUESTMETHOD:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.HttpAggregationRequestMethod.BUTTON_HTTP_AGGREGATION_REQUESTMETHOD_ID)) {
				httpCatorizationOnRequestMethodActive = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.HttpAggregationRequestMethod.BUTTON_HTTP_AGGREGATION_REQUESTMETHOD_ID);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.CLEAR_BUFFER);
			preferences.add(PreferenceId.LIVEMODE);
		}
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.TIMELINE);
		preferences.add(PreferenceId.HTTP_AGGREGATION_REQUESTMETHOD);
		preferences.add(PreferenceId.TIME_RESOLUTION);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}
}
