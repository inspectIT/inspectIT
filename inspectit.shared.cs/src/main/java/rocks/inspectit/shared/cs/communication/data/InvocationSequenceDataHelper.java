package rocks.inspectit.shared.cs.communication.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.mutable.MutableDouble;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.tracing.data.Span;
import rocks.inspectit.shared.cs.cmr.service.ISpanService;

/**
 * Helper class to easier query {@link InvocationSequenceData} objects.
 *
 * @author Stefan Siegl
 */
public final class InvocationSequenceDataHelper {

	/**
	 * Private constructor for utility class.
	 */
	private InvocationSequenceDataHelper() {
	}

	/**
	 * Checks if the invocation sequence data object itself contains parameters.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return if this data object contains captured parameters.
	 */
	public static boolean hasCapturedParametersInInvocationSequence(InvocationSequenceData data) {
		return (null != data.getParameterContentData()) && !data.getParameterContentData().isEmpty();
	}

	/**
	 * Checks whether the invocation sequence data object itself or some nested data element
	 * (timerdata) provides captured parameters.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether the invocation sequence data object itself or some nested data element
	 *         (timerdata) provides captured parameters.
	 */
	public static boolean hasCapturedParameters(InvocationSequenceData data) {
		if (hasCapturedParametersInInvocationSequence(data)) {
			return true;
		}
		return hasTimerData(data) && (null != data.getTimerData().getParameterContentData()) && !data.getTimerData().getParameterContentData().isEmpty();
	}

	/**
	 * Returns the captured data of the invocation sequence.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @param sorted
	 *            if parameters should be sorted by
	 * @return the captured data of the invocation sequence.
	 */
	public static List<ParameterContentData> getCapturedParameters(InvocationSequenceData data, boolean sorted) {
		if (!hasCapturedParameters(data)) {
			return Collections.emptyList();
		}

		List<ParameterContentData> parameterContents = new ArrayList<ParameterContentData>();

		if (CollectionUtils.isNotEmpty(data.getParameterContentData())) {
			for (ParameterContentData contentData : data.getParameterContentData()) {
				if (!parameterContents.contains(contentData)) {
					parameterContents.add(contentData);
				}
			}
		}

		if (hasTimerData(data)) {
			if (CollectionUtils.isNotEmpty(data.getTimerData().getParameterContentData())) {
				for (ParameterContentData contentData : data.getTimerData().getParameterContentData()) {
					if (!parameterContents.contains(contentData)) {
						parameterContents.add(contentData);
					}
				}
			}
		}

		if (sorted) {
			Collections.sort(parameterContents);
		}

		return parameterContents;
	}

	/**
	 * Checks whether this data object contains a timer data object of some sort.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains a timer data object.
	 */
	public static boolean hasTimerData(InvocationSequenceData data) {
		return null != data.getTimerData();
	}

	/**
	 * Checks whether this data object contains a http timer data object.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains a http timer data object.
	 */
	public static boolean hasHttpTimerData(InvocationSequenceData data) {
		return data.getTimerData() instanceof HttpTimerData;
	}

	/**
	 * Checks whether this data object contains a <code>LoggingData</code> object.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains a logging data object.
	 */
	public static boolean hasLoggingData(InvocationSequenceData data) {
		return null != data.getLoggingData();
	}

	/**
	 * Checks whether this data object contains exception data.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains exception data.
	 */
	public static boolean hasExceptionData(InvocationSequenceData data) {
		return (null != data.getExceptionSensorDataObjects()) && !data.getExceptionSensorDataObjects().isEmpty();
	}

	/**
	 * Checks whether this data object contains SQL data.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object contains SQL data.
	 */
	public static boolean hasSQLData(InvocationSequenceData data) {
		return (null != data.getSqlStatementData()) && (1 == data.getSqlStatementData().getCount());
	}

	/**
	 * If invocation has span ident connected.
	 *
	 * @param data
	 *            InvocationSequenceData
	 * @return <code>true</code> if span ident exists in this invocation.
	 */
	public static boolean hasSpanIdent(InvocationSequenceData data) {
		return null != data.getSpanIdent();
	}

	/**
	 * Checks whether this data object has a parent element.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object has a parent element.
	 */
	public static boolean hasParentElementInSequence(InvocationSequenceData data) {
		return null != data.getParentSequence();
	}

	/**
	 * Checks whether this data object is the root element of the invocation.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return whether this data object is the root element of the invocation.
	 */
	public static boolean isRootElementInSequence(InvocationSequenceData data) {
		return !hasParentElementInSequence(data);
	}

	/**
	 * Returns root object of the sequence the given data belongs to by iterating till the tree
	 * root.
	 *
	 * @param data
	 *            Invocation
	 * @return Root invocation of the sequence given invocation belongs to.
	 */
	public static InvocationSequenceData getRootElementInSequence(InvocationSequenceData data) {
		InvocationSequenceData invoc = data;
		while (null != invoc.getParentSequence()) {
			invoc = invoc.getParentSequence();
		}
		return invoc;
	}

