package info.novatec.inspectit.ci.business.valuesource.impl;

import info.novatec.inspectit.ci.business.expression.impl.StringMatchingExpression;
import info.novatec.inspectit.ci.business.valuesource.StringValueSource;
import info.novatec.inspectit.cmr.model.PlatformIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.InvocationSequenceData;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This configuration element indicates that the host name or IP is to be used as the string value
 * for the {@link StringMatchingExpression}.
 *
 * @author Alexander Wert
 *
 */
@XmlRootElement(name = "host")
public class HostValueSource extends StringValueSource {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getStringValues(InvocationSequenceData invocSequence, ICachedDataService cachedDataService) {
		PlatformIdent pIdent = cachedDataService.getPlatformIdentForId(invocSequence.getPlatformIdent());
		List<String> ipList = pIdent.getDefinedIPs();
		String[] result = new String[ipList.size()];
		ipList.toArray(result);
		return result;
	}

}
