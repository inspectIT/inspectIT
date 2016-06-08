package rocks.inspectit.ui.rcp.formatter;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

import rocks.inspectit.shared.all.cmr.model.JmxDefinitionDataIdent;
import rocks.inspectit.shared.all.cmr.model.MethodIdent;
import rocks.inspectit.shared.all.cmr.model.PlatformIdent;
import rocks.inspectit.shared.all.communication.data.ExceptionSensorData;
import rocks.inspectit.shared.all.communication.data.HttpInfo;
import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationAwareData;
import rocks.inspectit.shared.all.communication.data.SqlStatementData;
import rocks.inspectit.shared.all.communication.data.TimerData;
import rocks.inspectit.shared.all.communication.data.cmr.AgentStatusData;
import rocks.inspectit.shared.cs.ci.assignment.ISensorAssignment;
import rocks.inspectit.shared.cs.ci.assignment.impl.MethodSensorAssignment;
import rocks.inspectit.shared.cs.ci.sensor.ISensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.exception.impl.ExceptionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.jmx.JmxSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.ConnectionSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.HttpSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.InvocationSequenceSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.Log4jLoggingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementParameterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.PreparedStatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteApacheHttpClientV40InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteHttpUrlConnectionInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteJettyHttpClientV61InserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQConsumerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQInserterSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.RemoteMQListenerExtractorSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.StatementSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.method.impl.TimerSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ClassLoadingSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CompilationSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.CpuSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.MemorySensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.RuntimeSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.SystemSensorConfig;
import rocks.inspectit.shared.cs.ci.sensor.platform.impl.ThreadSensorConfig;
import rocks.inspectit.shared.cs.communication.data.cmr.WritingStatus;
import rocks.inspectit.shared.cs.storage.LocalStorageData;
import rocks.inspectit.shared.cs.storage.StorageData;
import rocks.inspectit.shared.cs.storage.StorageData.StorageState;
import rocks.inspectit.shared.cs.storage.label.AbstractStorageLabel;
import rocks.inspectit.shared.cs.storage.label.BooleanStorageLabel;
import rocks.inspectit.shared.cs.storage.label.type.AbstractCustomStorageLabelType;
import rocks.inspectit.shared.cs.storage.label.type.AbstractStorageLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.AssigneeLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.CreationDateLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.DataTimeFrameLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.ExploredByLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.RatingLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.StatusLabelType;
import rocks.inspectit.shared.cs.storage.label.type.impl.UseCaseLabelType;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.model.AgentFolderFactory;
import rocks.inspectit.ui.rcp.model.AgentLeaf;
import rocks.inspectit.ui.rcp.repository.CmrRepositoryDefinition;
import rocks.inspectit.ui.rcp.repository.RepositoryDefinition;
import rocks.inspectit.ui.rcp.util.data.RegExAggregatedHttpTimerData;
import rocks.inspectit.ui.rcp.validation.ValidationControlDecoration;
import rocks.inspectit.ui.rcp.validation.ValidationState;

/**
 * This class provides some static methods to create some common {@link String} and
 * {@link StyledString} objects.
 *
 * @author Patrice Bouillet
 * @author Stefan Siegl
 * @author Ivan Senic
 * @author Marius Oehler
 */
public final class TextFormatter {

	/** Logical Name for the font used for the error marker. */
	public static final String FONT_ERROR_MARKER = "de.inspectit.font.errormarker";

	/**
	 * Default size of the font used in the error marker. This will be used if the size of default
	 * system font can not be read.
	 */
	public static final int DEFAULT_FONT_ERROR_SIZE = 10;

