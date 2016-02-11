package info.novatec.inspectit.rcp.model.storage;

import info.novatec.inspectit.rcp.formatter.ImageFormatter;
import info.novatec.inspectit.rcp.formatter.TextFormatter;
import info.novatec.inspectit.rcp.model.Composite;
import info.novatec.inspectit.rcp.model.Leaf;
import info.novatec.inspectit.storage.LocalStorageData;
import info.novatec.inspectit.storage.label.AbstractStorageLabel;
import info.novatec.inspectit.storage.label.type.AbstractStorageLabelType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * Tree model manager for storage manager view that displays the local storage data.
 * 
 * @author Ivan Senic
 * 
 */
public class LocalStorageTreeModelManager {

	/**
	 * Collection of {@link LocalStorageData} to be displayed in tree.
	 */
	private Collection<LocalStorageData> localStorageDataCollection;

	/**
	 * Label type for grouping.
	 */
	private AbstractStorageLabelType<?> storageLabelType;

	/**
	 * Default constructor.
	 * 
	 * @param localStorageDataCollection
	 *            Collection of {@link LocalStorageData} to be displayed in tree.
	 * @param storageLabelType
	 *            Label type for grouping.
	 */
	public LocalStorageTreeModelManager(Collection<LocalStorageData> localStorageDataCollection, AbstractStorageLabelType<?> storageLabelType) {
		super();
		this.localStorageDataCollection = localStorageDataCollection;
		this.storageLabelType = storageLabelType;
	}

	/**
	 * Returns objects divided either by the provided label class, or by
	 * {@link info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition} they are located to.
	 * 
	 * @return Returns objects divided either by the provided label class, or by
	 *         {@link info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition} they are
	 *         located to.
	 */
	public Object[] getRootObjects() {
		if (CollectionUtils.isEmpty(localStorageDataCollection)) {
			return new Object[0];
		}

		if (null != storageLabelType) {
			Composite unknown = new Composite();
			unknown.setName("Unknown");
			unknown.setImage(ImageFormatter.getImageForLabel(storageLabelType));
			boolean addUnknown = false;
			Map<Object, Composite> map = new HashMap<Object, Composite>();
			for (LocalStorageData localStorageData : localStorageDataCollection) {
				List<? extends AbstractStorageLabel<?>> labelList = localStorageData.getLabels(storageLabelType);
				if (CollectionUtils.isNotEmpty(labelList)) {
					for (AbstractStorageLabel<?> label : labelList) {
						Composite c = map.get(TextFormatter.getLabelValue(label, true));
						if (c == null) {
							c = new Composite();
							c.setName(TextFormatter.getLabelName(label) + ": " + TextFormatter.getLabelValue(label, true));
							c.setImage(ImageFormatter.getImageForLabel(storageLabelType));
							map.put(TextFormatter.getLabelValue(label, true), c);
						}
						LocalStorageLeaf localStorageLeaf = new LocalStorageLeaf(localStorageData);
						localStorageLeaf.setParent(c);
						c.addChild(localStorageLeaf);
					}
				} else {
					unknown.addChild(new LocalStorageLeaf(localStorageData));
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
			List<Leaf> leafList = new ArrayList<Leaf>();
			for (LocalStorageData localStorageData : localStorageDataCollection) {
				leafList.add(new LocalStorageLeaf(localStorageData));
			}
			return leafList.toArray();
		}
	}

}
