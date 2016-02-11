package info.novatec.inspectit.rcp.model.storage;

import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.GroupedLabelsComposite;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;
import info.novatec.inspectit.storage.StorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * Tree model manager for storage manager view.
 * 
 * @author Ivan Senic
 * 
 */
public class StorageTreeModelManager {

	/**
	 * Storage and repository map.
	 */
	private Map<StorageData, CmrRepositoryDefinition> storageRepositoryMap;

	/**
	 * Label type for grouping.
	 */
	private AbstractStorageLabelType<?> storageLabelType;

	/**
	 * @param storageRepositoryMap
	 *            map of {@link StorageData} objects and repositories where they are located.
	 * @param storageLabelType
	 *            {@link AbstractStorageLabelType} to define the label ordering. It can be null,
	 *            then Storages will be ordered by repository.
	 */
	public StorageTreeModelManager(Map<StorageData, CmrRepositoryDefinition> storageRepositoryMap, AbstractStorageLabelType<?> storageLabelType) {
		super();
		this.storageRepositoryMap = storageRepositoryMap;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * Returns objects divided either by the provided label class, or by
	 * {@link CmrRepositoryDefinition} they are located to.
	 * 
	 * @return Returns objects divided either by the provided label class, or by
	 *         {@link CmrRepositoryDefinition} they are located to.
	 */
	public Object[] getRootObjects() {
		if (null == storageRepositoryMap || storageRepositoryMap.isEmpty()) {
			return new Object[0];
		}

		if (null != storageLabelType) {
			Composite unknown = new GroupedLabelsComposite();
			unknown.setName("Unknown");
			unknown.setImage(ImageFormatter.getImageForLabel(storageLabelType));
			boolean addUnknown = false;
			Map<Object, Composite> map = new HashMap<Object, Composite>();
			for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : storageRepositoryMap.entrySet()) {
				List<? extends AbstractStorageLabel<?>> labelList = entry.getKey().getLabels(storageLabelType);
				if (CollectionUtils.isNotEmpty(labelList)) {
					for (AbstractStorageLabel<?> label : labelList) {
						Composite c = map.get(TextFormatter.getLabelValue(label, true));
						if (c == null) {
							c = new GroupedLabelsComposite(label);
							c.setName(TextFormatter.getLabelName(label) + ": " + TextFormatter.getLabelValue(label, true));
							c.setImage(ImageFormatter.getImageForLabel(storageLabelType));
							map.put(TextFormatter.getLabelValue(label, true), c);
						}
						StorageLeaf storageLeaf = new StorageLeaf(entry.getKey(), entry.getValue());
						storageLeaf.setParent(c);
						c.addChild(storageLeaf);
					}
				} else {
					unknown.addChild(new StorageLeaf(entry.getKey(), entry.getValue()));
					addUnknown = true;
				}
			}
			ArrayList<Composite> returnList = new ArrayList<Composite>();
			returnList.addAll(map.values());
			if (addUnknown) {
				returnList.add(unknown);
			}
			return returnList.toArray(new Composite[returnList.size()]);
		} else {
			Map<CmrRepositoryDefinition, Composite> map = new HashMap<CmrRepositoryDefinition, Composite>();
			for (Map.Entry<StorageData, CmrRepositoryDefinition> entry : storageRepositoryMap.entrySet()) {
				CmrRepositoryDefinition cmrRepositoryDefinition = entry.getValue();
				Composite c = map.get(cmrRepositoryDefinition);
				if (c == null) {
					c = new Composite();
					c.setName(cmrRepositoryDefinition.getName());
					c.setImage(InspectIT.getDefault().getImage(InspectITImages.IMG_SERVER_ONLINE_SMALL));
					map.put(cmrRepositoryDefinition, c);
				}
				StorageLeaf storageLeaf = new StorageLeaf(entry.getKey(), entry.getValue());
				storageLeaf.setParent(c);
				c.addChild(storageLeaf);
			}
			return map.values().toArray(new Composite[map.values().size()]);
		}
	}
}
