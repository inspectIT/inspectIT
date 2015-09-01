package info.novatec.inspectit.communication.comparator;

import org.apache.commons.lang.BooleanUtils;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.communication.data.JmxSensorValueData;

/**
 * Comparators for the {@link JmxSensorValueData}.
 * 
 * @author Marius Oehler
 *
 */
public enum JmxDataComparatorEnum implements IDataComparator<JmxSensorValueData> {

	/**
	 * Sorts on the ability to chart the data.
	 */
	CHARTING,

	/**
	 * Sorts on the derived package name.
	 */
	DERIVED_DOMAINNAME,

	/**
	 * Sorts on the derived type name.
	 */
	DERIVED_TYPENAME,

	/**
	 * Sorts on the object name.
	 */
	OBJECTNAME,

	/**
	 * Sorts on the attribute name.
	 */
	ATTRIBUTENAME,

	/**
	 * Sorts on the value.
	 */
	VALUE,

	/**
	 * Sorts on the readable attribute.
	 */
	READABLE,

	/**
	 * Sorts on the writable attribute.
	 */
	WRITABLE,

	/**
	 * Sorts on the isIsGetter attribute.
	 */
	IS_ISGETTER;

	/**
	 * {@inheritDoc}
	 */
	public int compare(JmxSensorValueData o1, JmxSensorValueData o2, ICachedDataService cachedDataService) {
		switch (this) {
		case VALUE:
			return o1.getValue().compareTo(o2.getValue());
		case CHARTING:
			return BooleanUtils.toBooleanObject(o1.isBooleanOrNumeric()).compareTo(o2.isBooleanOrNumeric());
		default:
			break;
		}

		if (null == cachedDataService) {
			return 0;
		}

		JmxDefinitionDataIdent jmxIdent1 = cachedDataService.getJmxDefinitionDataIdentForId(o1.getJmxSensorDefinitionDataIdentId());
		JmxDefinitionDataIdent jmxIdent2 = cachedDataService.getJmxDefinitionDataIdentForId(o2.getJmxSensorDefinitionDataIdentId());

		switch (this) {
		case ATTRIBUTENAME:
			return jmxIdent1.getmBeanAttributeName().compareTo(jmxIdent2.getmBeanAttributeName());
		case DERIVED_DOMAINNAME:
			return jmxIdent1.getDerivedDomainName().compareTo(jmxIdent2.getDerivedDomainName());
		case DERIVED_TYPENAME:
			return jmxIdent1.getDerivedTypeName().compareTo(jmxIdent2.getDerivedTypeName());
		case OBJECTNAME:
			return jmxIdent1.getmBeanObjectName().compareTo(jmxIdent2.getmBeanObjectName());
		case READABLE:
			return jmxIdent1.getmBeanAttributeIsReadable().compareTo(jmxIdent2.getmBeanAttributeIsReadable());
		case WRITABLE:
			return jmxIdent1.getmBeanAttributeIsWritable().compareTo(jmxIdent2.getmBeanAttributeIsWritable());
		case IS_ISGETTER:
			return jmxIdent1.getmBeanAttributeIsIs().compareTo(jmxIdent2.getmBeanAttributeIsIs());
		default:
			return 0;
		}

	}

}