	/**
	 * Checks whether this data object has nested SQL statements.
	 *
	 * @param data
	 *            {@link InvocationSequenceData}
	 * @return True if it has nested SQLs, false otherwise.
	 */
	public static boolean hasNestedSqlStatements(InvocationSequenceData data) {
		return (null != data.isNestedSqlStatements()) && data.isNestedSqlStatements().booleanValue();
	}

	/**
	 * Checks whether this data object has nested SQL statements.
	 *
	 * @param data
	 *            {@link InvocationSequenceData}
	 * @return True if it has nested SQLs, false otherwise.
	 */
	public static boolean hasNestedExceptions(InvocationSequenceData data) {
		return (null != data.isNestedExceptions()) && data.isNestedExceptions().booleanValue();
	}

	/**
	 * Calculates the duration starting from this invocation sequence data element.
	 *
	 * @param data
	 *            the {@link InvocationSequenceData}s object.
	 * @return the duration starting from this invocation sequence data element.
	 */
	public static double calculateDuration(InvocationSequenceData data) {
		return calculateDuration(data, null);
	}

	/**
	 * Calculates the duration starting from this invocation sequence data element. Includes span
	 * duration as last resource if the span ident exists on the data.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @param spanService
	 *            Span service providing the additional span information if needed.
	 * @return the duration starting from this invocation sequence data element.
	 */
	public static double calculateDuration(InvocationSequenceData data, ISpanService spanService) {
		double duration = -1.0d;
		if (InvocationSequenceDataHelper.hasTimerData(data)) {
			duration = data.getTimerData().getDuration();
		} else if (InvocationSequenceDataHelper.hasSQLData(data)) {
			duration = data.getSqlStatementData().getDuration();
		} else if (InvocationSequenceDataHelper.isRootElementInSequence(data)) {
			duration = data.getDuration();
		} else if ((null != spanService) && hasSpanIdent(data)) {
			Span span = spanService.get(data.getSpanIdent());
			return span.getDuration();
		}
		return duration;
	}

	/**
	 * Calculates the exclusive time of this invocation sequence data element.
	 *
	 * @param data
	 *            the <code>InvocationSequenceData</code> object.
	 * @return the exclusive time of this invocation sequence data element.
	 */
	public static double calculateExclusiveTime(InvocationSequenceData data) {
		TimerData timerData;
		if (InvocationSequenceDataHelper.hasTimerData(data)) {
			timerData = data.getTimerData();
		} else if (InvocationSequenceDataHelper.hasSQLData(data)) {
			timerData = data.getSqlStatementData();
		} else {
			return data.getDuration() - computeNestedDuration(data);
		}

		if (timerData.isExclusiveTimeDataAvailable()) {
			return timerData.getExclusiveDuration();
		} else {
			return timerData.getDuration() - computeNestedDuration(data);
		}
	}

	/**
	 * Computes the duration of the nested invocation elements.
	 *
	 * @param data
	 *            The data objects which is inspected for its nested elements.
	 * @return The duration of all nested sequences (with their nested sequences as well).
	 */
	public static double computeNestedDuration(InvocationSequenceData data) {
		return computeNestedDuration(data, null);
	}

	/**
	 * Computes the duration of the nested invocation elements. Includes span duration as last
	 * resource if the span ident exists on the data.
	 *
	 * @param data
	 *            The data objects which is inspected for its nested elements.
	 * @param spanService
	 *            Span service providing the additional span information if needed.
	 * @return The duration of all nested sequences (with their nested sequences as well).
	 */
	public static double computeNestedDuration(InvocationSequenceData data, ISpanService spanService) {
		if (data.getNestedSequences().isEmpty()) {
			return 0;
		}

		double nestedDuration = 0d;
		for (InvocationSequenceData nestedData : data.getNestedSequences()) {
			double duration = calculateDuration(nestedData, spanService);
			if (duration != -1.0d) {
				nestedDuration += duration;
			} else if (!nestedData.getNestedSequences().isEmpty()) {
				// nothing was added, but there could be child elements with
				// time measurements
				nestedDuration += computeNestedDuration(nestedData, spanService);
			}
		}

		return nestedDuration;
	}

	/**
	 * Processes all the {@link SqlStatementData}s in the given invocations creating the list of the
	 * existing statement and calculating the total duration of the statements.
	 *
	 * @param invocationSequenceDataList
	 *            Input as list of invocations
	 * @param sqlStatementDataList
	 *            List where results will be stored. Needed because of reflection.
	 * @param totalDuration
	 *            {@link MutableDouble} where total duration will be stored.
	 */
	public static void collectSqlsInInvocations(List<InvocationSequenceData> invocationSequenceDataList, List<SqlStatementData> sqlStatementDataList, MutableDouble totalDuration) {
		for (InvocationSequenceData invocationSequenceData : invocationSequenceDataList) {
			if (null != invocationSequenceData.getSqlStatementData()) {
				sqlStatementDataList.add(invocationSequenceData.getSqlStatementData());
				totalDuration.add(invocationSequenceData.getSqlStatementData().getDuration());
			}
			if (CollectionUtils.isNotEmpty(invocationSequenceDataList)) {
				collectSqlsInInvocations(invocationSequenceData.getNestedSequences(), sqlStatementDataList, totalDuration);
			}
		}
	}

}
