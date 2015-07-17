package info.novatec.inspectit.rcp.model;

import info.novatec.inspectit.cmr.model.MethodIdent;
import info.novatec.inspectit.cmr.model.MethodIdentToSensorType;
import info.novatec.inspectit.cmr.model.MethodSensorTypeIdent;
import info.novatec.inspectit.rcp.editor.inputdefinition.EditorPropertiesData;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition.IdDefinition;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.util.ObjectUtils;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

import com.google.common.base.Objects;

/**
 * This composite shows only one sensor for each method of the class that has a
 * {@link SensorTypeEnum} type.
 * 
 * @author Ivan Senic
 * 
 */
public class FilteredDeferredClassComposite extends DeferredClassComposite {

	/**
	 * Sensor to show.
	 */
	private SensorTypeEnum sensorTypeEnumToShow;

	/**
	 * @param sensorTypeEnum
	 *            Set the sensor type to show.
	 */
	public FilteredDeferredClassComposite(SensorTypeEnum sensorTypeEnum) {
		this.sensorTypeEnumToShow = sensorTypeEnum;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		try {
			List<MethodIdent> methods = getMethods();
			Composite classComposite = (Composite) object;
			monitor.beginTask("Loading of Method Elements...", methods.size());
			for (MethodIdent method : methods) {
				for (MethodIdentToSensorType methodIdentToSensorType : method.getMethodIdentToSensorTypes()) {
					if (!isHideInactiveInstrumentations() || methodIdentToSensorType.isActive()) {
						MethodSensorTypeIdent methodSensorTypeIdent = methodIdentToSensorType.getMethodSensorTypeIdent();
						String fqn = methodSensorTypeIdent.getFullyQualifiedClassName();
						SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(fqn);
						if (sensorTypeEnum == sensorTypeEnumToShow) { // NOPMD
							if (sensorTypeEnum.isOpenable()) {
								Component targetSensorType = new Leaf();
								targetSensorType.setEnabled(methodIdentToSensorType.isActive());
								if (null != method.getParameters()) {
									String parameters = method.getParameters().toString();
									parameters = parameters.substring(1, parameters.length() - 1);

									targetSensorType.setName(String.format(METHOD_FORMAT, method.getMethodName(), parameters));
								} else {
									targetSensorType.setName(String.format(METHOD_FORMAT, method.getMethodName(), ""));
								}
								targetSensorType.setImage(ModifiersImageFactory.getImage(method.getModifiers()));

								InputDefinition inputDefinition = new InputDefinition();
								inputDefinition.setRepositoryDefinition(getRepositoryDefinition());
								inputDefinition.setId(sensorTypeEnum);

								EditorPropertiesData editorPropertiesData = new EditorPropertiesData();
								editorPropertiesData.setSensorImage(sensorTypeEnum.getImage());
								editorPropertiesData.setSensorName(sensorTypeEnum.getDisplayName());
								editorPropertiesData.setViewName(TextFormatter.getMethodString(method));
								editorPropertiesData.setViewImage(ModifiersImageFactory.getImage(method.getModifiers()));
								inputDefinition.setEditorPropertiesData(editorPropertiesData);

								IdDefinition idDefinition = new IdDefinition();
								idDefinition.setPlatformId(method.getPlatformIdent().getId());
								idDefinition.setSensorTypeId(methodSensorTypeIdent.getId());
								idDefinition.setMethodId(method.getId());

								inputDefinition.setIdDefinition(idDefinition);
								targetSensorType.setInputDefinition(inputDefinition);

								collector.add(targetSensorType, monitor);
								classComposite.addChild(targetSensorType);
							}
							break;
						}
					}
				}

				monitor.worked(1);
				if (monitor.isCanceled()) {
					break;
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean select(MethodIdent methodIdent) {
		for (MethodIdentToSensorType methodIdentToSensorType : methodIdent.getMethodIdentToSensorTypes()) {
			SensorTypeEnum sensorTypeEnum = SensorTypeEnum.get(methodIdentToSensorType.getMethodSensorTypeIdent().getFullyQualifiedClassName());
			if (ObjectUtils.equals(sensorTypeEnum, sensorTypeEnumToShow)) {
				return super.select(methodIdent);
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
		FilteredDeferredClassComposite that = (FilteredDeferredClassComposite) object;
		return Objects.equal(this.sensorTypeEnumToShow, that.sensorTypeEnumToShow);
	}

}
