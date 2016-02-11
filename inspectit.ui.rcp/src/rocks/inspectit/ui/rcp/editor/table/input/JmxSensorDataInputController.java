package info.novatec.inspectit.rcp.editor.table.input;

import info.novatec.inspectit.cmr.model.JmxDefinitionDataIdent;
import info.novatec.inspectit.cmr.service.ICachedDataService;
import info.novatec.inspectit.cmr.service.IJmxDataAccessService;
import info.novatec.inspectit.communication.comparator.DefaultDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.IDataComparator;
import info.novatec.inspectit.communication.comparator.JmxDataComparatorEnum;
import info.novatec.inspectit.communication.comparator.ResultComparator;
import info.novatec.inspectit.communication.data.JmxSensorValueData;
import info.novatec.inspectit.rcp.InspectIT;
import info.novatec.inspectit.rcp.InspectITImages;
import info.novatec.inspectit.rcp.editor.inputdefinition.InputDefinition;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceEventCallback.PreferenceEvent;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId;
import info.novatec.inspectit.rcp.editor.preferences.PreferenceId.LiveMode;
import info.novatec.inspectit.rcp.editor.root.IRootEditor;
import info.novatec.inspectit.rcp.editor.table.TableViewerComparator;
import info.novatec.inspectit.rcp.editor.viewers.StyledCellIndexLabelProvider;
import info.novatec.inspectit.rcp.formatter.NumberFormatter;
import info.novatec.inspectit.rcp.repository.CmrRepositoryDefinition;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

/**
 * This input controller displays the acquired jmx sensor data in a table.
 * 
 * @author Alfred Krauss
 * @author Marius Oehler
 * 
 */
public class JmxSensorDataInputController extends AbstractTableInputController {

	/**
	 * The private inner enumeration used to define the used IDs which are mapped into the columns.
	 * The order in this enumeration represents the order of the columns. If it is reordered,
	 * nothing else has to be changed.
	 * 
	 * @author Alfred Krauss
	 * @author Marius Oehler
	 * 
	 */
	private static enum Column {
		/** Icon whether data can be charted. */
		CHARTING("Charting", 20, null, JmxDataComparatorEnum.CHARTING),
		/** The column for the domain name. */
		DOMAINNAME("Domain", 250, InspectITImages.IMG_PACKAGE, JmxDataComparatorEnum.DERIVED_DOMAINNAME),
		/** The column for the object name. */
		TYPENAME("Type", 250, InspectITImages.IMG_BOOK, JmxDataComparatorEnum.DERIVED_TYPENAME),
		/** The column for the attribute name. */
		ATTRIBUTE("Attribute", 300, InspectITImages.IMG_BLUE_DOCUMENT_TABLE, JmxDataComparatorEnum.ATTRIBUTENAME),
		/** The column for the time stamp when the last value was acquired. */
		TIMESTAMP("Timestamp", 100, InspectITImages.IMG_TIMESTAMP, DefaultDataComparatorEnum.TIMESTAMP),
		/** The column for the most recent value of the attribute. */
		VALUE("Value", 150, null, JmxDataComparatorEnum.VALUE),
		/** The column indicates if the attribute is readable. */
		ISREADABLE("Readable", 100, null, JmxDataComparatorEnum.READABLE),
		/** The column indicates if the attribute is writable. */
		ISWRITEABLE("Writeable", 100, null, JmxDataComparatorEnum.WRITABLE);

		/**
		 * The name.
		 */
		private String name;

		/**
		 * The width of the column.
		 */
		private int width;

		/**
		 * The image descriptor. Can be <code>null</code>
		 */
		private Image image;

		/** Comparator for the column. */
		private IDataComparator<? super JmxSensorValueData> dataComparator;

		/**
		 * Default constructor which creates a column enumeration object.
		 * 
		 * @param name
		 *            The name of the column.
		 * @param width
		 *            The width of the column.
		 * @param imageName
		 *            The name of the image. Names are defined in
		 *            {@link info.novatec.inspectit.rcp.InspectITImages}.
		 * @param dataComparator
		 *            Comparator for the column.
		 */
		private Column(String name, int width, String imageName, IDataComparator<? super JmxSensorValueData> dataComparator) {
			this.name = name;
			this.width = width;
			this.image = InspectIT.getDefault().getImage(imageName);
			this.dataComparator = dataComparator;
		}