	static {
		FontData[] fontData = JFaceResources.getDefaultFontDescriptor().getFontData();
		if (fontData.length > 0) {
			FontData defaultFontData = fontData[0];
			int height = (int) defaultFontData.height;
			JFaceResources.getFontRegistry().put(FONT_ERROR_MARKER, new FontData[] { new FontData("Arial", height, SWT.BOLD | SWT.ITALIC) });
		} else {
			JFaceResources.getFontRegistry().put(FONT_ERROR_MARKER, new FontData[] { new FontData("Arial", DEFAULT_FONT_ERROR_SIZE, SWT.BOLD | SWT.ITALIC) });
		}
	}

	/**
	 * Private constructor. Prevent instantiation.
	 */
	private TextFormatter() {
	}

	/**
	 * Returns a Styled String out of the {@link MethodIdent} objects which looks like:
	 * 'name'('parameter') - 'package'.'class'. Additionally, as this returns a {@link StyledString}
	 * , the last part is colored.
	 *
	 * @param methodIdent
	 *            The object which contains the information to create the styled method string.
	 * @return The created styled method string.
	 */
	public static StyledString getStyledMethodString(MethodIdent methodIdent) {
		StyledString styledString = new StyledString();

		styledString.append(getMethodWithParameters(methodIdent));
		String decoration;
		if ((methodIdent.getPackageName() != null) && !methodIdent.getPackageName().equals("")) {
			decoration = MessageFormat.format("- {0}.{1}", new Object[] { methodIdent.getPackageName(), methodIdent.getClassName() });
		} else {
			decoration = MessageFormat.format("- {0}", new Object[] { methodIdent.getClassName() });
		}

		styledString.append(decoration, StyledString.QUALIFIER_STYLER);

		return styledString;
	}

	/**
	 * Returns a method string which is appended by the parameters.
	 *
	 * @param methodIdent
	 *            The object which contains the information to create the styled method string.
	 * @return The created method + parameters string.
	 */
	public static String getMethodWithParameters(MethodIdent methodIdent) {
		StringBuilder builder = new StringBuilder();
		String parameterText = "";
		if (null != methodIdent.getParameters()) {
			List<String> parameterList = new ArrayList<>();
			for (String parameter : methodIdent.getParameters()) {
				String[] split = parameter.split("\\.");
				parameterList.add(split[split.length - 1]);
			}

			parameterText = parameterList.toString();
			parameterText = parameterText.substring(1, parameterText.length() - 1);
		}

		builder.append(methodIdent.getMethodName());
		builder.append('(');
		builder.append(parameterText);
		builder.append(") ");

		return builder.toString();
	}

