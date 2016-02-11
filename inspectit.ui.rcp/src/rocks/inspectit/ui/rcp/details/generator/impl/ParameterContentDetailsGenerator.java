package info.novatec.inspectit.rcp.details.generator.impl;

import info.novatec.inspectit.communication.DefaultData;
import info.novatec.inspectit.communication.MethodSensorData;
import info.novatec.inspectit.communication.data.ParameterContentData;
import info.novatec.inspectit.communication.data.ParameterContentType;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.details.DetailsTable;
import info.novatec.inspectit.rcp.details.generator.IDetailsGenerator;
import info.novatec.inspectit.rcp.repository.RepositoryDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Details generator for the parameter content data in the {@link MethodSensorData}.
 * 
 * @author Ivan Senic
 * 
 */
public class ParameterContentDetailsGenerator implements IDetailsGenerator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canGenerateFor(DefaultData defaultData) {
		return defaultData instanceof MethodSensorData && CollectionUtils.isNotEmpty(((MethodSensorData) defaultData).getParameterContentData());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DetailsTable generate(DefaultData defaultData, RepositoryDefinition repositoryDefinition, Composite parent, FormToolkit toolkit) {
		Map<ParameterContentType, List<ParameterContentData>> contentMap = getContentTypeMap(((MethodSensorData) defaultData).getParameterContentData());

		DetailsTable table = new DetailsTable(parent, toolkit, "Parameter Content Data", 1);

		for (Map.Entry<ParameterContentType, List<ParameterContentData>> entry : contentMap.entrySet()) {
			List<String[]> rows = new ArrayList<>();
			for (ParameterContentData data : entry.getValue()) {
				rows.add(new String[] { data.getName(), data.getContent() });
			}
			String heading = StringUtils.capitalize(entry.getKey().toString().toLowerCase()) + ":";
			table.addContentTable(heading, getImageForParameterContentType(entry.getKey()), 2, new String[] { "Name", "Value" }, rows);
		}

		return table;
	}

	/**
	 * Returns map of the {@link ParameterContentData} divided by the {@link ParameterContentType}s.
	 * 
	 * @param parameterContentDatas
	 *            Data to divide in groups.
	 * @return Map<ParameterContentType, Collection<ParameterContentData>>
	 */
	private Map<ParameterContentType, List<ParameterContentData>> getContentTypeMap(Collection<ParameterContentData> parameterContentDatas) {
		if (CollectionUtils.isEmpty(parameterContentDatas)) {
			return Collections.emptyMap();
		}

		Map<ParameterContentType, List<ParameterContentData>> map = new HashMap<ParameterContentType, List<ParameterContentData>>();
		for (ParameterContentData data : parameterContentDatas) {
			List<ParameterContentData> collection = map.get(data.getContentType());
			if (null == collection) {
				collection = new ArrayList<>(1);
				map.put(data.getContentType(), collection);
			}
			collection.add(data);
		}

		for (List<ParameterContentData> paramList : map.values()) {
			Collections.sort(paramList);
		}

		return map;
	}

	/**
	 * Returns icon for {@link ParameterContentType}.
	 * 
	 * @param parameterContentType
	 *            {@link ParameterContentType}.
	 * @return Returns icon for {@link ParameterContentType}.
	 */
	private Image getImageForParameterContentType(ParameterContentType parameterContentType) {
		if (parameterContentType == ParameterContentType.FIELD) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_FIELD);
		} else if (parameterContentType == ParameterContentType.PARAM) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_PARAMETER);
		} else if (parameterContentType == ParameterContentType.RETURN) {
			return InspectIT.getDefault().getImage(InspectITImages.IMG_RETURN);
		}
		return null;
	}
}