		/**
		 * Converts an ordinal into a column.
		 * 
		 * @param i
		 *            The ordinal.
		 * @return The appropriate column.
		 */
		public static Column fromOrd(int i) {
			if (i < 0 || i >= Column.values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return Column.values()[i];
		}
	}

	/**
	 * The cached service is needed because of the ID mappings.
	 */
	private ICachedDataService cachedDataService;

	/**
	 * List that is displayed after processing the invocation.
	 */
	private List<JmxSensorValueData> jmxDataList = new ArrayList<>(0);

	/**
	 * The used data access service to access the data on the CMR.
	 */
	private IJmxDataAccessService jmxDataAccessService;

	/**
	 * The template object which is send to the server.
	 */
	private JmxSensorValueData template;

	/**
	 * Empty styled string.
	 */
	private final StyledString emptyStyledString = new StyledString();

	/**
	 * Date to display invocations from.
	 */
	private Date fromDate = null;

	/**
	 * Date to display invocations to.
	 */
	private Date toDate = null;

	/**
	 * Are we in live mode.
	 */
	private boolean autoUpdate = LiveMode.ACTIVE_DEFAULT;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInputDefinition(InputDefinition inputDefinition) {
		super.setInputDefinition(inputDefinition);

		template = new JmxSensorValueData();
		template.setPlatformIdent(inputDefinition.getIdDefinition().getPlatformId());
		template.setSensorTypeIdent(inputDefinition.getIdDefinition().getSensorTypeId());
		template.setJmxSensorDefinitionDataIdentId(inputDefinition.getIdDefinition().getJmxDefinitionId());

		jmxDataAccessService = inputDefinition.getRepositoryDefinition().getJmxDataAccessService();
		cachedDataService = inputDefinition.getRepositoryDefinition().getCachedDataService();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createColumns(TableViewer tableViewer) {
		for (Column column : Column.values()) {
			TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
			viewerColumn.getColumn().setMoveable(true);
			viewerColumn.getColumn().setResizable(true);
			viewerColumn.getColumn().setText(column.name);
			viewerColumn.getColumn().setWidth(column.width);
			if (null != column.image) {
				viewerColumn.getColumn().setImage(column.image);
			}
			mapTableViewerColumn(column, viewerColumn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<PreferenceId> getPreferenceIds() {
		Set<PreferenceId> preferences = EnumSet.noneOf(PreferenceId.class);
		if (getInputDefinition().getRepositoryDefinition() instanceof CmrRepositoryDefinition) {
			preferences.add(PreferenceId.LIVEMODE);
		}
		preferences.add(PreferenceId.UPDATE);
		preferences.add(PreferenceId.TIMELINE);
		return preferences;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void preferenceEventFired(PreferenceEvent preferenceEvent) {
		switch (preferenceEvent.getPreferenceId()) {
		case TIMELINE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.FROM_DATE_ID)) {
				fromDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.FROM_DATE_ID);
			}
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.TimeLine.TO_DATE_ID)) {
				toDate = (Date) preferenceEvent.getPreferenceMap().get(PreferenceId.TimeLine.TO_DATE_ID);
			}
			break;
		case LIVEMODE:
			if (preferenceEvent.getPreferenceMap().containsKey(PreferenceId.LiveMode.BUTTON_LIVE_ID)) {
				autoUpdate = (Boolean) preferenceEvent.getPreferenceMap().get(PreferenceId.LiveMode.BUTTON_LIVE_ID);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTableInput() {
		return jmxDataList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doRefresh(IProgressMonitor monitor, IRootEditor rootEditor) {
		monitor.beginTask("Updating JMX Overview", IProgressMonitor.UNKNOWN);
		monitor.subTask("Retrieving the JMX Overview");

		List<JmxSensorValueData> data;
		if (autoUpdate || fromDate == null || toDate == null) {
			data = (List<JmxSensorValueData>) jmxDataAccessService.getJmxDataOverview(template);
		} else {
			data = (List<JmxSensorValueData>) jmxDataAccessService.getJmxDataOverview(template, fromDate, toDate);
		}

		jmxDataList.clear();
		if (CollectionUtils.isNotEmpty(data)) {
			jmxDataList.addAll(data);
		}

		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	public ViewerComparator getComparator() {
		TableViewerComparator<JmxSensorValueData> timerDataViewerComparator = new TableViewerComparator<JmxSensorValueData>();
		for (Column column : Column.values()) {
			ResultComparator<JmxSensorValueData> resultComparator = new ResultComparator<JmxSensorValueData>(column.dataComparator, cachedDataService);
			timerDataViewerComparator.addColumn(getMappedTableViewerColumn(column).getColumn(), resultComparator);
		}

		return timerDataViewerComparator;
	}

	/**
	 * {@inheritDoc}
	 */
	public IContentProvider getContentProvider() {
		return new ArrayContentProvider();
	}

	/**
	 * {@inheritDoc}
	 */
	public IBaseLabelProvider getLabelProvider() {
		return new LabelProvider();
	}

	/**
	 * The label provider.
	 * 
	 * @author Alfred Krauss
	 * 
	 */
	private final class LabelProvider extends StyledCellIndexLabelProvider {

		/**
		 * Creates the styled text.
		 * 
		 * @param element
		 *            The element to create the styled text for.
		 * @param index
		 *            The index in the column.
		 * @return The created styled string.
		 */
		@Override
		public StyledString getStyledText(Object element, int index) {
			JmxSensorValueData data = (JmxSensorValueData) element;
			Column enumId = Column.fromOrd(index);
			JmxDefinitionDataIdent jmxDefinitionDataIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());

			return getStyledTextForColumn(jmxDefinitionDataIdent, data, enumId);
		}

		@Override
		protected Image getColumnImage(Object element, int index) {
			JmxSensorValueData data = (JmxSensorValueData) element;
			Column enumId = Column.fromOrd(index);

			JmxDefinitionDataIdent jmxDefinitionDataIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());

			switch (enumId) {
			case ISREADABLE:
				if (jmxDefinitionDataIdent.getmBeanAttributeIsReadable()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK);
				} else {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CLOSE);
				}
			case ISWRITEABLE:
				if (jmxDefinitionDataIdent.getmBeanAttributeIsWritable()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CHECKMARK);
				} else {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CLOSE);
				}
			case CHARTING:
				if (data.isBooleanOrNumeric()) {
					return InspectIT.getDefault().getImage(InspectITImages.IMG_CHART_PIE);
				}
			default:
				return super.getColumnImage(element, index);
			}
		}

	}

