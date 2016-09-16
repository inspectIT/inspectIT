package rocks.inspectit.server.processor.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.influx.dao.impl.InfluxDBDao;
import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.EUMData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * Processor for writing EUM Data, captured by the Javascript Agent, into the Influx DB.
 *
 * @author David Monschein
 *
 */
public class EUMDataCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBDao influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		EUMData data = (EUMData) defaultData;

		String baseHost = getDomainName(data.getBaseUrl());

		// write it into influxdb
		for (PageLoadRequest plReq : data.getPageLoadRequests()) {
			plReq.baseline();

			Point point = createPageloadPoint(data, plReq, "request_pageload");

			influxDb.insert(point);
		}

		for (ResourceLoadRequest rlReq : data.getResourceLoadRequests()) {
			String host = getDomainName(rlReq.getUrl());

			Point point;
			if (host.equalsIgnoreCase(baseHost)) {
				// 1st party
				point = createRequestPoint(data, rlReq, "request_resourceload", true);
			} else {
				// 3rd party
				point = createRequestPoint(data, rlReq, "request_resourceload", false);
			}

			influxDb.insert(point);
		}

		for (AjaxRequest ajaxReq : data.getAjaxRequests()) {

			Point point = createAjaxPoint(data, ajaxReq, "request_ajax");

			influxDb.insert(point);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return influxDb.isOnline() && (defaultData instanceof EUMData);
	}

	/**
	 * Gets the host name from an URL.
	 *
	 * @param url
	 *            the complete URL
	 * @return the name of the host
	 */
	private String getDomainName(String url) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return "";
		}
		return uri.getHost();
	}

	/**
	 * Creates a point for an request load.
	 *
	 * @param data
	 *            the eum data object
	 * @param rlReq
	 *            the resource load request object
	 * @param measurement
	 *            the name of the measurement (in influx)
	 * @param firstparty
	 *            is the request 1st party or 3rd?
	 * @return a point which can be inserted into the influxDB
	 */
	private Point createRequestPoint(EUMData data, ResourceLoadRequest rlReq, String measurement, boolean firstparty) {
		return Point.measurement(measurement).time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.tag("browser", data.getUserSession().getBrowser())
				.tag("device", data.getUserSession().getDevice())
				.tag("language", data.getUserSession().getLanguage())
				.tag("sessId", data.getUserSession().getSessionId())
				.tag("host", getDomainName(rlReq.getUrl()))
				.tag("name", rlReq.getUrl())
				.tag("initiatorUrl", rlReq.getInitiatorUrl())
				.tag("initiatorType", rlReq.getInitiatorType())
				.tag("firstparty", String.valueOf(firstparty)) // bool not allowed
				.addField("browser", data.getUserSession().getBrowser())
				.addField("os", data.getUserSession().getDevice())
				.addField("language", data.getUserSession().getLanguage())
				.addField("url", rlReq.getUrl())
				.addField("initiatorType", rlReq.getInitiatorType())
				.addField("endTime", rlReq.getEndTime())
				.addField("name", rlReq.getUrl())
				.addField("duration", rlReq.getEndTime() - rlReq.getStartTime())
				.addField("size", rlReq.getTransferSize())
				.addField("startTime", rlReq.getStartTime()).build();
	}

	/**
	 * Creates a point for an pageload request.
	 *
	 * @param data
	 *            the eum data object
	 * @param plReq
	 *            the page load object holding informations
	 * @param measurement
	 *            the name of the measurement (in influx)
	 * @return a point which can be inserted into the influxDB.
	 */
	private Point createPageloadPoint(EUMData data, PageLoadRequest plReq, String measurement) {
		return Point.measurement(measurement).time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.tag("browser", data.getUserSession().getBrowser())
				.tag("device", data.getUserSession().getDevice())
				.tag("language", data.getUserSession().getLanguage())
				.tag("url", plReq.getUrl())
				.tag("sessId", data.getUserSession().getSessionId())
				.addField("browser", data.getUserSession().getBrowser())
				.addField("os", data.getUserSession().getDevice())
				.addField("language", data.getUserSession().getLanguage())
				.addField("url", plReq.getUrl())
				.addField("navigationStart", plReq.getNavigationStartW())
				.addField("connectEnd", plReq.getConnectEndW())
				.addField("connectStart", plReq.getConnectStartW())
				.addField("domContentLoadedEventStart", plReq.getDomContentLoadedEventStartW())
				.addField("domContentLoadedEventEnd", plReq.getDomContentLoadedEventEndW())
				.addField("domInteractive", plReq.getDomInteractiveW())
				.addField("domLoading", plReq.getDomLoadingW())
				.addField("domainLookupStart", plReq.getDomainLookupStartW())
				.addField("domainLookupEnd", plReq.getDomainLookupEndW())
				.addField("fetchStart", plReq.getFetchStartW())
				.addField("loadEventStart", plReq.getLoadEventStartW())
				.addField("loadEventEnd", plReq.getLoadEventEndW())
				.addField("redirectStart", plReq.getRedirectStartW())
				.addField("redirectEnd", plReq.getRedirectEndW())
				.addField("requestStart", plReq.getRequestStartW())
				.addField("responseStart", plReq.getResponseStartW())
				.addField("responseEnd", plReq.getResponseEndW())
				.addField("unloadEventStart", plReq.getUnloadEventStartW())
				.addField("unloadEventEnd", plReq.getUnloadEventEndW())
				.addField("speedindex", plReq.getSpeedindex())
				.addField("firstpaint", plReq.getFirstpaint())
				.addField("resourceCount", ((double) data.getResourceLoadRequests().size()) / ((double) data.getPageLoadRequests().size()))
				.build();
	}

	/**
	 * Creates a point for inserting into the influxDB representing an ajax request.
	 *
	 * @param data
	 *            the eum data object
	 * @param ajaxReq
	 *            the object which contains informations about the ajax request
	 * @param measurement
	 *            the name of the measurement (in influx)
	 * @return a point which represents an ajax request.
	 */
	private Point createAjaxPoint(EUMData data, AjaxRequest ajaxReq, String measurement) {
		return Point.measurement(measurement).time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
				.tag("browser", data.getUserSession().getBrowser())
				.tag("device", data.getUserSession().getDevice())
				.tag("language", data.getUserSession().getLanguage())
				.tag("url", ajaxReq.getUrl())
				.tag("sessId", data.getUserSession().getSessionId())
				.addField("baseurl", ajaxReq.getBaseUrl())
				.addField("browser", data.getUserSession().getBrowser())
				.addField("os", data.getUserSession().getDevice())
				.addField("language", data.getUserSession().getLanguage())
				.addField("url", ajaxReq.getUrl())
				.addField("status", ajaxReq.getStatus())
				.addField("duration", ajaxReq.getEndTime() - ajaxReq.getStartTime())
				.addField("method", ajaxReq.getMethod())
				.addField("endTime", ajaxReq.getEndTime())
				.addField("startTime", ajaxReq.getStartTime()).build();
	}

}
