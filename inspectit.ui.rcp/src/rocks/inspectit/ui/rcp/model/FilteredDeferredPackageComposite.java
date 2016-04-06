package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.util.ObjectUtils;

import com.google.common.base.Objects;

/**
 * Filtered package composite delegates the children creation to the
 * {@link FilteredDeferredClassComposite}.
 * 
 * @author Ivan Senic
 * 
 */
public class FilteredDeferredPackageComposite extends DeferredPackageComposite {

	/**
	 * Sensor to show.
	 */
	private SensorTypeEnum sensorTypeEnumToShow;

	/**
	 * @param sensorTypeEnum
	 *            Set the sensor type to show.
	 */
	public FilteredDeferredPackageComposite(SensorTypeEnum sensorTypeEnum) {
		this.sensorTypeEnumToShow = sensorTypeEnum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DeferredClassComposite getNewChild() {
		return new FilteredDeferredClassComposite(sensorTypeEnumToShow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean select(MethodIdent methodIdent) {
		for (MethodIdentToSensorType methodIdentToSensorType : methodIdent.getMethodIdentToSensorTypes()) {
			SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(methodIdentToSensorType.getMethodSensorTypeIdent().getFullyQualifiedClassName());
			if (ObjectUtils.equals(sensorTypeEnum, sensorTypeEnumToShow)) {
				if (!isHideInactiveInstrumentations() || methodIdentToSensorType.isActive()) {
					return super.select(methodIdent);
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hashCode(super.hashCode(), sensorTypeEnumToShow);
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
		if (!super.equals(object)) {
			return false;
		}
		FilteredDeferredPackageComposite that = (FilteredDeferredPackageComposite) object;
		return Objects.equal(this.sensorTypeEnumToShow, that.sensorTypeEnumToShow);
	}

}
