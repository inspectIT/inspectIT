package info.novatec.inspectit.rcp.util.data;

import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdentHelper;
import info.novatec.inspectit.communication.data.AggregatedHttpTimerData;
import info.novatec.inspectit.communication.data.HttpTimerData;
import info.novatec.inspectit.communication.data.HttpTimerDataHelper;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

/**
 * Simple extension of {@link AggregatedHttpTimerData}.
 * 
 * @author Ivan Senic
 * 
 */
public class RegExAggregatedHttpTimerData extends AggregatedHttpTimerData {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = -7733383915089404688L;

	/**
	 * Transformed URI.
	 */
	private String transformedUri;

	/**
	 * List of aggregated HTTP datas.
	 */
	private List<HttpTimerData> aggregatedDataList = new ArrayList<>();

	/**
	 * No-arg constructor.
	 */
	public RegExAggregatedHttpTimerData() {
	}

	/**
	 * Default constructor.
	 * 
	 * @param transformedUri
	 *            Transformed URI.
	 */
	public RegExAggregatedHttpTimerData(String transformedUri) {
		this.transformedUri = transformedUri;
	}

	/**
	 * Returns transformed URI.
	 * 
	 * @param object
	 *            {@link HttpTimerData}
	 * @param httpSensorTypeIdent
	 *            Sensor that holds the regEx expression and the template.
	 * @return Transformed URI or URI if regular expression is not provided or can not be compiled.
	 */
	public static String getTransformedUri(HttpTimerData object, MethodSensorTypeIdent httpSensorTypeIdent) {
		try {
			if (null != httpSensorTypeIdent && null != MethodSensorTypeIdentHelper.getRegEx(httpSensorTypeIdent)) {
				return HttpTimerDataHelper.getTransformedUri(object, MethodSensorTypeIdentHelper.getRegEx(httpSensorTypeIdent), MethodSensorTypeIdentHelper.getRegExTemplate(httpSensorTypeIdent));
			} else {
				return object.getHttpInfo().getUri();
			}
		} catch (IllegalArgumentException e) {
			return object.getHttpInfo().getUri();
		}
	}

	/**
	 * Gets {@link #transformedUri}.
	 * 
	 * @return {@link #transformedUri}
	 */
	public String getTransformedUri() {
		return transformedUri;
	}

	/**
	 * Sets {@link #transformedUri}.
	 * 
	 * @param transformedUri
	 *            New value for {@link #transformedUri}
	 */
	public void setTransformedUri(String transformedUri) {
		this.transformedUri = transformedUri;
	}

	/**
	 * Gets {@link #aggregatedDataList}.
	 * 
	 * @return {@link #aggregatedDataList}
	 */
	public List<HttpTimerData> getAggregatedDataList() {
		return aggregatedDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(transformedUri, aggregatedDataList);
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
		RegExAggregatedHttpTimerData that = (RegExAggregatedHttpTimerData) object;
		return Objects.equal(this.transformedUri, that.transformedUri) && Objects.equal(this.aggregatedDataList, that.aggregatedDataList);
	}
}