	/**
	 * Returns a method string which is appended by the parameters.
	 *
	 * @param methodSensorAssignment
	 *            {@link MethodSensorAssignment}
	 * @return The created method + parameters string.
	 */
	public static String getMethodWithParameters(MethodSensorAssignment methodSensorAssignment) {
		// can not create if we don't have name
		if ((null == methodSensorAssignment.getMethodName()) && !methodSensorAssignment.isConstructor()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		if (methodSensorAssignment.isConstructor()) {
			builder.append("<init>");
		} else {
			builder.append(methodSensorAssignment.getMethodName());
		}

		// if not defined then just a start
		if (null != methodSensorAssignment.getParameters()) {
			if (methodSensorAssignment.getParameters().isEmpty()) {
				builder.append("()");
			} else {
				String parameterText = methodSensorAssignment.getParameters().toString();
				parameterText = parameterText.substring(1, parameterText.length() - 1);
				builder.append('(');
				builder.append(parameterText);
				builder.append(')');
			}
		}

		return builder.toString();
	}

	/**
	 * Returns a String out of the {@link MethodIdent} objects which looks like: 'name'('parameter')
	 * - 'package'.'class'.
	 *
	 * @param methodIdent
	 *            The object which contains the information to create the method string.
	 * @return The created method string.
	 */
	public static String getMethodString(MethodIdent methodIdent) {
		return getStyledMethodString(methodIdent).getString();
	}

	/**
	 * Returns a {@link String} out of the {@link JmxDefinitionDataIdent} object which looks like:
	 * 'attributeName' - 'packagename':'typeName'.
	 *
	 * @param jmxIdent
	 *            the object which contains the information to create the jmx string.
	 * @return the created method string
	 */
	public static String getJmxDefinitionString(JmxDefinitionDataIdent jmxIdent) {
		return String.format("%1$s - %2$s:%3$s", jmxIdent.getmBeanAttributeName(), jmxIdent.getDerivedDomainName(), jmxIdent.getDerivedTypeName());
	}

	/**
	 * Returns styled string for invocation affilliation percentage.
	 *
	 * @param percentage
	 *            Percentage.
	 * @param invocationsNumber
	 *            the number of invocation in total
	 * @return Styled string.
	 */
	public static StyledString getInvocationAffilliationPercentageString(int percentage, int invocationsNumber) {
		StyledString styledString = new StyledString();

		styledString.append(String.valueOf(percentage), StyledString.QUALIFIER_STYLER);
		styledString.append("% (in ", StyledString.QUALIFIER_STYLER);
		styledString.append(String.valueOf(invocationsNumber), StyledString.QUALIFIER_STYLER);
		styledString.append(" inv)", StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	/**
	 * Creates a <code>StyledString</code> containing a warning.
	 *
	 * @return a <code>StyledString</code> containing a warning.
	 */
	public static StyledString getWarningSign() {
		return new StyledString(" !", new Styler() {

			@Override
			public void applyStyles(TextStyle textStyle) {
				textStyle.foreground = JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
				textStyle.font = JFaceResources.getFont(TextFormatter.FONT_ERROR_MARKER);
			}
		});
	}

	/**
	 * Get the textual representation of objects that will be displayed in the new view.
	 *
	 * @param invAwareData
	 *            Invocation aware object to get representation for.
	 * @param repositoryDefinition
	 *            Repository definition. Needed for the method name retrival.
	 * @return String.
	 */
	public static String getInvocationAwareDataTextualRepresentation(InvocationAwareData invAwareData, RepositoryDefinition repositoryDefinition) {
		if (invAwareData instanceof SqlStatementData) {
			SqlStatementData sqlData = (SqlStatementData) invAwareData;
			return "SQL: " + sqlData.getSql();
		} else if (invAwareData instanceof RegExAggregatedHttpTimerData) {
			return "transformed URI: " + ((RegExAggregatedHttpTimerData) invAwareData).getTransformedUri();
		} else if (invAwareData instanceof HttpTimerData) {
			HttpTimerData timerData = (HttpTimerData) invAwareData;
			// Print either URI or Usecase (tagged value) depending on the situation (which is
			// filled, that is)
			if (!HttpInfo.UNDEFINED.equals(timerData.getHttpInfo().getUri())) {
				return "URI: " + timerData.getHttpInfo().getUri();
			} else {
				return "Usecase: " + timerData.getHttpInfo().getInspectItTaggingHeaderValue();
			}
		} else if (invAwareData instanceof ExceptionSensorData) {
			ExceptionSensorData exData = (ExceptionSensorData) invAwareData;
			return "Exception: " + exData.getThrowableType();
		} else if (invAwareData instanceof TimerData) {
			TimerData timerData = (TimerData) invAwareData;
			MethodIdent methodIdent = repositoryDefinition.getCachedDataService().getMethodIdentForId(timerData.getMethodIdent());
			return TextFormatter.getMethodString(methodIdent);
		}
		return "";
	}

	/**
	 * Returns the styled string for the storage data and its CMR repository definition.
	 *
	 * @param storageData
	 *            {@link StorageData}.
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return Styled string for nicer representation.
	 */
	public static StyledString getStyledStorageDataString(StorageData storageData, CmrRepositoryDefinition cmrRepositoryDefinition) {
		StyledString styledString = new StyledString();
		styledString.append(storageData.getName());
		styledString.append(" ");
		styledString.append("[" + cmrRepositoryDefinition.getName() + "]", StyledString.QUALIFIER_STYLER);
		styledString.append(" - ");
		styledString.append(getStorageStateTextualRepresentation(storageData.getState()), StyledString.DECORATIONS_STYLER);
		if (InspectIT.getDefault().getInspectITStorageManager().isFullyDownloaded(storageData)) {
			styledString.append(", Downloaded", StyledString.DECORATIONS_STYLER);
		}
		styledString.append(", " + NumberFormatter.humanReadableByteCount(storageData.getDiskSize()), StyledString.DECORATIONS_STYLER);
		return styledString;
	}

	/**
	 * Returns the styled string for the {@link LocalStorageData}.
	 *
	 * @param localStorageData
	 *            Local storage data.
	 * @return Styled string for nicer representation.
	 */
	public static StyledString getStyledStorageDataString(LocalStorageData localStorageData) {
		StyledString styledString = new StyledString();
		styledString.append(localStorageData.getName());
		styledString.append(" ");
		styledString.append("[Local Disk]", StyledString.QUALIFIER_STYLER);
		styledString.append(" - ");
		styledString.append(NumberFormatter.humanReadableByteCount(localStorageData.getDiskSize()), StyledString.DECORATIONS_STYLER);
		return styledString;
	}

	/**
	 * Returns {@link StyledString} for the {@link AgentLeaf}.
	 *
	 * @param agentLeaf
	 *            {@link AgentLeaf}.
	 * @return Returns {@link StyledString} for the {@link AgentLeaf}.
	 */
	public static StyledString getStyledAgentLeafString(AgentLeaf agentLeaf) {
		StyledString styledString = new StyledString();
		if (agentLeaf.isInFolder()) {
			styledString.append(AgentFolderFactory.getAgentDisplayNameInFolder(agentLeaf.getPlatformIdent().getAgentName()));
		} else {
			styledString.append(agentLeaf.getPlatformIdent().getAgentName());
		}
		styledString.append(getStyledAgentDescription(agentLeaf.getPlatformIdent(), agentLeaf.getAgentStatusData()));
		return styledString;
	}

	/**
	 * Returns the styled information about the agent version and connection status.
	 *
	 * @param platformIdent
	 *            {@link PlatformIdent}
	 * @param agentStatusData
	 *            {@link AgentStatusData}
	 * @return {@link StyledString}
	 */
	private static StyledString getStyledAgentDescription(PlatformIdent platformIdent, AgentStatusData agentStatusData) {
		StyledString styledString = new StyledString();
		styledString.append(" ");
		styledString.append("[" + platformIdent.getVersion() + "]", StyledString.QUALIFIER_STYLER);
		styledString.append(" - ");
		if (null != agentStatusData) {
			switch (agentStatusData.getAgentConnection()) {
			case CONNECTED:
				if (null != agentStatusData.getMillisSinceLastData()) {
					long millis = agentStatusData.getMillisSinceLastData().longValue();
					// at last one minute of not sending data to display as the non active
					if (millis > 60000) {
						styledString.append("Connected :: Last data sent " + NumberFormatter.humanReadableMillisCount(millis, true) + " ago", StyledString.DECORATIONS_STYLER);
					} else {
						styledString.append("Connected :: Sending data", StyledString.DECORATIONS_STYLER);
					}
				} else {
					styledString.append("Connected :: No data sent", StyledString.DECORATIONS_STYLER);
				}
				break;
			case NO_KEEP_ALIVE:
				long timeSinceLastKeepAlive = System.currentTimeMillis() - agentStatusData.getLastKeepAliveTimestamp();
				styledString.append("No keep-alive signal for " + NumberFormatter.humanReadableMillisCount(timeSinceLastKeepAlive, true), StyledString.DECORATIONS_STYLER);
				break;
			case DISCONNECTED:
				styledString.append("Disconnected", StyledString.DECORATIONS_STYLER);
				break;
			default:
				styledString.append("Not connected", StyledString.DECORATIONS_STYLER);
				break;
			}
		} else {
			styledString.append("Not connected", StyledString.DECORATIONS_STYLER);
		}
		return styledString;
	}

	/**
	 * @param storageState
	 *            Storage state.
	 * @return Returns the textual representation of the storage state.
	 */
	public static String getStorageStateTextualRepresentation(StorageState storageState) {
		if (storageState == StorageState.CREATED_NOT_OPENED) {
			return "Created";
		} else if (storageState == StorageState.OPENED) {
			return "Writable";
		} else if (storageState == StorageState.CLOSED) {
			return "Readable";
		} else if (storageState == StorageState.RECORDING) {
			return "Recording";
		}
		return "UNKNOWN STATE";
	}

	/**
	 * Returns the name of the label, based on it's type. If label is <code>null</code>, string
	 * "null" will be returned.
	 *
	 * @param label
	 *            Label to get name for.
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelName(AbstractStorageLabel<?> label) {
		if (null == label) {
			return "null";
		} else {
			return getLabelName(label.getStorageLabelType());
		}
	}

	/**
	 * Returns the name of the label type. If label type is <code>null</code>, string "null" will be
	 * returned.
	 *
	 * @param labelType
	 *            Label type to get name for.
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelName(AbstractStorageLabelType<?> labelType) {
		if (null == labelType) {
			return "null";
		} else if (AssigneeLabelType.class.equals(labelType.getClass())) {
			return "Assignee";
		} else if (CreationDateLabelType.class.equals(labelType.getClass())) {
			return "Creation Date";
		} else if (ExploredByLabelType.class.equals(labelType.getClass())) {
			return "Explored By";
		} else if (RatingLabelType.class.equals(labelType.getClass())) {
			return "Rating";
		} else if (StatusLabelType.class.equals(labelType.getClass())) {
			return "Status";
		} else if (UseCaseLabelType.class.equals(labelType.getClass())) {
			return "Use Case";
		} else if (DataTimeFrameLabelType.class.equals(labelType.getClass())) {
			return "Data Timeframe";
		} else if (AbstractCustomStorageLabelType.class.isAssignableFrom(labelType.getClass())) {
			return ((AbstractCustomStorageLabelType<?>) labelType).getName();
		} else {
			return "Unknown Label";
		}
	}

	/**
	 * Returns the class type of the label type.
	 *
	 * @param labelType
	 *            Label type to get name for.
	 * @return Returns the class type of the label type.
	 */
	public static String getLabelValueType(AbstractStorageLabelType<?> labelType) {
		if (null == labelType) {
			return "null";
		} else if (Boolean.class.equals(labelType.getValueClass())) {
			return "Yes/No";
		} else if (Date.class.equals(labelType.getValueClass())) {
			return "Date";
		} else if (Number.class.equals(labelType.getValueClass())) {
			return "Number";
		} else if (String.class.equals(labelType.getValueClass())) {
			return "Text";
		} else {
			return "Unknown Label Type";
		}
	}

	/**
	 * Returns the name of the label, based on it's class. If label is <code>null</code>, string
	 * "null" will be returned.
	 *
	 * @param label
	 *            Label to get name for.
	 * @param grouped
	 *            Is is a representation for the grouped labels.
	 * @return Returns the name of the label, based on it's class.
	 */
	public static String getLabelValue(AbstractStorageLabel<?> label, boolean grouped) {
		if (null == label) {
			return "null";
		} else if (CreationDateLabelType.class.equals(label.getStorageLabelType().getClass())) {
			Date date = (Date) ((AbstractStorageLabel<?>) label).getValue();
			if (grouped) {
				return DateFormat.getDateInstance().format(date);
			} else {
				return DateFormat.getDateTimeInstance().format(date);
			}
		} else if (BooleanStorageLabel.class.equals(label.getClass())) {
			BooleanStorageLabel booleanStorageLabel = (BooleanStorageLabel) label;
			if (booleanStorageLabel.getValue().booleanValue()) {
				return "Yes";
			} else {
				return "No";
			}
		} else {
			return label.getFormatedValue();
		}
	}

	/**
	 * Returns text representation for the {@link WritingStatus} or empty string if status is
	 * <code>null</code>.
	 *
	 * @param recordingWritingStatus
	 *            Status of writing.
	 * @return String that represent the status.
	 */
	public static String getWritingStatusText(WritingStatus recordingWritingStatus) {
		if (null == recordingWritingStatus) {
			return "";
		}
		switch (recordingWritingStatus) {
		case GOOD:
			return "[OK] There are no problems.";
		case MEDIUM:
			return "[WARN] Amount of data tried to be recorded is slightly higer than what CMR can support. However, no data loss should be expected.";
		case BAD:
			return "[ALERT] Amount of data tried to be recorded is too high for CMR to manage. Data loss should be expected.";
		default:
			return "";
		}
	}

	/**
	 * Description of the agent.
	 *
	 * @param agent
	 *            {@link PlatformIdent}
	 * @return Description of the agent.
	 */
	public static String getAgentDescription(PlatformIdent agent) {
		return agent.getAgentName() + " [" + agent.getVersion() + "]";
	}

	/**
	 * Description of the agent with the connection information.
	 *
	 * @param agent
	 *            {@link PlatformIdent}
	 * @param agentStatusData
	 *            {@link AgentStatusData}.
	 * @return Description of the agent.
	 */
	public static String getAgentDescription(PlatformIdent agent, AgentStatusData agentStatusData) {
		return agent.getAgentName() + getStyledAgentDescription(agent, agentStatusData).getString();
	}

	/**
	 * Description of the {@link CmrRepositoryDefinition}.
	 *
	 * @param cmrRepositoryDefinition
	 *            {@link CmrRepositoryDefinition}.
	 * @return Description in form http://ip:port
	 */
	public static String getCmrRepositoryDescription(CmrRepositoryDefinition cmrRepositoryDefinition) {
		return "Central Management Repository @ http://" + cmrRepositoryDefinition.getIp() + ":" + cmrRepositoryDefinition.getPort();
	}

	/**
	 * Returns formated {@link String} for the {@link SqlStatementData} parameter values list.
	 * <p>
	 * Elements that are <code>null</code> in the list will be printed as '?'.
	 *
	 * @param parameterValues
	 *            List of parameter values.
	 * @return Formated string in form [param1, param2,.., paramN].
	 */
	public static String getSqlParametersText(List<String> parameterValues) {
		if ((null == parameterValues) || parameterValues.isEmpty()) {
			return "[]";
		} else {
			Iterator<String> it = parameterValues.iterator();
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			while (it.hasNext()) {
				String param = it.next();
				if (null != param) {
					sb.append(param);
				} else {
					sb.append('?');
				}
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			return sb.append(']').toString();
		}
	}

	/**
	 * The original text will be cleaned from the line breaks.
	 * <p>
	 * If string passed is <code>null</code>, null will be returned.
	 *
	 * @param originalText
	 *            Original text to modify.
	 * @return Returns text without any line breaks.
	 */
	public static String clearLineBreaks(String originalText) {
		if (null == originalText) {
			return originalText;
		}
		boolean lastCharWhitespace = false;
		StringBuilder stringBuilder = new StringBuilder(originalText.length());

		for (int i = 0; i < originalText.length(); i++) {
			char c = originalText.charAt(i);
			if ((c == '\r') || (c == '\n')) {
				if (!lastCharWhitespace) {
					stringBuilder.append(' ');
					lastCharWhitespace = true;
				}
			} else if (Character.isWhitespace(c)) {
				if (!lastCharWhitespace) {
					stringBuilder.append(' ');
					lastCharWhitespace = true;
				}
			} else {
				stringBuilder.append(c);
				lastCharWhitespace = false;
			}
		}
		return stringBuilder.toString();
	}

	/**
	 * Crops the string to the maxLength. The string will have '...' appended at the end. This
	 * method delegates to the {@link StringUtils#abbreviate(String, int)} method.
	 *
	 * @param string
	 *            String to crop.
	 * @param maxLength
	 *            Wanted maximum length.
	 * @see StringUtils#abbreviate(String, int)
	 * @return Cropped {@link String}.
	 */
	public static String crop(String string, int maxLength) {
		return StringUtils.abbreviate(string, maxLength);
	}

	/**
	 * Returns a new StyledString that contains the given text or "" if the given text was in fact
	 * <code>null</code>.
	 *
	 * @param text
	 *            the text to display, may be null.
	 * @return a new StyledString that contains the given text or "" if the given text was in fact
	 *         <code>null</code>.
	 */
	public static StyledString emptyStyledStringIfNull(String text) {
		return new StyledString(StringUtils.defaultString(text));
	}

	/**
	 * Returns name of the {@link ISensorConfig}.
	 *
	 * @param sensorConfig
	 *            {@link ISensorConfig}.
	 * @return Name or empty string if sensor name can be resolved.
	 */
	public static String getSensorConfigName(ISensorConfig sensorConfig) {
		return getSensorConfigName(sensorConfig.getClass());
	}

	/**
	 * Returns name of the {@link ISensorConfig class}.
	 *
	 * @param sensorClass
	 *            {@link ISensorConfig} class.
	 * @return Name or empty string if sensor name can be resolved.
	 */
	public static String getSensorConfigName(Class<? extends ISensorConfig> sensorClass) {
		if (ObjectUtils.equals(sensorClass, ExceptionSensorConfig.class)) {
			return "Exception Sensor";
		} else if (ObjectUtils.equals(sensorClass, ConnectionSensorConfig.class)) {
			return "JDBC Connection Sensor";
		} else if (ObjectUtils.equals(sensorClass, HttpSensorConfig.class)) {
			return "HTTP Sensor";
		} else if (ObjectUtils.equals(sensorClass, InvocationSequenceSensorConfig.class)) {
			return "Invocation Sequence Sensor";
		} else if (ObjectUtils.equals(sensorClass, PreparedStatementParameterSensorConfig.class)) {
			return "JDBC Prepared Statement Parameter Sensor";
		} else if (ObjectUtils.equals(sensorClass, PreparedStatementSensorConfig.class)) {
			return "JDBC Prepared Statement Sensor";
		} else if (ObjectUtils.equals(sensorClass, StatementSensorConfig.class)) {
			return "JDBC Statement Sensor";
		} else if (ObjectUtils.equals(sensorClass, TimerSensorConfig.class)) {
			return "Timer Sensor";
		} else if (ObjectUtils.equals(sensorClass, Log4jLoggingSensorConfig.class)) {
			return "Logging Sensor for log4j ";
		} else if (ObjectUtils.equals(sensorClass, ClassLoadingSensorConfig.class)) {
			return "Class Loading Information";
		} else if (ObjectUtils.equals(sensorClass, CompilationSensorConfig.class)) {
			return "Compilation Information";
		} else if (ObjectUtils.equals(sensorClass, CpuSensorConfig.class)) {
			return "CPU Information";
		} else if (ObjectUtils.equals(sensorClass, MemorySensorConfig.class)) {
			return "Memory Information";
		} else if (ObjectUtils.equals(sensorClass, RuntimeSensorConfig.class)) {
			return "Runtime Information";
		} else if (ObjectUtils.equals(sensorClass, SystemSensorConfig.class)) {
			return "System Information";
		} else if (ObjectUtils.equals(sensorClass, ThreadSensorConfig.class)) {
			return "Thread Information";
		} else if (ObjectUtils.equals(sensorClass, JmxSensorConfig.class)) {
			return "JMX Sensor";
		} else if (ObjectUtils.equals(sensorClass, RemoteApacheHttpClientV40InserterSensorConfig.class)) {
			return "Apache Http";
		} else if (ObjectUtils.equals(sensorClass, RemoteHttpExtractorSensorConfig.class)) {
			return "Http Extractor";
		} else if (ObjectUtils.equals(sensorClass, RemoteHttpUrlConnectionInserterSensorConfig.class)) {
			return "Java Http";
		} else if (ObjectUtils.equals(sensorClass, RemoteJettyHttpClientV61InserterSensorConfig.class)) {
			return "Jetty Http";
		} else if (ObjectUtils.equals(sensorClass, RemoteMQConsumerExtractorSensorConfig.class)) {
			return "MQ Consumer";
		} else if (ObjectUtils.equals(sensorClass, RemoteMQInserterSensorConfig.class)) {
			return "MQ Inserter";
		} else if (ObjectUtils.equals(sensorClass, RemoteMQListenerExtractorSensorConfig.class)) {
			return "MQ Listener";
		}
		return null;
	}

	/**
	 * Returns short (1 line) error message for the assignment based on the validation states.
	 *
	 * @param sensorAssignment
	 *            assignment
	 * @param states
	 *            {@link ValidationControlDecoration}
	 * @return short error message
	 */
	public static String getErroMessageShort(ISensorAssignment<?> sensorAssignment, Collection<ValidationState> states) {
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
		builder.append(" Assignment (");
		String singleStateMessage = StringUtils.EMPTY;
		int count = 0;
		for (ValidationState state : states) {
			if (!state.isValid()) {
				count++;
				singleStateMessage = state.getMessage();
			}
		}
		if (count > 1) {
			builder.append(count);
			builder.append(" fields contain validation errors)");
		} else {
			builder.append(singleStateMessage);
			builder.append(')');
		}

		return builder.toString();
	}

	/**
	 * Returns full error message for the assignment based on the validation states. In this message
	 * each line will contain error reported by any invalid {@link ValidationState}
	 *
	 * @param sensorAssignment
	 *            assignment
	 * @param states
	 *            {@link ValidationState}s
	 * @return fill error message
	 */
	public static String getErroMessageFull(ISensorAssignment<?> sensorAssignment, Collection<ValidationState> states) {
		StringBuilder builder = new StringBuilder();
		builder.append(TextFormatter.getSensorConfigName(sensorAssignment.getSensorConfigClass()));
		builder.append(" Assignment:");
		builder.append(getValidationConcatenatedMessage(states));
		return builder.toString();
	}

	/**
	 * Returns a validation errors count text for the given set of {@link ValidationState}s.
	 *
	 * @param states
	 *            set of {@link ValidationState}s
	 * @param element
	 *            name of the element (e.g. filed, part, etc.)
	 * @return the validation message, or null if the given set is empty.
	 */
	public static String getValidationErrorsCountText(Set<ValidationState> states, String element) {
		if (CollectionUtils.isNotEmpty(states)) {
			if (states.size() == 1) {
				return states.iterator().next().getMessage();
			} else if (states.size() > 1) {
				return states.size() + " " + element + "s contain validation errors";
			}
		}

		return null;
	}

	/**
	 * Returns a concatenated validation message for the given set of {@link ValidationState}s.
	 *
	 * @param states
	 *            set of {@link ValidationState}s
	 * @return a concatenated validation message, or an empty string if the given set is empty.
	 */
	public static String getValidationConcatenatedMessage(Collection<ValidationState> states) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (ValidationState state : states) {
			if (!state.isValid()) {
				if (!first) {
					builder.append('\n');
				}
				builder.append(state.getMessage());
				first = false;
			}
		}
		return builder.toString();
	}
}