	/**
	 * Returns the styled text for a specific column.
	 * 
	 * @param jmxDefinitionDataIdent
	 *            The object to extract the information from.
	 * @param data
	 *            The object to extract the value information from.
	 * @param enumId
	 *            The enumeration ID.
	 * @return The styled string containing the information from the data object.
	 */
	private StyledString getStyledTextForColumn(JmxDefinitionDataIdent jmxDefinitionDataIdent, JmxSensorValueData data, Column enumId) {

		switch (enumId) {
		case DOMAINNAME:
			return new StyledString(jmxDefinitionDataIdent.getDerivedDomainName());
		case TYPENAME:
			return new StyledString(jmxDefinitionDataIdent.getDerivedTypeName());
		case ATTRIBUTE:
			return new StyledString(jmxDefinitionDataIdent.getmBeanAttributeName());
		case TIMESTAMP:
			return new StyledString(NumberFormatter.formatTime(data.getTimeStamp()));
		case VALUE:
			return new StyledString(data.getValue());
		case ISREADABLE:
		case ISWRITEABLE:
		case CHARTING:
			return emptyStyledString;
		default:
			return new StyledString("error");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadableString(Object object) {
		if (object instanceof JmxSensorValueData) {
			JmxSensorValueData data = (JmxSensorValueData) object;
			StringBuilder sb = new StringBuilder();

			JmxDefinitionDataIdent jmxDefinitionDataIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());

			for (Column column : Column.values()) {
				sb.append(getStyledTextForColumn(jmxDefinitionDataIdent, data, column).toString());
				sb.append('\t');
			}
			return sb.toString();
		}
		throw new RuntimeException("Could not create the human readable string!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getColumnValues(Object object) {
		if (object instanceof JmxSensorValueData) {
			JmxSensorValueData data = (JmxSensorValueData) object;
			
			
			JmxDefinitionDataIdent jmxDefinitionDataIdent = cachedDataService.getJmxDefinitionDataIdentForId(data.getJmxSensorDefinitionDataIdentId());

			List<String> values = new ArrayList<String>();
			for (Column column : Column.values()) {
				values.add(getStyledTextForColumn(jmxDefinitionDataIdent, data, column).toString());
			}
			return values;
		}
		throw new RuntimeException("Could not create the column values!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SubViewClassification getSubViewClassification() {
		return SubViewClassification.MASTER;
	}

}
